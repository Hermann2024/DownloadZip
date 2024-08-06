package com.cylande.unitedretail.batch.task;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.service.TaskManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.TaskManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.batch.TaskKeyType;
import com.cylande.unitedretail.message.batch.TaskType;

public class TaskLoader
{
  public TaskLoader()
  {
  }

  /**
   * Load the batch definition
   * @return the batch definition
   * @throws EUBuildException if an error occure while retrieving batch définition
   */
  public static TaskType loadTaskDef(String pTaskName) throws EUBuildException
  {
    TaskType result = null;
    // REMARQUE : path chaine vide autorisé
    if (pTaskName == null)
    {
      throw new EUBuildException(BatchErrorDetail.LOAD_TASK_DEF_ERR, new Object[] { null });
    }
    TaskKeyType key = new TaskKeyType();
    key.setName(pTaskName);
    TaskManagerService taskMgr = new TaskManagerServiceImpl();
    try
    {
      result = taskMgr.getTask(key, null, ContextTransformer.fromLocale());
    }
    catch (Exception e)
    {
      throw new EUBuildException(BatchErrorDetail.LOAD_TASK_DEF_ERR, new Object[] { pTaskName });
    }
    if (result == null)
    {
      // le batch n'est pas défini
      throw new EUBuildException(BatchErrorDetail.LOAD_TASK_DEF_ERR, new Object[] { pTaskName });
    }
    return result;
  }
}
