package com.cylande.unitedretail.batch.execution.quartz;

public class TriggerJobParams extends JobParamMap
{
  public static final JobParamKey TRIGGER_JOB_NAME_KEY = new JobParamKey("TRIGGER_JOB_NAME", true);
  public static final JobParamKey TRIGGER_JOB_CLASS_KEY = new JobParamKey("TRIGGER_JOB_CLASS", true);
  public static final JobParamKey TRIGGER_JOB_PARAMS_KEY = new JobParamKey("TRIGGER_JOB_PARAMS", true);
  public static final JobParamKey TRIGGER_JOB_LAUNCH_DELAY_KEY = new JobParamKey("TRIGGER_JOB_LAUNCH_DELAY", true);

  public TriggerJobParams()
  {
  }
}
