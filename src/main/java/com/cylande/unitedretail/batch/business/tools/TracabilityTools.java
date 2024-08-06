package com.cylande.unitedretail.batch.business.tools;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.batch.service.BatchRunManagerServiceImpl;
import com.cylande.unitedretail.batch.service.TaskAuditManagerServiceImpl;
import com.cylande.unitedretail.batch.service.TaskRunManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.batch.service.common.BatchRunManagerService;
import com.cylande.unitedretail.batch.service.common.TaskAuditManagerService;
import com.cylande.unitedretail.batch.service.common.TaskRunManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaType;
import com.cylande.unitedretail.message.batch.TaskAuditKeyType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaIntegerType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.rmi.RemoteException;

public class TracabilityTools
{
  private BatchRunManagerService _batchRunManagerService = null;
  private TaskRunManagerService _taskRunManagerService = null;
  private TaskAuditManagerService _taskAuditManagerService = null;
  private BatchManagerService _batchManager = null;

  public TracabilityTools()
  {
    _batchRunManagerService = new BatchRunManagerServiceImpl();
    _taskRunManagerService = new TaskRunManagerServiceImpl();
    _taskAuditManagerService = new TaskAuditManagerServiceImpl();
    _batchManager = new BatchManagerServiceImpl();
  }

  /**
   * Récupère un BatchRunType à partir d'un ID et d'un Path de Batch
   * Si batchRun non trouvé, alors renvoie null
   * @param pId
   * @param pPath
   * @return BatchRunType
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public BatchRunType getBatchRun(Integer pId, String pPath) throws ServiceException, BatchException
  {
    if (pId == null || pPath == null)
    {
      throw new BatchException(BatchErrorDetail.GETBATCHRUN_PARAM);
    }
    BatchRunType result = null;
    try
    {
      BatchRunKeyType batchKey = new BatchRunKeyType();
      batchKey.setPath(pPath);
      batchKey.setId(pId);
      result = _batchRunManagerService.getBatchRun(batchKey, null, ContextTransformer.fromLocale());
    }
    catch (ServiceException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.GETBATCHRUN_ERR, new Object[] { pId, pPath }, e);
    }
    return result;
  }

  /**
   * Get List of Batch Execution
   * @param pParentId
   * @return BatchRunTypeList
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public BatchRunListType findBatchChildRun(Integer pParentId, ContextType pContext) throws ServiceException, BatchException
  {
    if (pParentId == null)
    {
      throw new BatchException(BatchErrorDetail.FINDBATCHCHILDRUN_PARAM);
    }
    BatchRunListType result = null;
    try
    {
      BatchRunCriteriaListType criteriaList = new BatchRunCriteriaListType();
      BatchRunCriteriaType criteria = new BatchRunCriteriaType();
      CriteriaIntegerType criteriaInteger = new CriteriaIntegerType();
      criteriaInteger.setEquals(pParentId);
      criteria.setParentId(criteriaInteger);
      criteriaList.getList().add(criteria);
      result = _batchRunManagerService.findBatchRun(criteriaList, null, pContext);
    }
    catch (ServiceException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.FINDBATCHCHILDRUN_ERR, new Object[] { pParentId }, e);
    }
    return result;
  }

  /**
   * Get List of Task Execution
   * @param pParentId
   * @return TaskRunList
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public TaskRunListType findTaskChildRun(Integer pParentId) throws BatchException, ServiceException
  {
    if (pParentId == null)
    {
      throw new BatchException(BatchErrorDetail.FINDTASKCHILDRUN_PARAM);
    }
    TaskRunListType result = null;
    try
    {
      TaskRunCriteriaListType criteriaList = new TaskRunCriteriaListType();
      TaskRunCriteriaType criteria = new TaskRunCriteriaType();
      CriteriaIntegerType criteriaInteger = new CriteriaIntegerType();
      criteriaInteger.setEquals(pParentId);
      criteria.setParentId(criteriaInteger);
      criteriaList.getList().add(criteria);
      result = _taskRunManagerService.findTaskRun(criteriaList, null, ContextTransformer.fromLocale());
      return result;
    }
    catch (ServiceException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.FINDBATCHCHILDRUN_ERR, new Object[] { pParentId }, e);
    }
  }

  /**
   * Getter of Task Audit
   * @param pId
   * @param pPath
   * @param pTaskId
   * @return TaskAuditType
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public TaskAuditType getTaskAudit(Integer pId, String pPath, Integer pTaskId) throws BatchException, ServiceException
  {
    if (pId == null || pPath == null || pTaskId == null)
    {
      throw new BatchException(BatchErrorDetail.GETTASKAUDIT_PARAM);
    }
    TaskAuditType taskAudit = null;
    try
    {
      TaskAuditKeyType taskAuditKey = new TaskAuditKeyType();
      taskAuditKey.setId(pId);
      taskAuditKey.setPath(pPath);
      taskAuditKey.setTask(pTaskId);
      taskAudit = _taskAuditManagerService.getTaskAudit(taskAuditKey, null, ContextTransformer.fromLocale());
    }
    catch (ServiceException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.GETTASKAUDIT_ERR, new Object[] { pId, pPath, pTaskId }, e);
    }
    return taskAudit;
  }

  /**
   * Get Task Audit List
   * @param pTaskId
   * @param pTaskPath
   * @return TaskAuditListType
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public TaskAuditListType findTaskAudit(Integer pTaskId, String pTaskPath) throws BatchException, ServiceException
  {
    if (pTaskId == null || pTaskPath == null)
    {
      throw new BatchException(BatchErrorDetail.FINDTASKAUDIT_PARAM);
    }
    TaskAuditListType result = null;
    try
    {
      TaskAuditCriteriaListType criteriaList = new TaskAuditCriteriaListType();
      TaskAuditCriteriaType criteria = new TaskAuditCriteriaType();
      CriteriaStringType criteriaPath = new CriteriaStringType();
      criteriaPath.setEquals(pTaskPath);
      CriteriaIntegerType criteriaTaskId = new CriteriaIntegerType();
      criteriaTaskId.setEquals(pTaskId);
      criteria.setTaskId(criteriaTaskId);
      criteria.setPath(criteriaPath);
      criteriaList.getList().add(criteria);
      result = _taskAuditManagerService.findTaskAudit(criteriaList, null, ContextTransformer.fromLocale());
    }
    catch (ServiceException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.FINDTASKAUDIT_ERR, new Object[] { pTaskId, pTaskPath }, e);
    }
    return result;
  }

  /**
   * Get Batch Defintion
   * @param pBatchName
   * @return BatchType
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public BatchType getBatch(String pBatchName) throws BatchException, RemoteException, ServiceException
  {
    if (pBatchName == null)
    {
      throw new BatchException(BatchErrorDetail.GETBATCH_PARAM);
    }
    BatchType batch = null;
    BatchKeyType key = new BatchKeyType();
    key.setName(pBatchName);
    batch = _batchManager.getBatch(key, null, ContextTransformer.fromLocale());
    return batch;
  }
}
