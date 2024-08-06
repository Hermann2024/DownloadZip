package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.service.common.BatchStatusEngineService;
import com.cylande.unitedretail.batch.service.exception.BatchStatusEngineServiceException;
import com.cylande.unitedretail.batch.transformer.coordinator.BatchReportCoordinator;
import com.cylande.unitedretail.batch.transformer.coordinator.TaskReportCoordinator;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.framework.service.AbstractFunctionnalServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.AbstractReportType;
import com.cylande.unitedretail.message.batch.BatchBooleanResponseType;
import com.cylande.unitedretail.message.batch.BatchReportContentListType;
import com.cylande.unitedretail.message.batch.BatchReportType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchStatusScenarioType;
import com.cylande.unitedretail.message.batch.ReportBooleanResponseType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskReportType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunScenarioType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Génère les rapports de batch pour la servlet
 */
public class BatchStatusEngineServiceImpl extends AbstractFunctionnalServiceImpl implements BatchStatusEngineService
{
  /** Traçabilité des batchs */
  private BatchRunManagerServiceImpl _batchRunManager = null;

  /** Traçabilité des tasks */
  private TaskRunManagerServiceImpl _taskRunManager = null;

  /**
   * Constructeur
   */
  public BatchStatusEngineServiceImpl()
  {
    super();
    _batchRunManager = new BatchRunManagerServiceImpl();
    _taskRunManager = new TaskRunManagerServiceImpl();
  }

  /**
   * Génère un rapport complet de batch à partir d'une clé de batch
   * @param pKey pKey
   * @param pScenario pScenario
   * @param pContext pContext
   * @return result
   * @throws RemoteException exception
   * @throws BatchStatusEngineServiceException exception
   */
  public BatchReportType getBatchRunStatus(BatchRunKeyType pKey, BatchStatusScenarioType pScenario, ContextType pContext) throws RemoteException, BatchStatusEngineServiceException
  {
    try
    {
      getTransaction().init(pContext);
      fillKeySiteCode(pKey, pContext);
      validateBatchRunKeyType(pKey, "getBatchRunStatus");
      BatchReportType result = null;
      // récupération du batchrun associé à la clé
      BatchRunType batchRun = getBatchRun(pKey, pContext, "getBatchRunStatus");
      result = generateBatchReport(batchRun, pContext);
      return result;
    }
    finally
    {
      release(pContext);
    }
  }

  /**
   * Complète le site de la clé avec le site local, si celui n'est pas renseigné
   * @param pKey
   * @param pContext
   */
  private void fillKeySiteCode(BatchRunKeyType pKey, ContextType pContext)
  {
    if (pKey != null && pKey.getSite() == null)
    {
      pKey.setSite(SiteUtils.getLocalSite(pContext));
    }
  }

