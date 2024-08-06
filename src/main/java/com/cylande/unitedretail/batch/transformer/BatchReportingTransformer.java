package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.BatchRunViewRow;
import com.cylande.unitedretail.batch.business.query.common.TaskRunViewRow;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.message.batch.BatchReportCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchReportCriteriaType;
import com.cylande.unitedretail.message.batch.BatchReportKeyType;
import com.cylande.unitedretail.message.batch.BatchReportType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunNameType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaIntegerType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Classe utilitaire de transformation pour le reporting des RUN
 * @deprecated TODO Le contenu de cette classe est à revoir
 */
public final class BatchReportingTransformer
{

  /**
   * Constructor
   * Privé car toutes les méthodes sont statics
   */
  private BatchReportingTransformer()
  {
  }

  /**
   * Transforme une chaîne de caractères en BatcnRunNameType
   * @param pString
   * @return résultat
   */
  public static BatchRunNameType string2BatchRunNameType(String pString)
  {
    BatchRunNameType result = null;
    if (pString == null)
    {
      return result;
    }
    result = new BatchRunNameType();
    result.setBatchName(pString);
    return result;
  }

  /**
   * Transforme un BatchRunViewRow en BatchReportType simple (premier niveau)
   * @param pRow
   * @param pContext
   * @return résultat
   */
  public static BatchReportType batchRunViewRow2BatchReportLite(BatchRunViewRow pRow, ContextType pContext)
  {
    BatchReportType report = null;
    if (pRow != null)
    {
      report = new BatchReportType();
      if (pRow.getId() != null)
      {
        report.setId(new BigInteger(pRow.getId().toString()));
      }
      if (pRow.getPath() != null)
      {
        report.setPath(pRow.getPath());
      }
      if (pRow.getStatus() != null)
      {
        report.setState(pRow.getStatus().booleanValue());
      }
      if (pRow.getStartTime() != null)
      {
        report.setStartDate(pRow.getStartTime().toCalendar());
      }
      if (pRow.getEndTime() != null)
      {
        report.setEndDate(pRow.getEndTime().toCalendar());
      }
      if (pRow.getDomain() != null)
      {
        report.setDomain(pRow.getDomain());
      }
      if (pRow.getSiteCode() != null)
      {
        report.setSiteCode(pRow.getSiteCode());
      }
    }
    return report;
  }

  /**
   * Crée un BatchRunCriteriaListType à partir d'un BatchReportCriteriaListType
   * @param pCriterias
   * @param pBatchRootOnly
   * @param pContext
   * @return résultat
   */
  public static BatchRunCriteriaListType batchReportCriteriaList2BatchRunCriteriaList(BatchReportCriteriaListType pCriterias, Boolean pBatchRootOnly, ContextType pContext)
  {
    BatchRunCriteriaListType bruncritResult = new BatchRunCriteriaListType();
    if (pCriterias == null)
    {
      if (pBatchRootOnly)
      {
        bruncritResult.setList(new ArrayList<BatchRunCriteriaType>());
        BatchRunCriteriaType criteria = new BatchRunCriteriaType();
        CriteriaStringType critString = new CriteriaStringType();
        critString.setNotContains(".");
        criteria.setPath(critString);
        bruncritResult.getList().add(criteria);
      }
      return bruncritResult;
    }
    Iterator reportcritIt = pCriterias.getList().iterator();
    if (reportcritIt.hasNext())
    {
      // initialiser la liste cible
      bruncritResult.setList(new ArrayList<BatchRunCriteriaType>());
      while (reportcritIt.hasNext())
      {
        //récupérer le report criteria
        BatchReportCriteriaType reportCrit = (BatchReportCriteriaType)reportcritIt.next();
        //créer un BRcriteria
        BatchRunCriteriaType criteria = new BatchRunCriteriaType();
        //le paramétrer avec les infos de reportCriteria
        if (reportCrit.getBatchId() != null)
        {
          criteria.setId(reportCrit.getBatchId());
        }
        if (reportCrit.getBatchPath() != null)
        {
          if (pBatchRootOnly)
          {
            reportCrit.getBatchPath().setNotContains(".");
          }
          criteria.setPath(reportCrit.getBatchPath());
        }
        if (reportCrit.getEndDate() != null)
        {
          criteria.setEndDate(reportCrit.getEndDate());
        }
        if (reportCrit.getStartDate() != null)
        {
          criteria.setStartDate(reportCrit.getStartDate());
        }
        if (reportCrit.getState() != null)
        {
          criteria.setCompleted(reportCrit.getState());
        }
        if (reportCrit.getSite() != null)
        {
          criteria.setSite(SiteUtils.getSiteFromCode(reportCrit.getSite().getCode(), pContext));
        }
        //l'insérer dans la liste resultat
        bruncritResult.getList().add(criteria);
      }
    }
    return bruncritResult;
  }

  /**
   * Crée un BatchRunKeyType à partir d'un BatchReportKeyType
   * @param pKey
   * @param pContext
   * @return résultat
   */
  public static BatchRunKeyType batchReportKey2BatchRunKey(BatchReportKeyType pKey, ContextType pContext)
  {
    if (pKey == null)
    {
      return null;
    }
    BatchRunKeyType brKey = new BatchRunKeyType();
    brKey.setId(pKey.getBatchId().intValue());
    brKey.setPath(pKey.getBatchPath());
    brKey.setSite(SiteUtils.getSiteFromCode(pKey.getSite().getCode(), pContext));
    return brKey;
  }

  /**
   * Crée un TaskAuditCriteriaListType à partir des informations d'un TaskRunViewRow
   * @param pVRow
   * @param pContext
   * @return résultat
   */
  public static TaskAuditCriteriaListType taskRunViewRow4TaskAuditCriteriaList(TaskRunViewRow pVRow, ContextType pContext)
  {
    TaskAuditCriteriaListType criteriaListResult = new TaskAuditCriteriaListType();
    if (pVRow == null)
    {
      return criteriaListResult;
    }
    criteriaListResult.setList(new ArrayList<TaskAuditCriteriaType>());
    TaskAuditCriteriaType criteria = new TaskAuditCriteriaType();
    CriteriaIntegerType critInt = new CriteriaIntegerType();
    critInt.setEquals(pVRow.getId().intValue());
    criteria.setTaskId(critInt);
    CriteriaStringType critStr = new CriteriaStringType();
    critStr.setEquals(pVRow.getPath());
    criteria.setPath(critStr);
    criteria.setSite(SiteUtils.getSiteFromCode(pVRow.getSiteCode(), pContext));
    criteriaListResult.getList().add(criteria);
    return criteriaListResult;
  }
}
