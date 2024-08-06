package com.cylande.unitedretail.batch.provider;

import java.io.Serializable;

/**
 * Paquet de donn�es
 * Data Package
 */
public class DataPackage implements Serializable
{
  /** la donn�e elle-m�me */
  private String _value = null;
  /** le num�ro du package */
  private int _packageNumber = 0;
  /** indique si il s'agit du dernier paquet */
  private boolean _lastPackage = false;
  /** repr�sentation de l'objet (methode to string) */
  private String _toString = null;
  /** nom de fichier associ� au datapackage **/
  private String _fileName = null;

  /**
   * Constructeur
   * Constructor
   */
  public DataPackage()
  {
  }

  /**
   * setter de la donn�e
   * _fileName setter
   * @param pValue : the _fileName
   */
  public void setFileName(String pValue)
  {
    _fileName = pValue;
  }

  /**
   * accesseur de la donn�e
   * @return the _fileName
   */
  public String getFileName()
  {
    return _fileName;
  }

  /**
   * setter de la donn�e
   * data setter
   * @param pValue : the data
   */
  public void setValue(String pValue)
  {
    _value = pValue;
    _toString = null;
  }

  /**
   * acesseur de la donn�e
   * @return the data
   */
  public String getValue()
  {
    return _value;
  }

  /**
   * Indique le num�ro du paquet
   * @param packageNumber : the package number
   */
  public void setPackageNumber(int packageNumber)
  {
    _packageNumber = packageNumber;
    _toString = null;
  }

  /**
   * Retourne le num�ro de paquet
   * @return the package number
   */
  public int getPackageNumber()
  {
    return _packageNumber;
  }

  /**
   * Setteur du flag de dernier paquet de flux
   * @param pLastPackage last package flag.
   */
  public void setLastPackage(boolean pLastPackage)
  {
    _lastPackage = pLastPackage;
    _toString = null;
  }

  /**
   * Indique si ce paquet est le dernier du flux
   * @return true if this is the least stream package
   */
  public boolean isLastPackage()
  {
    return _lastPackage;
  }

  public String toString()
  {
    if (_toString == null)
    {
      StringBuilder sbResult = new StringBuilder("DataPackage: packageNumber=");
      sbResult.append(_packageNumber);
      sbResult.append(", lastpackage=");
      sbResult.append(_lastPackage);
      sbResult.append(",data=");
      if (_value == null)
      {
        sbResult.append("null");
      }
      else
      {
        sbResult.append("\n");
        sbResult.append(_value);
        sbResult.append("\n");
      }
      _toString = sbResult.toString();
    }
    return _toString;
  }
}
