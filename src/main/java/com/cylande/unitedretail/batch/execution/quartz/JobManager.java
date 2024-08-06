package com.cylande.unitedretail.batch.execution.quartz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.exception.JobControlException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.execution.EUJobParams;
import com.cylande.unitedretail.common.tools.URParam;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.portal.BusinessGlobalParameterType;

/**
 * Manager pour les job quartz
 */
public class JobManager
{
  /** instance for this singleton */
  private static final JobManager INSTANCE;

  /** quartz group for scheduled jobs */
  private static final String SCHEDULED_JOB_GROUP = "SCHEDULED_JOB_GROUP";

  /** quartz group for immediate jobs */
  private static final String IMMEDIATE_JOB_GROUP = "IMMEDIATE_JOB_GROUP";

  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(JobManager.class);

  /** identifie la classe pour les logs */
  private static final String DEBUG_CLASS_INFO = " [Job Manager] ";

  /** instance for quartz scheduler */
  private Scheduler _sched = null;

  /** flag that indicate if scheduler is schutting down */
  private boolean _isShuttingDown = false;

  /** flag that indicate if scheduler is schutted down*/
  private boolean _isShutDown = true;

  /** flag that indicate if scheduler is starting */
  private boolean _isStarting = false;

  /** flag that indicate if scheduler is started */
  private boolean _isStarted = false;

  /** flag that indicate if scheduler is in stand by mode  */
  private boolean _isStdBy = false;

  /** Intenal : indicate if sheduler use persistent job store */
  private boolean _isInPersistentMode;

  /** Intenal : used to perform serialization tests */
  private ObjectOutputStream _testOutputStream;
  private volatile Map<String, String> _jobWasExecutedMap = new HashMap();

  static {
    INSTANCE = new JobManager();
    INSTANCE.initScheduler();
  }

  /**
   * return manager instance.
   * @return Manager instance
   */
  public static JobManager getInstance()
  {
    return INSTANCE;
  }

  /**
   * Indique si le job portant le nom donné est en cours d'exécution
   * @param pJobName : le nom du Job
   * @return true si le Job est en cours d'exécution
   */
  private boolean isJobInExecution(String pJobName)
  {
    boolean result = false;
    try
    {
      List<JobExecutionContext> runningJobs = _sched.getCurrentlyExecutingJobs();
      for (JobExecutionContext job: runningJobs)
      {
        result = job.getJobDetail().getName().equals(pJobName);
        if (result)
        {
          break;
        }
      }
    }
    catch (SchedulerException e)
    {
      result = false;
    }
    return result;
  }

  public List<JobExecutionContext> getExecutingJobList() throws SchedulerException
  {
    return _sched.getCurrentlyExecutingJobs();
  }

  /**
   * Attend qu'un job à exécution immédiate démarre
   * @param pJobName      : le nom du job
   * @param pTriggerName  : le nom du trigger
   * @param pWaitDelay    : delai d'attente maxi ( en millisecondes )
   * @return true si le job a démarré, false sinon.
   * @throws SchedulerException exception
   */
  private boolean waitImmediateJobStart(String pJobName, String pTriggerName, long pWaitDelay)
  {
    long maxEndTime = System.currentTimeMillis() + pWaitDelay;
    boolean result = false;
    int triggerState = Trigger.STATE_ERROR;
    int newState = Trigger.STATE_ERROR;
    while ((System.currentTimeMillis() < maxEndTime) && (!result))
    {
      try
      {
        Thread.sleep(5);
      }
      catch (InterruptedException e)
      {
        break;
      }
      try
      {
        newState = _sched.getTriggerState(pTriggerName, IMMEDIATE_JOB_GROUP);
      }
      catch (SchedulerException e)
      {
        LOGGER.debug(e, e);
        newState = Trigger.STATE_NONE;
      }
      LOGGER.debug("wait start of " + pJobName + ", state = " + triggerStateToString(newState));
      if (newState != Trigger.STATE_ERROR)
      {
        if (newState != triggerState)
        {
          triggerState = newState;
        }
        result = (triggerState == Trigger.STATE_COMPLETE) || isJobInExecution(pJobName) || isJobStarted(pJobName);
      }
      else
      {
        break;
      }
    }
    LOGGER.debug("wait start of " + pJobName + ", started  = " + result);
    return result;
  }

