package com.cylande.unitedretail.batch.execution;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.batch.BatchBuilder;
import com.cylande.unitedretail.batch.batch.BatchLoader;
import com.cylande.unitedretail.batch.batch.BatchQueueManager;
import com.cylande.unitedretail.batch.batch.ThreadPoolFactory;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.exception.JobControlException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.execution.quartz.ChildBatchJobListener;
import com.cylande.unitedretail.batch.execution.quartz.JobManager;
import com.cylande.unitedretail.batch.provider.pool.RemoteProviderPoolClient;
import com.cylande.unitedretail.batch.service.BatchRunManagerServiceImpl;
import com.cylande.unitedretail.batch.task.TaskLoader;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.context.URContext;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchChildType;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchEnum;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.QueueEnum;
import com.cylande.unitedretail.message.batch.TaskChildType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.batch.TriggerType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.VariablesRepository;

/**
 * Manager des Jobs associés au batch en cours d'execution
 */
public class EUJobManager
{

  public static final Map<Integer, Boolean> STOPPING_BATCH = new ConcurrentHashMap();
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(EUJobManager.class);
  /** Liste interne des job fils en cours */
  private final List<String> _childRunningList = new ArrayList<String>();
  /** Liste interne des job fils en cours */
  private final List<RemoteProviderPoolClient> _remoteProvidersClientPool = new ArrayList<RemoteProviderPoolClient>();
  /** Nom (identifiant) du job en cours */
  private String _jobName = null;
  /** Manager des jobs quartz */
  private JobManager _jobMgr = JobManager.getInstance();
  /** Délai d'attente de lancement d'un job */
  private int _launchDelay;
  /** URL de la servlet des providers distribués */
  private String _remoteProvidersServletUrl = null;
  private BatchRunManagerServiceImpl _batchRunManagerService;

  /**
   * Constructeur
   */
  public EUJobManager()
  {
    init();
  }

  /**
   * Constructeur
   * @param pJobName nom du job à gérer
   */
  public EUJobManager(String pJobName)
  {
    _jobName = pJobName;
    init();
  }

  /**
   * Interne : initialisation
   */
  private void init()
  {
    _batchRunManagerService = new BatchRunManagerServiceImpl();
    ResourceBundle engineProps = PropertyResourceBundle.getBundle("engine-config");
    try
    {
      String slaunchDelay = engineProps.getString("jobLaunchDelay").trim();
      _launchDelay = Integer.parseInt(slaunchDelay) * 1000;
    }
    catch (Exception e)
    {
      // par défaut une minute
      _launchDelay = 60000;
    }
    try
    {
      _remoteProvidersServletUrl = engineProps.getString("remoteProviders.servletUrl").trim();
    }
    catch (Exception e)
    {
      // par défaut une heure
      _remoteProvidersServletUrl = null;
    }
  }

  /**
   * Remove all Scheduled Jobs
   */
  public static void unScheduleAllBatchs()
  {
    JobManager.getInstance().removeAllScheduledJobs();
  }

