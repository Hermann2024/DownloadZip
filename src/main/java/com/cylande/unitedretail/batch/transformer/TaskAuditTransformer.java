package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.TaskAuditViewRow;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Transformer pour TaskAudit
 */
public final class TaskAuditTransformer
{
  /**
   * Constructor
   * Privé car toutes les méthodes sont statics
   */
  private TaskAuditTransformer()
  {
  }

  /**
   * Crée un bean TaskAuditType à partir d'un row TaskAuditViewRow
   * @param pRow
   * @param pContext
   * @return résultat
   */
  public static TaskAuditType toBean(TaskAuditViewRow pRow, ContextType pContext)
  {
    TaskAuditType result = null;
    if (pRow != null)
    {
      result = new TaskAuditType();
      result.setPath(pRow.getPath());
      result.setTask(pRow.getTask().intValue());
      result.setId(pRow.getId().intValue());
      if (pRow.getEventTime() != null)
      {
        result.setEventTime(pRow.getEventTime().toCalendar());
      }
      if (pRow.getErrorCode() != null)
      {
        result.setErrorCode(pRow.getErrorCode());
      }
      if (pRow.getErrorMessage() != null)
      {
        result.setErrorMessage(pRow.getErrorMessage());
      }
      if (pRow.getSiteCode() != null)
      {
        result.setSite(SiteUtils.getSiteFromCode(pRow.getSiteCode(), pContext));
      }
      if (pRow.getFileId() != null)
      {
        result.setFileId(pRow.getFileId().intValue());
      }
    }
    return result;
  }

  /**
   * Transforme un TaskAuditType en TaskAuditViewRow
   * @param pBean
   * @param pRow
   */
  public static void toRow(TaskAuditType pBean, TaskAuditViewRow pRow)
  {
    if (pBean != null && pRow != null)
    {
      if (pRow.getPath() == null && pBean.getPath() != null)
      {
        pRow.setPath(pBean.getPath());
      }
      if (pRow.getId() == null && pBean.getId() != null)
      {
        pRow.setId(new Integer(pBean.getTask()));
      }
      if (pRow.getTask() == null && pBean.getTask() != null)
      {
        pRow.setTask(new Integer(pBean.getTask()));
      }
      if (pBean.getEventTime() != null)
      {
        pRow.setEventTime(new Timestamp(pBean.getEventTime()));
      }
      if (pBean.getErrorCode() != null)
      {
        pRow.setErrorCode(pBean.getErrorCode());
      }
      if (pBean.getErrorMessage() != null)
      {
        pRow.setErrorMessage(pBean.getErrorMessage());
      }
      if (pRow.getSiteCode() == null && pBean.getSite() != null && !pBean.getSite().getCode().equals(""))
      {
        pRow.setSiteCode(pBean.getSite().getCode());
      }
      if (pBean.getFileId() != null)
      {
        pRow.setFileId(new Integer(pBean.getFileId().intValue()));
      }
    }
  }
}