  /**
   * Pour sécuriser le test de démarrage du job
   * @param pJobName nom du job
   * @return true si démarré
   */
  private boolean isJobStarted(String pJobName)
  {
    boolean result = false;
    JobDetail jobDetail = getJob(pJobName);
    if (jobDetail != null && jobDetail.getJobDataMap() != null)
    {
      LOGGER.debug(" [isJobStarted] " + pJobName + ", state = " + jobDetail.getJobDataMap().get("state"));
      result = jobDetail.getJobDataMap().get("state") != null;
    }
    else if (_jobWasExecutedMap.get(pJobName) != null)
    {
      LOGGER.debug(" [isJobStarted] " + pJobName + ", job was already executed");
      result = true;
      _jobWasExecutedMap.remove(pJobName);
    }
    return result;
  }

  /**
   * indique si un objet est sérialisable
   * attention : traitement couteux; utiliser avec parcimonie
   * @param pValue : l'objet
   * @return true si il est sérialisable
   */
  private boolean isSerializable(Object pValue)
  {
    boolean result = false;
    if (_testOutputStream != null)
    {
      try
      {
        _testOutputStream.writeObject(pValue);
        _testOutputStream.flush();
        _testOutputStream.reset();
        result = true;
      }
      catch (IOException e)
      {
        result = false;
      }
    }
    return result;
  }

  /**
   * Create a quartz Job
   * @param pJobName     : the name for this job
   * @param pJobClass    : the class for this job (extends quartz job class)
   * @param pJobGroup    : the group this job ( immediate or scheduled )
   * @param pTrigger     : the quartz trigger for this job
   * @param params      : the parameters to send to this job (into its job data map)
   * @param pWaitDelay   : the max delay to wait this job starts before method exits (for immediate jobs only)
   * @param pRemoveAuto  : true if job has to be removed after it's execution.
   * @return the corresponding job detail
   * @throws JobControlException if job failed to be created or launched
   * @throws SchedulerException if job failed to be created or launched
   * @throws EULaunchException
   */
  private JobDetail createQuartzJob(String pJobName, Class pJobClass, Trigger pTrigger, String pJobGroup, JobParamMap params, long pWaitDelay, boolean pRemoveAuto, String pNameListener) throws JobControlException, SchedulerException, EULaunchException
  {
    JobDetail result;
    result = new JobDetail(pJobName, pJobGroup, pJobClass);
    result.setRequestsRecovery(false);
    result.setDurability(!pRemoveAuto);
    if (pNameListener != null)
    {
      result.addJobListener(pNameListener);
    }
    else
    {
      result.addJobListener(new BatchJobListener().getName());
    }
    int nbJobs = _sched.getCurrentlyExecutingJobs().size();
    int nbThread = _sched.getMetaData().getThreadPoolSize();
    LOGGER.debug("Jobs actuels = " + nbJobs + "/" + nbThread + " lancement d'un nouveau job");
    if ((params != null) && (params.size() > 0))
    {
      JobDataMap jobDataMap = result.getJobDataMap();
      for (Entry<JobParamKey, Object> entry: params.entrySet())
      {
        if (entry.getKey().isMandatory() || !_isInPersistentMode || isSerializable(entry.getValue()))
        {
          jobDataMap.put(entry.getKey().getName(), entry.getValue());
        }
        else
        {
          LOGGER.warn("Can't serialize " + entry.getKey().getName() + " with value " + entry.getValue());
        }
      }
    }
    _sched.scheduleJob(result, pTrigger);
    if ((pWaitDelay > 0) && (IMMEDIATE_JOB_GROUP.equals(pJobGroup)))
    {
      boolean bStarted = waitImmediateJobStart(pJobName, pTrigger.getName(), pWaitDelay);
      if (!bStarted)
      {
        if (params == null || params.get(EUJobParams.BATCH_ID_KEY) == null)
        {
          getInstance().interruptJob(result.getName());
          getInstance().deleteJob(result.getName());
        }
        else
        {
          Integer batchId = (Integer)params.get(EUJobParams.BATCH_ID_KEY);
          new EUJobManager().stopBatch(result.getName(), batchId);
        }
        throw new JobControlException(BatchErrorDetail.SCHEDULER_JOB_LAUNCH_FAILED, new Object[] { result.getName(), nbJobs, pWaitDelay });
      }
    }
    return result;
  }

