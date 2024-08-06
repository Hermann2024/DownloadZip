package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.FlatMapperException;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.framework.tools.Calculator;

/**
 * Transformer de XML vers flux plat
 */
public class FlatTransformerOut
{
  private static final Logger LOGGER = Logger.getLogger(MapperWriterImpl.class);
  private static final Pattern FORMAT_SUBSTRING1_PATTERN = Pattern.compile("([0-9]+)s");
  private static final Pattern FORMAT_SUBSTRING2_PATTERN = Pattern.compile("([0-9]+),([0-9]+)s");
  private static final Pattern FORMAT_SUBSTRING3_PATTERN = Pattern.compile("([0-9]+),s");
  private static final Pattern FORMAT_DOUBLE_PATTERN = Pattern.compile(".*(\\.[0-9]+)f");
  private static final Pattern FORMAT_DATETIME_PATTERN = Pattern.compile("(.*)dt");
  private static final Pattern FORMAT_DATE_PATTERN = Pattern.compile("(.*)ds");
  private FlatTemplate _template;
  private Stack<AbstractFlatRow> _rowToWrite;
  private int _fieldIndex = -1;
  private Stack<AbstractFlatRow> _rowStack;
  private XMLRedirectReader _xmlReader;
  private XMLStreamReader _streamReader;
  private boolean _checkEndElement = true;
  private MapperWriterImpl _writer = null;
  private XMLInputFactory _xmlInput;
  private String _endElementRef = null;
  private String _templateFile = null;
  private boolean _first = true;
  private Stack<String> _xmlElementStack = new Stack();

  /**
   * Constructeur
   * @param pOutputStream
   * @param pTemplateFile
   */
  public FlatTransformerOut(MapperWriterImpl pOutputStream, String pTemplateFile)
  {
    _writer = pOutputStream;
    _xmlInput = XMLInputFactory.newInstance();
    _xmlInput.setProperty("javax.xml.stream.isCoalescing", true); // permet d'interpréter les entités xml (ex. : &amp;)
    _templateFile = pTemplateFile;
    _rowToWrite = new Stack<AbstractFlatRow>();
    _rowStack = new Stack<AbstractFlatRow>();
  }

  /**
   * Set l'élément de fin (pour garder sa référence afin de le re tester plus tard, pour voir si on est arrivé en bout de fichier)
   * @param pValue L'élément de fin
   */
  public void setEndElementRef(String pValue)
  {
    _endElementRef = pValue;
  }

