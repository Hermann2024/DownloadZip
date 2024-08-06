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
import com.cylande.unitedretail.batch.service.common.TriggerEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.BatchEditionCriteriaType;
import com.cylande.unitedretail.message.batch.BatchesEdition;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.TriggerEditionCriteriaType;
import com.cylande.unitedretail.message.batch.TriggerEditionScenarioType;
import com.cylande.unitedretail.message.batch.TriggerEditionType;
import com.cylande.unitedretail.message.batch.TriggersEdition;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class TriggerEditionManagerServiceImpl implements TriggerEditionManagerService
{

  public static final String MAIN_FILE_NAME = "/Triggers.xml";
  private JAXBManager _jaxbManager = new JAXBManager();
  private ContextType _context;

  /** {@inheritDoc} */
  public TriggersEdition getMainTrigger(ProjectDescriptionType project, TriggerEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggersEdition result = new TriggersEdition();
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
        if (pScenario != null)
        {
          if (!Boolean.TRUE.equals(pScenario.isNotManageDomain()) && !batchFile.exists())
          {
            throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
          }
        }
        TriggersEdition edition = readFile(batchFile);
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
  public void setMainTrigger(TriggersEdition pTriggers, TriggerEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTriggers != null && pTriggers.getProjectDescription() != null)
    {
      try
      {
        String location = pTriggers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(pTriggers, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public TriggersEdition findTrigger(TriggerEditionCriteriaType pCriteria, TriggerEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggersEdition result = new TriggersEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        TriggersEdition edition;
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
        boolean stopFind = false, testName;
        for (IncludeXMLType include: includeList)
        {
          if (include.getName().startsWith("Triggers_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (TriggerEditionType element: edition.getTrigger())
            {
              testName = BatchEditionUtil.testString(pCriteria.getTriggerName(), element.getName());
              if (testName)
              {
                result.getTrigger().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getTrigger().size() == pCriteria.getResultSize())
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

  private TriggersEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (TriggersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new TriggersEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  /** {@inheritDoc} */
  public void deleteTrigger(TriggersEdition pTriggers, TriggerEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTriggers != null && pTriggers.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, TriggersEdition> editionMap = new HashMap();
        String location = pTriggers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        TriggersEdition edition;
        BatchesEdition editionRef = null;
        BatchEditionCriteriaType batchCrit = new BatchEditionCriteriaType();
        BatchEditionManagerServiceImpl batchServ = new BatchEditionManagerServiceImpl();
        // parcours des éléments à supprimer
        for (TriggerEditionType elementToDelete: pTriggers.getTrigger())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          batchCrit.setBatchRef(elementToDelete.getName());
          batchCrit.setResultSize(1);
          editionRef = batchServ.findBatch(batchCrit, null, pContext);
          if (!editionRef.getBatch().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.TRIGGER_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getBatch().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, pTriggers.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (TriggerEditionType element: edition.getTrigger())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getTrigger().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, TriggersEdition> entry: editionMap.entrySet())
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

  private TriggersEdition getEditionFromMap(String pFileName, Map<String, TriggersEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    TriggersEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new TriggersEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        TriggerEditionScenarioType scenario = new TriggerEditionScenarioType();
        scenario.setNotManageDomain(true);
        TriggersEdition main = getMainTrigger(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainTrigger(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void postTrigger(TriggersEdition pTriggers, TriggerEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTriggers != null && pTriggers.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, TriggersEdition> editionMap = new HashMap();
        String location = pTriggers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        TriggersEdition edition;
        // parcours des éléments à modifier
        for (TriggerEditionType elementToUpdate: pTriggers.getTrigger())
        {
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, pTriggers.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (TriggerEditionType element: edition.getTrigger())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getTrigger().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getTrigger().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, TriggersEdition> entry: editionMap.entrySet())
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