  /**
   * Programme un batch déclenché par un trigger
   * @param pBatchName le nom du batch
   * @param pTriggerDef la définition du trigger
   * @param pActiveDomain le domaine d'exécution du batch
   * @param pVarRepo les variables de batch
   * @param propRepo les propriété de batch
   * @param pSiteKey le site local
   * @throws EUBuildException si erreur lors de la construction du batch
   * @throws EULaunchException si erreur lors de la programmation du batch
   */
  public void scheduleBatch(String pBatchName, TriggerType pTriggerDef, String pActiveDomain, String pAlternativeDomain,
      VariablesRepository pVarRepo, PropertiesRepository propRepo, SiteKeyType pSiteKey) throws EULaunchException, EUBuildException
  {
    if ((pTriggerDef != null) && (pTriggerDef.isActive()))
    {
      if ((pSiteKey == null) || (pSiteKey.getCode() == null) || (pSiteKey.getCode().trim().length() == 0))
      {
        throw new EULaunchException(BatchErrorDetail.ENGINE_SERVICE_EXEC_NOSITE, new Object[] { pBatchName });
      }
      EUJobParams jobParams = new EUJobParams();
      BatchLoader batchLoader = new BatchLoader();
      BatchType batchDef = batchLoader.loadBatchDef(pBatchName);
      jobParams.put(EUJobParams.BATCH_DEFINITION_KEY, batchDef);
      jobParams.put(EUJobParams.ACTIVE_DOMAIN_KEY, pActiveDomain);
      jobParams.put(EUJobParams.ALT_DOMAIN_KEY, pAlternativeDomain);
      jobParams.put(EUJobParams.SITE_KEY, pSiteKey);
      jobParams.put(EUJobParams.ENGINE_VARIABLES_KEY, pVarRepo);
      jobParams.put(EUJobParams.ENGINE_PROPERTIES_KEY, propRepo);
      setDefaultAuthenticationParams(jobParams);
      String jobName = "Batch-" + pBatchName;
      boolean bExclusive = batchDef.getType() == BatchEnum.STATEFULL;
      // creation du job
      Integer misFire = null;
      if (pTriggerDef.getMisfire() != null)
      {
        misFire = Integer.valueOf(pTriggerDef.getMisfire().intValue());
      }
      try
      {
        _jobMgr.createScheduledJob(jobName, EUJob.class, pTriggerDef.getName(), pTriggerDef.getCronExpression(), jobParams, bExclusive, misFire, _launchDelay);
      }
      catch (JobControlException e)
      {
        LOGGER.error(e);
        throw new EULaunchException(BatchErrorDetail.BATCH_SHEDULING_ERR, new Object[] { pBatchName }, e);
      }
    }
  }

  /**
   * Lance un batch Root
   * @param pBatchName le nom du batch à exécuter
   * @param pActiveDomain le domaine d'exécution du batch
   * @param pVarRepo les variables de batch
   * @param propRepo les propriétés de batch
   * @param pSiteKey le site local
   * @return le job Id pour cette instance de batch
   * @throws EUBuildException si erreur lors de la construction du batch
   * @throws EULaunchException si erreur lors du lancement du batch
   */
  public Integer launchRoot(String pBatchName, String pActiveDomain, String pAlternativeDomain, VariablesRepository pVarRepo,
      PropertiesRepository propRepo, SiteKeyType pSiteKey) throws EULaunchException, EUBuildException
  {
    return launchRoot(pBatchName, pActiveDomain, pAlternativeDomain, pVarRepo, propRepo, pSiteKey, false);
  }

