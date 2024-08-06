package com.cylande.unitedretail.batch.execution.quartz;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class BatchJobListener implements org.quartz.JobListener
{

  public String getName()
  {
    return BatchJobListener.class.getSimpleName();
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
    String jobName = job.getName();
    JobManager.getInstance().addJobExecuted(jobName);
  }
}
