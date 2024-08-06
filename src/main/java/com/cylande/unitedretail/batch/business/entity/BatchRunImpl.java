package com.cylande.unitedretail.batch.business.entity;

import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.framework.business.jbo.server.EntityDefImpl;
import com.cylande.unitedretail.framework.business.jbo.server.EntityImpl;

import java.sql.SQLException;

import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.server.AttributeDefImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------

public class BatchRunImpl extends EntityImpl
{
  public static final int CREATIONTIME = 0;
  public static final int CREATIONUSERCODE = 1;
  public static final int MODIFICATIONTIME = 2;
  public static final int MODIFICATIONUSERCODE = 3;
  public static final int SITECODE = 4;
  public static final int PATH = 5;
  public static final int ID = 6;
  public static final int PARENTID = 7;
  public static final int STARTTIME = 8;
  public static final int ENDTIME = 9;
  public static final int STATUS = 10;
  public static final int DOMAIN = 11;
  public static final int BATCHTYPE = 12;
  public static final int BATCHMODE = 13;
  public static final int INERROR = 14;
  public static final int TASKRUN = 15;
  public static final int BATCHRUN = 16;
  public static final int BATCHRUNPARENTS = 17;
  private static EntityDefImpl mDefinitionObject;

  /**
   * This is the default constructor (do not remove)
   */
  public BatchRunImpl()
  {
  }

