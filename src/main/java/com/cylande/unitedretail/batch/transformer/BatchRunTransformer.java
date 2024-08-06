package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.BatchRunViewRow;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Transformer pour BatchRun
 */
public final class BatchRunTransformer
{
  /**
   * Constructor
   * Privé car toutes les méthodes sont statics
   */
  private BatchRunTransformer()
  {
  }

  /**
   * Crée un bean BatchRunType à partir d'un row BatchRunViewRow
   * @param pRow
   * @return BatchRunType
   */
  public static BatchRunType toBean(BatchRunViewRow pRow, ContextType pContext)
  {
    BatchRunType result = null;
    if (pRow != null)
    {
      result = new BatchRunType();
      result.setPath(pRow.getPath());
      result.setId(pRow.getId().intValue());
      if (pRow.getStartTime() != null)
      {
        result.setStartTime(pRow.getStartTime().toCalendar());
      }
      if (pRow.getEndTime() != null)
      {
        result.setEndTime(pRow.getEndTime().toCalendar());
      }
      if (pRow.getStatus() != null)
      {
        result.setStatus(pRow.getStatus().booleanValue());
      }
      if (pRow.getParentId() != null)
      {
        result.setParentId(pRow.getParentId().intValue());
      }
      if (pRow.getDomain() != null)
      {
        result.setDomain(pRow.getDomain());
      }
      if (pRow.getSiteCode() != null)
      {
        result.setSite(SiteUtils.getSiteFromCode(pRow.getSiteCode(), pContext));
      }
      if (pRow.getBatchMode() != null)
      {
        result.setMode(pRow.getBatchMode());
      }
      if (pRow.getBatchType() != null)
      {
        result.setBatchType(pRow.getBatchType());
      }
      if (pRow.getInError() != null)
      {
        result.setInError(pRow.getInError().booleanValue());
      }
      if (EUJobManager.STOPPING_BATCH.containsKey(result.getId()))
      {
        result.setStopping(true);
      }
    }
    return result;
  }

  /**
   * Transforme un bean BatchRunType en BatchRunViewRow
   * @param pBean
   * @param pRow
   */
  public static void toRow(BatchRunType pBean, BatchRunViewRow pRow)
  {
    if (pBean != null && pRow != null)
    {
      if (pRow.getPath() == null && pBean.getPath() != null)
      {
        pRow.setPath(pBean.getPath());
      }
      if (pRow.getId() == null && pBean.getId() != null)
      {
        pRow.setId(new Integer(pBean.getId()));
      }
      if (pBean.getStartTime() != null)
      {
        pRow.setStartTime(new Timestamp(pBean.getStartTime()));
      }
      if (pBean.getStatus() != null)
      {
        pRow.setStatus(new Booleans(pBean.getStatus()));
      }
      if (pBean.getEndTime() != null)
      {
        pRow.setEndTime(new Timestamp(pBean.getEndTime()));
      }
      if (pBean.getParentId() != null)
      {
        pRow.setParentId(new Integer(pBean.getParentId()));
      }
      if (pBean.getDomain() != null && !pBean.getDomain().equals(""))
      {
        pRow.setDomain(pBean.getDomain());
      }
      if (pRow.getSiteCode() == null && pBean.getSite() != null && !pBean.getSite().getCode().equals(""))
      {
        pRow.setSiteCode(pBean.getSite().getCode());
      }
      if (pBean.getBatchType() != null && !pBean.getBatchType().equals(""))
      {
        pRow.setBatchType(pBean.getBatchType());
      }
      if (pBean.getMode() != null && !pBean.getMode().equals(""))
      {
        pRow.setBatchMode(pBean.getMode());
      }
      if (pBean.isInError() != null)
      {
        pRow.setInError(new Booleans(pBean.isInError()));
      }
    }
  }
}
