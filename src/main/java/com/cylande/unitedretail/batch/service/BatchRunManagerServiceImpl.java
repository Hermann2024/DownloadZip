package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.common.BatchRunManagerModule;
import com.cylande.unitedretail.batch.service.common.BatchRunManagerService;
import com.cylande.unitedretail.batch.service.exception.BatchStatusEngineServiceException;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchBooleanResponseType;
import com.cylande.unitedretail.message.batch.BatchCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunNameListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchStatusScenarioType;
import com.cylande.unitedretail.message.batch.BatchSummaryCriteriaCondensaType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class BatchRunManagerServiceImpl extends AbstractCRUDServiceImpl implements BatchRunManagerService
{

  /**
   * Constructeur
   */
  public BatchRunManagerServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "BatchRunManagerModule");
  }

  /** {@inheritDoc} */
  public BatchRunType createBatchRun(BatchRunType pBatchRun, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchRun, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createBatchRun(pBatchRun, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunListType createBatchRunList(BatchRunListType pList, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createBatchRunList(pList, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void deleteBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteBatchRun(pKey, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
  }

  /** {@inheritDoc} */
  public void deleteBatchRunList(BatchRunCriteriaListType pCriterias, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      try
      {
        myModule.initDeleteBatchRunList(pCriterias);
        while (myModule.hasNextPacketToDelete())
        {
          myModule.deleteBatchRunPacket(pScenario, pContext);
          getTransaction().commit(pContext);
        }
      }
      finally
      {
        myModule.releaseDeleteBatchRunList();
      }
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
  }

  /** {@inheritDoc} */
  public BatchRunListType findBatchRun(BatchRunCriteriaListType pCriterias, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.findBatchRun(pCriterias, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunType getBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getBatchRun(pKey, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunType postBatchRun(BatchRunType pBatchRun, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchRun, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postBatchRun(pBatchRun, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunListType postBatchRunList(BatchRunListType pList, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postBatchRunList(pList, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunType updateBatchRun(BatchRunType pBatchRun, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchRun, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateBatchRun(pBatchRun, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunListType updateBatchRunList(BatchRunListType pList, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateBatchRunList(pList, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  public BatchRunNameListType findBatchRunSummary(BatchSummaryCriteriaCondensaType pParams, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunNameListType result = null;
    getChrono().start();
    getJAXBManager().write(pParams, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.findBatchRunSummary(pParams, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }
  /* --- CUSTOM METHODS --- */

  public BatchBooleanResponseType atLeastTaskError(BatchRunKeyType pBatchRunKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatchRunKey == null || pBatchRunKey.getId() == null || pBatchRunKey.getPath() == null)
    {
      //TODO exception
      return null;
    }
    BatchBooleanResponseType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchRunKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.atLeastTaskError(pBatchRunKey, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  @Deprecated
  public BatchBooleanResponseType atLeastBatchInstanceInError(BatchCriteriaType pCriteria, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pCriteria == null)
    {
      //TODO exception
      return null;
    }
    BatchBooleanResponseType result = null;
    getChrono().start();
    getJAXBManager().write(pCriteria.getName().getEquals(), pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.atLeastBatchInstanceInError(pCriteria, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  public BatchBooleanResponseType atLeastBatchInstanceInError(BatchRunCriteriaType pCriteria, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pCriteria == null)
    {
      //TODO exception
      return null;
    }
    BatchBooleanResponseType result = null;
    getChrono().start();
    getJAXBManager().write(pCriteria, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.atLeastBatchInstanceInError(pCriteria, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  @Deprecated
  public BatchRunType getLastActivationBatch(BatchCriteriaType pBatchCrit, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatchCrit == null || pBatchCrit.getName() == null)
    {
      //TODO exception
      return null;
    }
    BatchRunType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchCrit.getName().getEquals(), pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getLastActivationBatch(pBatchCrit, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  public BatchRunType getLastActivationBatch(BatchRunCriteriaType pBatchCrit, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatchCrit == null || pBatchCrit.getPath() == null)
    {
      //TODO exception
      return null;
    }
    BatchRunType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchCrit, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getLastActivationBatch(pBatchCrit, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  public BatchBooleanResponseType atLeastBatchInstanceIsRunning(BatchRunCriteriaType pBatchCrit, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchBooleanResponseType result = null;
    getChrono().start();
    getJAXBManager().write(pBatchCrit, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.atLeastBatchInstanceIsRunning(pBatchCrit, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  public TaskAuditListType getTaskAuditOfBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pKey == null)
    {
      return null;
    }
    TaskAuditListType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getTaskAuditOfBatchRun(pKey, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  public BatchBooleanResponseType batchRunIsOnError(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchBooleanResponseType response = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      boolean result = myModule.batchRunIsOnError(pKey);
      response = new BatchBooleanResponseType();
      response.setValue(result);
      getJAXBManager().write(response);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return response;
  }

  public BatchRunListType getBatchRunChildrenOfBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunListType response = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      response = myModule.getBatchRunOfBatchRun(pKey, pScenario, pContext);
      getJAXBManager().write(response);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return response;
  }

  public TaskRunListType getTaskRunChildrenOfBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunListType response = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      response = myModule.getTaskRunOfBatchRun(pKey, pScenario, pContext);
      getJAXBManager().write(response);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return response;
  }

  public BatchRunType getLastBatchInProgress(BatchRunKeyType pKey, BatchStatusScenarioType pScenario, ContextType pContext) throws BatchStatusEngineServiceException, WrapperServiceException
  {
    BatchRunType result = null;
    getChrono().start();
    BatchRunManagerModule myModule = (BatchRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      result = myModule.getLastBatchInProgress(pKey, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }
}
