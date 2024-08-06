package com.cylande.unitedretail.batch.task;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.execution.EUJob;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.service.FileProviderTraceManagerServiceDelegate;
import com.cylande.unitedretail.batch.service.TaskAuditManagerServiceDelegate;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

import java.net.MalformedURLException;

import java.util.Calendar;

import org.apache.log4j.Logger;

/**
 * Classe utiitaire pour la gestion des logs de TASK_AUDIT.
 */
public class TaskAuditFactory
{
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(TaskAuditFactory.class);

  /**
   * Ajoute un log dans TASK_AUDIT en fonction de l'exception passées en
   * paramètre.
   * @param pProviderException Exception de provider
   * @param pPropManager Gestionnaire de propriétés
   * @param pTaskId Identifiant de la task
   * @param pProvider Provider courant
   */
  public static void createTaskAudit(ProviderException pProviderException, PropertiesManager pPropManager, Integer pTaskId, Provider pProvider)
  {
    if (pProviderException != null)
    {
      String errorDesc = pProviderException.getLocalizedMessage();
      if ((errorDesc == null) || (errorDesc.equalsIgnoreCase("")))
      {
        errorDesc = pProviderException.getMessage();
      }
      createTaskAudit(pProviderException.getCanonicalCode(), errorDesc, pPropManager, pTaskId, pProvider);
    }
  }

  /**
   * Ajoute un log dans TASK_AUDIT en fonction du code et de la description
   * d'erreur passés en paramètre.
   * @param pCode Code erreur
   * @param pDesc Description d'erreur
   * @param pPropManager Gestionnaire de propriétés
   * @param pTaskId
   * @param pProvider Provider courant
   */
  public static void createTaskAudit(String pCode, String pDesc, PropertiesManager pPropManager, Integer pTaskId, Provider pProvider)
  {
    LOGGER.debug("CREATE_TASK_AUDIT -> " + pCode + ":" + pDesc);
    try
    {
      String currentTaskPath = getSysPath(pPropManager).replaceAll(EUJob.THREAD_POOLED_NAME + "\\d+", "");
      Integer currentTaskId = pTaskId;
      if (currentTaskId == null)
      {
        currentTaskId = getSysId(pPropManager);
      }
      SiteKeyType siteKey = getSiteKey(pPropManager);
      if ((currentTaskPath.length() > 0) && (currentTaskId.intValue() > 0) && (siteKey != null))
      {
        TaskAuditManagerServiceDelegate taskAuditManagerService = new TaskAuditManagerServiceDelegate();
        TaskAuditType error = new TaskAuditType();
        error.setPath(currentTaskPath);
        error.setTask(currentTaskId);
        error.setErrorCode(pCode);
        error.setErrorMessage(pDesc);
        error.setEventTime(Calendar.getInstance());
        error.setSite(siteKey);
        error.setFileId(getFileId(pPropManager, currentTaskId, pProvider));
        taskAuditManagerService.createTaskAudit(error, null, ContextTransformer.fromLocale());
      }
      else
      {
        LOGGER.warn("Unable to add event using TaskAuditManagerService");
      }
    }
    catch (Exception e)
    {
      LOGGER.warn("Failed to add event using TaskAuditManagerService  ", e);
    }
  }

  /**
   * Sauvegarde le fichier dans la table de traces FILE_PROVIDER_TRACE.
   * @param pPropManager Gestionnaire de propriétés
   * @param pTaskId Identifiant de la task
   * @param pProvider Provider courant
   * @return Integer
   * @throws MalformedURLException Erreur d'URL
   * @throws ServiceException Erreur de service
   */
  private static Integer getFileId(PropertiesManager pPropManager, Integer pTaskId, Provider pProvider) throws MalformedURLException, ServiceException
  {
    Integer result = null;
    if (pProvider.getCurrentFileName() != null)
    {
      FileProviderTraceManagerServiceDelegate fileProviderTraceManagerService = new FileProviderTraceManagerServiceDelegate();
      FileProviderTraceType fileProviderTrace = new FileProviderTraceType();
      fileProviderTrace.setTaskId(pTaskId);
      fileProviderTrace.setTaskCode(getSysPath(pPropManager).replaceAll(EUJob.THREAD_POOLED_NAME + "\\d+", ""));
      fileProviderTrace.setSite(getSiteKey(pPropManager));
      ProviderType providerDef = pProvider.getProviderDef();
      if (providerDef != null)
      {
        fileProviderTrace.setProviderName(providerDef.getName());
      }
      fileProviderTrace.setDomain(pProvider.getCurrentDomain());
      ProviderFileType providerFileType = pProvider.getCurrentProviderFileType();
      if (providerFileType != null)
      {
        fileProviderTrace.setFilePath(providerFileType.getDir() + "/" + pProvider.getCurrentFileName());
      }
      fileProviderTrace.setFileName(pProvider.getCurrentFileName());
      fileProviderTrace.setInError(Boolean.TRUE);
      fileProviderTrace = fileProviderTraceManagerService.postFileProviderTrace(fileProviderTrace, null, ContextTransformer.fromLocale());
      result = fileProviderTrace.getId();
    }
    return result;
  }

  /**
   * Retourne la valeur affectée à la propriété currentTaskPath.
   * @param pPropManager Gestionnaire de propriétés
   * @return String
   */
  private static String getSysPath(PropertiesManager pPropManager)
  {
    return (String)pPropManager.getSysObject(AbstractTask.SYS_PATH_KEY_NAME, "");
  }

  /**
   * Retourne la valeur affectée à la propriété currentTaskId.
   * @param pPropManager Gestionnaire de propriétés
   * @return Integer
   */
  private static Integer getSysId(PropertiesManager pPropManager)
  {
    return (Integer)pPropManager.getSysObject(AbstractTask.SYS_ID_KEY_NAME, -1);
  }

  /**
   * Retourne la clé de site correspondant au code site stocké dans la propriété
   * siteCode.
   * @param pPropManager Gestionnaire de propriétés
   * @return SiteKeyType
   */
  private static SiteKeyType getSiteKey(PropertiesManager pPropManager)
  {
    SiteKeyType result = null;
    String siteCode = (String)pPropManager.getSysObject(AbstractTask.SYS_SITE_CODE_KEY_NAME, null);
    if (siteCode != null)
    {
      result = new SiteKeyType();
      result.setCode(siteCode);
    }
    return result;
  }
  /*public static void addLog(Integer pTaskId, String pMessage)
  {
    String threadIdStr = String.valueOf(Thread.currentThread().getId());
    String taskIdStr = "";
    if (pTaskId != null)
    {
      taskIdStr = pTaskId.toString();
    }
    if (pMessage == null)
    {
      pMessage = "";
    }
    LOGGER.debug("Thread : ".concat(threadIdStr).concat(", TaskId : ").concat(taskIdStr).concat(", ").concat(pMessage));
  }*/
}
