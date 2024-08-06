package com.cylande.unitedretail.batch.execution;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;

import com.cylande.unitedretail.batch.batch.BatchQueueManager;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.exception.JobControlException;
import com.cylande.unitedretail.batch.execution.quartz.JobManager;
import com.cylande.unitedretail.message.batch.BatchType;

public class EURootJobRunnable implements Runnable
{
  private static final Logger LOGGER = Logger.getLogger(EURootJobRunnable.class);
  private EUJobManager _manager;
  private String _batchName;
  private EUJobParams _jobParams;
  private boolean _exclusive;

  public EURootJobRunnable(EUJobManager pManager, String pBatchName, EUJobParams pJobParams, boolean pExclusive)
  {
    _manager = pManager;
    _batchName = pBatchName;
    _jobParams = pJobParams;
    _exclusive = pExclusive;
  }

  public void run()
  {
    String jobName = "Batch-" + _batchName + (_exclusive ? "" : '_' + JobManager.getNewUUID());
    try
    {
      JobDetail rootJob = JobManager.getInstance().createJob(jobName, EUJob.class, _jobParams, true, _manager.getLaunchDelay(), true, null);
      String rootJobName = rootJob.getName();
      while (!_manager.isEnded(rootJobName))
      {
        _manager.pauseThread();
      }
    }
    catch (JobControlException jce)
    {
      Exception e;
      // inutile d'afficher la trace complète de l'erreur quand le moteur est verrouillé
      if (!jce.getCanonicalCode().equals(BatchErrorDetail.LOCKED_BATCH_EXECUTION.getCanonicalCode()))
      {
        if (jce.getCanonicalCode().equals(BatchErrorDetail.SCHEDULER_STATEFULLJOB_RUNNING.getCanonicalCode()))
        {
          e = new EULaunchException(BatchErrorDetail.BATCH_EXCLUSION_ERR, new Object[] { _batchName }, jce);
        }
        else
        {
          e = new EULaunchException(BatchErrorDetail.BATCH_LAUNCH_ERR, new Object[] { _batchName }, jce);
        }
        LOGGER.error(e, e);
      }
    }
    LOGGER.info(" [EURootJobRunnable] Job STATEQUEUE " + jobName + " is finished");
    BatchQueueManager queueManager = new BatchQueueManager();
    BatchType batchDef = (BatchType)_jobParams.get(EUJobParams.BATCH_DEFINITION_KEY);
    String activeDomain = (String)_jobParams.get(EUJobParams.ACTIVE_DOMAIN_KEY);
    String alternativeDomain = (String)_jobParams.get(EUJobParams.ALT_DOMAIN_KEY);
    String queueName = queueManager.getQueueName(_batchName, batchDef.getQueueType(), activeDomain, alternativeDomain);
    queueManager.next(queueName);
  }
}
