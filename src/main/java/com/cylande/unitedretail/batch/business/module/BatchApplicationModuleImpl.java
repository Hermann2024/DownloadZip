package com.cylande.unitedretail.batch.business.module;

import com.cylande.unitedretail.batch.business.module.common.BatchApplicationModule;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Date;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Integer;
import com.cylande.unitedretail.framework.business.jbo.server.ApplicationModuleImpl;
import com.cylande.unitedretail.framework.business.jbo.server.ViewDefImpl;
import com.cylande.unitedretail.message.common.CommonScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.processapplication.ProcessIndicatorType;
import com.cylande.unitedretail.message.common.processapplication.ProcessSiteIndicatorListType;
import com.cylande.unitedretail.message.common.processapplication.ProcessSiteIndicatorType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
/**
 * Module applicatif d�di� aux batchs
 */
public class BatchApplicationModuleImpl extends ApplicationModuleImpl implements BatchApplicationModule
{
  /**
   * This is the default constructor (do not remove)
   */
  public BatchApplicationModuleImpl()
  {
    //Constructeur vide
  }

  /**
   * Sample main for debugging Business Components code using the tester.
   */
  public static void main(String[] args)
  { /* package name */
    /* Configuration Name */launchTester("com.cylande.unitedretail.batch.business.module", "BatchApplicationModuleLocal");
  }

  /**
   * Recherche de l'indicateur de traitements du jour
   * @param pScenario Scenario
   * @param pContext Contexte
   * @return ProcessIndicatorType
   */
  public ProcessIndicatorType getBatchIndicator(CommonScenarioType pScenario, ContextType pContext)
  {
    StringBuilder queryBuilder = new StringBuilder();
    List<Object> whereClauseParams = new ArrayList<Object>();
    writeGetBatchIndicatorQuery(queryBuilder, whereClauseParams);
    ViewDefImpl viewDef = new ViewDefImpl();
    viewDef.setQuery(queryBuilder.toString());
    viewDef.addViewAttribute("Quantity", "Quantity", Integer.class);
    viewDef.addViewAttribute("InError", "InError", Integer.class);
    viewDef.resolveDefObject();
    ViewObject view = this.createViewObject("GetBatchIndicator", viewDef);
    view.setForwardOnly(true);
    view.setWhereClauseParams(whereClauseParams.toArray());
    view.executeQuery();
    return buildGetBatchIndicatorResult(view);
  }

  /**
   * Construction de la requ�te de recherche de l'indicateur de traitements du jour
   * @param pQuery Requ�te
   * @param pParams Param�tres
   */
  private void writeGetBatchIndicatorQuery(StringBuilder pQuery, List pParams)
  {
    pQuery.append("SELECT COUNT(*) Quantity, SUM(IN_ERROR) InError");
    pQuery.append("  FROM BATCH_RUN");
    pQuery.append(" WHERE START_TIME >= ?");
    pQuery.append("   AND END_TIME IS NOT NULL");
    pParams.add(Date.getSimpleDate(Calendar.getInstance()));
  }

  /**
   * Construction du r�sultat de la requ�te de recherche de l'indicateur de traitements du jour
   * @param pView Vue executant la requ�te
   * @return ProcessIndicatorType
   */
  private ProcessIndicatorType buildGetBatchIndicatorResult(ViewObject pView)
  {
    ProcessIndicatorType result = null;
    if (pView.hasNext())
    {
      Row myRow = pView.next();
      result = new ProcessIndicatorType();
      Integer myInteger = (Integer)myRow.getAttribute("Quantity");
      result.setQuantity(myInteger == null ? 0 : myInteger.intValue());
      myInteger = (Integer)myRow.getAttribute("InError");
      result.setInError(myInteger == null ? 0 : myInteger.intValue());
    }
    pView.remove();
    return result;
  }

  /**
   * Recherche de la liste d'indicateurs de traitements par site du jour
   * @param pScenario Scenario
   * @param pContext Contexte
   * @return ProcessSiteIndicatorListType
   */
  public ProcessSiteIndicatorListType findBatchSiteIndicator(CommonScenarioType pScenario, ContextType pContext)
  {
    StringBuilder queryBuilder = new StringBuilder();
    List<Object> whereClauseParams = new ArrayList<Object>();
    writeFindBatchSiteIndicatorQuery(queryBuilder, whereClauseParams);
    ViewDefImpl viewDef = new ViewDefImpl();
    viewDef.setQuery(queryBuilder.toString());
    viewDef.addViewAttribute("SiteId", "SiteId", Integer.class);
    viewDef.addViewAttribute("SiteCode", "SiteCode", String.class);
    viewDef.addViewAttribute("SiteName", "SiteName", String.class);
    viewDef.addViewAttribute("Quantity", "Quantity", Integer.class);
    viewDef.addViewAttribute("InError", "InError", Integer.class);
    viewDef.resolveDefObject();
    ViewObject view = this.createViewObject("FindBatchSiteIndicator", viewDef);
    view.setForwardOnly(true);
    view.setWhereClauseParams(whereClauseParams.toArray());
    view.executeQuery();
    return buildFindBatchSiteIndicatorResult(view);
  }

  /**
   * Construction de la requ�te de recherche de la liste d'indicateurs de traitements par site du jour
   * @param pQuery Requ�te
   * @param pParams Param�tres
   */
  private void writeFindBatchSiteIndicatorQuery(StringBuilder pQuery, List pParams)
  {
    pQuery.append("     SELECT Site.ID SiteId, Site.Code SiteCode, Site.Name SiteName, BatchView.Quantity Quantity, BatchView.InError InError");
    pQuery.append("       FROM SITE Site");
    pQuery.append(" LEFT OUTER JOIN (SELECT COUNT(*) Quantity, SUM(IN_ERROR) InError, SITE_CODE SiteCode");
    pQuery.append("               FROM BATCH_RUN");
    pQuery.append("              WHERE START_TIME >= ?");
    pQuery.append("                AND END_TIME IS NOT NULL");
    pQuery.append("           GROUP BY SITE_CODE) BatchView");
    pQuery.append("         ON Site.CODE = BatchView.SiteCode");
    pParams.add(Date.getSimpleDate(Calendar.getInstance()));
  }

  /**
   * Construction du r�sultat de la requ�te de recherche de la liste d'indicateurs de traitements par site du jour
   * @param pView Vue executant la requ�te
   * @return ProcessSiteIndicatorListType
   */
  private ProcessSiteIndicatorListType buildFindBatchSiteIndicatorResult(ViewObject pView)
  {
    ProcessSiteIndicatorListType result = null;
    if (pView.hasNext())
    {
      result = new ProcessSiteIndicatorListType();
      ProcessSiteIndicatorType indicator = null;
      Integer myInteger = null;
      while (pView.hasNext())
      {
        Row myRow = pView.next();
        indicator = new ProcessSiteIndicatorType();
        indicator.setSite(new SiteKeyType());
        myInteger = (Integer)myRow.getAttribute("SiteId");
        if (myInteger != null)
        {
          indicator.getSite().setId(myInteger.intValue());
        }
        indicator.getSite().setCode((String)myRow.getAttribute("SiteCode"));
        indicator.getSite().setName((String)myRow.getAttribute("SiteName"));
        myInteger = (Integer)myRow.getAttribute("Quantity");
        indicator.setQuantity(myInteger == null ? 0 : myInteger.intValue());
        myInteger = (Integer)myRow.getAttribute("InError");
        indicator.setInError(myInteger == null ? 0 : myInteger.intValue());
        result.getValues().add(indicator);
      }
    }
    pView.remove();
    return result;
  }
}