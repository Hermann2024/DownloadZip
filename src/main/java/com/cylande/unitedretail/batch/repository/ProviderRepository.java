package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.message.batch.ProviderCriteriaListType;
import com.cylande.unitedretail.message.batch.ProviderCriteriaType;
import com.cylande.unitedretail.message.batch.ProviderKeyType;
import com.cylande.unitedretail.message.batch.ProviderListType;
import com.cylande.unitedretail.message.batch.ProviderScenarioType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.repository.CriteriaTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Référentiel des définitions de providers
 */
public final class ProviderRepository
{
  /** singleton */
  private static final ProviderRepository INSTANCE;
  static {
    INSTANCE = new ProviderRepository();
  }

  /** repository of definition of Providers */
  private ConcurrentHashMap<String, ProviderType> _providerRepositoryMap = null;

  /**
   * Constructor
   */
  private ProviderRepository()
  {
    _providerRepositoryMap = new ConcurrentHashMap<String, ProviderType>();
  }

  /**
   * Get Unique Instance of ProviderRepository
   * @return instance of ProviderRepository
   */
  public static ProviderRepository getInstance()
  {
    return INSTANCE;
  }

  public ProviderType createProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext)
  {
    if (pProvider != null)
    {
      _providerRepositoryMap.put(pProvider.getName(), pProvider);
    }
    return pProvider;
  }

  public ProviderListType createProviderList(ProviderListType pList, ProviderScenarioType pScenario, ContextType pContext)
  {
    ProviderListType result = null;
    if (pList != null)
    {
      result = new ProviderListType();
      for (ProviderType provider: pList.getValues())
      {
        result.getValues().add(createProvider(provider, pScenario, pContext));
      }
    }
    return result;
  }

  public ProviderType getProvider(ProviderKeyType pKey, ProviderScenarioType pScenario, ContextType pContext)
  {
    ProviderType result = null;
    if (pKey != null)
    {
      result = _providerRepositoryMap.get(pKey.getName());
    }
    return result;
  }

  public ProviderListType findProvider(ProviderCriteriaListType pCriterias, ProviderScenarioType pScenario, ContextType pContext)
  {
    ProviderListType result = null;
    if (pCriterias != null)
    {
      if (!pCriterias.getList().isEmpty())
      {
        result = findCriterias(pCriterias, pScenario, pContext);
      }
      else
      {
        result = new ProviderListType();
        Collection<ProviderType> values = _providerRepositoryMap.values();
        result.getValues().addAll(values);
      }
    }
    return result;
  }

  public void deleteProvider(ProviderKeyType pKey, ProviderScenarioType pScenario, ContextType pContext)
  {
    if (pKey != null)
    {
      if (_providerRepositoryMap.containsKey(pKey.getName()))
      {
        _providerRepositoryMap.remove(pKey.getName());
      }
    }
  }

  public void deleteProviderList(ProviderCriteriaListType pCriterias, ProviderScenarioType pScenario, ContextType pContext)
  {
    if (pCriterias != null)
    {
      ProviderListType pList = findProvider(pCriterias, pScenario, pContext);
      for (ProviderType provider: pList.getValues())
      {
        _providerRepositoryMap.remove(provider.getName());
      }
    }
  }

  public ProviderType updateProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext)
  {
    return updateProvider(pProvider, pScenario, pContext, false);
  }

  private ProviderType updateProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext, boolean pCreate)
  {
    ProviderType result = null;
    if (pProvider != null)
    {
      ProviderKeyType pKey = new ProviderKeyType();
      pKey.setName(pProvider.getName());
      ProviderType beanProvider = getProvider(pKey, pScenario, pContext);
      if (beanProvider != null)
      {
        result = _providerRepositoryMap.put(pProvider.getName(), pProvider);
      }
      else if (pCreate)
      {
        result = createProvider(pProvider, pScenario, pContext);
      }
    }
    return result;
  }

  public ProviderListType updateProviderList(ProviderListType pList, ProviderScenarioType pScenario, ContextType pContext)
  {
    ProviderListType result = null;
    if (pList != null)
    {
      result = new ProviderListType();
      for (ProviderType provider: pList.getValues())
      {
        result.getValues().add(updateProvider(provider, pScenario, pContext));
      }
    }
    return result;
  }

  public ProviderType postProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext)
  {
    return updateProvider(pProvider, pScenario, pContext, true);
  }

  public ProviderListType postProviderList(ProviderListType pList, ProviderScenarioType pScenario, ContextType pContext)
  {
    ProviderListType result = null;
    if (pList != null)
    {
      result = new ProviderListType();
      for (ProviderType provider: pList.getValues())
      {
        result.getValues().add(postProvider(provider, pScenario, pContext));
      }
    }
    return result;
  }

  public ProviderListType findCriterias(ProviderCriteriaListType pList, ProviderScenarioType pScenario, ContextType pContext)
  {
    ProviderListType result = new ProviderListType();
    if (pList == null)
    {
      return result;
    }
    List<ProviderType> providerList = new ArrayList<ProviderType>();
    for (ProviderCriteriaType criteria: pList.getList())
    {
      providerList.addAll(matchProviderCriteria(criteria));
    }
    result.setValues(providerList);
    return result;
  }

  private Set<ProviderType> matchProviderCriteria(ProviderCriteriaType pCriteria)
  {
    Set<ProviderType> resultProviderSet = new HashSet<ProviderType>();
    Collection<ProviderType> values = _providerRepositoryMap.values();
    for (ProviderType provider: values)
    {
      if (pCriteria.getName() != null && CriteriaTools.matchCriteriaString(pCriteria.getName(), provider.getName()))
      {
        resultProviderSet.add(provider);
      }
    }
    return resultProviderSet;
  }
}
