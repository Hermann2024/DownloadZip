package com.cylande.unitedretail.batch.service;

import java.util.Calendar;
import java.util.List;

import com.cylande.unitedretail.batch.service.common.BatchGanttEngineService;
import com.cylande.unitedretail.batch.tools.GanttNode;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractFunctionnalServiceImpl;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.AbstractRunDetailType;
import com.cylande.unitedretail.message.batch.BatchAndTaskRunDetailsListType;
import com.cylande.unitedretail.message.batch.BatchGanttCriteriaType;
import com.cylande.unitedretail.message.batch.BatchGanttRowListType;
import com.cylande.unitedretail.message.batch.BatchGanttScenarioType;
import com.cylande.unitedretail.message.batch.BatchReportingScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunDetailsType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.TaskRunDetailsType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaBooleanType;
import com.cylande.unitedretail.message.common.criteria.CriteriaIntegerType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.common.criteria.CriteriaTimestampType;
import com.cylande.unitedretail.message.common.criteria.ListParameterType;
import com.cylande.unitedretail.message.common.criteria.TimestampCriteriaParameterType;

public class BatchGanttEngineServiceImpl extends AbstractFunctionnalServiceImpl implements BatchGanttEngineService
{
  private static final Integer MAX_SIZE = 1000;

  /**
   * Vérification et initialisation des paramètres
   *
   * @param pCriteria
   */
  private void checkDefaultValues(BatchGanttCriteriaType pCriteria)
  {
    if (pCriteria.getFrom() == null)
    {
      pCriteria.setFrom(0L);
    }

    if (pCriteria.getTo() == null)
    {
      pCriteria.setTo(Long.MAX_VALUE);
    }

    if (pCriteria.getName() == null)
    {
      pCriteria.setName("");
    }

    if (pCriteria.getDuration() == null)
    {
      pCriteria.setDuration(5000L);
    }

    if (!pCriteria.getError())
    {
      pCriteria.setError(null);
    }
  }

