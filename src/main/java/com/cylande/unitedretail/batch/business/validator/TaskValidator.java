package com.cylande.unitedretail.batch.business.validator;

import com.cylande.unitedretail.batch.service.TaskRunManagerServiceDelegate;
import com.cylande.unitedretail.framework.business.BusinessException;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.TaskRunKeyType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;

import org.apache.log4j.Logger;
/**
 * Implémentation du validator sur les Tasks.
 * @version 1.0
 * @since 01/04/2008
 */
public class TaskValidator
{
  private static final Logger LOGGER = Logger.getLogger(TaskValidator.class);

  /**
   * Validation de l'existance d'une Task
   * @param pTaskId instance de la Task
   * @return existance de la Task
   */
  public static boolean isExisting(String pPathId, Integer pTaskId, ContextType pContext)
  {
    boolean result = false;
    if (pTaskId == null)
    {
      result = true;
    }
    else
    {
      TaskRunKeyType myKey = new TaskRunKeyType();
      myKey.setId(pTaskId.intValue());
      myKey.setPath(pPathId);
      try
      {
        TaskRunManagerServiceDelegate taskRunService = new TaskRunManagerServiceDelegate();
        TaskRunType myTaskRun = taskRunService.getTaskRun(myKey, null, pContext);
        if (myTaskRun != null)
        {
          result = true;
        }
      }
      catch (ServiceException e)
      {
        LOGGER.error(e.getMessage());
        throw new BusinessException(500000, e.getMessage());
      }
    }
    return result;
  }
}
