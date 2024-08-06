package com.cylande.unitedretail.batch.execution.quartz;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUExecutionException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;

/**
 * Trigger for immediate Jobs
 * Override Quartz simple Trigger to put somes informations in dataMap at batch end.
 */
public class SimpleJobTrigger extends SimpleTrigger
{
  /**
   * Constructor
   * @param pJobName  : the job name wich is associated with this trigger.
   * @param pJobGroup : the group for this trigger
   */
  public SimpleJobTrigger(String pJobName, String pJobGroup)
  {
    super(pJobName, pJobGroup, 0, 0);
  }

  /**
   * Operation processed when job ended.
   * @param pContext   : the quartz execution context
   * @param pException : the quartz job exception (if any occured)
   * @return quartz execution results
   */
  public int executionComplete(JobExecutionContext pContext, JobExecutionException pException)
  {
    int result = super.executionComplete(pContext, pException);
    Object exception = this.getJobDataMap().get("exception");
    if (exception != null)
    {
      pContext.getJobDetail().getJobDataMap().put("exception", exception);
    }
    else
    {
      if (pException != null)
      {
        exception = new EUExecutionException(BatchErrorDetail.SCHEDULER_JOB_EXECUTION, new Object[] { pContext.getJobDetail().getName() }, pException);
        pContext.getJobDetail().getJobDataMap().put("exception", exception);
      }
    }
    pContext.getJobDetail().getJobDataMap().put("state", "ended");
    return result;
  }
}