  /**
   * get a new UUID for job unique name creation
   * @return an new UUID as String
   */
  public static String getNewUUID()
  {
    return UUID.randomUUID().toString();
  }

  /**
   * Create a sheduled Job
   * @param pJobName     : the name for this job
   * @param pJobClass    : the class for this job (extends quartz job class)
   * @param pTriggerName : the name for trigger associated to this job
   * @param pCronExp     : the cron expression to program the trigger
   * @param pParams      : the parameters to send to this job (into its job data map)
   * @param pExclusive   : true if a job with this name need to be executed only once
   * @param pMisfireOpt  : Option in case of trigger misFire
   * @param pWaitDelay   : the max delay to wait this job starts before method exits (for immediate jobs only)
   * @return the corresponding job detail
   * @throws JobControlException if job failed to be launched
   */
  private JobDetail createQuartzTriggerJob(String pJobName, Class pJobClass, String pTriggerName, String pCronExp, JobParamMap pParams, boolean pExclusive, Integer pMisfireOpt, long pWaitDelay) throws JobControlException
  {
    try
    {
      JobDetail result = null;
      String jobName = null;
      if ((pTriggerName != null) && (pCronExp != null))
      {
        jobName = pJobName + '_' + pTriggerName + '_' + getNewUUID();
        CronTrigger trigger = new CronTrigger(jobName, SCHEDULED_JOB_GROUP, pCronExp);
        int misfireOpt = CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        if ((pMisfireOpt != null) && (pMisfireOpt == CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW))
        {
          misfireOpt = pMisfireOpt;
        }
        trigger.setMisfireInstruction(misfireOpt);
        TriggerJobParams triggedJobParams = new TriggerJobParams();
        triggedJobParams.put(TriggerJobParams.TRIGGER_JOB_NAME_KEY, pJobName);
        triggedJobParams.put(TriggerJobParams.TRIGGER_JOB_CLASS_KEY, pJobClass);
        triggedJobParams.put(TriggerJobParams.TRIGGER_JOB_PARAMS_KEY, pParams);
        triggedJobParams.put(TriggerJobParams.TRIGGER_JOB_LAUNCH_DELAY_KEY, Long.valueOf(pWaitDelay));
        if (pExclusive)
        {
          // statefull
          result = createQuartzJob(jobName, TriggerStatefullJob.class, trigger, SCHEDULED_JOB_GROUP, triggedJobParams, 0, false, null);
        }
        else
        {
          // stateless
          result = createQuartzJob(jobName, TriggerJob.class, trigger, SCHEDULED_JOB_GROUP, triggedJobParams, 0, false, null);
        }
      }
      else
      {
        throw new JobControlException(BatchErrorDetail.TRIGGER_PARSE_CRON_ERROR, new Object[] { pTriggerName, pCronExp });
      }
      return result;
    }
    catch (ParseException pe)
    {
      throw new JobControlException(BatchErrorDetail.TRIGGER_PARSE_CRON_ERROR, new Object[] { pTriggerName, pCronExp }, pe);
    }
    catch (ObjectAlreadyExistsException oae)
    {
      throw new JobControlException(BatchErrorDetail.SCHEDULER_STATEFULLJOB_RUNNING, new Object[] { pJobName }, oae);
    }
    catch (SchedulerException se)
    {
      throw new JobControlException(BatchErrorDetail.SCHEDULER_CREATE_QUARTZ_JOB, new Object[] { pJobName }, se);
    }
    catch (EULaunchException e)
    {
      throw new JobControlException(BatchErrorDetail.SCHEDULER_CREATE_QUARTZ_JOB, new Object[] { pJobName }, e);
    }
  }

