package com.cylande.unitedretail.batch.business.query.common;

import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;

import oracle.jbo.Row;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---------------------------------------------------------------------
public interface TaskAuditViewRow extends Row
{
  Timestamp getCreationTime();

  String getCreationUserCode();

  String getErrorCode();

  String getErrorMessage();

  Timestamp getEventTime();

  Integer getId();

  Timestamp getModificationTime();

  String getModificationUserCode();

  String getPath();

  String getSiteCode();

  Integer getTask();

  void setErrorCode(String value);

  void setErrorMessage(String value);

  void setEventTime(Timestamp value);

  void setId(Integer value);

  void setPath(String value);

  void setSiteCode(String value);

  void setTask(Integer value);

  Integer getFileId();

  void setFileId(Integer value);
}