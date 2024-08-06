package com.cylande.unitedretail.batch.mapper.flatmapper;

import com.cylande.unitedretail.batch.mapper.EngineWriter;
import com.cylande.unitedretail.message.batch.CylandeTemplateType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/** {@inheritDoc} */
public class MapperWriterImpl extends EngineWriter
{
  private static final Logger LOGGER = Logger.getLogger(MapperWriterImpl.class);
  private StringBuilder _xml = new StringBuilder();
  private FlatTransformerOut _transformer = null;
  private String _endElement = null;
  private String _endElementRef = null;
  private String _startElement = null;
  private Pattern _pattern = null;
  private Matcher _matcher = null;
  private CylandeTemplateType _template;
  private String _templateFile;

  /**
   * Constructeur
   * @param pOutPut
   * @param pTemplateFile
   * @throws IOException exception
   */
  public MapperWriterImpl(OutputStream pOutPut, String pTemplateFile, CylandeTemplateType pTemplate) throws IOException
  {
    super(pOutPut);
    _transformer = new FlatTransformerOut(this, pTemplateFile);
    _pattern = Pattern.compile("<([^>]*)>");
    _templateFile = pTemplateFile;
    _template = pTemplate;
  }

  /**
   * Cette méthode initialise l'élément ouvrant et fermant du début de fichier, puis rempli le buffer de manière a toujours avoir un xml valide.
   * @param pBuffer Buffer
   * @param pOff Offset
   * @param pLen Nombre d'octets à lire
   * @throws IOException exception
   */
  public void write(char[] pBuffer, int pOff, int pLen) throws IOException
  {
    try
    {
      String str = new String(pBuffer, pOff, pLen);
      _matcher = _pattern.matcher(str);
      if (_startElement == null || _endElement == null)
      {
        String elem = null;
        int i = 0;
        // On va d'abord récupérer l'élément de début et de fin
        while (_matcher.find())
        {
          i++;
          if (i == 2)
          {
            elem = _matcher.group(1);
            _startElement = "<" + elem + ">";
            _endElementRef = "</" + elem + ">";
            _xml.append(_startElement);
            _matcher.find();
            _endElement = "</" + _matcher.group(1) + ">";
            _transformer.setEndElementRef(_matcher.group(1)); // on garde une trace de l'élément de fin
            str = str.substring(_matcher.start());
            break;
          }
        }
        if (_template.getHeader() != null)
        {
          FlatTemplate template = FlatTemplate.getInstance(elem, _templateFile);
          write(_template.getHeader() + template.getLineSeparator());
        }
      }
      // On a nos deux éléments, on peut donc lancer la transformation du xml
      if (_startElement != null && _endElement != null)
      {
        boolean truncateEndElement = false;
        _pattern = Pattern.compile(_endElement);
        if (!str.startsWith("</") && str.length() < _endElement.length())
        {
          // gestion du cas particulier où la balise de fin (</values>) du dernier élément du fichier est tronquée
          _xml.append(str);
          str = _endElement;
          truncateEndElement = true;
        }
        _matcher = _pattern.matcher(str);
        int lastIdx = 0;
        // Tant qu'on trouve notre élément de fin (par ex : </values>)
        while (_matcher.find())
        {
          if (!truncateEndElement)
          {
            // On écrit tout dans notre xml depuis la dernière balise </values> qu'on a trouvé (on aura donc un élément de xml complet)
            _xml.append(str.substring(lastIdx, _matcher.end()));
          }
          // On remet notre offset à jour, pour que le prochain coup on ne découpe que l'élément suivant
          lastIdx = _matcher.end();
          // On va essayer de récupérer un élément complet, soit par exemple de <values> à </values>
          // On attends donc d'avoir le même nombre d'élément start ouvrant que fermant,
          // au cas où par exemple, on aurait un xml avec plusieurs <values> pour un élément
          String startElem = _endElement.replace("/", "");
          // On compte d'abord nos éléments ouvrant (<values> la plupart du temps)
          Scanner scanner = new Scanner(_xml.toString().replace(_startElement, ""));
          scanner.useDelimiter(startElem);
          int i = 0;
          while (scanner.hasNext())
          {
            scanner.next();
            i++;
          }
          // On compte ensuite nos éléments fermant
          Scanner scanner2 = new Scanner(_xml.toString());
          scanner2.useDelimiter(_endElement);
          int j = 0;
          while (scanner2.hasNext())
          {
            scanner2.next();
            j++;
          }
          // On a le même nombre d'ouvrant que de fermant, le xml est valide et prêt à être transformé
          if (i == j)
          {
            // On transforme notre élément (de <values> à </values> par exemple)
            // Si notre xml ne contient pas le root element, on va lui ajouter <root> et </root>, on ignorera ces balises lors
            // de la transformation xml, si on ne fait pas ça et qu'on envoit 2 élements à Stax (<values>...</values><values>...</values>),
            // il verra 2 éléments root xml (values), et plantera, en ajoutant ces <root> on s'assure d'avoir un seul élément root
            // De plus on retire la fin du vrai élément root (ex : </productListType>)
            if (!_xml.toString().contains(_startElement))
            {
              _xml = new StringBuilder("<root>").append(_xml.toString().replace(_endElementRef, "")).append("</root>");
            }
            // On envoit le xml à Stax
            _transformer.setBuffer(_xml);
            // On le transforme en fichier plat
            _transformer.write();
            _xml = new StringBuilder();
          }
        }
        if (str.startsWith(_endElementRef) && _template.getFooter() != null)
        {
          write(_template.getFooter());
        }
        // On n'a su trouver un élément xml complet, on écrit la fin du buffer de lecture dans notre xml, on continuera au prochain tick de write
        _xml.append(str.substring(lastIdx));
      }
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
  }
}
