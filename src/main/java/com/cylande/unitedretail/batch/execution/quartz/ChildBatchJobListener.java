package com.cylande.unitedretail.batch.execution.quartz;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cylande.unitedretail.batch.batch.BatchQueueManager;
import com.cylande.unitedretail.batch.execution.EUJobParams;
import com.cylande.unitedretail.message.batch.BatchType;

public class ChildBatchJobListener implements org.quartz.JobListener
{
  private static final Logger LOGGER = Logger.getLogger(ChildBatchJobListener.class);

  public String getName()
  {
    return ChildBatchJobListener.class.getSimpleName();
  }

  public void jobExecutionVetoed(JobExecutionContext pContext)
  {
  }

  public void jobToBeExecuted(JobExecutionContext pContext)
  {
  }

  public void jobWasExecuted(JobExecutionContext pContext, JobExecutionException pException)
  {
    JobDetail job = pContext.getJobDetail();
    BatchType batchDef = (BatchType)job.getJobDataMap().get(EUJobParams.BATCH_DEFINITION_KEY.getName());
    String jobName = job.getName();
    LOGGER.info(" [ChildBatchJobListener] Job STATEQUEUE " + jobName + " is finished");
    // on doit forcer la suppression malgrès tout car le job peut encore exister à ce niveau
    JobManager.getInstance().deleteJob(jobName);
    BatchQueueManager queueManager = new BatchQueueManager();
    String activeDomain = (String)job.getJobDataMap().get(EUJobParams.ACTIVE_DOMAIN_KEY);
    String alternativeDomain = (String)job.getJobDataMap().get(EUJobParams.ALT_DOMAIN_KEY);
    String queueName = queueManager.getQueueName(batchDef.getName(), batchDef.getQueueType(), activeDomain, alternativeDomain);
    queueManager.next(queueName);
  }
}
