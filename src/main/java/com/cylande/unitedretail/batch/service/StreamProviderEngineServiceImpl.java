package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.service.common.FileProviderTraceManagerService;
import com.cylande.unitedretail.batch.service.common.StreamProviderEngineService;
import com.cylande.unitedretail.batch.service.exception.StreamProviderEngineServiceException;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceNotDeliveredException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchFileCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.FileProviderContentType;
import com.cylande.unitedretail.message.batch.FileProviderTraceCriteriaListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceCriteriaType;
import com.cylande.unitedretail.message.batch.FileProviderTraceListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceScenarioType;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.batch.ProviderFileResponseType;
import com.cylande.unitedretail.message.batch.StreamProviderEngineScenarioType;
import com.cylande.unitedretail.message.batch.TaskFileCriteriaType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaIntegerType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;

/**
 * Classe d'implémentation permettant de récupérer une liste de fichier
 * utilisé pour une exécution de batch ou tache ainsi que de modifier
 * son contenu.
 */
public class StreamProviderEngineServiceImpl implements StreamProviderEngineService
{
  /**
   * constructor
   */
  public StreamProviderEngineServiceImpl()
  {
  }

  /**
   * Permet de récupérer une Liste de ProviderFileType lié au BatchCriteria passé en paramètre.
   * La présence du BatchId dans les critères indiquera s’il faut parcourir les définitions de batch
   * ou faire une recherche en base.
   * @param pBatchCriteria : critère de recherche
   * @param pScenario : scénario
   * @param pContext : contexte
   * @return ProviderFileListType : Liste de ProviderFileType
   * @throws RemoteException exception
   * @throws WrapperServiceException exception
   * @throws StreamProviderEngineServiceException exception
   */
  public FileProviderTraceListType getBatchFile(BatchFileCriteriaType pBatchCriteria, StreamProviderEngineScenarioType pScenario, ContextType pContext) throws RemoteException, WrapperServiceException, StreamProviderEngineServiceException
  {
    FileProviderTraceListType result = new FileProviderTraceListType();
    if ((pBatchCriteria == null) || (pBatchCriteria.getBatchRunKey() == null))
    {
      // TODO log
      return result;
    }
    try
    {
      BatchRunListType batchRunList = getBatchRunList(pBatchCriteria.getBatchRunKey(), pContext);
      if (batchRunList.getValues().isEmpty())
      {
        throw new StreamProviderEngineServiceException(BatchErrorDetail.NO_BATCH_RUN_FOUND, new Object[] { pBatchCriteria.getBatchRunKey().getId() });
      }
      TaskRunListType taskList = new TaskRunListType();
      for (BatchRunType batchRun: batchRunList.getValues())
      {
        TaskRunListType taskChildrenList;
        BatchRunKeyType batchKey = new BatchRunKeyType();
        batchKey.setId(batchRun.getId());
        batchKey.setPath(batchRun.getPath());
        batchKey.setSite(batchRun.getSite());
        taskChildrenList = getTaskRunList(batchKey, pContext);
        addInList(taskList, taskChildrenList);
      }
      for (TaskRunType taskRun: taskList.getValues())
      {
        FileProviderTraceListType fileProviderTraceList;
        fileProviderTraceList = getTaskFile(toTaskFileCriteria(taskRun), pScenario, pContext);
        addInList(result, fileProviderTraceList);
      }
    }
    catch (ServiceException e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /**
   * Permet de récupérer une Liste de ProviderFileType lié au TaskFileCriteriaType passé en paramètre.
   * @param pTaskCriteria : critère de recherche
   * @param pScenario : scénario
   * @param pContext : contexte
   * @return ProviderFileListType : Liste de ProviderFileType
   * @throws RemoteException exception
   * @throws WrapperServiceException exception
   * @throws StreamProviderEngineServiceException exception
   */
  public FileProviderTraceListType getTaskFile(TaskFileCriteriaType pTaskCriteria, StreamProviderEngineScenarioType pScenario, ContextType pContext) throws RemoteException, WrapperServiceException, StreamProviderEngineServiceException
  {
    FileProviderTraceListType result = new FileProviderTraceListType();
    if ((pTaskCriteria == null) || (pTaskCriteria.getTaskRunKey() == null))
    {
      // TODO log
      return result;
    }
    FileProviderTraceManagerService fileProviderTrace = new FileProviderTraceManagerServiceImpl();
    FileProviderTraceCriteriaType fileProviderTraceCriteria = new FileProviderTraceCriteriaType();
    FileProviderTraceCriteriaListType fileProviderTraceCriteriaList = new FileProviderTraceCriteriaListType();
    //création du criteria
    fileProviderTraceCriteria.setTaskId(getCriteriaInteger(pTaskCriteria.getTaskRunKey().getId()));
    fileProviderTraceCriteria.setTaskCode(getCriteriaString(pTaskCriteria.getTaskRunKey().getPath()));
    fileProviderTraceCriteria.setSite(pTaskCriteria.getTaskRunKey().getSite());
    fileProviderTraceCriteria.setDomainList(pTaskCriteria.getDomainList());
    fileProviderTraceCriteriaList.getList().add(fileProviderTraceCriteria);
    // appel au managerModule pour le find
    try
    {
      result = fileProviderTrace.findFileProviderTrace(fileProviderTraceCriteriaList, new FileProviderTraceScenarioType(), pContext);
    }
    catch (ServiceException e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /**
   * Permet de récupérer le contenu du fichier sous forme de texte.
   * @param pProviderFile : Fichier source
   * @param pScenario : scénario
   * @param pContext : contexte
   * @return FileProviderContentType : Contenu du fichier
   * @throws StreamProviderEngineServiceException exception
   */
  public FileProviderContentType getContent(FileProviderTraceType pProviderFile, StreamProviderEngineScenarioType pScenario, ContextType pContext) throws StreamProviderEngineServiceException
  {
    FileProviderContentType result = new FileProviderContentType();
    if (pProviderFile == null)
    {
      // TODO log
      return result;
    }
    String content = loadFile(pProviderFile.getFilePath());
    result = toFileProviderContentType(pProviderFile);
    result.setText(content);
    return result;
  }

  /**
   * Permet de sauvegarder les modifications dans le  fichier source.
   * @param pFileProviderContent : contenu à sauvegarder
   * @param pScenario : scénario
   * @param pContext : contexte
   * @return ProviderFileResponseType : indique sauvegarde s'est bien déroulée
   * @throws StreamProviderEngineServiceException exception
   */
  public ProviderFileResponseType putContent(FileProviderContentType pFileProviderContent, StreamProviderEngineScenarioType pScenario, ContextType pContext) throws StreamProviderEngineServiceException
  {
    ProviderFileResponseType result = new ProviderFileResponseType();
    result.setSuccess(false);
    if (pFileProviderContent == null)
    {
      // TODO log
      return result;
    }
    if ("".equals(pFileProviderContent.getText()))
    {
      result.setMessage("Aucun contenu à sauvegarder.");
      return result;
    }
    try
    {
      writeFile(pFileProviderContent);
      result.setSuccess(true);
    }
    catch (IOException e)
    {
      throw new StreamProviderEngineServiceException(BatchErrorDetail.CAN_NOT_MODIFY_FILE, new Object[] { pFileProviderContent.getFilePath() }, e);
    }
    return result;
  }

  private void writeFile(FileProviderContentType pFileProviderContent) throws IOException
  {
    FileWriter fileWriter = null;
    try
    {
      fileWriter = new FileWriter(pFileProviderContent.getFilePath(), false);
      fileWriter.write(pFileProviderContent.getText());
      fileWriter.flush();
    }
    finally
    {
      if (fileWriter != null)
      {
        fileWriter.close();
      }
    }
  }

  /**
   * créer un criteria à partir d'un integer
   * @param pValue
   * @return CriteriaIntegerType
   */
  private CriteriaIntegerType getCriteriaInteger(int pValue)
  {
    CriteriaIntegerType criteria = new CriteriaIntegerType();
    criteria.setEquals(pValue);
    return criteria;
  }

  /**
   * créer un criteria à partir d'un String
   * @param pValue
   * @return CriteriaStringType
   */
  private CriteriaStringType getCriteriaString(String pValue)
  {
    CriteriaStringType criteria = new CriteriaStringType();
    if (pValue != null)
    {
      criteria.setEquals(pValue);
    }
    return criteria;
  }

  /**
   * récupère pour un batch donné l'ensemble des ses fils.
   * @param pKey
   * @param pContext
   * @return résultat
   * @throws RemoteException exception
   * @throws TechnicalServiceNotDeliveredException exception
   * @throws ServiceException exception
   */
  private BatchRunListType getBatchRunList(BatchRunKeyType pKey, ContextType pContext) throws RemoteException, TechnicalServiceNotDeliveredException, ServiceException
  {
    BatchRunListType result;
    BatchRunManagerServiceImpl batchRunMAnagerService = new BatchRunManagerServiceImpl();
    // on cherche les batchs fils
    result = batchRunMAnagerService.getBatchRunChildrenOfBatchRun(pKey, new BatchRunScenarioType(), pContext);
    //si le service renvoi null, on réinstantie l'objet pour pouvoir ajouter le batch parent
    if (result == null)
    {
      result = new BatchRunListType();
    }
    BatchRunType batchRunParent;
    batchRunParent = batchRunMAnagerService.getBatchRun(pKey, new BatchRunScenarioType(), pContext);
    // on ajoute le batch parent à la liste
    if (batchRunParent != null)
    {
      result.getValues().add(batchRunParent);
    }
    return result;
  }

  /**
   * récupère pour un batch donné l'ensemble de ses tasks
   * @param pKey key
   * @param pContext context
   * @return résultat
   * @throws WrapperServiceException exception
   */
  private TaskRunListType getTaskRunList(BatchRunKeyType pKey, ContextType pContext) throws WrapperServiceException
  {
    TaskRunListType result;
    BatchRunManagerServiceImpl batchRunManagerService = new BatchRunManagerServiceImpl();
    result = batchRunManagerService.getTaskRunChildrenOfBatchRun(pKey, new BatchRunScenarioType(), pContext);
    // Si le service renvoi null, on réinstantie le result pour renvoyer une liste vide
    if (result == null)
    {
      result = new TaskRunListType();
    }
    return result;
  }

  /**
   * ajoute à la premiere liste la seconde liste
   * @param pTaskList
   * @param pTaskChildrenList
   */
  private void addInList(TaskRunListType pTaskList, TaskRunListType pTaskChildrenList)
  {
    if (pTaskList != null && pTaskChildrenList != null)
    {
      for (TaskRunType taskRun: pTaskChildrenList.getValues())
      {
        pTaskList.getValues().add(taskRun);
      }
    }
  }

  /**
   * ajoute à la premiere liste la seconde liste
   * @param pFileProviderTraceList
   * @param pFileProviderTraceListChild
   */
  private void addInList(FileProviderTraceListType pFileProviderTraceList, FileProviderTraceListType pFileProviderTraceListChild)
  {
    if (pFileProviderTraceList != null && pFileProviderTraceListChild != null)
    {
      for (FileProviderTraceType fileProviderTrace: pFileProviderTraceListChild.getList())
      {
        pFileProviderTraceList.getList().add(fileProviderTrace);
      }
    }
  }

  /**
   * transforme le TaskRun en TaskFileCriteria
   * @param pTaskRun
   */
  private TaskFileCriteriaType toTaskFileCriteria(TaskRunType pTaskRun)
  {
    TaskFileCriteriaType taskFileCriteria = new TaskFileCriteriaType();
    taskFileCriteria.setTaskRunKey(pTaskRun);
    return taskFileCriteria;
  }

  /**
   * Charge le contenu du fichier dans une String
   * @param pPathName chemin du fichier
   * @return String : contenu du fichier
   */
  private String loadFile(String pPathName) throws StreamProviderEngineServiceException
  {
    if (pPathName == null)
    {
      throw new StreamProviderEngineServiceException(BatchErrorDetail.CAN_NOT_READ_FILE);
    }
    StringWriter result = new StringWriter();
    try
    {
      writeFile(pPathName, result);
    }
    catch (IOException e)
    {
      throw new StreamProviderEngineServiceException(BatchErrorDetail.CAN_NOT_READ_FILE, new Object[] { pPathName }, e);
    }
    return result.toString();
  }

  private void writeFile(String pPathName, StringWriter pResult) throws FileNotFoundException, IOException
  {
    BufferedInputStream in = null;
    try
    {
      in = new BufferedInputStream(new FileInputStream(new File(pPathName)));
      int b;
      while ((b = in.read()) != -1)
      {
        pResult.write(b);
      }
      pResult.flush();
    }
    finally
    {
      try
      {
        if (in != null)
        {
          in.close();
        }
      }
      finally
      {
        pResult.close();
      }
    }
  }

  /**
   * Transforme le FileProviderTrace en FilProviderContent
   * @param pProviderFile
   * @return résultat
   */
  private FileProviderContentType toFileProviderContentType(FileProviderTraceType pProviderFile)
  {
    FileProviderContentType result = new FileProviderContentType();
    if (pProviderFile == null)
    {
      // TODO log
      return result;
    }
    result.setTaskId(pProviderFile.getTaskId());
    result.setTaskCode(pProviderFile.getTaskCode());
    result.setSite(pProviderFile.getSite());
    result.setProviderName(pProviderFile.getProviderName());
    result.setDomain(pProviderFile.getDomain());
    result.setFilePath(pProviderFile.getFilePath());
    return result;
  }
}
