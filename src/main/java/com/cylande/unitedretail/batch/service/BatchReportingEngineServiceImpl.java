package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.service.common.BatchReportingEngineService;
import com.cylande.unitedretail.batch.transformer.coordinator.TaskRunCoordinator;
import com.cylande.unitedretail.framework.service.AbstractFunctionnalServiceImpl;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.AbstractRunDetailType;
import com.cylande.unitedretail.message.batch.BatchAndTaskRunDetailsListType;
import com.cylande.unitedretail.message.batch.BatchBooleanResponseType;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchCriteriaType;
import com.cylande.unitedretail.message.batch.BatchDefNameListType;
import com.cylande.unitedretail.message.batch.BatchDefNameType;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchReportingScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunDetailsType;
import com.cylande.unitedretail.message.batch.BatchRunHistoType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchScenarioType;
import com.cylande.unitedretail.message.batch.BatchSummaryCriteriaType;
import com.cylande.unitedretail.message.batch.BatchSummaryListType;
import com.cylande.unitedretail.message.batch.BatchSummaryType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.EXTRACTION;
import com.cylande.unitedretail.message.batch.FileProviderTraceListType;
import com.cylande.unitedretail.message.batch.GTBatchCriteriaListType;
import com.cylande.unitedretail.message.batch.GTBatchCriteriaType;
import com.cylande.unitedretail.message.batch.GTBatchGlobalType;
import com.cylande.unitedretail.message.batch.GTBatchModuleType;
import com.cylande.unitedretail.message.batch.GTBatchSummaryType;
import com.cylande.unitedretail.message.batch.GTBatchTestCaseType;
import com.cylande.unitedretail.message.batch.GTBatchType;
import com.cylande.unitedretail.message.batch.GTTaskErrorType;
import com.cylande.unitedretail.message.batch.GTTaskType;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.PathFileProviderType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.batch.TaskChildType;
import com.cylande.unitedretail.message.batch.TaskFileCriteriaType;
import com.cylande.unitedretail.message.batch.TaskKeyType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaType;
import com.cylande.unitedretail.message.batch.TaskRunDetailsType;
import com.cylande.unitedretail.message.batch.TaskRunKeyType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunScenarioType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaBooleanType;
import com.cylande.unitedretail.message.common.criteria.CriteriaIntegerType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.common.criteria.CriteriaTimestampType;
import com.cylande.unitedretail.message.common.criteria.ListParameterType;
import com.cylande.unitedretail.message.globaltest.ExpectedExceptionCodeType;
import com.cylande.unitedretail.message.globaltest.ExpectedExceptionListType;
import com.cylande.unitedretail.message.globaltest.ExpectedExceptionType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.message.network.businessunit.SiteScenarioType;
import com.cylande.unitedretail.message.network.businessunit.SiteType;
import com.cylande.unitedretail.network.service.SiteManagerServiceDelegate;

import java.math.BigInteger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utilis�e par la console de supervision TODO Attention classe � r�viser car trop ancienne
 */
public class BatchReportingEngineServiceImpl extends AbstractFunctionnalServiceImpl implements BatchReportingEngineService
{
  private static final Logger LOGGER = Logger.getLogger(BatchReportingEngineServiceImpl.class);
  private Map<String, String> _taskRefDescMap; // pour stocker les descriptions sur les ref de task exploit�es par GlobalTest
  private BatchManagerServiceImpl _batchManagerService;
  private BatchRunManagerServiceImpl _batchRunManagerService;
  private TaskManagerServiceImpl _taskManagerService;
  private TaskRunManagerServiceImpl _taskRunManagerService;
  private TaskAuditManagerServiceImpl _taskAuditManagerService;
  private SiteManagerServiceDelegate _siteManagerService;
  private StreamProviderEngineServiceImpl _providerEngineService;
  private ContextType _context;
  private String _batchName;
  private Boolean _detailsOfBatchRecursing = true;
  private GTBatchCriteriaType _gtBatchCriteria;

  /**
   * Constructor
   */
  public BatchReportingEngineServiceImpl()
  {
    _batchManagerService = new BatchManagerServiceImpl();
    _batchRunManagerService = new BatchRunManagerServiceImpl();
    _taskManagerService = new TaskManagerServiceImpl();
    _taskRunManagerService = new TaskRunManagerServiceImpl();
    _taskAuditManagerService = new TaskAuditManagerServiceImpl();
    _siteManagerService = new SiteManagerServiceDelegate();
    _providerEngineService = new StreamProviderEngineServiceImpl();
  }

