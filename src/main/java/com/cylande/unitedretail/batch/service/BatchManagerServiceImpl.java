package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.repository.BatchRepository;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchDefNameListType;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchScenarioType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Service qui permet de créer, supprimer et modifier des Batchs.
 * @author eselosse.
 * @since 05/03/08.
 */
public class BatchManagerServiceImpl extends AbstractCRUDServiceImpl implements BatchManagerService
{
  private BatchRepository _batchRepository = null;

  /**
   * Constructor
   */
  public BatchManagerServiceImpl()
  {
    _batchRepository = BatchRepository.getInstance();
  }

  /** {@inheritDoc} */
  public BatchType createBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchType result = null;
    getChrono().start();
    getJAXBManager().write(pBatch, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.createBatch(pBatch, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchListType createBatchList(BatchListType pList, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.createBatchList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void deleteBatch(BatchKeyType pKey, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _batchRepository.deleteBatch(pKey, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
  }

  /** {@inheritDoc} */
  public void deleteBatchList(BatchCriteriaListType pCriterias, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _batchRepository.deleteBatchList(pCriterias, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
  }

  /** {@inheritDoc} */
  public BatchListType findBatch(BatchCriteriaListType pCriterias, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.findBatch(pCriterias, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchDefNameListType getBatchDefList(BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchDefNameListType result = null;
    getChrono().start();
    getJAXBManager().write(pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.getBatchDefNameList(pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchType getBatch(BatchKeyType pKey, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.getBatch(pKey, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchType updateBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchType result = null;
    getChrono().start();
    getJAXBManager().write(pBatch, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.updateBatch(pBatch, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchListType updateBatchList(BatchListType pList, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.updateBatchList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchType postBatch(BatchType pBatch, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchType result = null;
    getChrono().start();
    getJAXBManager().write(pBatch, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.postBatch(pBatch, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchListType postBatchList(BatchListType pList, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _batchRepository.postBatchList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }
}
