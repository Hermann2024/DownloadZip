package com.cylande.unitedretail.batch.execution.quartz;

import java.io.Serializable;

/**
 * Key object for Job Param
 */
public class JobParamKey implements Serializable
{
  /** internal key name */
  private String _name = null;

  /** internal mandatory flag*/
  private boolean _mandatory = true;

  /**
   * Constructor
   * @param pName : the name of the key
   * @param pMandatory : indicate if this param is mandatory
   */
  public JobParamKey(String pName, boolean pMandatory)
  {
    _name = pName;
    _mandatory = pMandatory;
  }

  /**
   * indicate if this param is mandatory
   * @return true or false
   */
  public boolean isMandatory()
  {
    return _mandatory;
  }

  /**
   * @return the param given name
   */
  public String getName()
  {
    return _name;
  }

  /**
   * @override Object.equals
   */
  public boolean equals(Object pOther)
  {
    boolean result = false;
    if (this == pOther)
    {
      result = true;
    }
    else
    {
      if (pOther instanceof JobParamKey)
      {
        result = _name.equals(((JobParamKey)pOther).getName());
      }
    }
    return result;
  }

  /**
   * @override Object.hashCode
   */
  public int hashCode()
  {
    int result = 0;
    if (_name != null)
    {
      result = _name.hashCode();
    }
    return result;
  }
}
