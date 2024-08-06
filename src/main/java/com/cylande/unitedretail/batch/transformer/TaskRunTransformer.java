package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.TaskRunViewRow;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.message.batch.ContentInfoType;
import com.cylande.unitedretail.message.batch.PathFileProviderListType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Transformer pour TaskRun
 */
public final class TaskRunTransformer
{
  /**
   * Constructor
   * Privé car toutes les méthodes sont statics
   */
  private TaskRunTransformer()
  {
  }

  /**
   * Crée un TaskRunType à partir d'un TaskRunViewRow
   * @param pRow
   * @param pContext
   * @return résultat
   */
  public static TaskRunType toBean(TaskRunViewRow pRow, ContextType pContext)
  {
    TaskRunType result = null;
    if (pRow != null)
    {
      result = new TaskRunType();
      // PATH
      result.setPath(pRow.getPath());
      // ID
      result.setId(pRow.getId().intValue());
      // START TIME
      if (pRow.getStartTime() != null)
      {
        result.setStartTime(pRow.getStartTime().toCalendar());
      }
      // END TIME
      if (pRow.getEndTime() != null)
      {
        result.setEndTime(pRow.getEndTime().toCalendar());
      }
      // STATE
      if (pRow.getStatus() != null)
      {
        result.setStatus(pRow.getStatus().booleanValue());
      }
      // WORKLOAD
      if (pRow.getWorkLoad() != null)
      {
        result.setWorkLoad(pRow.getWorkLoad().intValue());
      }
      // STEP
      if (pRow.getStep() != null)
      {
        result.setStep(pRow.getStep());
      }
      // WORKPROGRESS
      if (pRow.getWorkProgress() != null)
      {
        result.setWorkProgress(pRow.getWorkProgress().intValue());
      }
      // PARENTID
      if (pRow.getParentId() != null)
      {
        result.setParentId(pRow.getParentId().intValue());
      }
      // DOMAIN
      if (pRow.getDomain() != null)
      {
        result.setDomain(pRow.getDomain());
      }
      // SITE
      if (pRow.getSiteCode() != null)
      {
        result.setSite(SiteUtils.getSiteFromCode(pRow.getSiteCode(), pContext));
      }
      // TASKTYPE
      if (pRow.getTaskType() != null)
      {
        result.setTaskType(pRow.getTaskType());
      }
      // PROCESS NAME
      ContentInfoType processInfo = new ContentInfoType();
      if (pRow.getProcessName() != null)
      {
        processInfo.setName(pRow.getProcessName());
      }
      // PROCESS DOMAIN
      if (pRow.getProcessDomain() != null)
      {
        processInfo.setDomain(pRow.getProcessDomain());
      }
      result.setProcessInfo(processInfo);
      // INPUT NAME
      PathFileProviderListType inputProviderInfo = new PathFileProviderListType();
      if (pRow.getInputName() != null)
      {
        inputProviderInfo.setName(pRow.getInputName());
      }
      // INPUT DOMAIN
      if (pRow.getInputDomain() != null)
      {
        inputProviderInfo.setDomain(pRow.getInputDomain());
      }
      if (inputProviderInfo.getName() != null || inputProviderInfo.getDomain() != null)
      {
        result.setInputProvider(inputProviderInfo);
      }
      // RESPONSE NAME
      PathFileProviderListType responseProviderInfo = new PathFileProviderListType();
      if (pRow.getResponseName() != null)
      {
        responseProviderInfo.setName(pRow.getResponseName());
      }
      // RESPONSE DOMAIN
      if (pRow.getResponseDomain() != null)
      {
        responseProviderInfo.setDomain(pRow.getResponseDomain());
      }
      if (responseProviderInfo.getName() != null || responseProviderInfo.getDomain() != null)
      {
        result.setResponseProvider(responseProviderInfo);
      }
      // REJECT NAME
      PathFileProviderListType rejectProviderInfo = new PathFileProviderListType();
      if (pRow.getRejectName() != null)
      {
        rejectProviderInfo.setName(pRow.getRejectName());
      }
      // REJECT DOMAIN
      if (pRow.getRejectDomain() != null)
      {
        rejectProviderInfo.setDomain(pRow.getRejectDomain());
      }
      if (rejectProviderInfo.getName() != null || rejectProviderInfo.getDomain() != null)
      {
        result.setRejectProvider(rejectProviderInfo);
      }
      //IN ERROR
      if (pRow.getInError() != null)
      {
        result.setInError(pRow.getInError().booleanValue());
      }
    }
    return result;
  }

