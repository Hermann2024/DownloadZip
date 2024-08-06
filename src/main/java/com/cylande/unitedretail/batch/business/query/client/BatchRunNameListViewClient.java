package com.cylande.unitedretail.batch.business.query.client;

import com.cylande.unitedretail.batch.business.query.common.BatchRunNameListView;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;

import oracle.jbo.client.remote.ViewUsageImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class BatchRunNameListViewClient extends ViewUsageImpl implements BatchRunNameListView
{
  /**This is the default constructor (do not remove)
   */
  public BatchRunNameListViewClient()
  {
  }

  public void findBatchRunRoot()
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this, "findBatchRunRoot", null, null);
    return;
  }

  public void findBatchRunRoot(String substring)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this, "findBatchRunRoot", new String[] { "java.lang.String" }, new Object[] { substring });
    return;
  }

  public void findBatchRunRoot(SiteKeyType pSite)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this, "findBatchRunRoot", new String[] { "com.cylande.unitedretail.message.network.businessunit.SiteKeyType" }, new Object[] { pSite });
    return;
  }

  public void findBatchRunRoot(String substring, SiteKeyType pSite)
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this, "findBatchRunRoot", new String[] { "java.lang.String", "com.cylande.unitedretail.message.network.businessunit.SiteKeyType" }, new Object[] { substring, pSite });
    return;
  }

  public void resetWhereClause()
  {
    Object _ret = getApplicationModuleProxy().riInvokeExportedMethod(this, "resetWhereClause", null, null);
    return;
  }
}