package com.cylande.unitedretail.batch.business.query.client;

import oracle.jbo.client.remote.RowImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class BatchRunNameListViewRowClient extends RowImpl
{
  /**This is the default constructor (do not remove)
   */
  public BatchRunNameListViewRowClient()
  {
  }

  public String getBrunpath()
  {
    return (String)getAttribute("Brunpath");
  }

  public void setBrunpath(String value)
  {
    setAttribute("Brunpath", value);
  }
}
