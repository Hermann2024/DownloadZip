package com.cylande.unitedretail.batch.execution.quartz;

import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;

import com.cylande.unitedretail.batch.batch.BatchQueueManager;
import com.cylande.unitedretail.batch.exception.JobControlException;
import com.cylande.unitedretail.batch.execution.EUJobParams;
import com.cylande.unitedretail.message.batch.BatchEnum;
import com.cylande.unitedretail.message.batch.BatchType;

/**
 * Job generique pour les jobs déclenchés par trigger Cron (mode Stateless)
 */
public class TriggerJob implements InterruptableJob
{
  private static final Logger LOGGER = Logger.getLogger(TriggerJob.class);
  /** Le nom du job enfant */
  private String _childJobName = null;

  /**
   * Constructeur
   */
  public TriggerJob()
  {
  }

  /**
   * Methode appelée par le Scheduler Quartz en cas de demande d'interruption
   * @implements InterruptableJob
   */
  public void interrupt()
  {
    JobManager jobMgr = JobManager.getInstance();
    jobMgr.interruptJob(_childJobName);
  }

  /**
   * Exécution du job Quartz.
   * @param pJobExecutionContext contexte d'exécution quartz
   * @implements InterruptableJob
   */
  public void execute(JobExecutionContext pJobExecutionContext)
  {
    JobManager jobMgr = JobManager.getInstance();
    JobDataMap params = pJobExecutionContext.getJobDetail().getJobDataMap();
    _childJobName = (String)params.get(TriggerJobParams.TRIGGER_JOB_NAME_KEY.getName());
    Class jobClass = (Class)params.get(TriggerJobParams.TRIGGER_JOB_CLASS_KEY.getName());
    JobParamMap jobParams = (JobParamMap)params.get(TriggerJobParams.TRIGGER_JOB_PARAMS_KEY.getName());
    BatchType batchDef = (BatchType)jobParams.get(EUJobParams.BATCH_DEFINITION_KEY);
    BatchQueueManager queueManager = new BatchQueueManager();
    String activeDomain = (String)jobParams.get(EUJobParams.ACTIVE_DOMAIN_KEY);
    String alternativeDomain = (String)jobParams.get(EUJobParams.ALT_DOMAIN_KEY);
    String queueName = queueManager.getQueueName(batchDef.getName(), batchDef.getQueueType(), activeDomain, alternativeDomain);
    if (batchDef.getType() == BatchEnum.STATEQUEUE && !queueManager.blockIfFree(queueName, batchDef.getQueueType()))
    {
      // le batch est mis en file d'attente
      queueManager.add(queueName, activeDomain, alternativeDomain, pJobExecutionContext);
      return;
    }
    long launchDelay = ((Long)params.get(TriggerJobParams.TRIGGER_JOB_LAUNCH_DELAY_KEY.getName())).longValue();
    boolean bJobComplete = false;
    JobDetail childJob = null;
    try
    {
      boolean bExclusive = (this instanceof StatefulJob);
      childJob = jobMgr.createJob(_childJobName, jobClass, jobParams, bExclusive, launchDelay, false, null);
      _childJobName = childJob.getName();
    }
    catch (JobControlException e)
    {
      e.log();
      bJobComplete = true;
    }
    while (!bJobComplete)
    {
      try
      {
        Thread.sleep(5);
        childJob = jobMgr.getJob(_childJobName);
        bJobComplete = "ended".equals(childJob.getJobDataMap().get("state"));
      }
      catch (InterruptedException e)
      {
        jobMgr.interruptJob(_childJobName);
      }
    }
    jobMgr.deleteJob(_childJobName);
    if (batchDef.getType() == BatchEnum.STATEQUEUE)
    {
      LOGGER.info(" [TriggerJob] Job STATEQUEUE " + _childJobName + " is finished");
      queueManager.next(queueName);
    }
  }
}