  /**
   * Lance un batch Root
   * @param pBatchName le nom du batch à exécuter
   * @param pActiveDomain le domaine d'exécution du batch
   * @param pVarRepo les variables de batch
   * @param propRepo les propriétés de batch
   * @param pSiteKey le site local
   * @param pWaitEnd true = attente de la fin d'exécution du batch
   * @return le job Id pour cette instance de batch
   * @throws EUBuildException si erreur lors de la construction du batch
   * @throws EULaunchException si erreur lors du lancement du batch
   */
  public Integer launchRoot(String pBatchName, String pActiveDomain, String pAlternativeDomain, VariablesRepository pVarRepo,
      PropertiesRepository propRepo, SiteKeyType pSiteKey, boolean pWaitEnd) throws EULaunchException, EUBuildException
  {
    if ((pSiteKey == null) || (pSiteKey.getCode() == null) || (pSiteKey.getCode().trim().length() == 0))
    {
      throw new EULaunchException(BatchErrorDetail.ENGINE_SERVICE_EXEC_NOSITE, new Object[] { pBatchName });
    }
    EUJobParams jobParams = new EUJobParams();
    BatchLoader batchLoader = new BatchLoader();
    BatchType batchDef = batchLoader.loadBatchDef(pBatchName);
    jobParams.put(EUJobParams.SITE_KEY, pSiteKey);
    jobParams.put(EUJobParams.BATCH_DEFINITION_KEY, batchDef);
    jobParams.put(EUJobParams.ACTIVE_DOMAIN_KEY, pActiveDomain);
    jobParams.put(EUJobParams.ALT_DOMAIN_KEY, pAlternativeDomain);
    jobParams.put(EUJobParams.ENGINE_VARIABLES_KEY, pVarRepo);
    jobParams.put(EUJobParams.ENGINE_PROPERTIES_KEY, propRepo);
    jobParams.put(EUJobParams.BATCH_ID_KEY, null);
    Integer result = null;
    setDefaultAuthenticationParams(jobParams);
    String jobName = "Batch-" + pBatchName;
    boolean bExclusive = (batchDef.getType() == BatchEnum.STATEFULL || (batchDef.getType() == BatchEnum.STATEQUEUE && batchDef.getQueueType() != QueueEnum.ONLYLASTDOMAIN));
    if (batchDef.getType() == BatchEnum.STATEQUEUE)
    {
      BatchQueueManager queueManager = new BatchQueueManager();
      String queueName = queueManager.getQueueName(pBatchName, batchDef.getQueueType(), pActiveDomain, pAlternativeDomain);
      if (!queueManager.blockIfFree(queueName, batchDef.getQueueType()))
      {
        // le batch est mis en file d'attente
        queueManager.add(queueName, pActiveDomain, pAlternativeDomain, pVarRepo, propRepo, pSiteKey);
        return null;
      }
      // récupération du numéro de séquence au dernier moment afin de ne pas consommer un id pour rien
      result = BatchBuilder.getNewId();
      jobParams.put(EUJobParams.BATCH_ID_KEY, result);
      // on lance le batch dans un nouveau thread sinon la servlet est bloquée jusqu'à la fin d'exécution du batch
      ThreadPoolFactory.getInstance().execute(new EURootJobRunnable(this, pBatchName, jobParams, bExclusive));
      return result;
    }
    result = BatchBuilder.getNewId();
    jobParams.put(EUJobParams.BATCH_ID_KEY, result);
    // creation du job
    try
    {
      JobDetail rootJob = JobManager.getInstance().createJob(jobName, EUJob.class, jobParams, bExclusive, _launchDelay, true, null);
      String rootJobName = rootJob.getName();
      if (pWaitEnd)
      {
        while (!isEnded(rootJobName))
        {
          pauseThread();
        }
      }
    }
    catch (JobControlException e)
    {
      if (e.getCanonicalCode().equals(BatchErrorDetail.SCHEDULER_STATEFULLJOB_RUNNING.getCanonicalCode()))
      {
        throw new EULaunchException(BatchErrorDetail.BATCH_EXCLUSION_ERR, new Object[] { pBatchName }, e);
      }
      else if (e.getCanonicalCode().equals(BatchErrorDetail.LOCKED_BATCH_EXECUTION.getCanonicalCode()))
      {
        throw new EULaunchException(BatchErrorDetail.LOCKED_BATCH_EXECUTION, new Object[] { pBatchName });
      }
      else
      {
        throw new EULaunchException(BatchErrorDetail.BATCH_LAUNCH_ERR, new Object[] { pBatchName }, e);
      }
    }
    return result;
  }

  /**
   * Set the default authentication params into the job params (from current context)
   * @param pJobParams the jobs params
   */
  private static void setDefaultAuthenticationParams(EUJobParams pJobParams)
  {
    pJobParams.put(EUJobParams.UE_LOGIN_KEY, URContext.getSecurityContext().getUserName());
    pJobParams.put(EUJobParams.UE_SUBJECT_KEY, URContext.getSecurityContext().getSubject());
  }