  /**
   * Cette méthode navigue dans le XML et redirige l'élément trouvé selon son type (ouvrant, fermant, données)
   */
  public void write()
  {
    try
    {
      int eventType;
      String endElement = null;
      while (_streamReader.hasNext())
      {
        eventType = _streamReader.next();
        switch (eventType)
        {
          case XMLEvent.START_ELEMENT:
            startElement();
            break;
          case XMLEvent.CHARACTERS:
            characters();
            break;
          case XMLEvent.END_ELEMENT:
            endElement = endElement();
            break;
        }
        // On teste si on est à la fin et que l'élément de fin correspond.
        // On doit tester si on est à la fin dans le cas où l'élément de fin (exemple </values>) apparaît plusieurs
        // fois dans le xml.
        if (_endElementRef.equals(endElement) && !_streamReader.hasNext())
        {
          break;
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
  }

  /**
   * Charge ou recharge le buffer
   * @param pBuf Buffer à charger/mettre à jour
   * @throws XMLStreamException exception
   */
  public void setBuffer(StringBuilder pBuf) throws XMLStreamException
  {
    _xmlReader = new XMLRedirectReader(pBuf);
    _streamReader = _xmlInput.createXMLStreamReader(_xmlReader);
  }

  /**
   * Ecrit la donnée dans la row et à l'emplacement qu'il faut
   */
  private void characters()
  {
    AbstractFlatRow row = _rowStack.peek();
    if (row != null) // Si on a déjà chargé un template en mémoire
    {
      // On a bien trouvé notre characters
      if (_fieldIndex != -1)
      {
        // on récupéré
        for (int i: row.getTemplate().getIndexList(_fieldIndex))
        {
          setField(row, i);
        }
        _checkEndElement = false;
      }
    }
  }

  private void setField(AbstractFlatRow pRow, int pFieldIndex)
  {
    String text = _streamReader.getText();
    // As-t'on une transco pour cet élément ?
    Map<String, String> transco = pRow.getTemplate()._transcoMap.get(pFieldIndex);
    if (transco != null && transco.containsKey(text))
    {
      text = transco.get(text);
    }
    String format = pRow.getTemplate()._formatFieldMap.get(pFieldIndex);
    if (format != null)
    {
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb);
      String subString = getSubstring(text, format);
      if (subString != null)
      {
        sb.append(subString);
      }
      else if (format.matches("[0-9]+"))
      {
        // formatage d'une chaine : la taille correspond à la taille max (règle de rétrocompatibilité)
        int sizeMax = Integer.parseInt(format);
        text = text.length() > sizeMax ? text.substring(0, sizeMax) : text;
        // règle de rétrocompatibilité : si seule la largeur du champ est précisée, on aligne à gauche
        formatter.format("%-" + format + "s", text);
      }
      else if (format.matches("-[0-9]+"))
      {
        // formatage d'une chaine avec alignement à droite : la taille correspond à la taille max
        format = format.substring(1);
        int sizeMax = Integer.parseInt(format);
        text = text.length() > sizeMax ? text.substring(0, sizeMax) : text;
        formatter.format("%" + format + "s", text);
      }
      else if (format.endsWith("d"))
      {
        // formatage d'un nombre : la taille correspond à la taille min
        formatter.format(format, Long.parseLong(text));
      }
      else if (format.endsWith("f"))
      {
        double value = Double.parseDouble(text);
        Matcher m = FORMAT_DOUBLE_PATTERN.matcher(format);
        if (m.matches())
        {
          value = Calculator.roundHalfEven(value, Integer.parseInt(m.group(1).substring(1)));
        }
        formatter.format(format, value);
      }
      else if (format.endsWith("dt"))
      {
        Matcher m = FORMAT_DATETIME_PATTERN.matcher(format);
        if (m.matches())
        {
          Calendar cal = DatatypeConverter.parseDateTime(text);
          sb.append(new SimpleDateFormat(m.group(1)).format(cal.getTime()));
        }
      }
      else if (format.endsWith("ds"))
      {
        Matcher m = FORMAT_DATE_PATTERN.matcher(format);
        if (m.matches())
        {
          Calendar cal = DatatypeConverter.parseDate(text);
          sb.append(new SimpleDateFormat(m.group(1)).format(cal.getTime()));
        }
      }
      formatter.close();
      text = sb.toString();
    }
    if (",".equals(_template.getSeparator()) && "\"".equals(_template.getFieldPrefix()) && "\"".equals(_template.getFieldSuffix()))
    {
      // implémentation de la règle de la RFC 4180 qui impose d'échapper les double quotes internes par un second double quote si les champs sont
      // encapsulés par des doubles quotes
      text = text.replaceAll("\"", "\"\"");
    }
    text = _template.getFieldPrefix() == null ? text : _template.getFieldPrefix() + text;
    text = _template.getFieldSuffix() == null ? text : text + _template.getFieldSuffix();
    pRow.setField(pFieldIndex, text);
  }

  private String getSubstring(String pText, String pFormat)
  {
    String result = null;
    Matcher m = FORMAT_SUBSTRING1_PATTERN.matcher(pFormat);
    int sizeMax = pText.length(), index = 0;
    if (m.matches())
    {
      sizeMax = Integer.parseInt(m.group(1));
    }
    else if (!m.matches())
    {
      m = FORMAT_SUBSTRING2_PATTERN.matcher(pFormat);
      if (m.matches())
      {
        index = Integer.parseInt(m.group(1));
        sizeMax = Integer.parseInt(m.group(2));
        sizeMax += index;
      }
      else
      {
        m = FORMAT_SUBSTRING3_PATTERN.matcher(pFormat);
        if (m.matches())
        {
          index = Integer.parseInt(m.group(1));
        }
      }
    }
    if (m.matches())
    {
      if (index > pText.length() - 1)
      {
        result = "";
      }
      else
      {
        sizeMax = sizeMax > pText.length() ? pText.length() : sizeMax;
        result = pText.substring(index, sizeMax);
      }
    }
    return result;
  }

  /**
   * Gère le cas de l'élément ouvrant. Si aucun template n'est défini, il tente de le récupérer. On tente ensuite de regarder si l'élément ouvrant
   * correspond à une ligne dans le template
   * @throws IOException exception
   * @throws TechnicalServiceException exception
   * @throws FlatMapperException exception
   */
  private void startElement() throws IOException, TechnicalServiceException, FlatMapperException
  {
    String element = _streamReader.getName().toString();
    if ("root".equals(element))
    {
      return;
    }
    _xmlElementStack.push(element);
    getTemplate(element);
    if (!_rowStack.isEmpty()) // Si on a déjà chargé un template en mémoire
    {
      AbstractFlatRow row = _rowStack.peek();
      // On va chercher si notre élément fait bien parti de notre template courant (dans ce cas, on pourra l'écrire dans le row)
      // Si notre élément est une sous-balise, l'élément est d'abord recherché à partir de son xpath relatif complet (ex. : person.address.addressLine1)
      _fieldIndex = row.getTemplate().getIndex(getCurrentXPath(false));
      if (_fieldIndex == -1)
      {
        // Sinon, le sous-élément peut être décrit dans un template dédié (descriptions, translations ...)
        // On reconstitue donc le xpath relatif en supprimant les éléments n'ayant pas de template pour ne pas fausser le xpath
        String path = getCurrentXPath(true);
        if (!path.equals(""))
        {
          _fieldIndex = row.getTemplate().getIndex(getCurrentXPath(true));
        }
      }
      if (_fieldIndex != -1) // Il en fait partie, on sort de la méthode afin de passer à l'élément suivant (le characters)
      {
        return;
      }
    }
    AbstractFlatRow templateRow = _template.getRowDescriptor(_xmlElementStack.size() - 1, element);
    if (templateRow != null) // L'élément correspond à un template
    {
      AbstractFlatRow row = null;
      if (templateRow instanceof FlatRowTemplate)
      {
        row = new FlatRow((FlatRowTemplate)templateRow);
      }
      if (templateRow.getFieldsSize() == 0 && _first) // On est sur un template de type liste
      {
        _first = false;
        row.write(_writer);
      }
      _rowStack.push(row);
    }
  }

  private String getCurrentXPath(boolean purgeElement)
  {
    List<String> xmlElementList = new ArrayList(_xmlElementStack);
    if (purgeElement && !_xmlElementStack.isEmpty())
    {
      for (int i = _xmlElementStack.size() - 1; i > -1; i--)
      {
        if (_template.getRowDescriptor(i, _xmlElementStack.get(i)) == null)
        {
          xmlElementList.remove(i);
        }
      }
    }
    StringBuilder builder = new StringBuilder();
    for (int i = _rowStack.peek()._depth + 1; i < xmlElementList.size(); i++)
    {
      builder.append("." + xmlElementList.get(i));
    }
    return builder.length() > 0 ? builder.toString().substring(1) : builder.toString();
  }

  /**
   * Vérifie que l'élément de fin correspond à la row en cours, puis on supprime ce template row (il n'est plus utile vu qu'on vient de recevoir
   * l'élément fermant)
   * @return L'élément fermant
   * @throws IOException exception
   */
  private String endElement() throws IOException
  {
    String element = _streamReader.getName().toString();
    if ("root".equals(element))
    {
      return null;
    }
    _xmlElementStack.pop();
    if (_checkEndElement)
    {
      AbstractFlatRow row = _rowStack.peek();
      if (row.getRootElement().equals(element))
      {
        // On va ajouter la row de ce template dans la liste (afin de tout écrire à la fin)
        // Tout en supprimant ce template (vu que l'élément fermant clôture ce template)
        _rowToWrite.push(_rowStack.pop());
        // S'il ne reste qu'un élément dans la pile (l'élément ouvrant) on va écrire nos rows
        if (_rowStack.size() == 1)
        {
          while (!_rowToWrite.isEmpty())
          {
            _rowToWrite.pop().write(_writer);
          }
          _writer.flush();
        }
      }
    }
    else
    {
      _checkEndElement = true;
    }
    AbstractFlatRow row = _rowStack.peek();
    // Si notre dernier élément est celui à fermer
    if (row.getTemplate()._inFields != null && row.getTemplate()._inFields.get(row.getTemplate()._inFields.size() - 1).equals(element))
    {
      row.getTemplate()._inFields.remove(row.getTemplate()._inFields.size() - 1);
      // On réinitialise la map si elle est vide (on est sorti de tous nos niveaux)
      if (row.getTemplate()._inFields.isEmpty())
      {
        (row.getTemplate())._inFields = null;
      }
    }
    return element;
  }

  /**
   * Tente de récupérer le template correspondant à l'élément pElement
   * @param pElement
   * @throws TechnicalServiceException exception
   */
  private void getTemplate(String pElement) throws TechnicalServiceException
  {
    if (_template == null) // On tente de récupérer le template
    {
      _template = FlatTemplate.getInstance(pElement, _templateFile);
    }
  }
}
