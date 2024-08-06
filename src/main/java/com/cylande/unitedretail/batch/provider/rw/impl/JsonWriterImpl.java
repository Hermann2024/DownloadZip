package com.cylande.unitedretail.batch.provider.rw.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.XML;

import com.cylande.unitedretail.batch.mapper.EngineWriter;

/** {@inheritDoc} */
public class JsonWriterImpl extends EngineWriter
{
  private static final Logger LOGGER = Logger.getLogger(JsonWriterImpl.class);
  private StringBuilder _xml = new StringBuilder();
  private String _startElement = null;
  private String _endElement = null;
  private String _endElementRoot = null;
  private String _startElementRoot = null;
  private Pattern _pattern = null;
  private Matcher _matcher = null;
  private boolean _first = true;
  private boolean _listMode = true;
  private boolean _jsonArray;

  /**
   * Constructeur
   * @param pOutPut
   * @param pTemplateFile
   * @throws IOException exception
   */
  public JsonWriterImpl(OutputStream pOutPut, boolean pJsonArray) throws IOException
  {
    super(pOutPut);
    _pattern = Pattern.compile("<([^>]*)>");
    _jsonArray = pJsonArray;
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
      int endElementIndex;
      String xmlString;
      if (_startElementRoot == null)
      {
        String elem = null, subElem = null;
        int i = 0;
        // On va d'abord récupérer l'élément de début et de fin
        while (_matcher.find())
        {
          i++;
          if (i == 2)
          {
            elem = _matcher.group(1);
            _listMode = elem.endsWith("ListType") || elem.endsWith("List");
            _startElementRoot = "<" + elem + ">";
            _endElementRoot = "</" + elem + ">";
            _matcher.find();
            subElem = _matcher.group(1);
            _startElement = "<" + subElem + ">";
            _endElement = "</" + subElem + ">";
            str = str.substring(_matcher.start());
            break;
          }
        }
        if (_listMode)
        {
          this.write(_jsonArray ? "[" : "{\"" + elem + "\":{\"" + subElem + "\":[");
        }
        else
        {
          this.write("{\"" + elem + "\":");
        }
      }
      // On a nos deux éléments, on peut donc lancer la transformation du xml
      if (_startElementRoot != null && _endElement != null)
      {
        if (str.equals(_endElementRoot))
        {
          if (_listMode)
          {
            this.write(_jsonArray ? "]" : "]}}");
          }
          else
          {
            this.write(XML.toJSONObject(_xml.toString(), true) + "}");
          }
          return;
        }
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
        while (_listMode && _matcher.find())
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
          // On compte d'abord nos éléments ouvrant (<values> la plupart du temps)
          Scanner scanner = new Scanner(_xml.toString());
          scanner.useDelimiter(_startElement);
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
            xmlString = _xml.toString();
            while (true)
            {
              endElementIndex = xmlString.toString().indexOf(_endElement);
              if (endElementIndex == -1)
              {
                break;
              }
              if (!_first)
              {
                this.write(",");
              }
              this.write(XML.toJSONObject(xmlString.substring(_startElement.length(), endElementIndex), true).toString());
              xmlString = xmlString.substring(endElementIndex + _endElement.length());
              if (_first)
              {
                _first = false;
              }
            }
            _xml = new StringBuilder();
          }
        }
        // On n'a su trouver un élément xml complet, on écrit la fin du buffer de lecture dans notre xml
        _xml.append(str.substring(lastIdx));
      }
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
  }

  public boolean isListMode()
  {
    return _listMode;
  }

  public String getEndElement()
  {
    return _endElement;
  }

  public String getStartElement()
  {
    return _startElement;
  }
}