  /**
   * Create a sheduled Job
   * @param pJobName     : the name for this job
   * @param pJobClass    : the class for this job (extends quartz job class)
   * @param pTriggerName : the name for trigger associated to this job
   * @param pCronExp     : the cron expression to program the trigger
   * @param pParams      : the parameters to send to this job (into its job data map)
   * @param pExclusive   : true if a job with this name need to be executed only once
   * @param pMisfireOpt  : Option in case of trigger misFire
   * @param pWaitDelay   : the max delay to wait this job starts before method exits (for immediate jobs only)
   * @return the corresponding job detail
   * @throws JobControlException if job failed to be launched
   */
  public JobDetail createScheduledJob(String pJobName, Class pJobClass, String pTriggerName, String pCronExp, JobParamMap pParams, boolean pExclusive, Integer pMisfireOpt, long pWaitDelay) throws JobControlException
  {
    return createQuartzTriggerJob(pJobName, pJobClass, pTriggerName, pCronExp, pParams, pExclusive, pMisfireOpt, pWaitDelay);
  }

  /**
   * Create a Job
   * @param pJobName    : the name for this job
   * @param pJobClass   : the class for this job (extends quartz job class)
   * @param pParams     : the parameters to send to this job (into its job data map)
   * @param pExclusive  : true if a job with this name need to be executed only once
   * @param pWaitDelay  : the max delay to wait this job starts before method exits (for immediate jobs only)
   * @return the corresponding job detail
   * @throws JobControlException if job failed to be launched
   */
  public JobDetail createJob(String pJobName, Class pJobClass, JobParamMap pParams, boolean pExclusive, long pWaitDelay, boolean pRemoveAuto, String pNameListener) throws JobControlException
  {
    JobDetail result = null;
    if (isCreateJob(pJobName))
    {
      String jobName;
      if (pExclusive)
      {
        // exécution exclusive : pas d'autre job identique en concurrence
        // le nom de job reste celui donné
        jobName = pJobName;
      }
      else
      {
        // exécution ouverte  : peut être executée plusieurs fois dans des threads différents
        // on suffixe par un UUID
        jobName = pJobName + '_' + getNewUUID();
      }
      try
      {
        SimpleTrigger trigger = new SimpleJobTrigger(jobName, IMMEDIATE_JOB_GROUP);
        trigger.setNextFireTime(new Date());
        trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        result = createQuartzJob(jobName, pJobClass, trigger, IMMEDIATE_JOB_GROUP, pParams, pWaitDelay, pRemoveAuto, pNameListener);
      }
      catch (ObjectAlreadyExistsException oae)
      {
        throw new JobControlException(BatchErrorDetail.SCHEDULER_STATEFULLJOB_RUNNING, new Object[] { pJobName }, oae);
      }
      catch (SchedulerException se)
      {
        throw new JobControlException(BatchErrorDetail.SCHEDULER_CREATE_QUARTZ_JOB, new Object[] { pJobName }, se);
      }
      catch (EULaunchException e)
      {
        throw new JobControlException(BatchErrorDetail.SCHEDULER_CREATE_QUARTZ_JOB, new Object[] { pJobName }, e);
      }
    }
    else
    {
      Exception e = new JobControlException(BatchErrorDetail.LOCKED_BATCH_EXECUTION, new Object[] { pJobName });
      LOGGER.error(e.getLocalizedMessage());
      throw new JobControlException(BatchErrorDetail.LOCKED_BATCH_EXECUTION, new Object[] { pJobName });
    }
    return result;
  }

