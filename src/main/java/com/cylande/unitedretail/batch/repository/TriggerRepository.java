package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.batch.TriggerCriteriaListType;
import com.cylande.unitedretail.message.batch.TriggerCriteriaType;
import com.cylande.unitedretail.message.batch.TriggerKeyType;
import com.cylande.unitedretail.message.batch.TriggerListType;
import com.cylande.unitedretail.message.batch.TriggerScenarioType;
import com.cylande.unitedretail.message.batch.TriggerType;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Référentiel des définitions de triggers
 */
public final class TriggerRepository
{
  /** singleton */
  private static final TriggerRepository INSTANCE;
  static {
    INSTANCE = new TriggerRepository();
  }

  /** repository of definitions of triggers */
  private ConcurrentHashMap<String, TriggerType> _triggerRepository = null;

  /**
   * Constructor
   */
  private TriggerRepository()
  {
    _triggerRepository = new ConcurrentHashMap<String, TriggerType>();
  }

  /**
   * Get Unique Instance of Repository
   * @return instance of Repository
   */
  public static TriggerRepository getInstance()
  {
    return INSTANCE;
  }

  public TriggerType createTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext)
  {
    if (pTrigger != null)
    {
      _triggerRepository.put(pTrigger.getName(), pTrigger);
    }
    return pTrigger;
  }

  public TriggerListType createTriggerList(TriggerListType pList, TriggerScenarioType pScenario, ContextType pContext)
  {
    TriggerListType result = null;
    if (pList != null)
    {
      result = new TriggerListType();
      for (int i = 0; i < pList.getValues().size(); i++)
      {
        result.getValues().add(createTrigger(pList.getValues().get(i), pScenario, pContext));
      }
    }
    return result;
  }

  public TriggerType getTrigger(TriggerKeyType pKey, TriggerScenarioType pScenario, ContextType pContext)
  {
    TriggerType result = null;
    if (pKey != null)
    {
      result = _triggerRepository.get(pKey.getName());
    }
    return result;
  }

  public TriggerListType findTrigger(TriggerCriteriaListType pCriterias, TriggerScenarioType pScenario, ContextType pContext)
  {
    TriggerListType result = null;
    if (pCriterias != null)
    {
      if (!pCriterias.getList().isEmpty())
      {
        result = findCriterias(pCriterias, pScenario, pContext);
      }
      else
      {
        Collection values = _triggerRepository.values();
        if (!values.isEmpty())
        {
          result = new TriggerListType();
          for (Iterator listIterator = values.iterator(); listIterator.hasNext(); )
          {
            TriggerType pTrigger = (TriggerType)listIterator.next();
            if (pTrigger != null)
            {
              result.getValues().add(pTrigger);
            }
          }
        }
      }
    }
    return result;
  }

  public void deleteTrigger(TriggerKeyType pKey, TriggerScenarioType pScenario, ContextType pContext)
  {
    if (pKey != null)
    {
      if (_triggerRepository.containsKey(pKey.getName()))
      {
        _triggerRepository.remove(pKey.getName());
      }
    }
  }

  public void deleteTriggerList(TriggerCriteriaListType pCriterias, TriggerScenarioType pScenario, ContextType pContext)
  {
    if (pCriterias != null)
    {
      TriggerListType pList = findTrigger(pCriterias, pScenario, pContext);
      for (int i = 0; i < pList.getValues().size(); i++)
      {
        TriggerType beanTrigger = pList.getValues().get(i);
        if (beanTrigger != null)
        {
          _triggerRepository.remove(beanTrigger.getName());
        }
      }
    }
  }

  public TriggerType updateTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext)
  {
    return updateTrigger(pTrigger, pScenario, pContext, false);
  }

  public TriggerListType updateTriggerList(TriggerListType pList, TriggerScenarioType pScenario, ContextType pContext)
  {
    TriggerListType result = null;
    if (pList != null)
    {
      result = new TriggerListType();
      for (int i = 0; i < pList.getValues().size(); i++)
      {
        result.getValues().add(updateTrigger(pList.getValues().get(i), pScenario, pContext));
      }
    }
    return result;
  }

  private TriggerType updateTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext, boolean pCreate)
  {
    TriggerType result = null;
    if (pTrigger != null)
    {
      TriggerKeyType pKey = new TriggerKeyType();
      pKey.setName(pTrigger.getName());
      TriggerType beanTrigger = getTrigger(pKey, pScenario, pContext);
      if (beanTrigger != null)
      {
        _triggerRepository.put(pTrigger.getName(), pTrigger);
      }
      else if (pCreate)
      {
        result = createTrigger(pTrigger, pScenario, pContext);
      }
    }
    return result;
  }

  public TriggerType postTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext)
  {
    return updateTrigger(pTrigger, pScenario, pContext, true);
  }

  public TriggerListType postTriggerList(TriggerListType pList, TriggerScenarioType pScenario, ContextType pContext)
  {
    TriggerListType result = null;
    if (pList != null)
    {
      result = new TriggerListType();
      for (int i = 0; i < pList.getValues().size(); i++)
      {
        result.getValues().add(postTrigger(pList.getValues().get(i), pScenario, pContext));
      }
    }
    return result;
  }

  public TriggerListType findCriterias(TriggerCriteriaListType pCriterias, TriggerScenarioType pScenario, ContextType pContext)
  {
    TriggerListType result = null;
    for (int i = 0; i < pCriterias.getList().size(); i++)
    {
      TriggerCriteriaType pCriteria = pCriterias.getList().get(i);
      if (pCriteria != null)
      {
        Collection values = _triggerRepository.values();
        if (!values.isEmpty())
        {
          result = new TriggerListType();
          for (Iterator listIterator = values.iterator(); listIterator.hasNext(); )
          {
            TriggerType pTrigger = (TriggerType)listIterator.next();
            if (pTrigger != null)
            {
              if (pCriteria.getName() != null)
              {
                if (pCriteria.getName().getEquals() != null)
                {
                  if (pTrigger.getName().equals(pCriteria.getName().getEquals()))
                  {
                    result.getValues().add(pTrigger);
                  }
                }
                else if (pCriteria.getName().getContains() != null)
                {
                  if (pTrigger.getName().contains(pCriteria.getName().getContains()))
                  {
                    result.getValues().add(pTrigger);
                  }
                }
                else if (pCriteria.getName().getStartsWith() != null)
                {
                  if (pTrigger.getName().startsWith(pCriteria.getName().getStartsWith()))
                  {
                    result.getValues().add(pTrigger);
                  }
                }
              }
            }
          }
        }
      }
    }
    return result;
  }
}