  /**
   * Transforme une TaskRunType un TaskRunViewRow
   * @param pBean
   * @param pRow
   */
  public static void toRow(TaskRunType pBean, TaskRunViewRow pRow)
  {
    if (pBean != null && pRow != null)
    {
      // PATH
      if (pRow.getPath() == null && pBean.getPath() != null)
      {
        pRow.setPath(pBean.getPath());
      }
      // ID
      if (pRow.getId() == null && pBean.getId() != null)
      {
        pRow.setId(new Integer(pBean.getId()));
      }
      // STARTTIME
      if (pBean.getStartTime() != null)
      {
        pRow.setStartTime(new Timestamp(pBean.getStartTime()));
      }
      // STATE
      if (pBean.getStatus() != null)
      {
        pRow.setStatus(new Booleans(pBean.getStatus()));
      }
      // ENDTIME
      if (pBean.getEndTime() != null)
      {
        pRow.setEndTime(new Timestamp(pBean.getEndTime()));
      }
      // STEP
      if (pBean.getStep() != null)
      {
        pRow.setStep(pBean.getStep());
      }
      // WORKLOAD
      if (pBean.getWorkLoad() != null)
      {
        pRow.setWorkLoad(new Integer(pBean.getWorkLoad()));
      }
      // WORKPROGRESS
      if (pBean.getWorkProgress() != null)
      {
        pRow.setWorkProgress(new Integer(pBean.getWorkProgress()));
      }
      // PARENTID
      if (pBean.getParentId() != null)
      {
        pRow.setParentId(new Integer(pBean.getParentId()));
      }
      // DOMAIN
      if (pBean.getDomain() != null && !pBean.getDomain().equals(""))
      {
        pRow.setDomain(pBean.getDomain());
      }
      // Site
      if (pRow.getSiteCode() == null && pBean.getSite() != null && !pBean.getSite().getCode().equals(""))
      {
        pRow.setSiteCode(pBean.getSite().getCode());
      }
      // TASKTYPE
      if (pBean.getTaskType() != null && !pBean.getTaskType().equals(""))
      {
        pRow.setTaskType(pBean.getTaskType());
      }
      // PROCESS INFO
      if (pBean.getProcessInfo() != null)
      {
        // PROCESS NAME
        if (pBean.getProcessInfo().getName() != null && !pBean.getProcessInfo().getName().equals(""))
        {
          pRow.setProcessName(pBean.getProcessInfo().getName());
        }
        // PROCESS DOMAIN
        if (pBean.getProcessInfo().getDomain() != null && !pBean.getProcessInfo().getDomain().equals(""))
        {
          pRow.setProcessDomain(pBean.getProcessInfo().getDomain());
        }
      }
      // INPUT INFO
      if (pBean.getInputProvider() != null)
      {
        // INPUT NAME
        if (pBean.getInputProvider().getName() != null && !pBean.getInputProvider().getName().equals(""))
        {
          pRow.setInputName(pBean.getInputProvider().getName());
        }
        // INPUT DOMAIN
        if (pBean.getInputProvider().getDomain() != null && !pBean.getInputProvider().getDomain().equals(""))
        {
          pRow.setInputDomain(pBean.getInputProvider().getDomain());
        }
      }
      // RESPONSE INFO
      if (pBean.getResponseProvider() != null)
      {
        // RESPONSE NAME
        if (pBean.getResponseProvider().getName() != null && !pBean.getResponseProvider().getName().equals(""))
        {
          pRow.setResponseName(pBean.getResponseProvider().getName());
        }
        // RESPONSE DOMAIN
        if (pBean.getResponseProvider().getDomain() != null && !pBean.getResponseProvider().getDomain().equals(""))
        {
          pRow.setResponseDomain(pBean.getResponseProvider().getDomain());
        }
      }
      // REJECT INFO
      if (pBean.getRejectProvider() != null)
      {
        // REJECT NAME
        if (pBean.getRejectProvider().getName() != null && !pBean.getRejectProvider().getName().equals(""))
        {
          pRow.setRejectName(pBean.getRejectProvider().getName());
        }
        // REJECT DOMAIN
        if (pBean.getRejectProvider().getDomain() != null && !pBean.getRejectProvider().getDomain().equals(""))
        {
          pRow.setRejectDomain(pBean.getRejectProvider().getDomain());
        }
      }
    }
  }
}
