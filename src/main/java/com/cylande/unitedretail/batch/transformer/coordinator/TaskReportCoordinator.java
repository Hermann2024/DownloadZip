package com.cylande.unitedretail.batch.transformer.coordinator;

import com.cylande.unitedretail.message.batch.ErrorListType;
import com.cylande.unitedretail.message.batch.ErrorType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.batch.TaskInfoType;
import com.cylande.unitedretail.message.batch.TaskReportType;
import com.cylande.unitedretail.message.batch.TaskRunType;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.List;

public final class TaskReportCoordinator
{
  /**
   * Constructeur privé car toutes les méthode sont statics
   */
  private TaskReportCoordinator()
  {
  }

  /**
   * Complète un rapport de Tache à partir d'un taskRun
   * @param pReport
   * @param pTaskRun
   */
  public static void fillTaskReport(TaskReportType pReport, TaskRunType pTaskRun)
  {
    if (pReport == null)
    {
      pReport = new TaskReportType();
    }
    // ID
    pReport.setId(new BigInteger(pTaskRun.getId().toString()));
    // STATE
    pReport.setState(pTaskRun.getStatus());
    // PATH
    pReport.setPath(pTaskRun.getPath());
    // START TIME
    pReport.setStartDate(pTaskRun.getStartTime());
    // END TIME
    if (pTaskRun.getEndTime() != null)
    {
      pReport.setEndDate(pTaskRun.getEndTime());
    }
    // PROGRESS
    if (pTaskRun.getWorkProgress() != null)
    {
      pReport.setProgress(new BigInteger(pTaskRun.getWorkProgress().toString()));
    }
    // WORKLOAD
    if (pTaskRun.getWorkLoad() != null)
    {
      pReport.setWorkLoad(new BigInteger(pTaskRun.getWorkLoad().toString()));
    }
    // STEP
    if (pTaskRun.getStep() != null)
    {
      pReport.setStep(pTaskRun.getStep());
    }
    // DOMAIN
    if (pTaskRun.getDomain() != null)
    {
      pReport.setDomain(pTaskRun.getDomain());
    }
    // SITECODE
    pReport.setSiteCode(pTaskRun.getSite().getCode());
    // TYPE
    if (pTaskRun.getTaskType() != null)
    {
      TaskInfoType taskinfo = new TaskInfoType();
      taskinfo.setType(pTaskRun.getTaskType());
      pReport.setTaskInfo(taskinfo);
    }
    // PROCESS
    if (pTaskRun.getProcessInfo() != null)
    {
      pReport.setProcessInfo(pTaskRun.getProcessInfo());
    }
    // INPUT PROVIDER
    if (pTaskRun.getInputProvider() != null)
    {
      pReport.setInputProvider(pTaskRun.getInputProvider());
    }
    // OUTPUT PROVIDER
    if (pTaskRun.getResponseProvider() != null)
    {
      pReport.setResponseProvider(pTaskRun.getResponseProvider());
    }
    // REJECT PROVIDER
    if (pTaskRun.getRejectProvider() != null)
    {
      pReport.setRejectProvider(pTaskRun.getRejectProvider());
    }
  }

  /**
   * Complète un rapport de task en ajoutant la liste des erreurs fournie en paramètre par le bean TaskAuditListType
   * @param pReport
   * @param pTaskAuditList
   */
  public static void fillTaskReportError(TaskReportType pReport, TaskAuditListType pTaskAuditList)
  {
    if (pTaskAuditList == null)
    {
      return;
    }
    if (pReport == null)
    {
      pReport = new TaskReportType();
    }
    ErrorListType errorList = generateErrorListType(pTaskAuditList);
    if (errorList.getError().isEmpty())
    {
      return;
    }
    int nberror = errorList.getError().size();
    // évalution des erreurs
    // ERROR
    if (nberror > 0)
    {
      pReport.setError(true);
    }
    else
    {
      pReport.setError(false);
    }
    // ajout de la liste d'erreur
    // ERRORLIST
    pReport.setErrorList(errorList);
  }

  /**
   * Génère un ErrorListType à partir d'un TaskAuditListType
   * @param pTaskAuditList
   * @return résultat
   */
  private static ErrorListType generateErrorListType(TaskAuditListType pTaskAuditList)
  {
    List<ErrorType> list = new ArrayList<ErrorType>();
    if (pTaskAuditList != null && !pTaskAuditList.getValues().isEmpty())
    {
      ErrorType error = null;
      for (TaskAuditType taskaudit: pTaskAuditList.getValues())
      {
        error = new ErrorType();
        ErrorReportCoordinator.fillErrorType(error, taskaudit);
        list.add(error);
      }
    }
    ErrorListType errorList = new ErrorListType();
    errorList.setError(list);
    return errorList;
  }
}
