package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.exception.ErrorDetail;

public enum BatchEditionErrorDetail implements ErrorDetail
{
  /** code erreur */
  IMPOSSIBLE_TO_CREATE_DIRECTORY("impossible.to.create.directory"),
  /** code erreur */
  EXISTING_PROJECT("existing.project"),
  /** code erreur */
  MISSING_FILE("missing.file"),
  /** code erreur */
  MISSING_DIRECTORY("missing.directory"),
  /** code erreur */
  BATCH_UNDELETABLE_ALREADY_USED("batch.undeletable.already.used"),
  /** code erreur */
  TASK_UNDELETABLE_ALREADY_USED("task.undeletable.already.used"),
  /** code erreur */
  TRIGGER_UNDELETABLE_ALREADY_USED("trigger.undeletable.already.used"),
  /** code erreur */
  PROVIDER_UNDELETABLE_ALREADY_USED("provider.undeletable.already.used"),
  /** code erreur */
  PROCESSOR_UNDELETABLE_ALREADY_USED("processor.undeletable.already.used"),
  /** code erreur */
  STYLESHEET_UNDELETABLE_ALREADY_USED("stylesheet.undeletable.already.used"),
  /** code erreur */
  MAPPER_UNDELETABLE_ALREADY_USED("mapper.undeletable.already.used"),
  /** code erreur */
  CLASS_NOT_FOUND("class.notfound"),
  /** code erreur */
  XSL_ERROR("xsl.error"),
  /** code erreur */
  ERROR_READING_FILE("error.reading.file");

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
   * Attribute containing the value for this enum : the error code
   */
  private String _code;

  /**
   * Constructor to associate a value to this enum
   * @param pCode : The error code
   */
  BatchEditionErrorDetail(String pCode)
  {
    this._code = pCode;
  }

  /**
   * Sample error code : 00001
   * @return a String containing the error code (value of one of the constants defined into this Enum)
   */
  public String getCode()
  {
    return _code;
  }

  /**
   * Sample error code : IT0001
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
   * Sample module code : Foundation
   * @return a String containing the module code
   */
  public String getModule()
  {
    return MODULE;
  }

  /**
   * Sample project code : Framework
   * @return a String containing the project code
   */
  public String getProject()
  {
    return PROJECT;
  }

  /**
   * Sample canonical code : URHO-Foundation-Framework-00001
   * @return a String containing the canonical code
   */
  public String getCanonicalCode()
  {
    return getProduct() + "-" + getModule() + "-" + getProject() + "-" + getCode();
  }
}
