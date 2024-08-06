package com.cylande.unitedretail.batch.business.query.client;

import com.cylande.unitedretail.batch.business.query.common.TaskAuditView;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;

import oracle.jbo.AttributeList;
import oracle.jbo.Row;
import oracle.jbo.client.remote.ViewUsageImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class TaskAuditViewClient extends ViewUsageImpl implements TaskAuditView
{
  /**This is the default constructor (do not remove)
   */
  public TaskAuditViewClient()
  {
  }

  public void findTaskAuditOfBatch(BatchRunKeyType pKey)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this, "findTaskAuditOfBatch", new String[] { "com.cylande.unitedretail.message.batch.BatchRunKeyType" }, new Object[] { pKey });
    return;
  }

  public Row createAndInitRow(AttributeList pAttributesList)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"createAndInitRow",new String [] {"oracle.jbo.AttributeList"},new Object[] {pAttributesList});
    return (Row)_ret;
  }

  public boolean atLeastFindResult(TaskAuditCriteriaListType pCriterias)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"atLeastFindResult",new String [] {"com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType"},new Object[] {pCriterias});
    return ((Boolean)_ret).booleanValue();
  }

  public void findByCriterias(TaskAuditCriteriaListType pCriterias)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"findByCriterias",new String [] {"com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType"},new Object[] {pCriterias});
    return;
  }

  public void findTaskAuditOfBatch(BatchRunCriteriaType pBatchRunCriteria)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this,"findTaskAuditOfBatch",new String [] {"com.cylande.unitedretail.message.batch.BatchRunCriteriaType"},new Object[] {pBatchRunCriteria});
    return;
  }
}
