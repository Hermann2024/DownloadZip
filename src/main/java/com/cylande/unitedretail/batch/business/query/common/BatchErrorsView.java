package com.cylande.unitedretail.batch.business.query.common;

import com.cylande.unitedretail.message.batch.BatchRunKeyType;

import oracle.jbo.ViewObject;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---------------------------------------------------------------------
public interface BatchErrorsView extends ViewObject
{
  boolean atLeastTaskError(BatchRunKeyType pKey);

  void findTaskAuditOfBatch(BatchRunKeyType pKey);
}
