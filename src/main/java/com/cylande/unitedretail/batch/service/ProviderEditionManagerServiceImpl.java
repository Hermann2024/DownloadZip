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
import com.cylande.unitedretail.batch.service.common.ProviderEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.ProviderEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProviderEditionScenarioType;
import com.cylande.unitedretail.message.batch.ProviderEditionType;
import com.cylande.unitedretail.message.batch.ProvidersEdition;
import com.cylande.unitedretail.message.batch.TaskEditionCriteriaType;
import com.cylande.unitedretail.message.batch.TasksEdition;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class ProviderEditionManagerServiceImpl implements ProviderEditionManagerService
{
  public static final String MAIN_FILE_NAME = "/Providers.xml";
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
  public ProvidersEdition getMainProvider(ProjectDescriptionType project, ProviderEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProvidersEdition result = new ProvidersEdition();
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
        ProvidersEdition edition = readFile(batchFile);
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
  public void setMainProvider(ProvidersEdition providers, ProviderEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (providers != null && providers.getProjectDescription() != null)
    {
      try
      {
        String location = providers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(providers, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public ProvidersEdition findProvider(ProviderEditionCriteriaType pCriteria, ProviderEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProvidersEdition result = new ProvidersEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        ProvidersEdition edition;
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
        boolean testName, testType, testMapperRef;
        for (IncludeXMLType include: includeList)
        {
          if (include.getName().startsWith("Providers_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (ProviderEditionType element: edition.getProvider())
            {
              testName = BatchEditionUtil.testString(pCriteria.getProviderName(), element.getName());
              testType = pCriteria.getProviderType() == null || pCriteria.getProviderType().equals(element.getType());
              testMapperRef = pCriteria.getMapperRef() == null || (element.getMapper() != null && pCriteria.getMapperRef().equals(element.getMapper().getRef()));
              if (testName && testType && testMapperRef)
              {
                result.getProvider().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getProvider().size() == pCriteria.getResultSize())
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

  private ProvidersEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (ProvidersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new ProvidersEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  /** {@inheritDoc} */
  public void deleteProvider(ProvidersEdition providers, ProviderEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (providers != null && providers.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, ProvidersEdition> editionMap = new HashMap();
        String location = providers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        ProvidersEdition edition;
        TasksEdition editionRef = null;
        TaskEditionCriteriaType taskCrit = new TaskEditionCriteriaType();
        TaskEditionManagerServiceImpl taskServ = new TaskEditionManagerServiceImpl();
        // parcours des éléments à supprimer
        for (ProviderEditionType elementToDelete: providers.getProvider())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          taskCrit.setProviderRef(elementToDelete.getName());
          taskCrit.setResultSize(1);
          editionRef = taskServ.findTask(taskCrit, null, pContext);
          if (!editionRef.getTask().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.PROVIDER_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getTask().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, providers.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (ProviderEditionType element: edition.getProvider())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getProvider().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, ProvidersEdition> entry: editionMap.entrySet())
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

  private ProvidersEdition getEditionFromMap(String pFileName, Map<String, ProvidersEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    ProvidersEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new ProvidersEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        ProviderEditionScenarioType scenario = new ProviderEditionScenarioType();
        scenario.setNotManageDomain(true);
        ProvidersEdition main = getMainProvider(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainProvider(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void postProvider(ProvidersEdition providers, ProviderEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (providers != null && providers.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, ProvidersEdition> editionMap = new HashMap();
        String location = providers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        ProvidersEdition edition;
        // parcours des éléments à modifier
        for (ProviderEditionType elementToUpdate: providers.getProvider())
        {
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, providers.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (ProviderEditionType element: edition.getProvider())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getProvider().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getProvider().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, ProvidersEdition> entry: editionMap.entrySet())
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
