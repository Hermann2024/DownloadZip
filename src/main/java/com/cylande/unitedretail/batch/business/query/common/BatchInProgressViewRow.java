package com.cylande.unitedretail.batch.business.query.common;

import oracle.jbo.Row;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---------------------------------------------------------------------
public interface BatchInProgressViewRow extends Row
{
  String getBatchCode();

  String getBatchMode();

  String getBatchType();

  Date getCreationTime();

  String getCreationUserCode();

  String getDomain();

  Date getEndTime();

  Number getId();

  Date getModificationTime();

  String getModificationUserCode();

  Number getParentId();

  String getSiteCode();

  Date getStartTime();

  Number getState();

  void setBatchCode(String value);

  void setBatchMode(String value);

  void setBatchType(String value);

  void setCreationTime(Date value);

  void setCreationUserCode(String value);

  void setDomain(String value);

  void setEndTime(Date value);

  void setId(Number value);

  void setModificationTime(Date value);

  void setModificationUserCode(String value);

  void setParentId(Number value);

  void setSiteCode(String value);

  void setStartTime(Date value);

  void setState(Number value);

  Number getInError();

  void setInError(Number value);
}