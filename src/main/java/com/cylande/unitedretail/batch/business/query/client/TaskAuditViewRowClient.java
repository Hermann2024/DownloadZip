package com.cylande.unitedretail.batch.business.query.client;

import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;

import oracle.jbo.client.remote.RowImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class TaskAuditViewRowClient extends RowImpl
{
  /**This is the default constructor (do not remove)
   */
  public TaskAuditViewRowClient()
  {
  }

  public Timestamp getCreationTime()
  {
    return (Timestamp)getAttribute("CreationTime");
  }

  public String getCreationUserCode()
  {
    return (String)getAttribute("CreationUserCode");
  }

  public String getErrorCode()
  {
    return (String)getAttribute("ErrorCode");
  }

  public String getErrorMessage()
  {
    return (String)getAttribute("ErrorMessage");
  }

  public Timestamp getEventTime()
  {
    return (Timestamp)getAttribute("EventTime");
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

  public String getPath()
  {
    return (String)getAttribute("Path");
  }

  public String getSiteCode()
  {
    return (String)getAttribute("SiteCode");
  }

  public Integer getTask()
  {
    return (Integer)getAttribute("Task");
  }

  public void setErrorCode(String value)
  {
    setAttribute("ErrorCode", value);
  }

  public void setErrorMessage(String value)
  {
    setAttribute("ErrorMessage", value);
  }

  public void setEventTime(Timestamp value)
  {
    setAttribute("EventTime", value);
  }

  public void setId(Integer value)
  {
    setAttribute("Id", value);
  }

  public void setPath(String value)
  {
    setAttribute("Path", value);
  }

  public void setSiteCode(String value)
  {
    setAttribute("SiteCode", value);
  }

  public void setTask(Integer value)
  {
    setAttribute("Task", value);
  }

  public Integer getFileId()
  {
    return (Integer)getAttribute("FileId");
  }

  public void setFileId(Integer value)
  {
    setAttribute("FileId", value);
  }
}