  private boolean isCreateJob(String pJobName) throws JobControlException
  {
    if (!"true".equals(System.getProperty("devMode")))
    {
      ContextType context = ContextTransformer.fromLocale();
      context.setBusinessFunction("-");
      context.setBusinessUnit(0);
      BusinessGlobalParameterType urParam = URParam.get(context);
      if (urParam != null)
      {
        return !Boolean.TRUE.equals(urParam.isLockedBatchExecution());
      }
      else
      {
        throw new JobControlException(BatchErrorDetail.CHECK_LOCKED_BATCH_ERROR, new Object[] { pJobName });
      }
    }
    return true;
  }
  /**
   * getJobExecutionContext
   * @param pJobName
   * @return résultat
   * @throws SchedulerException exception
   */
  public JobExecutionContext getJobExecutionContext(String pJobName) throws SchedulerException
  {
    JobExecutionContext result = null;
    List<JobExecutionContext> listJobs;
    listJobs = _sched.getCurrentlyExecutingJobs();
    Iterator<JobExecutionContext> it = listJobs.iterator();
    JobExecutionContext jobContext;
    while (it.hasNext())
    {
      jobContext = it.next();
      if (jobContext.getJobDetail().getName().equals(pJobName))
      {
        result = jobContext;
        break;
      }
    }
    return result;
  }

  /**
   * Permet de retrouver un job
   * @param pJobName    : le nom donné au job en creation
   * @param pCriterias  : une liste d'éléments contenus dans la jobDataMap du Job
   * @return résultat
   * @throws SchedulerException exception
   */
  public List<JobDetail> findJob(String pJobName, JobParamMap pCriterias) throws SchedulerException
  {
    List<JobDetail> result = new ArrayList();
    try
    {
      String[] listJobs = _sched.getJobNames(IMMEDIATE_JOB_GROUP);
      int i = 0;
      JobDetail tmpJobDetail;
      String[] jobInfo;
      boolean bfound = false;
      while ((i < listJobs.length) && (!bfound))
      {
        jobInfo = listJobs[i].split("_");
        if ((jobInfo.length > 0) && (jobInfo[0].equals(pJobName) || listJobs[i].equals(pJobName)))
        {
          tmpJobDetail = _sched.getJobDetail(listJobs[i], IMMEDIATE_JOB_GROUP);
          bfound = true;
          if (pCriterias != null)
          {
            JobDataMap dataMap = tmpJobDetail.getJobDataMap();
            Iterator<JobParamKey> keysIterator = pCriterias.keyIterator();
            JobParamKey key;
            while (keysIterator.hasNext() && bfound)
            {
              key = keysIterator.next();
              bfound = pCriterias.get(key).equals(dataMap.get(key.getName()));
            }
          }
          if (bfound)
          {
            result.add(tmpJobDetail);
          }
        }
        i++;
      }
    }
    catch (SchedulerException e)
    {
      LOGGER.error(DEBUG_CLASS_INFO + "Erreur (" + e.getErrorCode() + ") lors de la recherche du job (" + pJobName + ")", e);
    }
    return result;
  }

  /**
   * Return quartz Job Detail with quartz job Name
   * @param pJobName : the quartz job name
   * @return Quartz job detail, null if not found
   */
  public JobDetail getJob(String pJobName)
  {
    JobDetail result = null;
    try
    {
      result = _sched.getJobDetail(pJobName, IMMEDIATE_JOB_GROUP);
    }
    catch (SchedulerException e)
    {
      LOGGER.error(DEBUG_CLASS_INFO + "Erreur (" + e.getErrorCode() + ") lors de la recherche du job (" + pJobName + ")", e);
    }
    return result;
  }

