package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Objet correspondant a une ligne de fichier plat
 */
public abstract class AbstractFlatRow
{

  /**
   * Separateur regexp
   */
  protected static final String SEPARATOR_IN = "\\|";

  /**
   * Separateur
   */
  protected static final String SEPARATOR_OUT = "|";
  public Map<Integer, String> _levelMap = new HashMap<Integer, String>();
  public Map<Integer, Map<String, String>> _transcoMap = new HashMap<Integer, Map<String, String>>();
  /** Nous sert à connaître le niveau de profondeur dans lequel on est dans le xml (ex : person,key,id) */
  public List<String> _inFields = null;
  /**
   * Longueur en caractères des champs
   */
  protected List<Integer> _fieldsLength;
  protected Boolean _header = true;
  protected boolean _endSeparator = true;
  protected String _lineSeparator;
  protected boolean _noSeparator;
  protected String _fieldPrefix;
  protected String _fieldSuffix;
  protected String _fieldPrefixWithoutData;
  protected String _fieldSuffixWithoutData;
  /**
   * Template
   */
  protected AbstractFlatRow _template;
  /**
   * Profondeur dans l'arbre xml
   */
  protected int _depth;
  /**
   * Element root
   */
  protected String _rootElement;
  /**
   * Liste des champs
   */
  protected List<String> _fields;
  protected Map<Integer, String> _formatFieldMap = new HashMap();

  /**
   * Parsing d'une ligne
   * @param pLine
   */
  public abstract void read(String pLine);

  /**
   * methode d'écriture
   * @param pWriter
   * @throws IOException exception
   */
  public abstract void write(Writer pWriter) throws IOException;

  /**
   * Nombre de champs
   * @return résultat
   */
  public int getFieldsSize()
  {
    return _fields.size();
  }

  /**
   * Affectation d'un champ
   * @param pIndex index du champ
   * @param pValue valeur a affecter
   */
  public void setField(int pIndex, String pValue)
  {
    _fields.set(pIndex, pValue);
  }

  /**
   * Lecture d'un champ
   * @param pIndex index de lecture
   * @return résultat
   */
  public String getField(int pIndex)
  {
    return _fields.get(pIndex);
  }

  /**
   * Setter profondeur
   * @param pDepth
   */
  public void setDepth(int pDepth)
  {
    this._depth = pDepth;
  }

  /**
   * Getter profondeur
   * @return résultat
   */
  public int getDepth()
  {
    return _depth;
  }

  /**
   * Getter sur root element
   * @return résultat
   */
  public String getRootElement()
  {
    return _rootElement;
  }

  /**
   * Setter sur root element
   * @param pRootElement
   */
  public void setRootElement(String pRootElement)
  {
    _rootElement = pRootElement;
  }

  /**
   * Getter sur la longueur des champs
   * @return résultat
   */
  public List<Integer> getFieldsLength()
  {
    return _fieldsLength;
  }

  /**
   * Getter sur le Template
   * @return résultat
   */
  public AbstractFlatRow getTemplate()
  {
    return _template;
  }

  /**
   * Retourne l'index d'un champ en fonction de son nom
   * @param pFieldName
   * @return résultat
   */
  public int getIndex(String pFieldName)
  {
    return _fields.indexOf(pFieldName);
  }

  /**
   * Récupère la liste des numéros de champs ayant la même valeur correspondante à pFieldIndex
   * @param pFieldIndex
   * @return
   */
  public List<Integer> getIndexList(int pFieldIndex)
  {
    List<Integer> result = new ArrayList();
    String name = _fields.get(pFieldIndex);
    int i = 0;
    for (String n: _fields)
    {
      if (n.equals(name))
      {
        result.add(i);
      }
      i++;
    }
    return result;
  }
}
