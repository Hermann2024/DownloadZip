package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchCriteriaType;
import com.cylande.unitedretail.message.batch.BatchDefNameListType;
import com.cylande.unitedretail.message.batch.BatchDefNameType;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchScenarioType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.repository.CriteriaTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Référentiel des définitions des batchs
 */
public final class BatchRepository
{
  /** singleton */
  private static final BatchRepository INSTANCE;
  static {
    INSTANCE = new BatchRepository();
  }

  /** repository of definitions of Batchs */
  private ConcurrentMap<String, BatchType> _batchRepositoryMap = null;

  /**
   * Constructor
   */
  private BatchRepository()
  {
    _batchRepositoryMap = new ConcurrentHashMap<String, BatchType>();
  }

  /**
   * Get unique Instance of BatchRepository
   * @return instance of BatchRepository
   */
  public static BatchRepository getInstance()
  {
    return INSTANCE;
  }

  public BatchType createBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext)
  {
    if (pBatch != null)
    {
      _batchRepositoryMap.put(pBatch.getName(), pBatch);
    }
    return pBatch;
  }

  public BatchListType createBatchList(BatchListType pList, BatchScenarioType pScenario, ContextType pContext)
  {
    BatchListType result = null;
    if (pList != null)
    {
      result = new BatchListType();
      for (BatchType batch: pList.getValues())
      {
        result.getValues().add(createBatch(batch, pScenario, pContext));
      }
    }
    return result;
  }

  public BatchType getBatch(BatchKeyType pKey, BatchScenarioType pScenario, ContextType pContext)
  {
    BatchType result = null;
    if (pKey != null)
    {
      result = _batchRepositoryMap.get(pKey.getName());
    }
    return result;
  }

  public BatchListType findBatch(BatchCriteriaListType pCriterias, BatchScenarioType pScenario, ContextType pContext)
  {
    BatchListType result = null;
    if (pCriterias != null)
    {
      if (!pCriterias.getList().isEmpty())
      {
        result = findCriterias(pCriterias, pScenario, pContext);
      }
      else
      {
        result = new BatchListType();
        Collection<BatchType> values = _batchRepositoryMap.values();
        result.getValues().addAll(values);
      }
    }
    return result;
  }

  public void deleteBatch(BatchKeyType pKey, BatchScenarioType pScenario, ContextType pContext)
  {
    if (pKey != null)
    {
      if (_batchRepositoryMap.containsKey(pKey.getName()))
      {
        _batchRepositoryMap.remove(pKey.getName());
      }
    }
  }

  public void deleteBatchList(BatchCriteriaListType pCriterias, BatchScenarioType pScenario, ContextType pContext)
  {
    if (pCriterias != null)
    {
      BatchListType pList = findBatch(pCriterias, pScenario, pContext);
      for (BatchType batch: pList.getValues())
      {
        _batchRepositoryMap.remove(batch.getName());
      }
    }
  }

  public BatchType updateBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext)
  {
    return updateBatch(pBatch, pScenario, pContext, false);
  }

  private BatchType updateBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext, boolean pCreate)
  {
    BatchType result = null;
    if (pBatch != null)
    {
      BatchKeyType key = new BatchKeyType();
      key.setName(pBatch.getName());
      BatchType beanBatch = getBatch(key, pScenario, pContext);
      if (beanBatch != null)
      {
        result = _batchRepositoryMap.put(pBatch.getName(), pBatch);
      }
      else if (pCreate)
      {
        result = createBatch(pBatch, pScenario, pContext);
      }
    }
    return result;
  }

  public BatchListType updateBatchList(BatchListType pList, BatchScenarioType pScenario, ContextType pContext)
  {
    BatchListType result = null;
    if (pList != null)
    {
      result = new BatchListType();
      for (BatchType batch: pList.getValues())
      {
        result.getValues().add(updateBatch(batch, pScenario, pContext));
      }
    }
    return result;
  }

  public BatchType postBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext)
  {
    return updateBatch(pBatch, pScenario, pContext, true);
  }

  public BatchListType postBatchList(BatchListType pList, BatchScenarioType pScenario, ContextType pContext)
  {
    BatchListType result = null;
    if (pList != null)
    {
      result = new BatchListType();
      for (BatchType batch: pList.getValues())
      {
        result.getValues().add(postBatch(batch, pScenario, pContext));
      }
    }
    return result;
  }

  public BatchListType findCriterias(BatchCriteriaListType pList, BatchScenarioType pScenario, ContextType pContext)
  {
    BatchListType result = new BatchListType();
    if (pList == null)
    {
      return result;
    }
    List<BatchType> batchList = new ArrayList<BatchType>();
    for (BatchCriteriaType criteria: pList.getList())
    {
      batchList.addAll(matchBatchCriteria(criteria));
    }
    result.setValues(batchList);
    return result;
  }

  private Set<BatchType> matchBatchCriteria(BatchCriteriaType pCriteria)
  {
    Set<BatchType> resultBatchSet = new HashSet<BatchType>();
    Collection<BatchType> values = _batchRepositoryMap.values();
    for (BatchType batch: values)
    {
      boolean test = true;
      if (pCriteria.getName() != null)
      {
        test = test && CriteriaTools.matchCriteriaString(pCriteria.getName(), batch.getName());
      }
      if (pCriteria.getType() != null)
      {
        test = test && CriteriaTools.matchCriteriaString(pCriteria.getType(), batch.getType().toString());
      }
      if (test)
      {
        resultBatchSet.add(batch);
      }
    }
    return resultBatchSet;
  }

  public BatchDefNameListType getBatchDefNameList(BatchScenarioType pScenario, ContextType pContext)
  {
    Set setkey = _batchRepositoryMap.keySet();
    if (setkey.isEmpty())
    {
      return null;
    }
    BatchDefNameListType result = new BatchDefNameListType();
    List<BatchDefNameType> namelist = new ArrayList<BatchDefNameType>();
    Iterator<String> it = setkey.iterator();
    while (it.hasNext())
    {
      BatchDefNameType name = new BatchDefNameType();
      name.setBatchName(it.next());
      namelist.add(name);
    }
    result.setList(namelist);
    return result;
  }
}
