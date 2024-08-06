package com.cylande.unitedretail.batch.logging;

import com.cylande.unitedretail.framework.logging.TraceDetail;

/** {@inheritDoc} */
public enum BatchEditionTraceDetail implements TraceDetail
{
  DEFAULT(""),
  PROCESS_VAR_VALUE_CONVERSION_ERROR("processor.variable.value.conversion.error"),
  BATCH_VAR_VALUE_CONVERSION_ERROR("batch.variable.value.conversion.error");

  /**
   * Product code
   */
  public static final String PRODUCT = "URHO";

  /**
   * Module code
   */
  public static final String MODULE = "ENG";

  /**
   * Project code
   */
  public static final String PROJECT = "BAT";

  /**
   * Domain code
   */
  public static final String DOMAIN = "";

  /**
   * Attribute containing the value for this enum : the error code
   */
  private String _code;

  /**
   * Constructor to associate a value to this enum
   * @param pCode The error code
   */
  BatchEditionTraceDetail(String pCode)
  {
    this._code = pCode;
  }

  /**
   * Sample error code : IT0001
   * @return a String containing the error code (value of one of the constants defined into this Enum)
   */
  public String getCode()
  {
    return _code;
  }

  /**
   * Sample error code : IT0001
   * @param pCode a String containing the error code (value of one of the constants defined into this Enum)
   */
  public void setCode(String pCode)
  {
    this._code = pCode;
  }

  /**
   * Sample product code : URHO
   * @return a String containing the product code
   */
  public String getProduct()
  {
    return PRODUCT;
  }

  /**
   * Sample module code : FND
   * @return a String containing the module code
   */
  public String getModule()
  {
    return MODULE;
  }

  /**
   * Sample project code : FWK
   * @return a String containing the project code
   */
  public String getProject()
  {
    return PROJECT;
  }

  public String getDomain()
  {
    return DOMAIN;
  }

  public String getCanonicalContext()
  {
    StringBuilder str = new StringBuilder("");
    if (!getProduct().equals(""))
    {
      str.append(getProduct());
    }
    if (!getModule().equals(""))
    {
      str.append("-").append(getModule());
    }
    if (!getProject().equals(""))
    {
      str.append("-").append(getProject());
    }
    return str.toString();
  }

  public String getCanonicalDomain()
  {
    return getCanonicalContext() + (!getDomain().equals("") ? "-" + getDomain() : "");
  }

  /**
   * Sample canonical code : URHO-FND-FWK-00001
   * @return a String containing the canonical code
   */
  public String getCanonicalCode()
  {
    return getCanonicalContext() + ((getCode() != null && !getCode().equals("")) ? "-" + getCode() : "");
  }
}
