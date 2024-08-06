package com.cylande.unitedretail.batch.business.entity;

import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.framework.business.jbo.server.EntityDefImpl;
import com.cylande.unitedretail.framework.business.jbo.server.EntityImpl;

import oracle.jbo.Key;
import oracle.jbo.server.AttributeDefImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class ProviderCrcImpl extends EntityImpl
{
  public static final int CRC = 0;
  public static final int BATCHNAME = 1;
  public static final int OVERRIDDEN = 2;
  public static final int CREATIONTIME = 3;
  public static final int CREATIONUSERCODE = 4;
  public static final int MODIFICATIONTIME = 5;
  public static final int MODIFICATIONUSERCODE = 6;
  private static com.cylande.unitedretail.framework.business.jbo.server.EntityDefImpl mDefinitionObject;

  /**This is the default constructor (do not remove)
   */
  public ProviderCrcImpl()
  {
  }

  /**Retrieves the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject()
  {
    if (mDefinitionObject == null)
    {
      mDefinitionObject = (com.cylande.unitedretail.framework.business.jbo.server.EntityDefImpl)EntityDefImpl.findDefObject("com.cylande.unitedretail.batch.business.entity.ProviderCrc");
    }
    return mDefinitionObject;
  }

  /**Gets the attribute value for Crc, using the alias name Crc
   */
  public String getCrc()
  {
    return (String)getAttributeInternal(CRC);
  }

  /**Sets <code>value</code> as the attribute value for Crc
   */
  public void setCrc(String value)
  {
    setAttributeInternal(CRC, value);
  }

  /**Gets the attribute value for BatchName, using the alias name BatchName
   */
  public String getBatchName()
  {
    return (String)getAttributeInternal(BATCHNAME);
  }

  /**Sets <code>value</code> as the attribute value for BatchName
   */
  public void setBatchName(String value)
  {
    setAttributeInternal(BATCHNAME, value);
  }

  /**Gets the attribute value for Overridden, using the alias name Overridden
   */
  public Booleans getOverridden()
  {
    return (Booleans)getAttributeInternal(OVERRIDDEN);
  }

  /**Sets <code>value</code> as the attribute value for Overridden
   */
  public void setOverridden(Booleans value)
  {
    setAttributeInternal(OVERRIDDEN, value);
  }

  /**Gets the attribute value for CreationTime, using the alias name CreationTime
   */
  public Timestamp getCreationTime()
  {
    return (Timestamp)getAttributeInternal(CREATIONTIME);
  }

  /**Gets the attribute value for CreationUserCode, using the alias name CreationUserCode
   */
  public String getCreationUserCode()
  {
    return (String)getAttributeInternal(CREATIONUSERCODE);
  }

  /**Gets the attribute value for ModificationTime, using the alias name ModificationTime
   */
  public Timestamp getModificationTime()
  {
    return (Timestamp)getAttributeInternal(MODIFICATIONTIME);
  }

  /**Gets the attribute value for ModificationUserCode, using the alias name ModificationUserCode
   */
  public String getModificationUserCode()
  {
    return (String)getAttributeInternal(MODIFICATIONUSERCODE);
  }

  /**getAttrInvokeAccessor: generated method. Do not modify.
   */
  protected Object getAttrInvokeAccessor(int index, AttributeDefImpl attrDef) throws Exception
  {
    switch (index)
    {
      case CRC:
        return getCrc();
      case BATCHNAME:
        return getBatchName();
      case OVERRIDDEN:
        return getOverridden();
      case CREATIONTIME:
        return getCreationTime();
      case CREATIONUSERCODE:
        return getCreationUserCode();
      case MODIFICATIONTIME:
        return getModificationTime();
      case MODIFICATIONUSERCODE:
        return getModificationUserCode();
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
      case CRC:
        setCrc((String)value);
        return;
      case BATCHNAME:
        setBatchName((String)value);
        return;
      case OVERRIDDEN:
        setOverridden((Booleans)value);
        return;
      case MODIFICATIONTIME:
        setModificationTime((Timestamp)value);
        return;
      default:
        super.setAttrInvokeAccessor(index, value, attrDef);
        return;
    }
  }

  /**Sets <code>value</code> as the attribute value for ModificationTime
   */
  public void setModificationTime(Timestamp value)
  {
    setAttributeInternal(MODIFICATIONTIME, value);
  }

  /**Creates a Key object based on given key constituents
   */
  public static Key createPrimaryKey(String crc)
  {
    return new Key(new Object[] { crc });
  }
}
