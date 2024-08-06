package com.cylande.unitedretail.batch.business.query;

import com.cylande.unitedretail.batch.business.entity.BatchRunImpl;
import com.cylande.unitedretail.batch.business.query.common.BatchRunViewRow;
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
public class BatchRunViewRowImpl extends ViewRowImpl implements BatchRunViewRow
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
  public static final int DOMAIN = 10;
  public static final int SITECODE = 11;
  public static final int BATCHTYPE = 12;
  public static final int BATCHMODE = 13;
  public static final int INERROR = 14;
  public static final int BATCHRUNCHILDRENVIEW = 15;
  public static final int TASKRUNCHILDRENVIEW = 16;

  /**This is the default constructor (do not remove)
   */
  public BatchRunViewRowImpl()
  {
  }

  /**Gets BatchRun entity object.
   */
  public BatchRunImpl getBatchRun()
  {
    return (BatchRunImpl)getEntity(0);
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

  /**Gets the attribute value for BATCH_CODE using the alias name Path
   */
  public String getPath()
  {
    return (String)getAttributeInternal(PATH);
  }

  /**Sets <code>value</code> as attribute value for BATCH_CODE using the alias name Path
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
      case DOMAIN:
        return getDomain();
      case SITECODE:
        return getSiteCode();
      case BATCHTYPE:
        return getBatchType();
      case BATCHMODE:
        return getBatchMode();
      case INERROR:
        return getInError();
      case BATCHRUNCHILDRENVIEW:
        return getBatchRunChildrenView();
      case TASKRUNCHILDRENVIEW:
        return getTaskRunChildrenView();
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
      case DOMAIN:
        setDomain((String)value);
        return;
      case SITECODE:
        setSiteCode((String)value);
        return;
      case BATCHTYPE:
        setBatchType((String)value);
        return;
      case BATCHMODE:
        setBatchMode((String)value);
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

  /**Gets the attribute value for BATCH_TYPE using the alias name BatchType
   */
  public String getBatchType()
  {
    return (String)getAttributeInternal(BATCHTYPE);
  }

  /**Sets <code>value</code> as attribute value for BATCH_TYPE using the alias name BatchType
   */
  public void setBatchType(String value)
  {
    setAttributeInternal(BATCHTYPE, value);
  }

  /**Gets the attribute value for BATCH_MODE using the alias name BatchMode
   */
  public String getBatchMode()
  {
    return (String)getAttributeInternal(BATCHMODE);
  }

  /**Sets <code>value</code> as attribute value for BATCH_MODE using the alias name BatchMode
   */
  public void setBatchMode(String value)
  {
    setAttributeInternal(BATCHMODE, value);
  }

    /**Gets the attribute value for IN_ERROR using the alias name InError
     */
    public Booleans getInError() {
        return (Booleans)getAttributeInternal(INERROR);
    }

    /**Sets <code>value</code> as attribute value for IN_ERROR using the alias name InError
     */
    public void setInError(Booleans value) {
        setAttributeInternal(INERROR, value);
    }

  /**Gets the associated <code>RowIterator</code> using master-detail link BatchRunChildrenView
   */
  public RowIterator getBatchRunChildrenView()
  {
    return (RowIterator)getAttributeInternal(BATCHRUNCHILDRENVIEW);
  }

  /**Gets the associated <code>RowIterator</code> using master-detail link TaskRunChildrenView
   */
  public RowIterator getTaskRunChildrenView()
  {
    return (RowIterator)getAttributeInternal(TASKRUNCHILDRENVIEW);
  }
}
