package com.cylande.unitedretail.batch.batch;


public class BatchQueueRunnable implements Runnable
{
  private String _queueName;
  private String _eltId;

  public BatchQueueRunnable(String pQueueName, String pElementId)
  {
    _queueName = pQueueName;
    _eltId = pElementId;
  }

  public void run()
  {
    new BatchQueueManager().launch(_queueName, _eltId);
  }
}
