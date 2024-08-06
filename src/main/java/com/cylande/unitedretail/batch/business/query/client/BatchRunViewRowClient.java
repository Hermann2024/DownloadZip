package com.cylande.unitedretail.batch.business.query.client;

import com.cylande.unitedretail.batch.business.query.common.BatchRunViewRow;
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
public class BatchRunViewRowClient extends RowImpl implements BatchRunViewRow
{
  /**This is the default constructor (do not remove)
   */
  public BatchRunViewRowClient()
  {
  }

  public RowIterator getBatchRunChildrenView()
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"getBatchRunChildrenView",null,null);
    return (RowIterator)_ret;
  }

  public RowIterator getTaskRunChildrenView()
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"getTaskRunChildrenView",null,null);
    return (RowIterator)_ret;
  }

  public String getBatchMode()
  {
    return (String)getAttribute("BatchMode");
  }

  public String getBatchType()
  {
    return (String)getAttribute("BatchType");
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

  public void setBatchMode(String value)
  {
    setAttribute("BatchMode", value);
  }

  public void setBatchType(String value)
  {
    setAttribute("BatchType", value);
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

  public void setParentId(Integer value)
  {
    setAttribute("ParentId", value);
  }

  public void setPath(String value)
  {
    setAttribute("Path", value);
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

  public Booleans getInError()
  {
    return (Booleans)getAttribute("InError");
  }

  public void setInError(Booleans value)
  {
    setAttribute("InError", value);
  }
}