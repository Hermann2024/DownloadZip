package com.cylande.unitedretail.batch.execution.quartz;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class JobParamMap implements Serializable
{
  private Hashtable<JobParamKey, Object> _internalMap = new Hashtable<JobParamKey, Object>();

  public JobParamMap()
  {
  }

  /**
   * Defini une nouvelle valeur de paramètre
   * @param pKey   : la clef
   * @param pValue : la valeur
   * @return Object : l'ancienne valeur
   */
  public Object put(JobParamKey pKey, Object pValue)
  {
    Object result = null;
    if ((pKey != null) && (pValue != null))
    {
      result = _internalMap.put((JobParamKey)pKey, pValue);
    }
    return result;
  }

  /**
   * Retourne la valeur pour un paramètre
   * @param pKey : la clef
   * @return la valeur
   */
  public Object get(JobParamKey pKey)
  {
    Object result = null;
    if (pKey != null)
    {
      result = _internalMap.get(pKey);
    }
    return result;
  }

  public Iterator<JobParamKey> keyIterator()
  {
    return _internalMap.keySet().iterator();
  }

  public Set<Entry<JobParamKey, Object>> entrySet()
  {
    return _internalMap.entrySet();
  }

  public int size()
  {
    return _internalMap.size();
  }
}
