package com.cylande.unitedretail.batch.service;

import java.io.File;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.batch.ServletLauncher;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.scheduler.BatchEngine;
import com.cylande.unitedretail.batch.scheduler.SynchronousBatch;
import com.cylande.unitedretail.batch.service.common.BatchEngineService;
import com.cylande.unitedretail.batch.service.exception.BatchEngineServiceException;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceNotDeliveredException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.framework.tools.filemanagement.DirectoryFileManager;
import com.cylande.unitedretail.message.batch.BatchReportingScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.PathFileProviderType;
import com.cylande.unitedretail.message.batch.TaskKeyType;
import com.cylande.unitedretail.message.batch.TaskRunDetailsType;
import com.cylande.unitedretail.message.batch.TaskRunKeyType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.batchenginemessages.BatchEngineParamsType;
import com.cylande.unitedretail.message.batchenginemessages.BatchEngineResponseType;
import com.cylande.unitedretail.message.batchenginemessages.BatchEngineScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.engineproperties.PropertyListType;
import com.cylande.unitedretail.message.enginevariables.VariableListType;
import com.cylande.unitedretail.message.enginevariables.VariableType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.message.network.businessunit.SiteScenarioType;
import com.cylande.unitedretail.message.network.businessunit.SiteType;
import com.cylande.unitedretail.message.process.ProcessorAbstractBlockContentType;
import com.cylande.unitedretail.message.process.ProcessorKeyType;
import com.cylande.unitedretail.message.process.ProcessorServiceType;
import com.cylande.unitedretail.message.process.ProcessorType;
import com.cylande.unitedretail.network.service.SiteManagerServiceDelegate;
import com.cylande.unitedretail.process.exception.ServiceProcessException;
import com.cylande.unitedretail.process.implementation.ServiceImpl;
import com.cylande.unitedretail.process.service.ProcessorManagerServiceImpl;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.VariablesManager;
import com.cylande.unitedretail.process.tools.VariablesRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 * Service frontal du moteur de batch
 */
public class BatchEngineServiceImpl implements BatchEngineService
{
  private static final BatchReportingScenarioType TASKRUN_DETAILS_SCENARIO;
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(BatchEngineServiceImpl.class);
  /** instance du moteur */
  private BatchEngine _engine = null;
  /** le site local */
  private SiteType _localSite = null;
  static
  {
    TASKRUN_DETAILS_SCENARIO = new BatchReportingScenarioType();
    TASKRUN_DETAILS_SCENARIO.setManageFileProvider(true);
  }

