package com.cylande.unitedretail.batch.transformer.coordinator;

import com.cylande.unitedretail.message.batch.BatchReportType;
import com.cylande.unitedretail.message.batch.BatchRunType;

import java.math.BigInteger;

/**
 * Coordonne les informations pour constituer des rapports de Batch
 */
public final class BatchReportCoordinator
{
  /**
   * Constructeur priv� car toutes les m�thodes sont statics
   */
  private BatchReportCoordinator()
  {
  }

  /**
   * Compl�te le bean BatchReportType � partir d'un bean BatchRunType
   * @param pReport
   * @param pBatchRun
   */
  public static void fillBatchReport(BatchReportType pReport, BatchRunType pBatchRun)
  {
    if (pReport == null)
    {
      pReport = new BatchReportType();
    }
    pReport.setId(new BigInteger(pBatchRun.getId().toString()));
    pReport.setPath(pBatchRun.getPath());
    pReport.setState(pBatchRun.getStatus());
    pReport.setStartDate(pBatchRun.getStartTime());
    pReport.setEndDate(pBatchRun.getEndTime());
    pReport.setDomain(pBatchRun.getDomain());
    pReport.setBatchType(pBatchRun.getBatchType());
    pReport.setSiteCode(pBatchRun.getSite().getCode());
  }
}
