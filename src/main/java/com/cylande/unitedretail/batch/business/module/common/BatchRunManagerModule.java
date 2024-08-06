package com.cylande.unitedretail.batch.business.module.common;

import com.cylande.unitedretail.message.batch.BatchBooleanResponseType;
import com.cylande.unitedretail.message.batch.BatchCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunNameListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchStatusScenarioType;
import com.cylande.unitedretail.message.batch.BatchSummaryCriteriaCondensaType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.common.context.ContextType;

import oracle.jbo.ApplicationModule;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---------------------------------------------------------------------
public interface BatchRunManagerModule extends ApplicationModule
{
  BatchRunType createBatchRun(BatchRunType pBatchRun, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunListType createBatchRunList(BatchRunListType pList, BatchRunScenarioType pScenario, ContextType pContext);

  void deleteBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext);

  void deleteBatchRunList(BatchRunCriteriaListType pCriterias, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunListType findBatchRun(BatchRunCriteriaListType pCriterias, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunType getBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunType postBatchRun(BatchRunType pBatchRun, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunListType postBatchRunList(BatchRunListType pList, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunType updateBatchRun(BatchRunType pBatchRun, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunListType updateBatchRunList(BatchRunListType pList, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunNameListType findBatchRunSummary(BatchSummaryCriteriaCondensaType pParams, BatchRunScenarioType pScenario, ContextType pContext);

  BatchBooleanResponseType atLeastTaskError(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext);

  BatchBooleanResponseType atLeastBatchInstanceInError(BatchCriteriaType pCriteria, BatchRunScenarioType pScenario, ContextType pContext);

  TaskAuditListType getTaskAuditOfBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunListType getBatchRunOfBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext);

  TaskRunListType getTaskRunOfBatchRun(BatchRunKeyType pKey, BatchRunScenarioType pScenario, ContextType pContext);

  boolean batchRunIsOnError(BatchRunKeyType pKey);

  BatchBooleanResponseType atLeastBatchInstanceIsRunning(BatchRunCriteriaType pBatchCrit, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunType getLastActivationBatch(BatchRunCriteriaType pBatchCrit, BatchRunScenarioType pScenario, ContextType pContext);

  BatchRunType getLastBatchInProgress(BatchRunKeyType pKey, BatchStatusScenarioType pScenario, ContextType pContext);

  BatchRunType getLastActivationBatch(BatchCriteriaType pBatchCrit, BatchRunScenarioType pScenario, ContextType pContext);

  BatchBooleanResponseType atLeastBatchInstanceInError(BatchRunCriteriaType pCriteria, BatchRunScenarioType pScenario, ContextType pContext);

  void deleteBatchRunPacket(BatchRunScenarioType pScenario, ContextType pContext);

  boolean hasNextPacketToDelete();

  void initDeleteBatchRunList(BatchRunCriteriaListType pCriterias);

  void releaseDeleteBatchRunList();
}