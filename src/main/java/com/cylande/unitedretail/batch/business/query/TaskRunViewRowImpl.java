package com.cylande.unitedretail.batch.business.query;

import com.cylande.unitedretail.batch.business.entity.TaskRunImpl;
import com.cylande.unitedretail.batch.business.query.common.TaskRunViewRow;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.framework.business.jbo.server.ViewRowImpl;

import oracle.jbo.RowIterator;
import oracle.jbo.server.AttributeDefImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class TaskRunViewRowImpl extends ViewRowImpl implements TaskRunViewRow
{
  public static final int CREATIONTIME = 0;
  public static final int CREATIONUSERCODE = 1;
  public static final int MODIFICATIONTIME = 2;
  public static final int MODIFICATIONUSERCODE = 3;
  public static final int PATH = 4;
  public static final int ID = 5;
  public static final int PARENTID = 6;
  public static final int STARTTIME = 7;
  public static final int ENDTIME = 8;
  public static final int STATUS = 9;
  public static final int WORKLOAD = 10;
  public static final int WORKPROGRESS = 11;
  public static final int STEP = 12;
  public static final int DOMAIN = 13;
  public static final int SITECODE = 14;
  public static final int TASKTYPE = 15;
  public static final int PROCESSNAME = 16;
  public static final int PROCESSDOMAIN = 17;
  public static final int INPUTNAME = 18;
  public static final int INPUTDOMAIN = 19;
  public static final int RESPONSENAME = 20;
  public static final int RESPONSEDOMAIN = 21;
  public static final int REJECTNAME = 22;
  public static final int REJECTDOMAIN = 23;
  public static final int INERROR = 24;
  public static final int FILEPROVIDERTRACEVIEW = 25;
  public static final int TASKAUDITVIEW = 26;

  /**This is the default constructor (do not remove)
   */
  public TaskRunViewRowImpl()
  {
  }

  /**Gets TaskRun entity object.
   */
  public TaskRunImpl getTaskRun()
  {
    return (TaskRunImpl)getEntity(0);
  }

  /**Gets the attribute value for CREATION_TIME using the alias name CreationTime
   */
  public Timestamp getCreationTime()
  {
    return (Timestamp)getAttributeInternal(CREATIONTIME);
  }

  /**Gets the attribute value for CREATION_USER_CODE using the alias name CreationUserCode
   */
  public String getCreationUserCode()
  {
    return (String)getAttributeInternal(CREATIONUSERCODE);
  }

  /**Gets the attribute value for MODIFICATION_TIME using the alias name ModificationTime
   */
  public Timestamp getModificationTime()
  {
    return (Timestamp)getAttributeInternal(MODIFICATIONTIME);
  }

  /**Gets the attribute value for MODIFICATION_USER_CODE using the alias name ModificationUserCode
   */
  public String getModificationUserCode()
  {
    return (String)getAttributeInternal(MODIFICATIONUSERCODE);
  }

  /**Gets the attribute value for TASK_CODE using the alias name Path
   */
  public String getPath()
  {
    return (String)getAttributeInternal(PATH);
  }

  /**Sets <code>value</code> as attribute value for TASK_CODE using the alias name Path
   */
  public void setPath(String value)
  {
    setAttributeInternal(PATH, value);
  }

  /**Gets the attribute value for ID using the alias name Id
   */
  public Integer getId()
  {
    return (Integer)getAttributeInternal(ID);
  }

  /**Sets <code>value</code> as attribute value for ID using the alias name Id
   */
  public void setId(Integer value)
  {
    setAttributeInternal(ID, value);
  }

  /**Gets the attribute value for START_TIME using the alias name StartTime
   */
  public Timestamp getStartTime()
  {
    return (Timestamp)getAttributeInternal(STARTTIME);
  }

  /**Sets <code>value</code> as attribute value for START_TIME using the alias name StartTime
   */
  public void setStartTime(Timestamp value)
  {
    setAttributeInternal(STARTTIME, value);
  }

  /**Gets the attribute value for END_TIME using the alias name EndTime
   */
  public Timestamp getEndTime()
  {
    return (Timestamp)getAttributeInternal(ENDTIME);
  }

  /**Sets <code>value</code> as attribute value for END_TIME using the alias name EndTime
   */
  public void setEndTime(Timestamp value)
  {
    setAttributeInternal(ENDTIME, value);
  }

  /**Gets the attribute value for STATE using the alias name Status
   */
  public Booleans getStatus()
  {
    return (Booleans)getAttributeInternal(STATUS);
  }

  /**Sets <code>value</code> as attribute value for STATE using the alias name Status
   */
  public void setStatus(Booleans value)
  {
    setAttributeInternal(STATUS, value);
  }

  /**Gets the attribute value for WORK_LOAD using the alias name WorkLoad
   */
  public Integer getWorkLoad()
  {
    return (Integer)getAttributeInternal(WORKLOAD);
  }

  /**Sets <code>value</code> as attribute value for WORK_LOAD using the alias name WorkLoad
   */
  public void setWorkLoad(Integer value)
  {
    setAttributeInternal(WORKLOAD, value);
  }

  /**Gets the attribute value for STEP using the alias name Step
   */
  public String getStep()
  {
    return (String)getAttributeInternal(STEP);
  }

  /**Sets <code>value</code> as attribute value for STEP using the alias name Step
   */
  public void setStep(String value)
  {
    setAttributeInternal(STEP, value);
  }

  /**getAttrInvokeAccessor: generated method. Do not modify.
   */
  protected Object getAttrInvokeAccessor(int index, AttributeDefImpl attrDef) throws Exception
  {
    switch (index)
    {
      case CREATIONTIME:
        return getCreationTime();
      case CREATIONUSERCODE:
        return getCreationUserCode();
      case MODIFICATIONTIME:
        return getModificationTime();
      case MODIFICATIONUSERCODE:
        return getModificationUserCode();
      case PATH:
        return getPath();
      case ID:
        return getId();
      case PARENTID:
        return getParentId();
      case STARTTIME:
        return getStartTime();
      case ENDTIME:
        return getEndTime();
      case STATUS:
        return getStatus();
      case WORKLOAD:
        return getWorkLoad();
      case WORKPROGRESS:
        return getWorkProgress();
      case STEP:
        return getStep();
      case DOMAIN:
        return getDomain();
      case SITECODE:
        return getSiteCode();
      case TASKTYPE:
        return getTaskType();
      case PROCESSNAME:
        return getProcessName();
      case PROCESSDOMAIN:
        return getProcessDomain();
      case INPUTNAME:
        return getInputName();
      case INPUTDOMAIN:
        return getInputDomain();
      case RESPONSENAME:
        return getResponseName();
      case RESPONSEDOMAIN:
        return getResponseDomain();
      case REJECTNAME:
        return getRejectName();
      case REJECTDOMAIN:
        return getRejectDomain();
      case INERROR:
        return getInError();
      case FILEPROVIDERTRACEVIEW:
        return getFileProviderTraceView();
      case TASKAUDITVIEW:
        return getTaskAuditView();
      default:
        return super.getAttrInvokeAccessor(index, attrDef);
    }
  }

  /**setAttrInvokeAccessor: generated method. Do not modify.
   */
  protected void setAttrInvokeAccessor(int index, Object value, AttributeDefImpl attrDef) throws Exception
  {
    switch (index)
    {
      case PATH:
        setPath((String)value);
        return;
      case ID:
        setId((Integer)value);
        return;
      case PARENTID:
        setParentId((Integer)value);
        return;
      case STARTTIME:
        setStartTime((Timestamp)value);
        return;
      case ENDTIME:
        setEndTime((Timestamp)value);
        return;
      case STATUS:
        setStatus((Booleans)value);
        return;
      case WORKLOAD:
        setWorkLoad((Integer)value);
        return;
      case WORKPROGRESS:
        setWorkProgress((Integer)value);
        return;
      case STEP:
        setStep((String)value);
        return;
      case DOMAIN:
        setDomain((String)value);
        return;
      case SITECODE:
        setSiteCode((String)value);
        return;
      case TASKTYPE:
        setTaskType((String)value);
        return;
      case PROCESSNAME:
        setProcessName((String)value);
        return;
      case PROCESSDOMAIN:
        setProcessDomain((String)value);
        return;
      case INPUTNAME:
        setInputName((String)value);
        return;
      case INPUTDOMAIN:
        setInputDomain((String)value);
        return;
      case RESPONSENAME:
        setResponseName((String)value);
        return;
      case RESPONSEDOMAIN:
        setResponseDomain((String)value);
        return;
      case REJECTNAME:
        setRejectName((String)value);
        return;
      case REJECTDOMAIN:
        setRejectDomain((String)value);
        return;
      case INERROR:
        setInError((Booleans)value);
        return;
      default:
        super.setAttrInvokeAccessor(index, value, attrDef);
        return;
    }
  }

  /**Gets the attribute value for PARENT_ID using the alias name ParentId
   */
  public Integer getParentId()
  {
    return (Integer)getAttributeInternal(PARENTID);
  }

  /**Sets <code>value</code> as attribute value for PARENT_ID using the alias name ParentId
   */
  public void setParentId(Integer value)
  {
    setAttributeInternal(PARENTID, value);
  }

  /**Gets the attribute value for ADVANCE using the alias name WorkProgress
   */
  public Integer getWorkProgress()
  {
    return (Integer)getAttributeInternal(WORKPROGRESS);
  }

  /**Sets <code>value</code> as attribute value for ADVANCE using the alias name WorkProgress
   */
  public void setWorkProgress(Integer value)
  {
    setAttributeInternal(WORKPROGRESS, value);
  }

  /**Gets the attribute value for DOMAIN using the alias name Domain
   */
  public String getDomain()
  {
    return (String)getAttributeInternal(DOMAIN);
  }

  /**Sets <code>value</code> as attribute value for DOMAIN using the alias name Domain
   */
  public void setDomain(String value)
  {
    setAttributeInternal(DOMAIN, value);
  }

  /**Gets the attribute value for SITE_CODE using the alias name SiteCode
   */
  public String getSiteCode()
  {
    return (String)getAttributeInternal(SITECODE);
  }

  /**Sets <code>value</code> as attribute value for SITE_CODE using the alias name SiteCode
   */
  public void setSiteCode(String value)
  {
    setAttributeInternal(SITECODE, value);
  }

  /**Gets the attribute value for TASK_TYPE using the alias name TaskType
   */
  public String getTaskType()
  {
    return (String)getAttributeInternal(TASKTYPE);
  }

  /**Sets <code>value</code> as attribute value for TASK_TYPE using the alias name TaskType
   */
  public void setTaskType(String value)
  {
    setAttributeInternal(TASKTYPE, value);
  }

  /**Gets the attribute value for PROCESS_NAME using the alias name ProcessName
   */
  public String getProcessName()
  {
    return (String)getAttributeInternal(PROCESSNAME);
  }

  /**Sets <code>value</code> as attribute value for PROCESS_NAME using the alias name ProcessName
   */
  public void setProcessName(String value)
  {
    setAttributeInternal(PROCESSNAME, value);
  }

  /**Gets the attribute value for PROCESS_DOMAIN using the alias name ProcessDomain
   */
  public String getProcessDomain()
  {
    return (String)getAttributeInternal(PROCESSDOMAIN);
  }

  /**Sets <code>value</code> as attribute value for PROCESS_DOMAIN using the alias name ProcessDomain
   */
  public void setProcessDomain(String value)
  {
    setAttributeInternal(PROCESSDOMAIN, value);
  }

  /**Gets the attribute value for INPUT_NAME using the alias name InputName
   */
  public String getInputName()
  {
    return (String)getAttributeInternal(INPUTNAME);
  }

  /**Sets <code>value</code> as attribute value for INPUT_NAME using the alias name InputName
   */
  public void setInputName(String value)
  {
    setAttributeInternal(INPUTNAME, value);
  }

  /**Gets the attribute value for INPUT_DOMAIN using the alias name InputDomain
   */
  public String getInputDomain()
  {
    return (String)getAttributeInternal(INPUTDOMAIN);
  }

  /**Sets <code>value</code> as attribute value for INPUT_DOMAIN using the alias name InputDomain
   */
  public void setInputDomain(String value)
  {
    setAttributeInternal(INPUTDOMAIN, value);
  }

  /**Gets the attribute value for RESPONSE_NAME using the alias name ResponseName
   */
  public String getResponseName()
  {
    return (String)getAttributeInternal(RESPONSENAME);
  }

  /**Sets <code>value</code> as attribute value for RESPONSE_NAME using the alias name ResponseName
   */
  public void setResponseName(String value)
  {
    setAttributeInternal(RESPONSENAME, value);
  }

  /**Gets the attribute value for RESPONSE_DOMAIN using the alias name ResponseDomain
   */
  public String getResponseDomain()
  {
    return (String)getAttributeInternal(RESPONSEDOMAIN);
  }

  /**Sets <code>value</code> as attribute value for RESPONSE_DOMAIN using the alias name ResponseDomain
   */
  public void setResponseDomain(String value)
  {
    setAttributeInternal(RESPONSEDOMAIN, value);
  }

  /**Gets the attribute value for REJECT_NAME using the alias name RejectName
   */
  public String getRejectName()
  {
    return (String)getAttributeInternal(REJECTNAME);
  }

  /**Sets <code>value</code> as attribute value for REJECT_NAME using the alias name RejectName
   */
  public void setRejectName(String value)
  {
    setAttributeInternal(REJECTNAME, value);
  }

  /**Gets the attribute value for REJECT_DOMAIN using the alias name RejectDomain
   */
  public String getRejectDomain()
  {
    return (String)getAttributeInternal(REJECTDOMAIN);
  }

  /**Sets <code>value</code> as attribute value for REJECT_DOMAIN using the alias name RejectDomain
   */
  public void setRejectDomain(String value)
  {
    setAttributeInternal(REJECTDOMAIN, value);
  }

  /**Gets the associated <code>RowIterator</code> using master-detail link FileProviderTraceView
   */
  public RowIterator getFileProviderTraceView()
  {
    return (RowIterator)getAttributeInternal(FILEPROVIDERTRACEVIEW);
  }

  /**Gets the associated <code>RowIterator</code> using master-detail link TaskAuditView
   */
  public RowIterator getTaskAuditView()
  {
    return (RowIterator)getAttributeInternal(TASKAUDITVIEW);
  }

  /**Gets the attribute value for IN_ERROR using the alias name InError
   */
  public Booleans getInError()
  {
    return (Booleans)getAttributeInternal(INERROR);
  }

  /**Sets <code>value</code> as attribute value for IN_ERROR using the alias name InError
   */
  public void setInError(Booleans value)
  {
    setAttributeInternal(INERROR, value);
  }
}