  /**
   * Lance un batch fils
   * @param pParent unité d'exécution parente
   * @param pBatchChildRef la référence du batch fils à lancer (incluant ses options d'execution)
   * @throws EUBuildException si erreur lors de la construction du batch
   * @throws EULaunchException si erreur lors du lancement du batch
   */
  public void launchChild(ExecutionUnit pParent, BatchChildrenAbstractType pBatchChildRef) throws EUBuildException, EULaunchException
  {
    EUJobParams jobParams = new EUJobParams();
    jobParams.put(EUJobParams.PARENT_BATCH_KEY, pParent);
    jobParams.put(EUJobParams.ACTIVE_DOMAIN_KEY, pBatchChildRef.getActiveDomain());
    jobParams.put(EUJobParams.ALT_DOMAIN_KEY, pBatchChildRef.getDefaultDomain());
    jobParams.put(EUJobParams.FAIL_ON_ERROR_KEY, pBatchChildRef.getFailOnError());
    setDefaultAuthenticationParams(jobParams);
    String unitExecutionName = pParent.getFilteredString(pBatchChildRef.getRef());
    String jobName = null;
    boolean bExclusive = false;
    int threadCount = 1;
    String nameListener = null;
    if (pBatchChildRef instanceof BatchChildType)
    {
      BatchLoader batchLoader = new BatchLoader();
      BatchType batchDef = batchLoader.loadBatchDef(unitExecutionName);
      jobParams.put(EUJobParams.BATCH_DEFINITION_KEY, batchDef);
      jobName = "Batch-" + unitExecutionName;
      bExclusive = (batchDef.getType() == BatchEnum.STATEFULL || (batchDef.getType() == BatchEnum.STATEQUEUE && batchDef.getQueueType() != QueueEnum.ONLYLASTDOMAIN));
      if (batchDef.getType() == BatchEnum.STATEQUEUE)
      {
        BatchQueueManager queueManager = new BatchQueueManager();
        String queueName = queueManager.getQueueName(unitExecutionName, batchDef.getQueueType(), pBatchChildRef.getActiveDomain(), pBatchChildRef.getDefaultDomain());
        if (!queueManager.blockIfFree(queueName, batchDef.getQueueType()))
        {
          // le batch est mis en file d'attente
          queueManager.add(queueName, pParent, pBatchChildRef);
          return;
        }
        nameListener = new ChildBatchJobListener().getName();
      }
    }
    else if (pBatchChildRef instanceof TaskChildType)
    {
      TaskChildType taskRef = (TaskChildType)pBatchChildRef;
      String taskName = pParent.getFilteredString(taskRef.getRef());
      TaskType taskDef = TaskLoader.loadTaskDef(taskName);
      // determination du nombre de thread.
      // nombre de thread = nombre de thread paramétrés ou 1 si pas de commit frequency
      if (taskDef instanceof INTEGRATION)
      {
        BigInteger oThreadCount = ((INTEGRATION)taskDef).getThreadCount();
        BigInteger oCommitFrequency = ((INTEGRATION)taskDef).getCommitFrequency();
        if ((oThreadCount != null) && (oCommitFrequency != null))
        {
          threadCount = oThreadCount.intValue();
          if ((threadCount < 1) || (oCommitFrequency.intValue() < 1))
          {
            threadCount = 1;
          }
        }
      }
      jobParams.put(EUJobParams.TASK_DEFINITION_KEY, taskDef);
      jobParams.put(EUJobParams.THREAD_COUNT_KEY, Integer.valueOf(threadCount));
      jobName = "Task-" + unitExecutionName;
      bExclusive = false;
    }
    JobDetail createdJob;
    try
    {
      int threadNumber = 0;
      String providerSessionId = null;
      jobParams.put(EUJobParams.THREAD_COUNT_KEY, Integer.valueOf(threadCount));
      if (EUJob.getClusterMode())
      {
        if (threadCount > 1)
        {
          RemoteProviderPoolClient providerPool = new RemoteProviderPoolClient(_remoteProvidersServletUrl);
          _remoteProvidersClientPool.add(providerPool);
          providerSessionId = providerPool.initSession();
          jobParams.put(EUJobParams.PROVIDER_POOL_URL_KEY, _remoteProvidersServletUrl);
          jobParams.put(EUJobParams.PROVIDER_POOL_SESSION_ID_KEY, providerSessionId);
        }
      }
      else // Si on n'est pas en mode cluster, le nombre de thread sera toujours à 1 (que ce soit le dispatcher ou les thread pooled "enfant")
      {
        threadCount = 1;
      }
      while (threadNumber < threadCount)
      {
        jobParams.put(EUJobParams.THREAD_NUMBER_KEY, Integer.valueOf(threadNumber));
        createdJob = _jobMgr.createJob(jobName, EUJob.class, jobParams, bExclusive, _launchDelay, false, nameListener);
        _childRunningList.add(createdJob.getName());
        threadNumber++;
      }
    }
    catch (JobControlException e)
    {
      // contruction du path de l'E.U. fille pour les logs.
      String sPath;
      sPath = pParent.getSysPath() + '.' + unitExecutionName;
      if (e.getCanonicalCode().equals(BatchErrorDetail.SCHEDULER_STATEFULLJOB_RUNNING.getCanonicalCode()))
      {
        throw new EULaunchException(BatchErrorDetail.BATCH_EXCLUSION_ERR, new Object[] { sPath }, e);
      }
      else if (e.getCanonicalCode().equals(BatchErrorDetail.LOCKED_BATCH_EXECUTION.getCanonicalCode()))
      {
        throw new EULaunchException(BatchErrorDetail.LOCKED_BATCH_EXECUTION, new Object[] { sPath });
      }
      else
      {
        LOGGER.error(e);
        throw new EULaunchException(BatchErrorDetail.BATCH_LAUNCH_ERR, new Object[] { sPath }, e);
      }
    }
    catch (ProviderException e)
    {
      throw new EULaunchException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_CLIENT_INIT_ERROR, e);
    }
  }

  /**
   * Cancel childs execution
   */
  public void cancelChilds()
  {
    for (String sChildJobName: _childRunningList)
    {
      _jobMgr.interruptJob(sChildJobName);
    }
  }

  /**
   * Internal : Retrieve child exception
   * @param pJobDetail the Quartz job detail
   * @param pExceptionResult the Exception result (to concat childs exception)
   * @return the exception result
   */
  private EUExecutionException retrieveChildException(JobDetail pJobDetail, EUExecutionException pExceptionResult)
  {
    EUExecutionException childException = (EUExecutionException)pJobDetail.getJobDataMap().get("exception");
    if (childException != null)
    {
      if (pExceptionResult == null)
      {
        pExceptionResult = new BatchException(BatchErrorDetail.SCHEDULER_JOB_EXECUTION, new Object[] { pJobDetail.getName() }, childException);
      }
      else
      {
        pExceptionResult.addToExceptions(childException);
      }
    }
    return pExceptionResult;
  }

  /**
   * Make a pause in the current thread Cancel childs execution if thread interrupted
   */
  protected void pauseThread()
  {
    try
    {
      Thread.sleep(5);
    }
    catch (InterruptedException e)
    {
      cancelChilds();
    }
  }

  protected boolean isEnded(String pJobName)
  {
    boolean result = false;
    JobDetail jobDetail = _jobMgr.getJob(pJobName);
    if ((jobDetail == null) || (jobDetail.getJobDataMap() == null))
    {
      result = true;
    }
    else
    {
      String jobState = (String)jobDetail.getJobDataMap().get("state");
      result = ((jobState != null) && (jobState.equals("ended")));
    }
    return result;
  }

  /**
   * Attends la fin d'exécution des jobs enfants ou, si pNbChildMax n'est pas nul, qu'au moins un job enfant se libère
   * @param pNbChildMax nombre de job enfant maximum pouvant s'exécuter simultanément
   * @throws EUExecutionException si une exception s'est produite lors de l'exécution d'un des enfants
   */
  public void waitChilds(Integer pNbChildMax) throws EUExecutionException
  {
    EUExecutionException exceptionResult = null;
    JobDetail childJobDetail;
    int i = 0, nbPause = 0;
    String childJobName;
    String jobState;
    boolean deleteJobOrWait = pNbChildMax == null ? !_childRunningList.isEmpty() : _childRunningList.size() >= pNbChildMax;
    while (deleteJobOrWait)
    {
      i = i % _childRunningList.size();
      childJobName = _childRunningList.get(i);
      childJobDetail = _jobMgr.getJob(childJobName);
      if (childJobDetail == null || childJobDetail.getJobDataMap() == null)
      {
        _childRunningList.remove(i);
        _jobMgr.deleteJob(childJobName);
      }
      else
      {
        jobState = (String)childJobDetail.getJobDataMap().get("state");
        if (jobState != null && jobState.equals("ended"))
        {
          exceptionResult = retrieveChildException(childJobDetail, exceptionResult);
          _childRunningList.remove(i);
          _jobMgr.deleteJob(childJobName);
        }
        else if (nbPause > 60000)
        {
          nbPause = 0;
          if (childJobDetail.getJobDataMap().get(EUJobParams.TASK_DEFINITION_KEY.getName()) != null)
          {
            // pour pallier à un problème de récupération de l'état du job en mémoire, toutes les 5 mn, on contrôle en BDD
            // si toutes les task du batch sont à l'état terminé afin de sortir de la boucle et passer à l'exécution suivante
            AbstractBatch parentBatch = (AbstractBatch)childJobDetail.getJobDataMap().get(EUJobParams.PARENT_BATCH_KEY.getName());
            boolean taskInProgress = checkTaskRunInProgress(parentBatch);
            if (!taskInProgress)
            {
              LOGGER.info("[waitChilds] all taskRun of batch " + parentBatch.getName() + " are over : forcing stop job " + childJobName);
              exceptionResult = retrieveChildException(childJobDetail, exceptionResult);
              _childRunningList.remove(i);
              _jobMgr.deleteJob(childJobName);
            }
          }
        }
        else
        {
          i++;
          nbPause++;
          pauseThread();
        }
      }
      deleteJobOrWait = pNbChildMax == null ? !_childRunningList.isEmpty() : _childRunningList.size() >= pNbChildMax;
    }
    RemoteProviderPoolClient remoteProviderClient;
    while (!_remoteProvidersClientPool.isEmpty())
    {
      remoteProviderClient = _remoteProvidersClientPool.remove(0);
      remoteProviderClient.releaseSession();
    }
    if (exceptionResult != null)
    {
      throw exceptionResult;
    }
  }

  private boolean checkTaskRunInProgress(AbstractBatch parentBatch)
  {
    BatchRunKeyType key = new BatchRunKeyType();
    key.setId(parentBatch.getSysId());
    try
    {
      ContextType ctx = ContextTransformer.fromLocale();
      TaskRunListType taskList = _batchRunManagerService.getTaskRunChildrenOfBatchRun(key, null, ctx);
      if (taskList != null)
      {
        for (TaskRunType taskRun: taskList.getValues())
        {
          if (taskRun.getEndTime() == null)
          {
            return true;
          }
        }
      }
    }
    catch (WrapperServiceException e)
    {
      LOGGER.error(e, e);
    }
    return false;
  }

  /**
   * Permet d'interrompre un batch en cours d'exécution
   * @param pBatchName le nom du batch
   * @param pBatchId l'Id du batch
   */
  public void stopBatch(String pBatchName, Integer pBatchId) throws EULaunchException
  {
    EUJobParams euMap = new EUJobParams();
    euMap.put(EUJobParams.BATCH_ID_KEY, pBatchId);
    List<JobDetail> jobList;
    try
    {
      jobList = _jobMgr.findJob(pBatchName.startsWith("Batch-") ? pBatchName : "Batch-" + pBatchName, euMap);
      if (jobList.size() == 1)
      {
        STOPPING_BATCH.put(pBatchId, true);
        String jobName = jobList.get(0).getName();
        _jobMgr.interruptJob(jobName);
        _jobMgr.deleteJob(jobName);
      }
      else
      {
        forcingEndBatch(pBatchName, pBatchId); // le batch n'existe plus dans Quartz, on force donc son état à "terminé" en base
      }
    }
    catch (SchedulerException e)
    {
      throw new EULaunchException(BatchErrorDetail.ENGINE_SERVICE_STOP_ERROR, new Object[] { pBatchName, pBatchId }, e);
    }
  }

  /**
   * Force l'état du batch à "terminé" en base s'il ne l'est pas afin qu'il n'apparaisse plus "en cours" dans la console de supervision
   * @param pBatchName
   * @param pBatchId
   * @throws EULaunchException
   */
  private void forcingEndBatch(String pBatchName, Integer pBatchId) throws EULaunchException
  {
    STOPPING_BATCH.remove(pBatchId);
    BatchRunKeyType key = new BatchRunKeyType();
    key.setId(pBatchId);
    try
    {
      ContextType ctx = ContextTransformer.fromLocale();
      BatchRunType batch = _batchRunManagerService.getBatchRun(key, null, ctx);
      if (batch != null && Boolean.FALSE.equals(batch.getStatus()))
      {
        batch.setStatus(true);
        _batchRunManagerService.postBatchRun(batch, null, ctx);
      }
      else
      {
        throw new EULaunchException(BatchErrorDetail.ENGINE_SERVICE_STOP_BATCHNOTFOUND, new Object[] { pBatchName, pBatchId });
      }
    }
    catch (WrapperServiceException e)
    {
      throw new EULaunchException(BatchErrorDetail.ENGINE_SERVICE_STOP_BATCHNOTFOUND, new Object[] { pBatchName, pBatchId }, e);
    }
  }

  public void run()
  {
  }

  public int getLaunchDelay()
  {
    return _launchDelay;
  }
}
