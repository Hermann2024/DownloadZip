package com.cylande.unitedretail.batch.task;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.message.batch.DISPATCHER;
import com.cylande.unitedretail.message.batch.EXTRACTION;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.THREADPOOLED;
import com.cylande.unitedretail.message.batch.TaskType;

/**
 * Factory of tasks : use task definition to dynamically instancie task's implementation class
 */
public class TaskFactory
{
  /**
   * Création d'une tâche
   * @param pParentBatch parent batch
   * @param pTaskDef task definition
   * @param pDomain execution domain of this task
   * @param pFailOnError error comportement
   * @return a task instance
   * @throws EUBuildException si on ne parvient pas à construire la tâche
   */
  public static AbstractTask create(AbstractBatch pParentBatch, TaskType pTaskDef, String pDomain, String pAlternativeDomain, Boolean pFailOnError) throws EUBuildException
  {
    AbstractTask result = null;
    try
    {
      if (pTaskDef instanceof INTEGRATION)
      {
        result = createINTEGRATIONTask(pParentBatch, pTaskDef, pDomain, pAlternativeDomain, pFailOnError);
      }
      else if (pTaskDef instanceof EXTRACTION)
      {
        result = createEXTRACTIONTask(pParentBatch, pTaskDef, pDomain, pAlternativeDomain, pFailOnError);
      }
      else
      {
        // gestion des cas de taskdef non valide
        // type concret de task non supporté et null
        String taskClassName = "null";
        if (pTaskDef != null)
        {
          taskClassName = pTaskDef.getClass().getName();
        }
        throw new EUBuildException(BatchErrorDetail.UNSUPORTED_TASKTYPE, new Object[] { taskClassName });
      }
    }
    catch (Exception e)
    {
      String taskName = null;
      if (pTaskDef != null)
      {
        taskName = pTaskDef.getName();
      }
      // Dump Exception stack in log subsystem
      throw new EUBuildException(BatchErrorDetail.LOAD_TASK_ERR, new Object[] { taskName }, e);
    }
    return result;
  }

  /**
   * Instancie une tâche d'intégration
   * @param pParentBatch parent batch
   * @param pTaskDef task definition
   * @param pDomain execution domain of this task
   * @param pFailOnError error comportement
   * @return an integration task instance
   * @throws TaskException si l'instanciation de la tâche a échoué
   */
  private static AbstractTask createINTEGRATIONTask(AbstractBatch pParentBatch, TaskType pTaskDef, String pDomain, String pAlternativeDomain, Boolean pFailOnError) throws TaskException
  {
    AbstractTask result = null;
    // en fonction de l'option de recyclage
    if (isUnitReject((INTEGRATION)pTaskDef))
    {
      // task avec retry unitaire sur les paquets rejetés
      if (pTaskDef instanceof DISPATCHER)
      {
        result = new TaskIntegrationDispatchImpl(pParentBatch, (INTEGRATION)pTaskDef);
        pParentBatch.setDispatcher((TaskIntegrationDispatchImpl)result);
      }
      else if (pTaskDef instanceof THREADPOOLED)
      {
        result = new TaskIntegrationUnitRejectThreadPooled(pParentBatch, (INTEGRATION)pTaskDef);
      }
      else
      {
        // task avec retry unitaire sur les paquets rejetés
        result = new TaskIntegrationUnitReject(pParentBatch, (INTEGRATION)pTaskDef);
      }
    }
    else
    {
      if (pTaskDef instanceof DISPATCHER)
      {
        result = new TaskIntegrationDispatchImpl(pParentBatch, (INTEGRATION)pTaskDef);
        pParentBatch.setDispatcher((TaskIntegrationDispatchImpl)result);
      }
      else if (pTaskDef instanceof THREADPOOLED)
      {
        result = new TaskIntegrationThreadPooledImpl(pParentBatch, (INTEGRATION)pTaskDef);
      }
      else
      {
        // task sans retry unitaire sur les paquets rejetés
        result = new TaskIntegrationImpl(pParentBatch, (INTEGRATION)pTaskDef);
      }
    }
    // pas de controle de nullité, result est forcement instancié
    result.setFailOnError(pFailOnError);
    result.setDomain(pDomain);
    result.setAlternativeDomain(pAlternativeDomain);
    return result;
  }

  /**
   * Instancie une tâche d'extraction
   * @param pParentBatch parent batch
   * @param pTaskDef task definition
   * @param pDomain execution domain of this task
   * @param pFailOnError error comportement
   * @return a extraction task instance
   * @throws TaskException si l'instanciation de la tâche a échoué
   */
  private static AbstractTask createEXTRACTIONTask(AbstractBatch pParentBatch, TaskType pTaskDef, String pDomain, String pAlternativeDomain, Boolean pFailOnError) throws TaskException
  {
    AbstractTask result = new TaskExtractionImpl(pParentBatch, (EXTRACTION)pTaskDef);
    // pas de controle de nullité, result est forcement instancié
    result.setFailOnError(pFailOnError);
    result.setDomain(pDomain);
    result.setAlternativeDomain(pAlternativeDomain);
    return result;
  }

  /**
   * Détermine si la tâche est une tâche qui ne rejette que des lignes et non des paquets
   * @param pTaskDef définition de la tâche
   * @return true si la tâche doit essayer d'intégrer les paquets rejetés ligne par ligne
   */
  private static boolean isUnitReject(INTEGRATION pTaskDef)
  {
    return pTaskDef.isUnitReject() == null ? false : pTaskDef.isUnitReject();
  }
}
