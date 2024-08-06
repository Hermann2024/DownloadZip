package com.cylande.unitedretail.batch.business.query.client;

import com.cylande.unitedretail.batch.business.query.common.TaskRunViewRow;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;

import oracle.jbo.RowIterator;
import oracle.jbo.client.remote.RowImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class TaskRunViewRowClient extends RowImpl implements TaskRunViewRow
{
  /**This is the default constructor (do not remove)
   */
  public TaskRunViewRowClient()
  {
  }

  public RowIterator getFileProviderTraceView()
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"getFileProviderTraceView",null,null);
    return (RowIterator)_ret;
  }

  public RowIterator getTaskAuditView()
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"getTaskAuditView",null,null);
    return (RowIterator)_ret;
  }

  public Timestamp getCreationTime()
  {
    return (Timestamp)getAttribute("CreationTime");
  }

  public String getCreationUserCode()
  {
    return (String)getAttribute("CreationUserCode");
  }

  public String getDomain()
  {
    return (String)getAttribute("Domain");
  }

  public Timestamp getEndTime()
  {
    return (Timestamp)getAttribute("EndTime");
  }

  public Integer getId()
  {
    return (Integer)getAttribute("Id");
  }

  public Booleans getInError()
  {
    return (Booleans)getAttribute("InError");
  }

  public String getInputDomain()
  {
    return (String)getAttribute("InputDomain");
  }

  public String getInputName()
  {
    return (String)getAttribute("InputName");
  }

  public Timestamp getModificationTime()
  {
    return (Timestamp)getAttribute("ModificationTime");
  }

  public String getModificationUserCode()
  {
    return (String)getAttribute("ModificationUserCode");
  }

  public Integer getParentId()
  {
    return (Integer)getAttribute("ParentId");
  }

  public String getPath()
  {
    return (String)getAttribute("Path");
  }

  public String getProcessDomain()
  {
    return (String)getAttribute("ProcessDomain");
  }

  public String getProcessName()
  {
    return (String)getAttribute("ProcessName");
  }

  public String getRejectDomain()
  {
    return (String)getAttribute("RejectDomain");
  }

  public String getRejectName()
  {
    return (String)getAttribute("RejectName");
  }

  public String getResponseDomain()
  {
    return (String)getAttribute("ResponseDomain");
  }

  public String getResponseName()
  {
    return (String)getAttribute("ResponseName");
  }

  public String getSiteCode()
  {
    return (String)getAttribute("SiteCode");
  }

  public Timestamp getStartTime()
  {
    return (Timestamp)getAttribute("StartTime");
  }

  public Booleans getStatus()
  {
    return (Booleans)getAttribute("Status");
  }

  public String getStep()
  {
    return (String)getAttribute("Step");
  }

  public String getTaskType()
  {
    return (String)getAttribute("TaskType");
  }

  public Integer getWorkLoad()
  {
    return (Integer)getAttribute("WorkLoad");
  }

  public Integer getWorkProgress()
  {
    return (Integer)getAttribute("WorkProgress");
  }

  public void setDomain(String value)
  {
    setAttribute("Domain", value);
  }

  public void setEndTime(Timestamp value)
  {
    setAttribute("EndTime", value);
  }

  public void setId(Integer value)
  {
    setAttribute("Id", value);
  }

  public void setInError(Booleans value)
  {
    setAttribute("InError", value);
  }

  public void setInputDomain(String value)
  {
    setAttribute("InputDomain", value);
  }

  public void setInputName(String value)
  {
    setAttribute("InputName", value);
  }

  public void setParentId(Integer value)
  {
    setAttribute("ParentId", value);
  }

  public void setPath(String value)
  {
    setAttribute("Path", value);
  }

  public void setProcessDomain(String value)
  {
    setAttribute("ProcessDomain", value);
  }

  public void setProcessName(String value)
  {
    setAttribute("ProcessName", value);
  }

  public void setRejectDomain(String value)
  {
    setAttribute("RejectDomain", value);
  }

  public void setRejectName(String value)
  {
    setAttribute("RejectName", value);
  }

  public void setResponseDomain(String value)
  {
    setAttribute("ResponseDomain", value);
  }

  public void setResponseName(String value)
  {
    setAttribute("ResponseName", value);
  }

  public void setSiteCode(String value)
  {
    setAttribute("SiteCode", value);
  }

  public void setStartTime(Timestamp value)
  {
    setAttribute("StartTime", value);
  }

  public void setStatus(Booleans value)
  {
    setAttribute("Status", value);
  }

  public void setStep(String value)
  {
    setAttribute("Step", value);
  }

  public void setTaskType(String value)
  {
    setAttribute("TaskType", value);
  }

  public void setWorkLoad(Integer value)
  {
    setAttribute("WorkLoad", value);
  }

  public void setWorkProgress(Integer value)
  {
    setAttribute("WorkProgress", value);
  }
}