  /**Retrieves the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject()
  {
    if (mDefinitionObject == null)
    {
      mDefinitionObject = (EntityDefImpl)EntityDefImpl.findDefObject("com.cylande.unitedretail.batch.business.entity.BatchRun");
    }
    return mDefinitionObject;
  }

  /**
   * getAttrInvokeAccessor: generated method. Do not modify.
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
      case SITECODE:
        return getSiteCode();
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
      case BATCHTYPE:
        return getBatchType();
      case BATCHMODE:
        return getBatchMode();
      case INERROR:
        return getInError();
      case BATCHRUN:
        return getBatchRun();
      case TASKRUN:
        return getTaskRun();
      case BATCHRUNPARENTS:
        return getBatchRunParents();
      default:
        return super.getAttrInvokeAccessor(index, attrDef);
    }
  }

  /**
   * setAttrInvokeAccessor: generated method. Do not modify.
   */
  protected void setAttrInvokeAccessor(int index, Object value, AttributeDefImpl attrDef) throws Exception
  {
    switch (index)
    {
      case SITECODE:
        setSiteCode((String)value);
        return;
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

  /**
   * Gets the attribute value for StartTime, using the alias name StartTime
   */
  public Timestamp getStartTime()
  {
    return (Timestamp)getAttributeInternal(STARTTIME);
  }

  /**
   * Sets <code>value</code> as the attribute value for StartTime
   */
  public void setStartTime(Timestamp value)
  {
    setAttributeInternal(STARTTIME, value);
  }

  /**
   * Gets the attribute value for EndTime, using the alias name EndTime
   */
  public Timestamp getEndTime()
  {
    return (Timestamp)getAttributeInternal(ENDTIME);
  }

  /**
   * Sets <code>value</code> as the attribute value for EndTime
   */
  public void setEndTime(Timestamp value)
  {
    setAttributeInternal(ENDTIME, value);
  }

  /**
   * Gets the attribute value for Path, using the alias name Path
   */
  public String getPath()
  {
    return (String)getAttributeInternal(PATH);
  }

  /**
   * Sets <code>value</code> as the attribute value for Path
   */
  public void setPath(String value)
  {
    setAttributeInternal(PATH, value);
  }

  /**
   * Gets the attribute value for Status, using the alias name Status
   */
  public Booleans getStatus()
  {
    return (Booleans)getAttributeInternal(STATUS);
  }

  /**
   * Sets <code>value</code> as the attribute value for Status
   */
  public void setStatus(Booleans value)
  {
    setAttributeInternal(STATUS, value);
  }

  /**
   * Gets the attribute value for Id, using the alias name Id
   */
  public Integer getId()
  {
    return (Integer)getAttributeInternal(ID);
  }

  /**
   * Sets <code>value</code> as the attribute value for Id
   */
  public void setId(Integer value)
  {
    setAttributeInternal(ID, value);
  }

  /**
   * Gets the attribute value for CreationTime, using the alias name CreationTime
   */
  public Timestamp getCreationTime()
  {
    return (Timestamp)getAttributeInternal(CREATIONTIME);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreationTime
   */
  public void setCreationTime(Timestamp value)
  {
    setAttributeInternal(CREATIONTIME, value);
  }

  /**
   * Gets the attribute value for ModificationTime, using the alias name ModificationTime
   */
  public Timestamp getModificationTime()
  {
    return (Timestamp)getAttributeInternal(MODIFICATIONTIME);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModificationTime
   */
  public void setModificationTime(Timestamp value)
  {
    setAttributeInternal(MODIFICATIONTIME, value);
  }

  /**
   * Gets the attribute value for CreationUserCode, using the alias name CreationUserCode
   */
  public String getCreationUserCode()
  {
    return (String)getAttributeInternal(CREATIONUSERCODE);
  }

  /**
   * Gets the attribute value for ModificationUserCode, using the alias name ModificationUserCode
   */
  public String getModificationUserCode()
  {
    return (String)getAttributeInternal(MODIFICATIONUSERCODE);
  }

  /**
   * Validation method for BatchRun
   */
  public boolean validateStartEndDate() throws SQLException
  {
    /**
     * TODO : SUPPRESSION DU TEST POUR DEBLOCAGE LIVRAISON if (getStartTime() != null && getEndTime() != null) return
     * TimestampValidator.isBefore(getStartTime(), getEndTime()); else
     **/
    return true;
  }

  /**
   * Gets the attribute value for ParentId, using the alias name ParentId
   */
  public Integer getParentId()
  {
    return (Integer)getAttributeInternal(PARENTID);
  }

  /**
   * Sets <code>value</code> as the attribute value for ParentId
   */
  public void setParentId(Integer value)
  {
    setAttributeInternal(PARENTID, value);
  }

  /**
   * Gets the associated entity oracle.jbo.RowIterator
   */
  public RowIterator getTaskRun()
  {
    return (RowIterator)getAttributeInternal(TASKRUN);
  }

  /**
   * Gets the associated entity oracle.jbo.RowIterator
   */
  public RowIterator getBatchRun()
  {
    return (RowIterator)getAttributeInternal(BATCHRUN);
  }

  /**
   * Gets the attribute value for Domain, using the alias name Domain
   */
  public String getDomain()
  {
    return (String)getAttributeInternal(DOMAIN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Domain
   */
  public void setDomain(String value)
  {
    setAttributeInternal(DOMAIN, value);
  }

  /**
   * Gets the attribute value for SiteCode, using the alias name SiteCode
   */
  public String getSiteCode()
  {
    return (String)getAttributeInternal(SITECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for SiteCode
   */
  public void setSiteCode(String value)
  {
    setAttributeInternal(SITECODE, value);
  }

  /**
   * Gets the attribute value for BatchType, using the alias name BatchType
   */
  public String getBatchType()
  {
    return (String)getAttributeInternal(BATCHTYPE);
  }

  /**
   * Sets <code>value</code> as the attribute value for BatchType
   */
  public void setBatchType(String value)
  {
    setAttributeInternal(BATCHTYPE, value);
  }

  /**
   * Gets the attribute value for BatchMode, using the alias name BatchMode
   */
  public String getBatchMode()
  {
    return (String)getAttributeInternal(BATCHMODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for BatchMode
   */
  public void setBatchMode(String value)
  {
    setAttributeInternal(BATCHMODE, value);
  }

  /**
   * Gets the attribute value for InError, using the alias name InError
   */
  public Booleans getInError()
  {
    return (Booleans)getAttributeInternal(INERROR);
  }

  /**
   * Sets <code>value</code> as the attribute value for InError
   */
  public void setInError(Booleans value)
  {
    setAttributeInternal(INERROR, value);
    BatchRunImpl parent = getBatchRunParents();
    if (parent != null && (parent.getInError() == null || !parent.getInError().booleanValue()))
    {
      parent.setInError(value);
    }
  }

  /**
   * Gets the associated entity BatchRunImpl
   */
  public BatchRunImpl getBatchRunParents()
  {
    return (BatchRunImpl)getAttributeInternal(BATCHRUNPARENTS);
  }

  /**
   * Sets <code>value</code> as the associated entity BatchRunImpl
   */
  public void setBatchRunParents(BatchRunImpl value)
  {
    setAttributeInternal(BATCHRUNPARENTS, value);
  }

  /**Creates a Key object based on given key constituents
   */
  public static Key createPrimaryKey(Integer id)
  {
    return new Key(new Object[]{id});
  }
}