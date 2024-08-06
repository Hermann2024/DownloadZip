package com.cylande.unitedretail.batch.business.module.client;

import com.cylande.unitedretail.batch.business.module.common.QueryDBSelectManagerModule;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryResultType;
import com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryScenarioType;
import com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryType;
import com.cylande.unitedretail.message.common.queryselect.QueryResultType;
import com.cylande.unitedretail.message.common.queryselect.QuerySelectScenarioType;
import com.cylande.unitedretail.message.common.queryselect.QuerySelectType;

import oracle.jbo.client.remote.ApplicationModuleImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class QueryDBSelectManagerModuleClient extends ApplicationModuleImpl implements QueryDBSelectManagerModule
{
  /**This is the default constructor (do not remove)
   */
  public QueryDBSelectManagerModuleClient()
  {
  }

  public AnalyzeCostQueryResultType analyzeCostQuery(AnalyzeCostQueryType pAnalyseCostQuery, AnalyzeCostQueryScenarioType pScenario, ContextType pContext)
  {
    Object _ret = this.riInvokeExportedMethod(this,"analyzeCostQuery",new String [] {"com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryType","com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryScenarioType","com.cylande.unitedretail.message.common.context.ContextType"},new Object[] {pAnalyseCostQuery, pScenario, pContext});
    return (AnalyzeCostQueryResultType)_ret;
  }

  public QueryResultType querySelect(QuerySelectType pQuerySelect, QuerySelectScenarioType pScenario, ContextType pContext)
  {
    Object _ret = this.riInvokeExportedMethod(this,"querySelect",new String [] {"com.cylande.unitedretail.message.common.queryselect.QuerySelectType","com.cylande.unitedretail.message.common.queryselect.QuerySelectScenarioType","com.cylande.unitedretail.message.common.context.ContextType"},new Object[] {pQuerySelect, pScenario, pContext});
    return (QueryResultType)_ret;
  }
}
