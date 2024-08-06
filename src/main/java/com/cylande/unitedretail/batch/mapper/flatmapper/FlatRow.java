package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/** {@inheritDoc} */
public class FlatRow extends AbstractFlatRow
{
  public String _separator = "|";
  private boolean _valid = true;
  private FlatTemplate _flatTemplate = null;

  /**
   * Constructeur de ligne de fichier plat
   * @param pLine
   * @param pTemplate
   */
  public FlatRow(String pLine, FlatTemplate pTemplate)
  {
    _flatTemplate = pTemplate;
    _separator = pTemplate.getSeparator();
    _fields = new ArrayList<String>();
    read(pLine);
  }

  /**
   * Constructeur de ligne de fichier plat
   * @param pLine
   * @param pTemplate
   * @param pSeparator
   */
  public FlatRow(String pLine, FlatTemplate pTemplate, String pSeparator)
  {
    _flatTemplate = pTemplate;
    _separator = pSeparator;
    _fields = new ArrayList<String>();
    read(pLine);
  }

  /**
   * Constructeur qui prend un template
   * @param pTemplate
   */
  public FlatRow(FlatRowTemplate pTemplate)
  {
    _header = pTemplate._header;
    _separator = pTemplate._noSeparator ? "" : pTemplate._separator;
    _template = pTemplate;
    _fields = new ArrayList<String>();
    _rootElement = pTemplate.getRootElement();
    _depth = pTemplate.getDepth();
    String format;
    for (int i = 0; i < pTemplate.getFieldsSize(); i++)
    {
      format = pTemplate._formatFieldMap.get(i);
      if (format != null && format.matches("[0-9]+"))
      {
        // règle de rétrocompatibilité pour renseigner uniquement avec des espaces un champ de taille fixe non lié à une balise
        _fields.add(String.format("%" + Integer.parseInt(format) + "s", ""));
      }
      else
      {
        _fields.add("");
      }
    }
  }

  /**
   * Getter sur la booleen qui définit si la ligne est valide
   * @return résultat
   */
  public boolean isValid()
  {
    return _valid;
  }

  /** {@inheritDoc} */
  public void read(String pLine)
  {
    if (pLine != null && pLine.length() > 0 && Character.isDigit(pLine.charAt(0)) && _flatTemplate.getHeader())
    {
      _valid = true;
      String[] tokens = parseLine(pLine);
      _depth = Integer.parseInt(tokens[0].trim());
      _rootElement = tokens[1].trim();
      for (int i = 2; i < tokens.length; i++)
      {
        _fields.add(tokens[i]);
      }
    }
    // Si on est en mode sans en-tête
    else if (pLine != null && pLine.length() > 0 && !_flatTemplate.getHeader())
    {
      _valid = true;
      String[] tokens = parseLine(pLine);
      // La profondeur du fichier de données sera toujours à 1
      _depth = 1;
      // On récupère l'élément root de la ligne du template (souvent values) car on ne l'a pas dans le fichier de données
      _rootElement = _flatTemplate.getLineHeader();
      for (int i = 0; i < tokens.length; i++)
      {
        _fields.add(tokens[i]);
      }
    }
    else
    {
      _valid = false;
    }
  }

  private String[] parseLine(String pLine)
  {
    String[] result = null;
    char separator = _separator.charAt(0);
    if (separator == ';')
    {
      // permet d'interpréter les caractères html "&gt;", "&lt;", "&quot;", "&amp;" et "&apos;" afin que le ";" qu'ils contiennent ne soit pas
      // confondu avec le séparateur ";"
      pLine = StringEscapeUtils.unescapeXml(pLine);
      if (_flatTemplate.getFieldSuffix() == null && _flatTemplate.getFieldPrefix() == null)
      {
        result = deleteDoubleQuote(pLine);
      }
    }
    else
    {
      result = pLine.split(Pattern.quote(_separator));
    }
    if (_flatTemplate.getFieldSuffix() != null || _flatTemplate.getFieldPrefix() != null)
    {
      if (result == null)
      {
        result = pLine.split(Pattern.quote(_separator));
      }
      List<String> fieldList = new ArrayList();
      String tmp;
      for (String field: result)
      {
        tmp = field;
        if (field.startsWith(_flatTemplate.getFieldPrefix()))
        {
          tmp = tmp.substring(_flatTemplate.getFieldPrefix().length());
        }
        if (field.endsWith(_flatTemplate.getFieldSuffix()))
        {
          tmp = tmp.substring(0, tmp.length() - _flatTemplate.getFieldSuffix().length());
        }
        fieldList.add(tmp);
      }
      result = fieldList.toArray(new String[fieldList.size()]);
    }
    return result;
  }

  /**
   * Supprime les doubles quotes sauf ceux échappés par '\'. Dans ce cas, seul le caractère '\' est supprimé.
   * @param pLine
   * @return
   */
  private String[] deleteDoubleQuote(String pLine)
  {
    String[] result;
    List<String> lines = new ArrayList<String>();
    boolean quoteField = false;
    char prevChar = '1';
    StringBuilder sb = new StringBuilder();
    for (char c: pLine.toCharArray())
    {
      if (c == '"' && sb.length() == 0)
      {
        quoteField = true;
        prevChar = c;
        continue;
      }
      switch (c)
      {
        case ';':
          if (quoteField)
          {
            if (prevChar == '"')
            {
              lines.add(sb.toString());
              sb = new StringBuilder();
              quoteField = false;
            }
            else
            {
              sb.append(c);
            }
          }
          else
          {
            lines.add(sb.toString());
            sb = new StringBuilder();
          }
          break;
        case '"':
          if (prevChar == '\\')
          {
            sb.deleteCharAt(sb.length() - 1);
            sb.append(c);
          }
          break;
        default:
          sb.append(c);
      }
      prevChar = c;
    }
    lines.add(sb.toString());
    result = lines.toArray(new String[lines.size()]);
    return result;
  }

  /** {@inheritDoc} */
  public void write(Writer pWriter) throws IOException
  {
    if (_header)
    {
      pWriter.write(_depth + _separator);
      pWriter.write(_rootElement + _separator);
    }
    int i = 1;
    String defaultValue;
    for (String field: _fields)
    {
      defaultValue = _template._transcoMap.get(i - 1) != null ? _template._transcoMap.get(i - 1).get("") : null;
      if (defaultValue == null && field.length() == 0 && _template._fieldPrefixWithoutData != null)
      {
        pWriter.write(_template._fieldPrefixWithoutData);
      }
      pWriter.write(defaultValue == null ? field : defaultValue);
      if (defaultValue == null && field.length() == 0 && _template._fieldSuffixWithoutData != null)
      {
        pWriter.write(_template._fieldSuffixWithoutData);
      }
      if (i < _fields.size())
      {
        pWriter.write(_separator);
      }
      else
      {
        pWriter.write(_template._endSeparator ? _separator : "");
      }
      i++;
    }
    if (_header || _depth != 0)
    {
      pWriter.write(_template._lineSeparator);
    }
  }
}