  /**
   * Start a quartz job immediatly
   * @param pJob : the quartz Job Detail
   * @param pParams : the jobs parameters
   * @throws JobControlException if an error occurs during Job launch
   */
  protected synchronized void launchJob(JobDetail pJob, Map pParams) throws JobControlException
  {
    SimpleJobTrigger trigger = new SimpleJobTrigger(pJob.getName() + "_" + getNewUUID(), IMMEDIATE_JOB_GROUP);
    try
    {
      pJob.getJobDataMap().putAll(pParams);
      _sched.scheduleJob(pJob, trigger);
    }
    catch (SchedulerException e)
    {
      throw new JobControlException(BatchErrorDetail.SCHEDULER_CREATE_QUARTZ_JOB, new Object[] { pJob.getName() }, e);
    }
  }

  /**
   * Delete Job from Scheduler Job Repository
   * @param pJobName : the quartz name for the job to delete
   */
  public void deleteJob(String pJobName)
  {
    try
    {
      _sched.deleteJob(pJobName, IMMEDIATE_JOB_GROUP);
    }
    catch (SchedulerException e)
    {
      LOGGER.warn(DEBUG_CLASS_INFO + "Erreur lors de la suppression du job " + pJobName, e);
    }
  }

  /**
   * remove all jobs
   */
  public void removeAllScheduledJobs()
  {
    String[] listJobs = null;
    try
    {
      if (_sched != null && !_sched.isShutdown())
      {
        listJobs = _sched.getJobNames(SCHEDULED_JOB_GROUP);
      }
    }
    catch (SchedulerException e)
    {
      LOGGER.error(DEBUG_CLASS_INFO + "Erreur lors de la récupération des jobs déclenchés par trigger ", e);
    }
    int i = 0;
    while (listJobs != null && i < listJobs.length)
    {
      try
      {
        LOGGER.debug(DEBUG_CLASS_INFO + "Suppression du job " + listJobs[i]);
        _sched.deleteJob(listJobs[i], SCHEDULED_JOB_GROUP);
      }
      catch (Exception e)
      {
        LOGGER.error(DEBUG_CLASS_INFO + "Erreur lors de la suppression du job programmé " + listJobs[i], e);
      }
      i++;
    }
  }

