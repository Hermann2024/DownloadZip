package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** {@inheritDoc} */
public class FlatRowTemplate extends AbstractFlatRow
{
  private static final Pattern FIELD_PATTERN = Pattern.compile("([\\.\\w]*)(\\{.*\\})?(\\[.*\\])?");
  private static final Pattern TRANSCO_FIELD_PATTERN = Pattern.compile("[\\.\\w]*\\{(.*)\\}(\\[.*\\])?");
  private static final Pattern FORMAT_FIELD_PATTERN = Pattern.compile(".*\\[(.*)\\]");
  protected String _separator = "";
  private String _quantifier;
  private String _transcoSeparator = "";

  /**
   * Constructeur de ligne de ligne template
   * @param pLine
   */
  public FlatRowTemplate(String pLine, FlatTemplate pTemplate)
  {
    _fields = new ArrayList<String>();
    _separator = pTemplate.getSeparator();
    _endSeparator = pLine.endsWith(_separator);
    _transcoSeparator = pTemplate.getTranscoSeparator();
    _header = pTemplate.getHeader();
    _fieldPrefix = pTemplate.getFieldPrefix();
    _fieldSuffix = pTemplate.getFieldSuffix();
    _fieldPrefixWithoutData = pTemplate.getFieldPrefixWithoutData();
    _fieldSuffixWithoutData = pTemplate.getFieldSuffixWithoutData();
    _lineSeparator = pTemplate.getLineSeparator();
    _noSeparator = pTemplate.getNoSeparator();
    read(pLine);
  }

  /**
   * Constructeur de ligne de ligne template
   * @param pLine
   */
  public FlatRowTemplate(String pLine)
  {
    _fields = new ArrayList<String>();
    read(pLine);
  }

  public Boolean getHeader()
  {
    return _header;
  }

  /**
   * Getter du quantifier
   * @return résultat
   */
  public String getQuantifier()
  {
    return _quantifier;
  }

  /** {@inheritDoc} */
  public void read(String pLine)
  {
    StringTokenizer strTok = new StringTokenizer(pLine, _separator);
    if (strTok.hasMoreElements())
    {
      _depth = Integer.parseInt(((String)strTok.nextElement()).trim());
    }
    if (strTok.hasMoreElements())
    {
      String element = (String)strTok.nextElement();
      _rootElement = element.replaceFirst("^(ns\\d+:)*([^(#\\*&]+).*", "$2");
      _quantifier = element.replaceFirst("^[^)]+\\)(.*)", "$1");
    }
    int i = 0;
    String[] transco;
    String field, name, format;
    while (strTok.hasMoreElements())
    {
      field = (String)strTok.nextElement();
      Matcher m = FIELD_PATTERN.matcher(field);
      if (m.matches())
      {
        name = m.group(1).trim();
        // On est dans un cas avec plusieurs niveaux de profondeur
        if (name.contains("."))
        {
          _levelMap.put(i, name);
        }
        m = TRANSCO_FIELD_PATTERN.matcher(field);
        if (m.matches())
        {
          transco = m.group(1).split(_transcoSeparator);
          Map<String, String> newTranscoMap = new HashMap<String, String>();
          _transcoMap.put(i, newTranscoMap);
          for (String transcoElem: transco)
          {
            newTranscoMap.put(transcoElem.split(":")[0], transcoElem.split(":")[1]);
          }
        }
        m = FORMAT_FIELD_PATTERN.matcher(field);
        if (m.matches())
        {
          if (m.group(1).endsWith("d") || m.group(1).endsWith("f"))
          {
            format = "%" + m.group(1);
          }
          else
          {
            format = m.group(1);
          }
          _formatFieldMap.put(i, format);
        }
        i++;
        _fields.add(name);
      }
    }
  }

  /** {@inheritDoc} */
  public void write(Writer pWriter)
  {
  }
}
