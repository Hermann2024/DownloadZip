package com.cylande.unitedretail.batch.execution;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.batch.BatchBuilder;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.task.TaskFactory;
import com.cylande.unitedretail.batch.task.TaskIntegrationDispatchImpl;
import com.cylande.unitedretail.batch.task.TaskIntegrationImpl;
import com.cylande.unitedretail.framework.context.URContext;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.VariablesRepository;

/**
 * Quartz job for unitExecution (ue) launch
 */
public class EUJob implements InterruptableJob, StatefulJob
{
  /** Variable utilisé pour nommer nos thread pooled */
  public static final String THREAD_POOLED_NAME = "-Thread";
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(EUJob.class);
  /** Identifie la classe pour les logs */
  private static final String DEBUG_CLASS_INFO = " [EU Job] ";
  /** Si cluster mode à true on sera dans l'ancien mode (remote provider) */
  private static final Boolean CLUSTER_MODE;
  /** The execution unit */
  private ExecutionUnit _executionUnit = null;
  /** Flag for interruption call : true if execution interruption already called once */
  private boolean _hadFirstInterrupt = false;
  /** the job name in quartz scheduler */
  private String _name;

  static
  {
    ResourceBundle engineProps = PropertyResourceBundle.getBundle("engine-config");
    Boolean clusterMode;
    try
    {
      clusterMode = Boolean.valueOf(engineProps.getString("clusterMode").trim());
    }
    catch (Exception e)
    {
      clusterMode = false;
    }
    CLUSTER_MODE = clusterMode;
  }

  /**
   * Default Constructor
   */
  public EUJob()
  {
  }

  public static Boolean getClusterMode()
  {
    return CLUSTER_MODE;
  }

  /**
   * For Quartz interruption call first interruption call : underlying execution unit finish its current job before closing (don't start new jobs
   * anymore). second interruption call : underlying execution unit stop it current treatment.
   */
  public void interrupt()
  {
    LOGGER.debug(DEBUG_CLASS_INFO + " interruption du job " + _name);
    if (_executionUnit != null)
    {
      if (!_hadFirstInterrupt)
      {
        _executionUnit.cancel(false);
      }
      else
      {
        _executionUnit.cancel(true);
      }
    }
    _hadFirstInterrupt = true;
  }