  /**
   * constructor
   * @throws BatchEngineServiceException exception
   */
  public BatchEngineServiceImpl() throws BatchEngineServiceException
  {
    try
    {
      _engine = new BatchEngine();
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_CONSTRUCTOR, e);
    }
  }

  public BatchEngineResponseType executeReject(BatchEngineParamsType params, BatchEngineScenarioType pScenario, ContextType pContext) throws BatchEngineServiceException
  {
    BatchEngineResponseType result = null;
    if (params != null && params.getTaskId() != null)
    {
      try
      {
        BatchReportingEngineServiceImpl serv = new BatchReportingEngineServiceImpl();
        TaskRunKeyType taskKey = new TaskRunKeyType();
        taskKey.setId(params.getTaskId());
        TaskRunDetailsType taskDetail = serv.getTaskRunDetails(taskKey , TASKRUN_DETAILS_SCENARIO, pContext);
        if (taskDetail != null && taskDetail.getTaskRun().getRejectProvider() != null)
        {
          String[] split = taskDetail.getTaskRun().getPath().split("\\.");
          String taskName = split[split.length - 1];
          if (params.getBatchName() == null)
          {
            params.setBatchName(split[split.length - 2]);
          }
          if (Boolean.FALSE.equals(pScenario.isMoveFileForReject()) && params.getVariables() != null)
          {
            Class inputClass = null;
            VariableType inputVar = null;
            for (VariableType var: params.getVariables().getVariable())
            {
              if ("input".equals(var.getName()))
              {
                inputVar = var;
                inputClass = var.getValue() == null ? getClassValue(taskName) : var.getValue().getClass();
                break;
              }
            }
            if (inputClass != null)
            {
              JAXBManager jaxbManager = new JAXBManager();
              for (PathFileProviderType rejectPath: taskDetail.getTaskRun().getRejectProvider().getList())
              {
                File rejectFile = new File(rejectPath.getFileName());
                if (rejectFile.exists())
                {
                  Object obj = jaxbManager.read(rejectPath.getFileName(), inputClass.newInstance());
                  inputVar.setValue(obj);
                  result = execute(params, null, pContext);
                  rejectFile.delete();
                }
              }
            }
          }
          else
          {
            File inputFile = new File(taskDetail.getTaskRun().getInputProvider().getList().get(0).getFileName());
            DirectoryFileManager dirManager = new DirectoryFileManager(inputFile.getParent());
            for (PathFileProviderType rejectPath: taskDetail.getTaskRun().getRejectProvider().getList())
            {
              File rejectFile = new File(rejectPath.getFileName());
              dirManager.moveFile(rejectFile.getPath(), inputFile.getParent(), rejectFile.getName(), true);
            }
            result = execute(params, null, pContext);
          }
        }
      }
      catch (Exception e)
      {
        throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_ERROR, new Object[] { params.getBatchName() }, e);
      }
    }
    return result;
  }

  private Class<? extends Object> getClassValue(String pTaskName) throws WrapperServiceException, ServiceProcessException
  {
    // CHECKSTYLE:OFF
    Class result = null;
    TaskKeyType taskKey = new TaskKeyType();
    taskKey.setName(pTaskName);
    TaskType task = new TaskManagerServiceImpl().getTask(taskKey, null, null);
    ProcessorKeyType procKey = new ProcessorKeyType();
    procKey.setName(task.getProcessor().getRef());
    ProcessorType processor = new ProcessorManagerServiceImpl().getProcessor(procKey , null, null);
    for (ProcessorAbstractBlockContentType blockContent: processor.getBlock().getCopyOrXsltOrService())
    {
      if (blockContent instanceof ProcessorServiceType)
      {
        ServiceImpl serv = new ServiceImpl((ProcessorServiceType)blockContent, new VariablesManager(), null);
        result = serv.getParamsMethod()[0];
        break;
      }
    }
    // CHECKSTYLE:ON
    return result;
  }

  /** {@inheritDoc} */
  public BatchEngineResponseType executeFromMonitoring(BatchEngineParamsType pParams, BatchEngineScenarioType pScenario, ContextType pContext) throws BatchEngineServiceException
  {
    try
    {
      VariableListType varlist = pParams.getVariables();
      int index = varlist.getVariable().get(0).getContent().indexOf("startDate");
      if (index != -1)
      {
        String extractedDate = varlist.getVariable().get(0).getContent().substring(varlist.getVariable().get(0).getContent().indexOf("startDate") + 10, varlist.getVariable().get(0).getContent().lastIndexOf("startDate") - 2);
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = utcFormat.parse(extractedDate);
        DateFormat pstFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        pstFormat.setTimeZone(Calendar.getInstance().getTimeZone());
        varlist.getVariable().get(0).setContent(varlist.getVariable().get(0).getContent().replace(extractedDate, pstFormat.format(date)));
      }
      return execute(pParams, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_ERROR, new Object[] { pParams.getBatchName() }, e);
    }
  }

  /** {@inheritDoc} */
  public BatchEngineResponseType execute(BatchEngineParamsType pParams, BatchEngineScenarioType pScenario, ContextType pContext) throws BatchEngineServiceException
  {
    if (pParams == null)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_NOPARAMS);
    }
    if (pParams.getBatchName() == null || pParams.getBatchName().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_NOBATCHNAME);
    }
    //BatchEngine engine;
    try
    {
      _engine = new BatchEngine();
      String batchName = pParams.getBatchName();
      _engine.setBatchNameToExe(batchName);
      if (pParams.getVariables() != null)
      {
        VariableListType varlist = pParams.getVariables();
        VariablesRepository varRepo = new VariablesRepository();
        varRepo.putVariableList(varlist);
        _engine.setVarEngRepo(varRepo);
      }
      if (pParams.getProperties() != null)
      {
        PropertyListType proplist = pParams.getProperties();
        PropertiesRepository propRepo = new PropertiesRepository();
        propRepo.putPropertyList(proplist);
        _engine.setPropEngRepo(propRepo);
      }
      //pParams.getProperties();
      String propertyContext = "default";
      if (pParams.getPropertyContext() != null && !"".equals(pParams.getPropertyContext()))
      {
        propertyContext = pParams.getPropertyContext();
      }
      _engine.setActiveDomain(propertyContext);
      String alternativeDomain = null;
      if (pParams.getDefaultDomain() != null && !"".equals(pParams.getDefaultDomain()))
      {
        alternativeDomain = pParams.getDefaultDomain();
      }
      _engine.setAlternativeDomain(alternativeDomain);
      String batchid;
      //Si le site est présent, on fait appel a la servlet du site demandé
      if (pParams.getSite() != null && !pParams.getSite().getCode().equals("") && !pParams.getSite().getCode().equals(getLocalSite().getCode()))
      {
        /* modif site */
        NameValuePair[] data = new NameValuePair[2];
        SiteType site = getSiteInfo(pParams.getSite());
        data[0] = new NameValuePair("j_username", site.getLogin());
        data[1] = new NameValuePair("j_password", site.getPass());
        ServletLauncher servletLauncher = new ServletLauncher();
        String responseBody = servletLauncher.launchBatch(site.getUrl(), ServletLauncher.getRessource(pParams.getBatchName(), propertyContext, "execute"), data);
        if (responseBody.contains("<id>"))
        {
          batchid = String.valueOf(responseBody.substring(responseBody.indexOf("<id>") + 4, responseBody.indexOf("</id>")));
        }
        else
        { // si la servlet renvoi une erreur, on lance une exception.
          throw new Exception();
        }
      }
      else
      { //sinon on execute le batch en local.
        batchid = _engine.execute();
      }
      BatchEngineResponseType response = new BatchEngineResponseType();
      response.setBatchId(batchid);
      return response;
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_ERROR, new Object[] { pParams.getBatchName() }, e);
    }
  }

  /** {@inheritDoc} */
  public BatchEngineResponseType stopBatch(BatchRunType pParams, BatchEngineScenarioType pScenario, ContextType pContext) throws BatchEngineServiceException
  {
    if (pParams == null)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_STOP_NOPARAMS);
    }
    String batchName = pParams.getPath();
    if (pParams.getPath() == null || pParams.getPath().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_STOP_NOBATCHNAME);
    }
    int i = pParams.getPath().lastIndexOf('.');
    if (i > 0)
    {
      batchName = batchName.substring(i + 1);
    }
    try
    {
      BatchEngineResponseType response = new BatchEngineResponseType();
      if (pParams.getSite() != null && !pParams.getSite().getCode().equals(getLocalSite().getCode()))
      {
        /* modif site */
        NameValuePair[] data = new NameValuePair[2];
        SiteType site = getSiteInfo(pParams.getSite());
        data[0] = new NameValuePair("j_username", site.getLogin());
        data[1] = new NameValuePair("j_password", site.getPass());
        ServletLauncher servletLauncher = new ServletLauncher();
        String responseBody = servletLauncher.launchBatch(site.getUrl(), ServletLauncher.getRessource(pParams.getPath(), pParams.getDomain(), "stopBatch"), data);
        if (responseBody.contains("<id>"))
        {
          response.setBatchId(String.valueOf(responseBody.substring(responseBody.indexOf("<id>") + 4, responseBody.indexOf("</id>"))));
        }
        else
        { // si la servlet renvoi une erreur, on lance une exception.
          throw new Exception();
        }
      }
      else
      {
        EUJobManager eu = new EUJobManager();
        eu.stopBatch(batchName, pParams.getId());
        response.setBatchId(String.valueOf(pParams.getId()));
      }
      return response;
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_STOP_ERROR, new Object[] { pParams.getPath(), pParams.getId() }, e);
    }
  }

  /** {@inheritDoc} */
  public BatchEngineResponseType executeSynchronous(BatchEngineParamsType pParams, BatchEngineScenarioType pScenario, ContextType pContext) throws BatchEngineServiceException
  {
    if (pParams == null)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_NOPARAMS);
    }
    if (pParams.getBatchName() == null || pParams.getBatchName().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_NOBATCHNAME);
    }
    //BatchEngine engine;
    try
    {
      // instance de batch synchro
      SynchronousBatch batchSynchro = new SynchronousBatch();
      // paramétrage du nom de batch
      String batchName = pParams.getBatchName();
      batchSynchro.setBatchNameToExe(batchName);
      // paramétrage des variables
      if (pParams.getVariables() != null)
      {
        VariableListType varlist = pParams.getVariables();
        VariablesRepository varRepo = new VariablesRepository();
        varRepo.putVariableList(varlist);
        batchSynchro.setVarEngRepo(varRepo);
      }
      // paramétrage des propriétés
      if (pParams.getProperties() != null)
      {
        PropertyListType proplist = pParams.getProperties();
        PropertiesRepository propRepo = new PropertiesRepository();
        propRepo.putPropertyList(proplist);
        batchSynchro.setPropEngRepo(propRepo);
      }
      // paramétrage du domaine d'exécution
      String propertyContext = "default";
      if (pParams.getPropertyContext() != null && !"".equals(pParams.getPropertyContext()))
      {
        propertyContext = pParams.getPropertyContext();
      }
      batchSynchro.setActivedContext(propertyContext);
      // synchro de la récupération du batchId
      String batchid = batchSynchro.execute();
      BatchEngineResponseType response = new BatchEngineResponseType();
      response.setBatchId(batchid);
      return response;
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_EXEC_ERROR, new Object[] { pParams.getBatchName() }, e);
    }
  }

  /**
   * Charge/recharge les scripts de batch
   * @throws BatchEngineServiceException exception
   */
  public void loadrepo() throws BatchEngineServiceException
  {
    try
    {
      _engine.loadRepository();
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_LOAD_ERROR, e);
    }
  }

  /**
   * lance le programmeur de batch
   * @throws BatchEngineServiceException exception
   */
  public void schedule() throws BatchEngineServiceException
  {
    try
    {
      _engine.scheduleBatchs();
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_STARTSCHEDULE_ERROR, e);
    }
  }

  /**
   * arrête les batchs programmés
   * @throws BatchEngineServiceException exception
   */
  public void stopSchedule() throws BatchEngineServiceException
  {
    try
    {
      _engine.stopScheduler();
    }
    catch (Exception e)
    {
      throw new BatchEngineServiceException(BatchErrorDetail.ENGINE_SERVICE_STOPSCHEDULE_ERROR, e);
    }
  }

  /**
   * Permet de récupérer les infos de connexion liés au site
   * @param pSite : SiteKeyType contenant le code du site à appeler
   * @return : SiteType contenant les infos de connexion liés au site
   */
  private SiteType getSiteInfo(SiteKeyType pSite) throws BatchEngineServiceException
  {
    if (pSite == null || pSite.getCode().equals(""))
    {
      return null;
    }
    SiteManagerServiceDelegate siteManagerService = new SiteManagerServiceDelegate();
    SiteType mySite = new SiteType();
    try
    {
      mySite = siteManagerService.getSite(pSite, new SiteScenarioType(), new ContextType());
    }
    catch (TechnicalServiceNotDeliveredException e)
    {
      LOGGER.error("TechnicalServiceNotDeliveredException occured : " + e.getMessage());
    }
    catch (TechnicalServiceException e)
    {
      LOGGER.error("TechnicalServiceException occured : " + e.getMessage());
    }
    catch (ServiceException e)
    {
      LOGGER.error("ServiceException occured : " + e.getMessage());
    }
    if (mySite.getUrl() == null || mySite.getUrl().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.NO_URL_SITE, new Object[] { mySite.getName(), mySite.getCode() });
    }
    else if (mySite.getName() == null || mySite.getName().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.NO_NAME_SITE, new Object[] { mySite.getName(), mySite.getCode() });
    }
    else if (mySite.getLogin() == null || mySite.getLogin().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.NO_LOGIN_SITE, new Object[] { mySite.getName(), mySite.getCode() });
    }
    else if (mySite.getPass() == null || mySite.getPass().equals(""))
    {
      throw new BatchEngineServiceException(BatchErrorDetail.NO_PASSWORD_SITE, new Object[] { mySite.getName(), mySite.getCode() });
    }
    return mySite;
  }

  /**
   * Récupère le site local
   * @return résultat
   * @throws EULaunchException exception
   */
  private SiteType getLocalSite() throws EULaunchException
  {
    if (_localSite == null)
    {
      _localSite = SiteUtils.getLocalSite();
    }
    return _localSite;
  }
}