  private void initPersistentMode()
  {
    try
    {
      _isInPersistentMode = _sched.getMetaData().jobStoreSupportsPersistence();
    }
    catch (Exception e)
    {
      _isInPersistentMode = false;
    }
    if (_isInPersistentMode)
    {
      removeAllScheduledJobs();
      try
      {
        _testOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());
      }
      catch (IOException e)
      {
        _testOutputStream = null;
      }
    }
    else
    {
      _testOutputStream = null;
    }
  }

  /**
   * init Scheduler
   */
  protected void initScheduler()
  {
    if ((!_isStarted) && (!_isStarting) && (!_isShuttingDown))
    {
      _isStarting = true;
      LOGGER.debug("Demarrage du Scheduler");
      SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
      try
      {
        _sched = schedFact.getScheduler();
        _sched.addJobListener(new ChildBatchJobListener());
        _sched.addJobListener(new BatchJobListener());
        initPersistentMode();
        try
        {
          _sched.start();
          _isShutDown = false;
          _isStdBy = false;
          _isStarted = true;
        }
        catch (Exception e)
        {
          LOGGER.error(DEBUG_CLASS_INFO + "Erreur lors du démarrage du Scheduler ", e);
        }
      }
      catch (SchedulerException e)
      {
        LOGGER.error(DEBUG_CLASS_INFO + "Erreur lors du démarrage du Scheduler ", e);
      }
      _isStarting = false;
    }
  }

  /**
   * Std by sheduler
   */
  protected void stdbyScheduler()
  {
    LOGGER.debug(DEBUG_CLASS_INFO + "Mise en Stand by du scheduler ");
    if ((!_isStdBy) && (!_isStarting) && (_isStarted) && (_sched != null))
    {
      try
      {
        _sched.standby();
        _isStdBy = true;
        _isStarted = false;
      }
      catch (Exception e)
      {
        LOGGER.warn(DEBUG_CLASS_INFO + "Impossible de mettre le scheduler en Stand by ", e);
      }
    }
  }

  /**
   * Stop the job scheduler
   */
  public void stopScheduler()
  {
    LOGGER.debug(DEBUG_CLASS_INFO + "Arret du scheduler ");
    if ((!_isShuttingDown) && (!_isShutDown) && (_sched != null))
    {
      try
      {
        _isShuttingDown = true;
        stdbyScheduler();
        _sched.shutdown(false);
        _isStdBy = false;
        _isStarting = false;
        _isStarted = false;
        _isShuttingDown = false;
        _isShutDown = true;
      }
      catch (Exception e)
      {
        LOGGER.warn(DEBUG_CLASS_INFO + "Erreur lors de l'arret du scheduler ", e);
      }
    }
  }

  /**
   * Interrupt a Job
   * @param pJobName the job Name
   */
  public void interruptJob(String pJobName)
  {
    try
    {
      if (pJobName != null)
      {
        _sched.interrupt(pJobName, IMMEDIATE_JOB_GROUP);
      }
    }
    catch (UnableToInterruptJobException e)
    {
      LOGGER.warn(DEBUG_CLASS_INFO + "Refus d'interruption du job " + pJobName, e);
    }
  }

  /**
   * Interrupt all Jobs
   */
  public void interruptAllJobs()
  {
    String[] listJobs = null;
    try
    {
      if ((_sched != null) && (!_sched.isShutdown()))
      {
        listJobs = _sched.getJobNames(IMMEDIATE_JOB_GROUP);
      }
    }
    catch (SchedulerException e)
    {
      LOGGER.error(DEBUG_CLASS_INFO + "Erreur lors de la récupération des jobs ", e);
    }
    int i = 0;
    while ((listJobs != null) && (i < listJobs.length))
    {
      try
      {
        LOGGER.debug(DEBUG_CLASS_INFO + "Interruption du job " + listJobs[i]);
        _sched.interrupt(listJobs[i], IMMEDIATE_JOB_GROUP);
      }
      catch (Exception e)
      {
        LOGGER.error(DEBUG_CLASS_INFO + "Erreur lors de l'interruption du job " + listJobs[i], e);
      }
      i++;
    }
  }

  /**
   * Convert trigger state to display string
   * @param pValue : the trigger state
   * @return the string representation of this trigger state
   */
  private static String triggerStateToString(int pValue)
  {
    String result;
    if (pValue == Trigger.STATE_BLOCKED)
    {
      result = "blocked";
    }
    else if (pValue == Trigger.STATE_COMPLETE)
    {
      result = "complete";
    }
    else if (pValue == Trigger.STATE_ERROR)
    {
      result = "error";
    }
    else if (pValue == Trigger.STATE_NONE)
    {
      result = "none";
    }
    else if (pValue == Trigger.STATE_NORMAL)
    {
      result = "normal";
    }
    else if (pValue == Trigger.STATE_PAUSED)
    {
      result = "paused";
    }
    else
    {
      result = "unknown";
    }
    return result;
  }

  public List<JobDetail> getExecutingJobScheculedList() throws SchedulerException
  {
    List<JobDetail> result = new ArrayList();
    List<JobExecutionContext> jobCtxList = _sched.getCurrentlyExecutingJobs();
    for (JobExecutionContext jobCtx: jobCtxList)
    {
      JobDetail jobDetail = jobCtx.getJobDetail();
      if (SCHEDULED_JOB_GROUP.equals(jobDetail.getGroup()))
      {
        result.add(jobDetail);
      }
    }
    return result;
  }

  public SchedulerMetaData getMetaData()
  {
    try
    {
      return _sched.getMetaData();
    }
    catch (SchedulerException e)
    {
      return null;
    }
  }

  public synchronized void addJobExecuted(String pJobName)
  {
    if (_jobWasExecutedMap.size() > 100)
    {
      _jobWasExecutedMap = new HashMap();
    }
    _jobWasExecutedMap.put(pJobName, "OK");
  }
}
