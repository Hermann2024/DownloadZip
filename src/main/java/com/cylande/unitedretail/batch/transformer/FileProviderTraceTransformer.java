package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.FileProviderTraceViewRow;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 */
public final class FileProviderTraceTransformer
{
  /**
   * Constructor
   * Privé car toutes les méthodes sont statics
   */
  private FileProviderTraceTransformer()
  {
  }

  /**
   * Crée un bean TaskAuditType à partir d'un row TaskAuditViewRow
   * @param pRow
   * @param pContext
   * @return résultat
   */
  public static FileProviderTraceType toBean(FileProviderTraceViewRow pRow, ContextType pContext)
  {
    FileProviderTraceType result = null;
    if (pRow != null)
    {
      result = new FileProviderTraceType();
      result.setId(pRow.getID().intValue());
      result.setTaskId(pRow.getTaskId().intValue());
      result.setTaskCode(pRow.getTaskCode());
      if (pRow.getSiteCode() != null)
      {
        result.setSite(SiteUtils.getSiteFromCode(pRow.getSiteCode(), pContext));
      }
      if (pRow.getDomain() != null)
      {
        result.setDomain(pRow.getDomain());
      }
      if (pRow.getProviderName() != null)
      {
        result.setProviderName(pRow.getProviderName());
      }
      if (pRow.getFilePath() != null)
      {
        result.setFilePath(pRow.getFilePath());
      }
      if (pRow.getFileName() != null)
      {
        result.setFileName(pRow.getFileName());
      }
      if (pRow.getInError() != null)
      {
        result.setInError(pRow.getInError().booleanValue());
      }
    }
    return result;
  }

  /**
   * Transforme un TaskAuditType en TaskAuditViewRow
   * @param pBean
   * @param pRow
   */
  public static void toRow(FileProviderTraceType pBean, FileProviderTraceViewRow pRow)
  {
    if (pBean != null && pRow != null)
    {
      if (pRow.getTaskId() == null && pBean.getTaskId() != null)
      {
        pRow.setTaskId(new Integer(pBean.getTaskId()));
      }
      if (pRow.getTaskCode() == null && pBean.getTaskCode() != null)
      {
        pRow.setTaskCode(pBean.getTaskCode());
      }
      if (pRow.getSiteCode() == null && pBean.getSite() != null && !pBean.getSite().getCode().equals(""))
      {
        pRow.setSiteCode(pBean.getSite().getCode());
      }
      if (pRow.getDomain() == null && pBean.getDomain() != null)
      {
        pRow.setDomain(pBean.getDomain());
      }
      if (pBean.getProviderName() != null)
      {
        pRow.setProviderName(pBean.getProviderName());
      }
      if (pBean.getFilePath() != null)
      {
        pRow.setFilePath(pBean.getFilePath());
      }
      if (pBean.getFileName() != null)
      {
        pRow.setFileName(pBean.getFileName());
      }
      if (pBean.getInError() != null)
      {
        pRow.setInError(new Booleans(pBean.getInError()));
      }
    }
  }
}
