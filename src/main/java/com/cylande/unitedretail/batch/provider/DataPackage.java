package com.cylande.unitedretail.batch.provider;

import java.io.Serializable;

/**
 * Paquet de données
 * Data Package
 */
public class DataPackage implements Serializable
{
  /** la donnée elle-même */
  private String _value = null;
  /** le numéro du package */
  private int _packageNumber = 0;
  /** indique si il s'agit du dernier paquet */
  private boolean _lastPackage = false;
  /** représentation de l'objet (methode to string) */
  private String _toString = null;
  /** nom de fichier associé au datapackage **/
  private String _fileName = null;

  /**
   * Constructeur
   * Constructor
   */
  public DataPackage()
  {
  }

  /**
   * setter de la donnée
   * _fileName setter
   * @param pValue : the _fileName
   */
  public void setFileName(String pValue)
  {
    _fileName = pValue;
  }

  /**
   * accesseur de la donnée
   * @return the _fileName
   */
  public String getFileName()
  {
    return _fileName;
  }

  /**
   * setter de la donnée
   * data setter
   * @param pValue : the data
   */
  public void setValue(String pValue)
  {
    _value = pValue;
    _toString = null;
  }

  /**
   * acesseur de la donnée
   * @return the data
   */
  public String getValue()
  {
    return _value;
  }

  /**
   * Indique le numéro du paquet
   * @param packageNumber : the package number
   */
  public void setPackageNumber(int packageNumber)
  {
    _packageNumber = packageNumber;
    _toString = null;
  }

  /**
   * Retourne le numéro de paquet
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
