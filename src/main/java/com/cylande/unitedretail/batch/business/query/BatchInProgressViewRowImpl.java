package com.cylande.unitedretail.batch.business.query;

import com.cylande.unitedretail.batch.business.query.common.BatchInProgressViewRow;
import com.cylande.unitedretail.framework.business.jbo.server.ViewRowImpl;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class BatchInProgressViewRowImpl extends ViewRowImpl implements BatchInProgressViewRow
{
  public static final int CREATIONTIME = 0;
  public static final int CREATIONUSERCODE = 1;
  public static final int MODIFICATIONTIME = 2;
  public static final int MODIFICATIONUSERCODE = 3;
  public static final int BATCHCODE = 4;
  public static final int ID = 5;
  public static final int PARENTID = 6;
  public static final int STARTTIME = 7;
  public static final int ENDTIME = 8;
  public static final int STATE = 9;
  public static final int DOMAIN = 10;
  public static final int SITECODE = 11;
  public static final int BATCHTYPE = 12;
  public static final int BATCHMODE = 13;
  public static final int INERROR = 14;

  /**This is the default constructor (do not remove)
   */
  public BatchInProgressViewRowImpl()
  {
  }

  /**Gets the attribute value for the calculated attribute CreationTime
   */
  public Date getCreationTime()
  {
    return (Date)getAttributeInternal(CREATIONTIME);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute CreationTime
   */
  public void setCreationTime(Date value)
  {
    setAttributeInternal(CREATIONTIME, value);
  }

  /**Gets the attribute value for the calculated attribute CreationUserCode
   */
  public String getCreationUserCode()
  {
    return (String)getAttributeInternal(CREATIONUSERCODE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute CreationUserCode
   */
  public void setCreationUserCode(String value)
  {
    setAttributeInternal(CREATIONUSERCODE, value);
  }

  /**Gets the attribute value for the calculated attribute ModificationTime
   */
  public Date getModificationTime()
  {
    return (Date)getAttributeInternal(MODIFICATIONTIME);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute ModificationTime
   */
  public void setModificationTime(Date value)
  {
    setAttributeInternal(MODIFICATIONTIME, value);
  }

  /**Gets the attribute value for the calculated attribute ModificationUserCode
   */
  public String getModificationUserCode()
  {
    return (String)getAttributeInternal(MODIFICATIONUSERCODE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute ModificationUserCode
   */
  public void setModificationUserCode(String value)
  {
    setAttributeInternal(MODIFICATIONUSERCODE, value);
  }

  /**Gets the attribute value for the calculated attribute BatchCode
   */
  public String getBatchCode()
  {
    return (String)getAttributeInternal(BATCHCODE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute BatchCode
   */
  public void setBatchCode(String value)
  {
    setAttributeInternal(BATCHCODE, value);
  }

  /**Gets the attribute value for the calculated attribute Id
   */
  public Number getId()
  {
    return (Number)getAttributeInternal(ID);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute Id
   */
  public void setId(Number value)
  {
    setAttributeInternal(ID, value);
  }

  /**Gets the attribute value for the calculated attribute ParentId
   */
  public Number getParentId()
  {
    return (Number)getAttributeInternal(PARENTID);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute ParentId
   */
  public void setParentId(Number value)
  {
    setAttributeInternal(PARENTID, value);
  }

  /**Gets the attribute value for the calculated attribute StartTime
   */
  public Date getStartTime()
  {
    return (Date)getAttributeInternal(STARTTIME);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute StartTime
   */
  public void setStartTime(Date value)
  {
    setAttributeInternal(STARTTIME, value);
  }

  /**Gets the attribute value for the calculated attribute EndTime
   */
  public Date getEndTime()
  {
    return (Date)getAttributeInternal(ENDTIME);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute EndTime
   */
  public void setEndTime(Date value)
  {
    setAttributeInternal(ENDTIME, value);
  }

  /**Gets the attribute value for the calculated attribute State
   */
  public Number getState()
  {
    return (Number)getAttributeInternal(STATE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute State
   */
  public void setState(Number value)
  {
    setAttributeInternal(STATE, value);
  }

  /**Gets the attribute value for the calculated attribute Domain
   */
  public String getDomain()
  {
    return (String)getAttributeInternal(DOMAIN);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute Domain
   */
  public void setDomain(String value)
  {
    setAttributeInternal(DOMAIN, value);
  }

  /**Gets the attribute value for the calculated attribute SiteCode
   */
  public String getSiteCode()
  {
    return (String)getAttributeInternal(SITECODE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute SiteCode
   */
  public void setSiteCode(String value)
  {
    setAttributeInternal(SITECODE, value);
  }

  /**Gets the attribute value for the calculated attribute BatchType
   */
  public String getBatchType()
  {
    return (String)getAttributeInternal(BATCHTYPE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute BatchType
   */
  public void setBatchType(String value)
  {
    setAttributeInternal(BATCHTYPE, value);
  }

  /**Gets the attribute value for the calculated attribute BatchMode
   */
  public String getBatchMode()
  {
    return (String)getAttributeInternal(BATCHMODE);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute BatchMode
   */
  public void setBatchMode(String value)
  {
    setAttributeInternal(BATCHMODE, value);
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
      case BATCHCODE:
        return getBatchCode();
      case ID:
        return getId();
      case PARENTID:
        return getParentId();
      case STARTTIME:
        return getStartTime();
      case ENDTIME:
        return getEndTime();
      case STATE:
        return getState();
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
      case CREATIONTIME:
        setCreationTime((Date)value);
        return;
      case CREATIONUSERCODE:
        setCreationUserCode((String)value);
        return;
      case MODIFICATIONTIME:
        setModificationTime((Date)value);
        return;
      case MODIFICATIONUSERCODE:
        setModificationUserCode((String)value);
        return;
      case BATCHCODE:
        setBatchCode((String)value);
        return;
      case ID:
        setId((Number)value);
        return;
      case PARENTID:
        setParentId((Number)value);
        return;
      case STARTTIME:
        setStartTime((Date)value);
        return;
      case ENDTIME:
        setEndTime((Date)value);
        return;
      case STATE:
        setState((Number)value);
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
        setInError((Number)value);
        return;
      default:
        super.setAttrInvokeAccessor(index, value, attrDef);
        return;
    }
  }

  /**Gets the attribute value for the calculated attribute InError
   */
  public Number getInError()
  {
    return (Number)getAttributeInternal(INERROR);
  }

  /**Sets <code>value</code> as the attribute value for the calculated attribute InError
   */
  public void setInError(Number value)
  {
    setAttributeInternal(INERROR, value);
  }
}