  /**
   * Génère un rapport complet de batch à partir d'un batchRun
   * @param pBatchRun
   * @param pContext
   * @return résultat
   * @throws BatchStatusEngineServiceException exception
   */
  private BatchReportType generateBatchReport(BatchRunType pBatchRun, ContextType pContext) throws BatchStatusEngineServiceException
  {
    BatchReportType result = null;
    try
    {
      result = new BatchReportType();
      BatchReportCoordinator.fillBatchReport(result, pBatchRun);
      List<AbstractReportType> list = new ArrayList<AbstractReportType>();
      // récupération des batchRun fils
      BatchRunListType batchrunlist = _batchRunManager.getBatchRunChildrenOfBatchRun(pBatchRun, new BatchRunScenarioType(), pContext);
      if (batchrunlist != null && !batchrunlist.getValues().isEmpty())
      {
        for (BatchRunType batchrun: batchrunlist.getValues())
        {
          // attention récursion sur les batchRuns : batchRun -* batchRun
          list.add(generateBatchReport(batchrun, pContext));
        }
      }
      // récupération des taskRun filles
      TaskRunListType taskrunlist = _batchRunManager.getTaskRunChildrenOfBatchRun(pBatchRun, new BatchRunScenarioType(), pContext);
      if (taskrunlist != null && !taskrunlist.getValues().isEmpty())
      {
        for (TaskRunType taskrun: taskrunlist.getValues())
        {
          //générer le rapport et ajouter le content
          list.add(generateTaskReport(taskrun, pContext));
        }
      }
      // préparation du content
      BatchReportContentListType content = new BatchReportContentListType();
      content.setTaskOrBatch(list);
      if (pBatchRun.getMode() != null)
      {
        content.setMode(pBatchRun.getMode());
      }
      result.setContent(content);
      // positionnement de la marque d'erreur
      // TODO à consolider dans les prochaines évolutions !
      BatchBooleanResponseType bbrt = _batchRunManager.batchRunIsOnError(pBatchRun, new BatchRunScenarioType(), pContext);
      result.setError(bbrt.getValue());
    }
    catch (WrapperServiceException e)
    {
      // une erreur s'est produite pendant la récupération d'informations pour la génération du rapport de batch de nom {0} d'id {1}
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_GEN_BATCH_REPORT_ERROR, new Object[] { pBatchRun.getPath(), pBatchRun.getId() }, e);
    }
    return result;
  }

  /**
   * Génération d'un rapport de task à partir de son taskRun
   * @param pTaskRun
   * @param pContext
   * @return résultat
   * @throws BatchStatusEngineServiceException exception
   */
  private TaskReportType generateTaskReport(TaskRunType pTaskRun, ContextType pContext) throws BatchStatusEngineServiceException
  {
    TaskReportType result = null;
    try
    {
      result = new TaskReportType();
      TaskReportCoordinator.fillTaskReport(result, pTaskRun);
      TaskAuditListType taskauditlist = _taskRunManager.getTaskAuditOfTaskRun(pTaskRun, new TaskRunScenarioType(), pContext);
      TaskReportCoordinator.fillTaskReportError(result, taskauditlist);
    }
    catch (WrapperServiceException e)
    {
      // Une erreur s'est produite pendant la récupération des erreurs d'un taskrun de nom {0} d'id {1}
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_GEN_TASK_REPORT_ERROR, new Object[] { pTaskRun.getPath(), pTaskRun.getId() }, e);
    }
    return result;
  }

  /**
   * Teste si un batch est en erreur
   * @param pKey pKey
   * @param pScenario pScenario
   * @param pContextType pContextType
   * @return result
   * @throws BatchStatusEngineServiceException exception
   */
  public ReportBooleanResponseType isBatchOnError(BatchRunKeyType pKey, BatchStatusScenarioType pScenario, ContextType pContextType) throws BatchStatusEngineServiceException
  {
    validateBatchRunKeyType(pKey, "isBatchOnError");
    ReportBooleanResponseType response = null;
    try
    {
      getTransaction().init(pContextType);
      BatchBooleanResponseType bbrt = _batchRunManager.batchRunIsOnError(pKey, new BatchRunScenarioType(), pContextType);
      response = new ReportBooleanResponseType();
      response.setValue(bbrt.getValue());
    }
    catch (WrapperServiceException e)
    {
      // une erreur s'est produite pendant le test d'erreur du batch de nom {0} d'id {1}
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_BATCH_TEST_ERROR, new Object[] { pKey.getPath(), pKey.getId() }, e);
    }
    finally
    {
      release(pContextType);
    }
    return response;
  }

  /**
   * Teste si un batch est complétement terminé
   * @param pKey pKey
   * @param pScenario pScenario
   * @param pContextType pContextType
   * @return result
   * @throws BatchStatusEngineServiceException exception
   */
  public ReportBooleanResponseType isBatchIsDone(BatchRunKeyType pKey, BatchStatusScenarioType pScenario, ContextType pContextType) throws BatchStatusEngineServiceException
  {
    try
    {
      getTransaction().init(pContextType);
      validateBatchRunKeyType(pKey, "isBatchIsDone");
      ReportBooleanResponseType response = null;
      BatchRunType batchRun = getBatchRun(pKey, pContextType, "isBatchIsDone");
      response = new ReportBooleanResponseType();
      boolean batchState = batchRun.getStatus().booleanValue();
      response.setValue(batchState);
      return response;
    }
    finally
    {
      release(pContextType);
    }
  }

  /** {@inheritDoc} */
  public BatchRunType getLastBatchInProgress(BatchRunKeyType pKey, BatchStatusScenarioType pScenario, ContextType pContext) throws BatchStatusEngineServiceException, WrapperServiceException
  {
    BatchRunType result = null;
    try
    {
      getTransaction().init(pContext);
      result = _batchRunManager.getLastBatchInProgress(pKey, pScenario, pContext);
    }
    finally
    {
      release(pContext);
    }
    return result;
  }

  /**
   * Vérifie que la clé de batch est correcte
   * @param pKey
   * @param pMethodName
   * @throws BatchStatusEngineServiceException exception
   */
  private void validateBatchRunKeyType(BatchRunKeyType pKey, String pMethodName) throws BatchStatusEngineServiceException
  {
    if (pKey == null)
    {
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_SERVICE_KEY_NULL, new Object[] { pMethodName });
    }
    if (pKey.getId() == null)
    {
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_SERVICE_KEY_ID_NULL, new Object[] { pMethodName });
    }
    if (pKey.getPath() == null)
    {
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_SERVICE_KEY_PATH_NULL, new Object[] { pMethodName });
    }
    if (pKey.getPath().equals(""))
    {
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_SERVICE_KEY_PATH_EMPTY, new Object[] { pMethodName });
    }
    if (pKey.getSite() == null || pKey.getSite().getCode() == null)
    {
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_SERVICE_KEY_SITE_NULL, new Object[] { pMethodName });
    }
  }

  /**
   * Récupère un batch à partir de sa clé
   * @param pKey
   * @param pContext
   * @param pMethodName
   * @return résultat
   * @throws RemoteException exception
   * @throws BatchStatusEngineServiceException exception
   */
  private BatchRunType getBatchRun(BatchRunKeyType pKey, ContextType pContext, String pMethodName) throws BatchStatusEngineServiceException
  {
    BatchRunType batchRun = null;
    try
    {
      batchRun = _batchRunManager.getBatchRun(pKey, new BatchRunScenarioType(), pContext);
    }
    catch (WrapperServiceException e)
    {
      // Une erreur s'est produite pendant la récupération du batchRun de nom {0} d'id {1}
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_GETBATCHRUN_ERROR, new Object[] { pKey.getPath(), pKey.getId() }, e);
    }
    if (batchRun == null)
    {
      throw new BatchStatusEngineServiceException(BatchErrorDetail.BATCH_STATUS_SERVICE_BATCHRUNKEY_UNKNOWN, new Object[] { pKey.getId(), pKey.getPath(), pKey.getSite().getCode(), pMethodName });
    }
    return batchRun;
  }
}