  /**
   * Permet de récupérer les données nécessaire au gantt
   */
  public BatchGanttRowListType getData(BatchGanttCriteriaType pCriteria, BatchGanttScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    GanttNode gantt = new GanttNode();
    BatchGanttRowListType result = new BatchGanttRowListType();
    BatchRunListType batchList = null;
    checkDefaultValues(pCriteria);
    try
    {
      if (pScenario.getOnlyDetails() != null && pScenario.getOnlyDetails().equals(true) && pCriteria.getBatchId() != null)
      {
        batchList = getBatchList(pCriteria.getBatchId(), pContext);
      }
      else
      {
        batchList = getBatchList(pCriteria.getFrom(), pCriteria.getTo(), pCriteria.getName(), pCriteria.getError(), pScenario.getOnlyRoot(), pContext);
      }

      if (batchList != null)
      {
        result.setLimited(batchList.getValues().size() == MAX_SIZE);
        BatchReportingEngineServiceDelegate batchReportingServ = new BatchReportingEngineServiceDelegate();
        BatchReportingScenarioType batchReportingScenarioType = new BatchReportingScenarioType();
        batchReportingScenarioType.setManageErrorDetails(false);
        batchReportingScenarioType.setManageFileProvider(false);
        BatchRunType loadingBatch = new BatchRunType();
        for (BatchRunType batch: batchList.getValues())
        {
          if (batch.getEndTime() != null && (batch.getEndTime().getTimeInMillis() - batch.getStartTime().getTimeInMillis() > pCriteria.getDuration()))
          {
            BatchRunKeyType key = new BatchRunKeyType();
            key.setId(batch.getId());
            key.setPath(batch.getPath());
            BatchAndTaskRunDetailsListType batchAndTaskRun = null;
            batchAndTaskRun = batchReportingServ.getDetailsOfBatch(key, batchReportingScenarioType, pContext);
            gantt.addChild(batch);
            if (pScenario.getOnlyRoot())
            {
              if (batchAndTaskRun != null && batchAndTaskRun.getList().size() != 0)
              {
                loadingBatch.setPath(batch.getPath() + ".Chargement");
                loadingBatch.setId(-1);
                loadingBatch.setParentId(batch.getId());
                loadingBatch.setEndTime(batch.getEndTime());
                loadingBatch.setStartTime(batch.getStartTime());
                gantt.addChild(loadingBatch);
              }
            }
            else if (batchAndTaskRun != null)
            {
              for (AbstractRunDetailType abstractRunDetailType: batchAndTaskRun.getList())
              {
                if (abstractRunDetailType instanceof BatchRunDetailsType)
                {
                  BatchRunType batchRun = ((BatchRunDetailsType)abstractRunDetailType).getBatchRun();
                  gantt.addChild(batchRun);
                }
                else if (abstractRunDetailType instanceof TaskRunDetailsType)
                {
                  TaskRunType taskRun = ((TaskRunDetailsType)abstractRunDetailType).getTaskRun();
                  gantt.addChild(taskRun);
                }
              }
            }
          }
        }
      }
      gantt.getGanttRow(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /**
   * Récupération des BatchRuns en fonction d'une liste d'IDs
   * @param pBatchIdList
   * @param pContextType
   * @return
   * @throws Exception
   */
  private BatchRunListType getBatchList(List<Integer> pBatchIdList, ContextType pContextType) throws Exception
  {
    BatchRunManagerServiceDelegate batchServ = new BatchRunManagerServiceDelegate();
    BatchRunListType batchList = new BatchRunListType();
    for (Integer id: pBatchIdList)
    {
      BatchRunKeyType key = new BatchRunKeyType();
      key.setId(id);
      batchList.getValues().add(batchServ.getBatchRun(key, new BatchRunScenarioType(), pContextType));
    }
    return batchList;
  }

  /**
   * Récupére la liste des batchs
   *
   * @param pTimestampStart Critère de début
   * @param pTimestampEnd Critère de fin
   * @param pName Critère de nom
   * @param pError Crière erreur
   * @param boolean1
   * @return BatchRunListType
   * @throws ServiceException
   */
  private BatchRunListType getBatchList(long pTimestampStart, long pTimestampEnd, String pName, Boolean pError, Boolean pOnlyRoot, ContextType pContextType) throws ServiceException
  {
    BatchRunManagerServiceDelegate batchServ = new BatchRunManagerServiceDelegate();

    BatchRunCriteriaType batchCrit = new BatchRunCriteriaType();

    CriteriaTimestampType startTimetampCriteria = new CriteriaTimestampType();
    CriteriaTimestampType endDateCriteria = new CriteriaTimestampType();

    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();

    startCalendar.setTimeInMillis(pTimestampStart);
    endCalendar.setTimeInMillis(pTimestampEnd);

    startTimetampCriteria.setMin(new TimestampCriteriaParameterType());
    startTimetampCriteria.getMin().setManageNull(false);
    startTimetampCriteria.getMin().setValue(startCalendar);

    startTimetampCriteria.setMax(new TimestampCriteriaParameterType());
    startTimetampCriteria.getMax().setManageNull(false);
    startTimetampCriteria.getMax().setValue(endCalendar);

    endDateCriteria.setMax(new TimestampCriteriaParameterType());
    endDateCriteria.getMax().setManageNull(false);
    endDateCriteria.getMax().setValue(endCalendar);

    endDateCriteria.setMin(new TimestampCriteriaParameterType());
    endDateCriteria.getMin().setManageNull(false);
    endDateCriteria.getMin().setValue(startCalendar);

    batchCrit.setStartDate(startTimetampCriteria);
    batchCrit.setEndDate(endDateCriteria);

    CriteriaIntegerType parentId = new CriteriaIntegerType();
    parentId.setEquals(-1);
    batchCrit.setParentId(parentId);

    if (!pName.equals(""))
    {
      CriteriaStringType pathCrit = new CriteriaStringType();
      pathCrit.setContains(pName);
      batchCrit.setPath(pathCrit);
    }

    if (pError != null)
    {
      CriteriaBooleanType errorCrit = new CriteriaBooleanType();
      errorCrit.setEquals(pError);
      batchCrit.setInError(errorCrit);
    }

    BatchRunCriteriaListType batchCritList = new BatchRunCriteriaListType();
    batchCritList.getList().add(batchCrit);
    batchCritList.setParameter(new ListParameterType());
    batchCritList.getParameter().setSize(MAX_SIZE);
    batchCritList.getParameter().setSort("START_TIME DESC");
    BatchRunScenarioType scenario = new BatchRunScenarioType();
    scenario.setManageChildRuns(false);
    BatchRunListType batchList = batchServ.findBatchRun(batchCritList, scenario, pContextType);

    return batchList;
  }
}