  /**
   * Build an execution Unit (Batch or Task)
   * @param pDataMap the quartz jobDataMap that contains the UE Build options
   * @throws EUBuildException If an error occurs during this UE Construction
   */
  private void buildExecutionUnit(JobDataMap pDataMap) throws EUBuildException
  {
    AbstractBatch parentBatch = (AbstractBatch)pDataMap.get(EUJobParams.PARENT_BATCH_KEY.getName());
    BatchType batchDef = (BatchType)pDataMap.get(EUJobParams.BATCH_DEFINITION_KEY.getName());
    SiteKeyType siteKey = (SiteKeyType)pDataMap.get(EUJobParams.SITE_KEY.getName());
    String activeDomain = (String)pDataMap.get(EUJobParams.ACTIVE_DOMAIN_KEY.getName());
    String alternativeDomain = (String)pDataMap.get(EUJobParams.ALT_DOMAIN_KEY.getName());
    Boolean failOnError = (Boolean)pDataMap.get(EUJobParams.FAIL_ON_ERROR_KEY.getName());
    if (batchDef != null)
    {
      // c'est un batch
      Integer batchId = (Integer)pDataMap.get(EUJobParams.BATCH_ID_KEY.getName());
      if (failOnError == null)
      {
        failOnError = false;
      }
      BatchBuilder builder = new BatchBuilder();
      _executionUnit = builder.buildBatch(batchDef, activeDomain, alternativeDomain, batchId, parentBatch, failOnError);
      if (parentBatch == null)
      {
        // cas du batch root
        VariablesRepository varEngRepo = (VariablesRepository)pDataMap.get(EUJobParams.ENGINE_VARIABLES_KEY.getName());
        PropertiesRepository propENGrepo = (PropertiesRepository)pDataMap.get(EUJobParams.ENGINE_PROPERTIES_KEY.getName());
        _executionUnit.setPropertiesEngRepo(propENGrepo);
        _executionUnit.setVariablesEngRepo(varEngRepo);
        _executionUnit.setSiteKey(siteKey);
      }
    }
    else
    {
      // c'est une tache
      TaskType taskDef = (TaskType)pDataMap.get(EUJobParams.TASK_DEFINITION_KEY.getName());
      _executionUnit = TaskFactory.create(parentBatch, taskDef, activeDomain, alternativeDomain, failOnError);
      int threadCount = (Integer)pDataMap.get(EUJobParams.THREAD_COUNT_KEY.getName());
      if (getClusterMode() && (threadCount > 1) && (_executionUnit instanceof TaskIntegrationImpl))
      {
        int threadNumber = (Integer)pDataMap.get(EUJobParams.THREAD_NUMBER_KEY.getName());
        String providerSessionId = (String)pDataMap.get(EUJobParams.PROVIDER_POOL_SESSION_ID_KEY.getName());
        String providerPoolUrl = (String)pDataMap.get(EUJobParams.PROVIDER_POOL_URL_KEY.getName());
        ((TaskIntegrationImpl)_executionUnit).setThreadNumber(threadNumber);
        try
        {
          ((TaskIntegrationImpl)_executionUnit).setThreadCount(threadCount);
          ((TaskIntegrationImpl)_executionUnit).activateRemoteProvidersMode(providerPoolUrl, providerSessionId);
        }
        catch (ProviderException e)
        {
          throw new EUBuildException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_CLIENT_INIT_ERROR, e);
        }
      }
      else if (_executionUnit instanceof TaskIntegrationDispatchImpl)
      {
        ((TaskIntegrationDispatchImpl)_executionUnit).setThreadCount(threadCount);
      }
    }
  }

  /**
   * Initialize securityContext
   * @param pSubject authenticated subject
   * @param pUserName authenticated user name (login)
   */
  private void initSecurityContext(Subject pSubject, String pUserName)
  {
    String userLogin;
    Subject userSubject;
    userLogin = _executionUnit.getUser();
    userSubject = _executionUnit.getAuthenticatedSubject();
    // pas de user spécifié on prends celui par defaut.
    if (userSubject == null)
    {
      // prends les valeurs par defaut
      userLogin = pUserName;
      userSubject = pSubject;
    }
    // met à jour l'unité d'exécution
    _executionUnit.setUser(userLogin);
    _executionUnit.setAuthenticatedSubject(userSubject);
    URContext.getSecurityContext().setSubject(_executionUnit.getAuthenticatedSubject());
    URContext.getSecurityContext().setUserPrincipal(_executionUnit.getUserPrincipal());
  }

  /**
   * For Quartz execution call build and execute the execution unit
   * @param pJobExecutionContext the quartz execution context
   */
  public void execute(JobExecutionContext pJobExecutionContext)
  {
    JobDetail jobDetail = pJobExecutionContext.getJobDetail();
    _name = jobDetail.getName();
    LOGGER.debug(DEBUG_CLASS_INFO + " démarrage du job " + _name);
    JobDataMap dataMap = jobDetail.getJobDataMap();
    dataMap.put("state", "started");
    EUExecutionException exception = null;
    if (!_hadFirstInterrupt)
    {
      try
      {
        buildExecutionUnit(dataMap);
        if (_executionUnit != null)
        {
          String defaultUserLogin = (String)dataMap.get(EUJobParams.UE_LOGIN_KEY.getName());
          Subject defaultUserSubject = (Subject)dataMap.get(EUJobParams.UE_SUBJECT_KEY.getName());
          initSecurityContext(defaultUserSubject, defaultUserLogin);
          EUJobManager ueJobManager = new EUJobManager();
          _executionUnit.setUeJobManager(ueJobManager);
          _executionUnit.run();
          if (_executionUnit.getException() != null)
          {
            exception = _executionUnit.getException();
          }
        }
      }
      catch (Exception e)
      {
        exception = new EUExecutionException(BatchErrorDetail.SCHEDULER_JOB_EXECUTION, new Object[] { _name }, e);
      }
    }
    Trigger trigger = pJobExecutionContext.getTrigger();
    JobDataMap triggerDataMap = trigger.getJobDataMap();
    if (exception != null)
    {
      triggerDataMap.put("exception", exception);
      dataMap.put("exception", exception);
    }
    triggerDataMap.put("state", "ended");
    dataMap.put("state", "ended");
    LOGGER.debug(DEBUG_CLASS_INFO + " fin du job " + _name);
    // On supprime les informations d'authentification au niveau du threadLocal
    // Les thread du moteur de batch sont remis dans le pool sans information d'authentification
    // Cela evite d'heriter de l'authentification d'un batch precedent
    URContext.resetSecurityContext();
  }
}
