package com.cylande.unitedretail.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cylande.unitedretail.batch.exception.BatchEditionErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.common.TaskEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.AbstractStream;
import com.cylande.unitedretail.message.batch.BatchEditionCriteriaType;
import com.cylande.unitedretail.message.batch.BatchesEdition;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.TaskEditionCriteriaType;
import com.cylande.unitedretail.message.batch.TaskEditionScenarioType;
import com.cylande.unitedretail.message.batch.TaskEditionType;
import com.cylande.unitedretail.message.batch.TasksEdition;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class TaskEditionManagerServiceImpl implements TaskEditionManagerService
{
  public static final String MAIN_FILE_NAME = "/Tasks.xml";
  private static final PropertyEditionScenarioType DOMAIN_PROPERTY_SCENARIO = new PropertyEditionScenarioType();
  static
  {
    DOMAIN_PROPERTY_SCENARIO.setFromIncludeXML(true);
    DOMAIN_PROPERTY_SCENARIO.setDomainListOnInput(true);
  }
  private JAXBManager _jaxbManager = new JAXBManager();
  private PropertyEditionManagerServiceImpl _propertyServ = new PropertyEditionManagerServiceImpl();
  private ContextType _context;

  /** {@inheritDoc} */
  public TasksEdition getMainTask(ProjectDescriptionType project, TaskEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TasksEdition result = new TasksEdition();
    try
    {
      if (project != null && project.getLocation() != null)
      {
        String location = project.getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        File batchFile = new File(location + MAIN_FILE_NAME);
        if (!batchFile.exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
        }
        TasksEdition edition = readFile(batchFile);
        if (pScenario != null)
        {
          if (!Boolean.TRUE.equals(pScenario.isNotManageDomain()) && edition.getPropertiesEdition() != null)
          {
            edition.getPropertiesEdition().setProjectDescription(project);
            _propertyServ.getDomainList(edition.getPropertiesEdition(), DOMAIN_PROPERTY_SCENARIO, pContext);
            edition.getPropertiesEdition().setProjectDescription(null);
          }
        }
        return edition;
      }
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /** {@inheritDoc} */
  public void setMainTask(TasksEdition pTasks, TaskEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTasks != null && pTasks.getProjectDescription() != null)
    {
      try
      {
        String location = pTasks.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(pTasks, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public TasksEdition findTask(TaskEditionCriteriaType pCriteria, TaskEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TasksEdition result = new TasksEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        TasksEdition edition;
        File batchFile;
        String location = pCriteria.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        List<IncludeXMLType> includeList;
        if (pCriteria.getFileName() != null)
        {
          IncludeXMLType include = new IncludeXMLType();
          include.setName(pCriteria.getFileName());
          includeList = new ArrayList();
          includeList.add(include);
        }
        else
        {
          batchFile = new File(location + MAIN_FILE_NAME);
          if (!batchFile.exists())
          {
            throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
          }
          edition = readFile(batchFile);
          includeList = edition.getIncludeXML();
        }
        boolean stopFind = false;
        boolean testName, testType, testProviderRef, testProcessorRef;
        for (IncludeXMLType include: includeList)
        {
          if (include.getName().startsWith("Tasks_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (TaskEditionType element: edition.getTask())
            {
              testName = BatchEditionUtil.testString(pCriteria.getTaskName(), element.getName());
              testType = pCriteria.getTaskType() == null || pCriteria.getTaskType().equals(element.getType());
              testProviderRef = pCriteria.getProviderRef() == null || isStreamUseRef(element.getInput(), pCriteria.getProviderRef())
                  || isStreamUseRef(element.getResponse(), pCriteria.getProviderRef()) || isStreamUseRef(element.getReject(), pCriteria.getProviderRef());
              testProcessorRef = pCriteria.getProcessorRef() == null || (element.getProcessor() != null && pCriteria.getProcessorRef().equals(element.getProcessor().getRef()));
              if (testName && testType && testProviderRef && testProcessorRef)
              {
                result.getTask().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getTask().size() == pCriteria.getResultSize())
              {
                stopFind = true;
                break;
              }
            }
          }
          if (stopFind)
          {
            break;
          }
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
    return result;
  }

  private TasksEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (TasksEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new TasksEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  private boolean isStreamUseRef(AbstractStream pStream, String pValueRef)
  {
    if (pStream != null && pStream.getProvider() != null && pValueRef.equals(pStream.getProvider().getRef()))
    {
      return true;
    }
    return false;
  }

  /** {@inheritDoc} */
  public void deleteTask(TasksEdition pTasks, TaskEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTasks != null && pTasks.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, TasksEdition> editionMap = new HashMap();
        String location = pTasks.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        TasksEdition edition;
        BatchesEdition editionRef = null;
        BatchEditionCriteriaType batchCrit = new BatchEditionCriteriaType();
        BatchEditionManagerServiceImpl batchServ = new BatchEditionManagerServiceImpl();
        // parcours des éléments à supprimer
        for (TaskEditionType elementToDelete: pTasks.getTask())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          batchCrit.setBatchRef(elementToDelete.getName());
          batchCrit.setResultSize(1);
          editionRef = batchServ.findBatch(batchCrit, null, pContext);
          if (!editionRef.getBatch().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.TASK_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getBatch().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, pTasks.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (TaskEditionType element: edition.getTask())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getTask().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, TasksEdition> entry: editionMap.entrySet())
        {
          _jaxbManager.write(entry.getValue(), location + "/" + entry.getKey());
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  private TasksEdition getEditionFromMap(String pFileName, Map<String, TasksEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    TasksEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new TasksEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        TaskEditionScenarioType scenario = new TaskEditionScenarioType();
        scenario.setNotManageDomain(true);
        TasksEdition main = getMainTask(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainTask(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void postTask(TasksEdition pTasks, TaskEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTasks != null && pTasks.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, TasksEdition> editionMap = new HashMap();
        String location = pTasks.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        TasksEdition edition;
        // parcours des éléments à modifier
        for (TaskEditionType elementToUpdate: pTasks.getTask())
        {
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, pTasks.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (TaskEditionType element: edition.getTask())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getTask().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getTask().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, TasksEdition> entry: editionMap.entrySet())
        {
          _jaxbManager.write(entry.getValue(), location + "/" + entry.getKey());
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }
}