  /** {@inheritDoc} */
  public BatchDefNameListType getAllBatchDefNameList(BatchReportingScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    try
    {
      return _batchManagerService.getBatchDefList(null, null);
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
    return null;
  }

  /** {@inheritDoc} */
  public BatchDefNameListType findBatch(BatchCriteriaListType pCriterias, BatchScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchDefNameListType result = null;
    try
    {
      BatchListType batchList = _batchManagerService.findBatch(pCriterias, pScenario, pContext);
      if (batchList != null)
      {
        result = new BatchDefNameListType();
        BatchDefNameType batchDef;
        for (BatchType batch: batchList.getValues())
        {
          batchDef = new BatchDefNameType();
          batchDef.setBatchName(batch.getName());
          result.getList().add(batchDef);
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchSummaryListType getFilteredBatchSummary(BatchSummaryCriteriaType pCriteria, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    BatchSummaryListType result = null;
    // listes des batchs summary g�n�r�s
    List<BatchSummaryType> summaryList = new ArrayList<BatchSummaryType>();
    // premi�re phase de filtrage sur le nom des batchs name
    BatchCriteriaListType criteria = new BatchCriteriaListType();
    List<BatchCriteriaType> batchCriteriaList = new ArrayList<BatchCriteriaType>();
    BatchCriteriaType batchCriteria = new BatchCriteriaType();
    batchCriteria.setName(pCriteria.getBatchName());
    batchCriteria.setSite(pCriteria.getSite());
    // Ajout du crit�re de date : si pas pr�cis�: une semaine
    if (pCriteria.getBatchStartDate() != null)
    {
      batchCriteria.setCreationTime(pCriteria.getBatchStartDate());
    }
    else
    {
      batchCriteria.setCreationTime(getLastWeekCriteria());
      pCriteria.setBatchStartDate(batchCriteria.getCreationTime());
    }
    batchCriteriaList.add(batchCriteria);
    criteria.setList(batchCriteriaList);
    BatchListType listbatchdef = _batchManagerService.findBatch(criteria, null, pContext);
    if (listbatchdef == null)
    {
      // si aucun batchname correspond au crit�re sur le nom, renvoyer null
      return result;
    }
    // sinon, pour chaque batch name, obtenir le summary, si autres crit�res respect�s
    for (BatchType batch: listbatchdef.getValues())
    {
      // g�n�ration du summary � partir du batchName et des crit�res de filtrages
      BatchSummaryType summary = getBatchSummary(batch, pCriteria, pContext);
      // si un summary a �t� g�n�r� pour le batchName avec les crit�res, l'enregister dans la liste de summary
      if (summary != null)
      {
        // test sur le domaine si il y a un filtre sur le domaine
        if (pCriteria.getDomain() != null && pCriteria.getDomain().length() > 1)
        {
          if (summary.getDomain() == null || !summary.getDomain().equals(pCriteria.getDomain()))
          {
            continue;
          }
        }
        // test sur le site si il y a un filtre sur le site
        if (pCriteria.getSite() != null && pCriteria.getSite().getCode().length() > 1)
        {
          if (summary.getSite() == null || summary.getSite().getCode() == null || !summary.getSite().getCode().equals(pCriteria.getSite().getCode()))
          {
            continue;
          }
          // on ajoute le nom associ� au code du sote
          summary.setSite(getSite(summary.getSite(), pContext));
        }
        summaryList.add(summary);
      }
    }
    // si aucun summary n'a �t� g�n�r�, retourner null
    if (summaryList.isEmpty())
    {
      return null;
    }
    // sinon, encapsuler la liste de summary dans l'objet de retour
    result = new BatchSummaryListType();
    result.setList(summaryList);
    return result;
  }

  /**
   * G�n�re un summary � partir d'un nom de d�finition de batch et de crit�re de g�n�ration
   *
   * @param pBatchDef
   * @param pCriteria
   * @return r�sultat
   * @throws WrapperServiceException exception
   */
  private BatchSummaryType getBatchSummary(BatchType pBatchDef, BatchSummaryCriteriaType pCriteria, ContextType pContext) throws WrapperServiceException
  {
    BatchSummaryType result = new BatchSummaryType();
    // le summary est d�j� constitu� du nom du batch (unique info si cas de batch jamais ex�cut�)
    result.setBatchName(pBatchDef.getName());
    BatchRunCriteriaType batchCriteria = createBatchRunCriteria(pBatchDef, pCriteria);
    // v�rifier si au moins une instance a �t� ex�cut�e pour ce pBatchCrit
    BatchRunListType batchRunList = getBatchRunList(batchCriteria, pContext);
    boolean batchwasrunned = !batchRunList.getValues().isEmpty();
    // marqueur pour "au moins un en cours"
    boolean testrunning = false;
    // marqueur pour "au moins une erreur"
    boolean testerror = false;
    if (!batchwasrunned)
    {
      // si le batch n'a jamais �t� ex�cut�, v�rifier les crit�res savoir si l'on ai dans le cas de filtre "J" unique
      if (pCriteria.getBatchsNeverRunned() != null && pCriteria.getBatchsNeverRunned().getEquals().booleanValue())
      {
        // si c'est le cas on renvoie directement le r�sultat qui ne comporte que le nom du batch
        return result;
      }
      return null;
    }
    BatchRunType lastBatchrun = null;
    for (BatchRunType batchRun: batchRunList.getValues())
    {
      if (lastBatchrun == null)
      {
        lastBatchrun = batchRun;
      }
      testrunning = testrunning || (batchRun.getEndTime() == null);
    }
    testerror = isBatchInError(batchCriteria, pContext);
    // filtrage sur STATE
    if (pCriteria.getBatchsRunning() != null && pCriteria.getBatchsRunning().getEquals().booleanValue() && pCriteria.getBatchsDonned() != null && pCriteria.getBatchsDonned().getEquals().booleanValue())
    {
      // crit�res : batch en cours et termin� -> peu importe l'�tat
      // pass
    }
    else if (pCriteria.getBatchsRunning() != null && pCriteria.getBatchsRunning().getEquals().booleanValue() && testrunning)
    {
      // crit�res : batch en cours uniquement
      // pass
    }
    else if (pCriteria.getBatchsDonned() != null && pCriteria.getBatchsDonned().getEquals().booleanValue() && !testrunning)
    {
      // crit�re : batch termin� uniquement
      // pass
    }
    else
    {
      // aucun crit�res d'�tat respect�s
      return null;
    }
    result.setAtLeastIsRunning(testrunning);
    // filtrage sur ERROR
    if (pCriteria.getBatchsWithError() != null && pCriteria.getBatchsWithError().getEquals().booleanValue() && pCriteria.getBatchsWithoutError() != null && pCriteria.getBatchsWithoutError().getEquals().booleanValue())
    {
      // pass
    }
    else if (pCriteria.getBatchsWithError() != null && pCriteria.getBatchsWithError().getEquals().booleanValue() && testerror)
    {
      // pass
    }
    else if (pCriteria.getBatchsWithoutError() != null && pCriteria.getBatchsWithoutError().getEquals().booleanValue() && !testerror)
    {
      // pass
    }
    else
    {
      // aucun crit�res d'erreur respect�s
      return null;
    }
    result.setAtLeastIsInError(testerror);
    // r�cup�rer le dernier batchrun activ� associ� � la d�finition du pBatchCrit
    if (lastBatchrun != null && lastBatchrun.getStartTime() != null)
    {
      // ... en r�cup�rer la date de derni�re activation
      result.setLastActivationDate(lastBatchrun.getStartTime());
    }
    if (lastBatchrun != null && lastBatchrun.getPath() != null)
    {
      // ... et le chemin d'ex�cution complet
      result.setLastExecutionPath(lastBatchrun.getPath());
    }
    if (lastBatchrun != null && lastBatchrun.getDomain() != null)
    {
      // on r�cup�re le domaine d'ex�cution
      result.setDomain(lastBatchrun.getDomain());
    }
    if (lastBatchrun != null && lastBatchrun.getSite() != null)
    {
      // on r�cup�re le site d'exploitation
      result.setSite(lastBatchrun.getSite());
    }
    return result;
  }

  /**
   * indique pour un type de batch pass� en param�tre s'il existe au moins une instance en tant que batchroot
   *
   * @param pBatchCrit
   * @return r�sultat
   * @throws WrapperServiceException exception
   */
  private BatchRunListType getBatchRunList(BatchRunCriteriaType pBatchCrit, ContextType pContext) throws WrapperServiceException
  {
    BatchRunListType result = null;
    // cr�ation du crit�re de recherche d'instance de batch associ� � batchName
    BatchRunCriteriaListType criterias = new BatchRunCriteriaListType();
    criterias.setParameter(new ListParameterType());
    criterias.getParameter().setSort("START_TIME DESC");
    criterias.setList(new ArrayList<BatchRunCriteriaType>());
    criterias.getList().add(pBatchCrit);
    // recherche des batchrun associ�
    result = _batchRunManagerService.findBatchRun(criterias, null, pContext);
    if (result == null)
    {
      result = new BatchRunListType();
    }
    return result;
  }

  private boolean isBatchInError(BatchRunCriteriaType pBatchCrit, ContextType pContext) throws WrapperServiceException
  {
    boolean result = false;
    BatchBooleanResponseType response = _batchRunManagerService.atLeastBatchInstanceInError(pBatchCrit, null, pContext);
    if (response != null)
    {
      result = response.getValue();
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunHistoType getHistoOfBatchInstanceEx(BatchSummaryCriteriaType pCriteria, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    if (pCriteria == null || pCriteria.getBatchName() == null || pCriteria.getBatchName().getEquals() == null || pCriteria.getBatchName().getEquals().equals(""))
    {
      // TODO exception
      return null;
    }
    CriteriaTimestampType endDateCriteria = pCriteria.getBatchEndDate();
    CriteriaTimestampType startDateCriteria = pCriteria.getBatchStartDate();
    BatchRunHistoType result = new BatchRunHistoType();
    BatchRunCriteriaListType findbatchcriterialist = new BatchRunCriteriaListType();
    BatchRunCriteriaType batchcriteria = new BatchRunCriteriaType();
    batchcriteria.setPath(pCriteria.getBatchName());
    batchcriteria.setStartDate(startDateCriteria);
    batchcriteria.setEndDate(endDateCriteria);
    List<BatchRunCriteriaType> list = new ArrayList<BatchRunCriteriaType>();
    list.add(batchcriteria);
    if (pScenario == null || (pScenario.getBatchRootOnly() != null && !pScenario.getBatchRootOnly().booleanValue()))
    {
      BatchRunCriteriaType batchcriteria2 = new BatchRunCriteriaType();
      CriteriaStringType stringcriteria2 = new CriteriaStringType();
      stringcriteria2.setContains("." + pCriteria.getBatchName().getEquals());
      batchcriteria2.setPath(stringcriteria2);
      batchcriteria2.setStartDate(startDateCriteria);
      batchcriteria2.setEndDate(endDateCriteria);
      list.add(batchcriteria2);
    }
    findbatchcriterialist.setList(list);
    BatchRunListType batchrunlistresult = _batchRunManagerService.findBatchRun(findbatchcriterialist, null, pContext);
    if (batchrunlistresult == null)
    {
      // aucun batchrun trouv� avec les crit�res sp�cifi�s
      return null;
    }
    // sinon, pour chaque batchrun obtenir le details et l'ajouter � la liste de histo
    // r�cup�ration du type de batch dans la d�finition
    String batchtype = this.getBatchTypeName(pCriteria.getBatchName().getEquals(), pContext);
    List<BatchRunDetailsType> detailslist = new ArrayList<BatchRunDetailsType>();
    for (BatchRunType item: batchrunlistresult.getValues())
    {
      BatchRunDetailsType detail = new BatchRunDetailsType();
      detail.setBatchRun(item);
      detail.setBatchType(batchtype);
      detail.setIsInError(item.getInError());
      detailslist.add(detail);
    }
    result.setList(detailslist);
    return result;
  }

  private CriteriaTimestampType getLastWeekCriteria()
  {
    CriteriaTimestampType result = new CriteriaTimestampType();
    Calendar now = Calendar.getInstance();
    Date startDate = now.getTime();
    startDate.setTime(startDate.getTime() - 7 * 24 * 3600 * 1000);
    Calendar lastWeek = Calendar.getInstance();
    lastWeek.setTime(startDate);
    result.setMaxValue(now);
    result.setMinValue(lastWeek);
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunHistoType getHistoOfBatchInstance(BatchDefNameType pBatchName, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    BatchSummaryCriteriaType pCriteria = new BatchSummaryCriteriaType();
    CriteriaStringType nameCriteria = new CriteriaStringType();
    nameCriteria.setEquals(pBatchName.getBatchName());
    pCriteria.setBatchName(nameCriteria);
    CriteriaTimestampType lastWeekCriteria = getLastWeekCriteria();
    pCriteria.setBatchStartDate(lastWeekCriteria);
    return getHistoOfBatchInstanceEx(pCriteria, pScenario, pContext);
  }

  /**
   * Donne le type (sequence ou fork) d'un batch � partir de son path
   *
   * @param pBatchPath
   * @return r�sultat
   */
  private String getBatchTypeName(String pBatchPath, ContextType pContext) throws RemoteException, ServiceException
  {
    if (pBatchPath == null)
    {
      return null;
    }
    String batchname = extractFromPath(pBatchPath);
    BatchKeyType batchkey = new BatchKeyType();
    batchkey.setName(batchname);
    BatchType batchtype = _batchManagerService.getBatch(batchkey, null, pContext);
    if (batchtype != null && batchtype.getSequence() != null)
    {
      return "sequence";
    }
    if (batchtype != null && batchtype.getFork() != null)
    {
      return "fork";
    }
    return "unknown";
  }

  /**
   * Extrait le dernier �l�ment d'un path
   *
   * @param pPath
   * @return r�sultat
   */
  private String extractFromPath(String path)
  {
    if (path == null)
    {
      return null;
    }
    String result = path;
    if (!result.equals(".") && result.contains("."))
    {
      int last = result.lastIndexOf('.');
      result = result.substring(last + 1);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchRunDetailsType getBatchRunDetails(BatchRunKeyType pBatchRunKey, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    // control param
    if (pBatchRunKey == null)
    {
      return null;
    }
    BatchRunDetailsType result = new BatchRunDetailsType();
    // r�cup�rer le batchrun
    BatchRunType batchrun = _batchRunManagerService.getBatchRun(pBatchRunKey, null, pContext);
    if (batchrun == null)
    {
      // aucun run associ� � cette cl�
      return null;
    }
    result.setBatchRun(batchrun);
    // r�cup�rer la def
    BatchKeyType batchkey = new BatchKeyType();
    batchkey.setName(extractFromPath(pBatchRunKey.getPath()));
    BatchType batchdef = _batchManagerService.getBatch(batchkey, null, pContext);
    if (batchdef == null)
    {
      // aucune d�finition pour cette cl�
      return null;
    }
    // r�cup�ration du type de batch
    String batchtypename = "unknow";
    List<BatchChildrenAbstractType> taskOrBatchList = new ArrayList();
    if (batchdef.getSequence() != null)
    {
      batchtypename = "sequence";
      taskOrBatchList = batchdef.getSequence().getTaskOrBatchOrComment();
    }
    else if (batchdef.getFork() != null)
    {
      batchtypename = "fork";
      taskOrBatchList = batchdef.getFork().getTaskOrBatchOrComment();
    }
    _taskRefDescMap = new HashMap();
    String domain;
    for (BatchChildrenAbstractType child: taskOrBatchList)
    {
      if (child instanceof TaskChildType)
      {
        domain = child.getActiveDomain() != null ? child.getActiveDomain() : child.getDefaultDomain();
        domain = domain != null ? domain : "default";
        _taskRefDescMap.put(batchdef.getName() + "." + child.getRef() + "." + domain, child.getDescription());
      }
    }
    result.setDescription(batchdef.getDescription());
    result.setBatchType(batchtypename);

    if (pScenario == null || Boolean.TRUE.equals(pScenario.isManageErrorDetails()))
    {
      if (batchrun.getInError() != null)
      {
        result.setIsInError(batchrun.getInError());
      }
      else
      {
        result.setIsInError(false);
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  public TaskRunDetailsType getTaskRunDetails(TaskRunKeyType pTaskRunKey, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    if (pTaskRunKey == null)
    {
      return null;
    }
    TaskRunDetailsType result = new TaskRunDetailsType();
    TaskRunScenarioType scenario = new TaskRunScenarioType();
    if (pScenario != null && Boolean.TRUE.equals(pScenario.isErrorInDepth()))
    {
      scenario.setManageTaskAudit(true);
    }
    // r�cup�rer le run associ�
    TaskRunType taskrun = _taskRunManagerService.getTaskRun(pTaskRunKey, scenario, pContext);
    if (taskrun == null)
    {
      // aucun run associ� � cette cl�
      return null;
    }
    result.setTaskRun(taskrun);
    // r�cup�rer la def associ�e
    TaskKeyType taskkey = new TaskKeyType();
    taskkey.setName(extractFromPath(pTaskRunKey.getPath() != null ? pTaskRunKey.getPath() : taskrun.getPath()));
    TaskType taskdef = _taskManagerService.getTask(taskkey, null, pContext);
    if (taskdef == null)
    {
      // aucune d�finition pour cette cl�
      return null;
    }
    String tasktypename = taskdef.getClass().getSimpleName();
    result.setTaskType(tasktypename);
    if (taskdef instanceof EXTRACTION)
    {
      result.setInterval(((EXTRACTION)taskdef).getMaxFetchSize());
    }
    else if (taskdef instanceof INTEGRATION)
    {
      result.setInterval(((INTEGRATION)taskdef).getCommitFrequency());
    }
    if (_taskRefDescMap == null)
    {
      _taskRefDescMap = new HashMap();
    }
    String desc = _taskRefDescMap.get(_batchName + "." + taskdef.getName() + "." + taskrun.getDomain());
    result.setDescription(desc != null ? desc : taskdef.getDescription());
    TaskRunCoordinator.setProvider(taskrun, taskdef);

    if (pScenario == null || Boolean.TRUE.equals(pScenario.isManageFileProvider()))
    {
      TaskFileCriteriaType fileCriteria = new TaskFileCriteriaType();
      fileCriteria.setTaskRunKey(pTaskRunKey);
      try
      {
        FileProviderTraceListType fileTraceList = _providerEngineService.getTaskFile(fileCriteria, null, pContext);
        TaskRunCoordinator.setFileTraceList(taskrun, fileTraceList);
      }
      catch (Exception e)
      {
        LOGGER.error("BatchReportingEngineServiceImpl [getTaskRunDetails]", e);
      }
    }

    if (pScenario == null || Boolean.TRUE.equals(pScenario.isManageErrorDetails()))
    {
      // v�rifier si des erreurs pour la task concern�e
      if (taskrun.getInError() != null)
      {
        result.setIsInError(taskrun.getInError());
      }
      else
      {
        result.setIsInError(false);
      }
    }

    if (taskrun.getWorkProgress() != null)
    {
      result.setProgress(new BigInteger(taskrun.getWorkProgress().toString()));
    }
    else
    {
      result.setProgress(BigInteger.ZERO);
    }
    return result;
  }

  /** {@inheritDoc} */
  public BatchAndTaskRunDetailsListType getDetailsOfBatch(BatchRunKeyType pBatchRunKey, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    if (pBatchRunKey == null || pBatchRunKey.getId() == null || pBatchRunKey.getPath() == null)
    {
      return null;
    }
    BatchAndTaskRunDetailsListType result = null;
    BatchRunDetailsType batchdetail = getBatchRunDetails(pBatchRunKey, pScenario, pContext);
    if (batchdetail != null)
    {
      result = new BatchAndTaskRunDetailsListType();
      result.getList().add(batchdetail);
      BatchAndTaskRunDetailsListType sousExecutionList = getDetailsOfBatchContent(pBatchRunKey, pScenario, pContext);
      result.getList().addAll(sousExecutionList.getList());
    }
    return result;
  }

  /**
   * Donne le detail d'ex�cution d'un batch � partir de son batchrunkey
   *
   * @param pBatchRunKey
   * @param pScenario
   * @param pContext
   * @return BatchAndTaskRunDetailsListType
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  private BatchAndTaskRunDetailsListType getDetailsOfBatchContent(BatchRunKeyType pBatchRunKey, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    // r�cup�rer le batchrun associ�
    BatchRunType batchrun = _batchRunManagerService.getBatchRun(pBatchRunKey, null, pContext);
    if (batchrun == null)
    {
      return null;
    }
    BatchAndTaskRunDetailsListType result = new BatchAndTaskRunDetailsListType();
    // r�cup�rer la liste des task fille et obtenir leurs d�tails
    TaskRunListType taskchildren = getTaskChildren(pBatchRunKey.getId(), pContext);
    if (taskchildren != null)
    {
      for (TaskRunType taskrun: taskchildren.getValues())
      {
        // construire la cl� de recherche pour obtenir le d�tail
        TaskRunKeyType taskkey = new TaskRunKeyType();
        taskkey.setId(taskrun.getId());
        taskkey.setPath(taskrun.getPath());
        _batchName = extractFromPath(batchrun.getPath());
        TaskRunDetailsType taskdetail = getTaskRunDetails(taskkey, pScenario, pContext);
        if (taskdetail != null)
        {
          result.getList().add(taskdetail);
        }
      }
    }
    // r�cup�rer la liste des batchs fils et obtenir le d�tails de mani�re r�cursive
    BatchRunListType batchchildren = getBatchChildren(pBatchRunKey.getId(), pContext);
    if (batchchildren != null)
    {
      for (BatchRunType batchrunchild: batchchildren.getValues())
      {
        // construire la cl� de recherche pour obtenir le d�tail
        BatchRunKeyType batchkey = new BatchRunKeyType();
        batchkey.setId(batchrunchild.getId());
        batchkey.setPath(batchrunchild.getPath());
        BatchRunDetailsType batchdetail = getBatchRunDetails(batchkey, pScenario, pContext);
        if (batchdetail != null)
        {
          result.getList().add(batchdetail);
        }
        if (Boolean.TRUE.equals(_detailsOfBatchRecursing))
        {
          BatchAndTaskRunDetailsListType sousbatchdetail = getDetailsOfBatchContent(batchkey, pScenario, pContext);
          if (sousbatchdetail != null)
          {
            result.getList().addAll(sousbatchdetail.getList());
          }
        }
      }
    }
    return result;
  }

  /**
   * Retourne les taches ayant pour ParentId l'Id pass� en param�tre
   *
   * @param pParentId
   * @return TaskRunListType
   * @throws WrapperServiceException exception
   */
  private TaskRunListType getTaskChildren(Integer pParentId, ContextType pContext) throws WrapperServiceException
  {
    TaskRunCriteriaListType criterias = new TaskRunCriteriaListType();
    criterias.setList(new ArrayList<TaskRunCriteriaType>());
    TaskRunCriteriaType criteria = new TaskRunCriteriaType();
    CriteriaIntegerType critint = new CriteriaIntegerType();
    critint.setEquals(pParentId);
    criteria.setParentId(critint);
    criterias.getList().add(criteria);
    return _taskRunManagerService.findTaskRun(criterias, null, pContext);
  }

  /**
   * Retourne les batchs ayant pour ParentId l'Id pass� en param�tre
   *
   * @param pParentId
   * @return BatchRunListType
   * @throws WrapperServiceException exception
   */
  private BatchRunListType getBatchChildren(Integer pParentId, ContextType pContext) throws WrapperServiceException
  {
    BatchRunCriteriaListType criterias = new BatchRunCriteriaListType();
    criterias.setList(new ArrayList<BatchRunCriteriaType>());
    BatchRunCriteriaType criteria = new BatchRunCriteriaType();
    CriteriaIntegerType critint = new CriteriaIntegerType();
    critint.setEquals(pParentId);
    criteria.setParentId(critint);
    criterias.getList().add(criteria);
    return _batchRunManagerService.findBatchRun(criterias, null, pContext);
  }

  /** {@inheritDoc} */
  public TaskAuditListType getTaskErrorOfBatch(BatchRunKeyType pBatchRunKey, BatchReportingScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchRunScenarioType scenario = new BatchRunScenarioType();
    if (pScenario != null && pScenario.getErrorInDepth().booleanValue())
    {
      scenario.setBatchErrorInDepth(true);
    }
    return _batchRunManagerService.getTaskAuditOfBatchRun(pBatchRunKey, scenario, pContext);
  }

  /** {@inheritDoc} */
  public TaskAuditListType getTaskErrorOfTask(TaskRunKeyType pTaskRunKey, BatchReportingScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTaskRunKey == null)
    {
      return null;
    }
    // pr�paration du crit�re
    Integer taskid = pTaskRunKey.getId();
    String taskcode = pTaskRunKey.getPath();
    TaskAuditCriteriaListType criterias = new TaskAuditCriteriaListType();
    criterias.setList(new ArrayList<TaskAuditCriteriaType>());
    TaskAuditCriteriaType criteria = new TaskAuditCriteriaType();
    CriteriaIntegerType critid = new CriteriaIntegerType();
    critid.setEquals(taskid);
    criteria.setTaskId(critid);
    CriteriaStringType critcode = new CriteriaStringType();
    critcode.setEquals(taskcode);
    criteria.setPath(critcode);
    criterias.getList().add(criteria);
    return _taskAuditManagerService.findTaskAudit(criterias, null, pContext);
  }

  private CriteriaStringType buildCriteriaString(String pValue)
  {
    CriteriaStringType result = new CriteriaStringType();
    result.setEquals(pValue);
    return result;
  }

  private BatchRunCriteriaType createBatchRunCriteria(BatchType pBatchType, BatchSummaryCriteriaType pBatchSummaryCriteria)
  {
    BatchRunCriteriaType batchCriteria = new BatchRunCriteriaType();
    batchCriteria.setPath(buildCriteriaString(pBatchType.getName()));
    batchCriteria.setStartDate(pBatchSummaryCriteria.getBatchStartDate());
    batchCriteria.setEndDate(pBatchSummaryCriteria.getBatchEndDate());
    batchCriteria.setSite(pBatchSummaryCriteria.getSite());
    if (pBatchSummaryCriteria.getDomain() != null)
    {
      batchCriteria.setDomain(buildCriteriaString(pBatchSummaryCriteria.getDomain()));
    }
    return batchCriteria;
  }

  /**
   * getSite
   *
   * @param pSite pSite
   * @param pContext pContext
   * @return result
   */
  private SiteKeyType getSite(SiteKeyType pSite, ContextType pContext)
  {
    if (pSite == null)
    {
      return null;
    }
    SiteType mySite = null;
    try
    {
      mySite = _siteManagerService.getSite(pSite, new SiteScenarioType(), pContext);
    }
    catch (ServiceException e)
    {
      LOGGER.debug(e, e);
    }
    if (mySite != null)
    {
      return mySite;
    }
    return pSite;
  }

  /** {@inheritDoc} */
  public GTBatchGlobalType findGTBatchReport(GTBatchCriteriaListType pCriteriaList, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    GTBatchGlobalType result = new GTBatchGlobalType();
    result.setBatchRunDetails(new BatchRunDetailsType());
    result.getBatchRunDetails().setDescription("GlobalTest");
    result.getBatchRunDetails().setBatchRun(new BatchRunType());
    result.getBatchRunDetails().getBatchRun().setStartTime(Calendar.getInstance());
    result.getBatchRunDetails().getBatchRun().setEndTime(Calendar.getInstance());
    result.setSummary(new GTBatchSummaryType());
    result.getSummary().setCountTest(0);
    result.getSummary().setCountTestOK(0);
    result.getSummary().setCountTestKO(0);
    result.getSummary().setDelay(0D);
    GTBatchGlobalType gtType;
    for (GTBatchCriteriaType crit: pCriteriaList.getList())
    {
      crit.setExpectedErrorList(pCriteriaList.getExpectedErrorList());
      gtType = getGTBatchReport(crit, pScenario, pContext);
      if (gtType != null)
      {
        result.getModules().addAll(gtType.getModules());
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  public GTBatchGlobalType getGTBatchReport(GTBatchCriteriaType pCriteria, BatchReportingScenarioType pScenario, ContextType pContext) throws RemoteException, ServiceException
  {
    _context = pContext;
    _detailsOfBatchRecursing = false; // optimisation de la mont�e m�moire : la liste des batchs enfants est charg�e au fur et � mesure avec
                                      // getDetailsOfBatch
    _gtBatchCriteria = pCriteria;
    GTBatchGlobalType result = null;
    BatchAndTaskRunDetailsListType batchDetails = getDetailsOfBatch(pCriteria.getBatchKey(), null, pContext);
    if (batchDetails != null && !batchDetails.getList().isEmpty())
    {
      result = new GTBatchGlobalType();
      result.setSummary(new GTBatchSummaryType());
      result.getSummary().setCountTest(0);
      result.getSummary().setCountTestOK(0);
      result.getSummary().setCountTestKO(0);
      result.setBatchRunDetails((BatchRunDetailsType)batchDetails.getList().get(0));
      result.getSummary().setDelay(getDelay(result.getBatchRunDetails().getBatchRun()));
      batchDetails.getList().remove(0);
      computeGTBatchModule(result, batchDetails);
    }
    return result;
  }

  private void computeGTBatchModule(GTBatchGlobalType pGTBatchGlobal, BatchAndTaskRunDetailsListType pBatchDetails) throws RemoteException, ServiceException
  {
    GTBatchModuleType batchModule;
    for (AbstractRunDetailType runDetail: pBatchDetails.getList())
    {
      if (runDetail instanceof BatchRunDetailsType)
      {
        batchModule = new GTBatchModuleType();
        batchModule.setSummary(new GTBatchSummaryType());
        batchModule.getSummary().setCountTest(0);
        batchModule.getSummary().setCountTestOK(0);
        batchModule.getSummary().setCountTestKO(0);
        batchModule.setBatchRunDetails((BatchRunDetailsType)runDetail);
        batchModule.getSummary().setDelay(getDelay(batchModule.getBatchRunDetails().getBatchRun()));
        computeGTBatchTestsCase(batchModule, getDetailsOfBatch(((BatchRunDetailsType)runDetail).getBatchRun(), null, _context));
        pGTBatchGlobal.getModules().add(batchModule);
        pGTBatchGlobal.getSummary().setCountTest(pGTBatchGlobal.getSummary().getCountTest() + batchModule.getSummary().getCountTest());
        pGTBatchGlobal.getSummary().setCountTestKO(pGTBatchGlobal.getSummary().getCountTestKO() + batchModule.getSummary().getCountTestKO());
        pGTBatchGlobal.getSummary().setCountTestOK(pGTBatchGlobal.getSummary().getCountTestOK() + batchModule.getSummary().getCountTestOK());
      }
    }
  }

  private void computeGTBatchTestsCase(GTBatchModuleType pBatchModule, BatchAndTaskRunDetailsListType pBatchDetails) throws RemoteException, ServiceException
  {
    if (pBatchDetails != null && !pBatchDetails.getList().isEmpty())
    {
      pBatchDetails.getList().remove(0);
      GTBatchTestCaseType batchTestCase;
      for (AbstractRunDetailType runDetail: pBatchDetails.getList())
      {
        if (runDetail instanceof BatchRunDetailsType)
        {
          batchTestCase = new GTBatchTestCaseType();
          batchTestCase.setBatchRunDetails((BatchRunDetailsType)runDetail);
          batchTestCase.setDelay(getDelay(batchTestCase.getBatchRunDetails().getBatchRun()));
          batchTestCase.setName(getName(batchTestCase.getBatchRunDetails().getBatchRun().getPath()));
          computeGTBatchTestCaseDetail(batchTestCase, getDetailsOfBatch(((BatchRunDetailsType)runDetail).getBatchRun(), null, _context));
          if (batchTestCase.getBatchCheck() != null && batchTestCase.getXmlUnits().isEmpty())
          {
            batchTestCase.getBatchCheck().getBatchRunDetails().setIsInError(Boolean.TRUE);
            batchTestCase.getBatchRunDetails().setIsInError(Boolean.TRUE);
            GTTaskErrorType error = new GTTaskErrorType();
            error.setTaskAudit(new TaskAuditType());
            error.getTaskAudit().setErrorMessage("Aucune extraction effectu�e sur la phase de Check");
            batchTestCase.getErrors().add(error);
          }
          pBatchModule.getTestsCase().add(batchTestCase);
          pBatchModule.getSummary().setCountTest(pBatchModule.getSummary().getCountTest() + 1);
          if (Boolean.TRUE.equals(batchTestCase.getBatchRunDetails().getIsInError()))
          {
            pBatchModule.getSummary().setCountTestKO(pBatchModule.getSummary().getCountTestKO() + 1);
          }
          else
          {
            pBatchModule.getSummary().setCountTestOK(pBatchModule.getSummary().getCountTestOK() + 1);
          }
        }
      }
    }
  }

  private String getName(String path)
  {
    return path.substring(path.lastIndexOf('.') + 1, path.length());
  }

  private Double getDelay(BatchRunType pBatchRun)
  {
    if (pBatchRun.getEndTime() != null)
    {
      double result = (pBatchRun.getEndTime().getTimeInMillis() - pBatchRun.getStartTime().getTimeInMillis()) / 1000.0;
      return Double.valueOf(result);
    }
    return null;
  }

  private void computeGTBatchTestCaseDetail(GTBatchTestCaseType pBatchTestCase, BatchAndTaskRunDetailsListType pBatchDetails) throws RemoteException, ServiceException
  {
    if (pBatchDetails != null && !pBatchDetails.getList().isEmpty())
    {
      pBatchDetails.getList().remove(0);
      GTBatchType batch;
      pBatchTestCase.getBatchRunDetails().setIsInError(Boolean.FALSE);
      for (AbstractRunDetailType runDetail: pBatchDetails.getList())
      {
        if (runDetail instanceof BatchRunDetailsType)
        {
          batch = new GTBatchType();
          batch.setBatchRunDetails((BatchRunDetailsType)runDetail);
          batch.getBatchRunDetails().setIsInError(Boolean.FALSE);
          if (batch.getBatchRunDetails().getBatchRun().getPath().endsWith("Init"))
          {
            pBatchTestCase.setBatchInit(batch);
          }
          else if (batch.getBatchRunDetails().getBatchRun().getPath().endsWith("Treatment"))
          {
            pBatchTestCase.setBatchTreatment(batch);
          }
          else if (batch.getBatchRunDetails().getBatchRun().getPath().endsWith("Check"))
          {
            pBatchTestCase.setBatchCheck(batch);
          }
          else if (batch.getBatchRunDetails().getBatchRun().getPath().endsWith("Clean"))
          {
            pBatchTestCase.setBatchPurge(batch);
          }
          computeGTTaskTestCaseDetail(pBatchTestCase, batch, getDetailsOfBatch(((BatchRunDetailsType)runDetail).getBatchRun(), null, _context));
        }
      }
    }
  }

  private void computeGTTaskTestCaseDetail(GTBatchTestCaseType pBatchTestCase, GTBatchType pBatch, BatchAndTaskRunDetailsListType pTaskDetails) throws RemoteException, ServiceException
  {
    if (pTaskDetails != null && !pTaskDetails.getList().isEmpty())
    {
      pTaskDetails.getList().remove(0);
      TaskRunDetailsType taskDetails;
      GTTaskType task;
      String path, xmlUnit;
      for (AbstractRunDetailType runDetail: pTaskDetails.getList())
      {
        if (runDetail instanceof TaskRunDetailsType)
        {
          taskDetails = (TaskRunDetailsType)runDetail;
          task = new GTTaskType();
          task.setTask(taskDetails);
          task.setName(getName(taskDetails.getTaskRun().getPath()));
          pBatch.getTasks().add(task);
          path = pBatch.getBatchRunDetails().getBatchRun().getPath();
          if (path.endsWith("Check") || path.endsWith("Clean"))
          {
            // ajout des noms des fichiers t�moins d�duits des providers d'extraction de la t�che
            if (taskDetails.getTaskRun().getResponseProvider() != null)
            {
              for (PathFileProviderType provider: taskDetails.getTaskRun().getResponseProvider().getList())
              {
                xmlUnit = getXmlUnit(provider.getFileName(), taskDetails.getTaskRun().getDomain());
                if (xmlUnit != null)
                {
                  pBatchTestCase.getXmlUnits().add(xmlUnit);
                }
              }
            }
          }
          // /!\ ne mettre � jour batch.inError que si true est d�tect�
          // sinon, on perd la mise � true si la prochaine task n'est pas en erreur
          pBatchTestCase.getErrors().addAll(getGTTaskErrorList(taskDetails, _gtBatchCriteria, pBatch));
          if (Boolean.TRUE.equals(pBatch.getBatchRunDetails().isIsInError()))
          {
            pBatchTestCase.getBatchRunDetails().setIsInError(Boolean.TRUE);
          }
        }
      }
    }
  }

  private List<GTTaskErrorType> getGTTaskErrorList(TaskRunDetailsType pTaskDetails, GTBatchCriteriaType pCriteria, GTBatchType pBatch) throws WrapperServiceException
  {
    List<GTTaskErrorType> taskErrorList = new ArrayList();
    TaskRunKeyType taskRunKey = new TaskRunKeyType();
    taskRunKey.setId(pTaskDetails.getTaskRun().getId());
    taskRunKey.setPath(pTaskDetails.getTaskRun().getPath());
    taskRunKey.setSite(pTaskDetails.getTaskRun().getSite());
    TaskRunCriteriaListType criterias = new TaskRunCriteriaListType();
    TaskAuditListType taskAuditList = _taskRunManagerService.getTaskAuditOfTaskRun(taskRunKey, null, _context);
    if (taskAuditList != null)
    {
      for (TaskAuditType audit: taskAuditList.getValues())
      {
        GTTaskErrorType error = new GTTaskErrorType();
        error.setTaskAudit(audit);
        error.setDomain(pTaskDetails.getTaskRun().getDomain());
        taskErrorList.add(error);
      }
    }
    List<ExpectedExceptionCodeType> expectedErrorCodeList = getTaskExpectedErrorCodeList(pTaskDetails.getTaskRun(), pCriteria.getExpectedErrorList());
    if (!expectedErrorCodeList.isEmpty())
    {
      // teste si toutes les erreurs lev�es sont des erreurs attendues et les flags en cons�quences
      boolean batchInError = false;
      for (GTTaskErrorType error: taskErrorList)
      {
        boolean find = false;
        for (ExpectedExceptionCodeType expectedError: expectedErrorCodeList)
        {
          if (error.getTaskAudit().getErrorCode().equals(expectedError.getErrorCode()))
          {
            error.setExpectedError(Boolean.TRUE);
            find = true;
            break;
          }
        }
        if (!find)
        {
          batchInError = true; // si au moins une erreur n'est pas attendue, le batch est mis en erreur
        }
      }
      if (batchInError)
      {
        // on ne met � jour batch.inError que si batchInError = true car batch.inError = false par d�faut
        // sinon, on perd la mise � true si la prochaine task n'est pas en erreur
        pBatch.getBatchRunDetails().setIsInError(Boolean.TRUE);
      }
      // teste si toutes les erreurs attendues ont �t� lev�es
      for (ExpectedExceptionCodeType expectedError: expectedErrorCodeList)
      {
        boolean find = false;
        for (GTTaskErrorType error: taskErrorList)
        {
          if (error.getTaskAudit().getErrorCode().equals(expectedError.getErrorCode()))
          {
            find = true;
            break;
          }
        }
        if (!find)
        {
          TaskRunListType taskList = null;
          if (taskErrorList.isEmpty())
          {
            // s'il n'y a pas d'erreur d'ex�cution et avant de mettre la tache en erreur attendue, on v�rifie en dernier lieu
            // si l'erreur attendue n'est pas potentiellement d�j� pr�sente sur une autre tache avec le m�me path et le m�me domaine d'ex�cution
            TaskRunCriteriaType crit = new TaskRunCriteriaType();
            crit.setPath(new CriteriaStringType());
            crit.getPath().setEquals(taskRunKey.getPath());
            crit.setDomain(new CriteriaStringType());
            crit.getDomain().setEquals(pTaskDetails.getTaskRun().getDomain());
            crit.setInError(new CriteriaBooleanType());
            crit.getInError().setEquals(true);
            criterias.getList().add(crit);
            taskList = _taskRunManagerService.findTaskRun(criterias, null, _context);
          }
          if (taskList == null)
          {
            // ajout d'une erreur pour indiquer que l'erreur attendue n'a pas �t� lev�e
            GTTaskErrorType error = new GTTaskErrorType();
            TaskAuditType audit = new TaskAuditType();
            audit.setPath(taskRunKey.getPath());
            audit.setErrorCode(expectedError.getErrorCode());
            audit.setErrorMessage("Cette erreur �tait attendue mais n'a pas �t� lev�e.");
            error.setTaskAudit(audit);
            error.setDomain(pTaskDetails.getTaskRun().getDomain());
            taskErrorList.add(error);
            pBatch.getBatchRunDetails().setIsInError(Boolean.TRUE);
            pTaskDetails.setIsInError(Boolean.TRUE);
          }
        }
      }
    }
    else if (!taskErrorList.isEmpty())
    {
      pBatch.getBatchRunDetails().setIsInError(Boolean.TRUE);
    }
    return taskErrorList;
  }

  private List<ExpectedExceptionCodeType> getTaskExpectedErrorCodeList(TaskRunType pTask, ExpectedExceptionListType pExpectedErrorList)
  {
    if (pExpectedErrorList != null)
    {
      for (ExpectedExceptionType expectedError: pExpectedErrorList.getValues())
      {
        if (expectedError.getTaskName().equals(pTask.getPath()) && expectedError.getDomain().equals(pTask.getDomain()))
        {
          return expectedError.getExceptions();
        }
      }
    }
    return new ArrayList();
  }

  private String getXmlUnit(String pFileName, String pDomain)
  {
    String result = null;
    String[] names = pFileName.split("[//\\\\]Out[//\\\\]");
    if (names.length > 1)
    {
      result = names[1].replaceAll("^[0-9]*_", "");
      result = result.replaceFirst("IXRetailTrace", "IXRetail");
      String[] dirs = names[0].split("[//\\\\]");
      String domain = dirs.length > 1 ? domain = dirs[dirs.length - 1] : pDomain;
      return "U_" + domain + "_" + result;
    }
    return result;
  }
}
