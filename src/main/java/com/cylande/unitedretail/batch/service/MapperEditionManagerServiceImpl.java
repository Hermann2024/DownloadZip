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
import com.cylande.unitedretail.batch.service.common.MapperEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.MapperEditionCriteriaType;
import com.cylande.unitedretail.message.batch.MapperEditionScenarioType;
import com.cylande.unitedretail.message.batch.MapperEditionType;
import com.cylande.unitedretail.message.batch.MappersEdition;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.ProviderEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProvidersEdition;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class MapperEditionManagerServiceImpl implements MapperEditionManagerService
{
  public static final String MAIN_FILE_NAME = "/Mappers.xml";
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
  public MappersEdition getMainMapper(ProjectDescriptionType project, MapperEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MappersEdition result = new MappersEdition();
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
        MappersEdition edition = readFile(batchFile);
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
  public void setMainMapper(MappersEdition pMappers, MapperEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pMappers != null && pMappers.getProjectDescription() != null)
    {
      try
      {
        String location = pMappers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(pMappers, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public MappersEdition findMapper(MapperEditionCriteriaType pCriteria, MapperEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MappersEdition result = new MappersEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        MappersEdition edition;
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
          if (include.getName().startsWith("Mappers_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (MapperEditionType element: edition.getMapper())
            {
              testName = BatchEditionUtil.testString(pCriteria.getMapperName(), element.getName());
              if (testName)
              {
                result.getMapper().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getMapper().size() == pCriteria.getResultSize())
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

  private MappersEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (MappersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new MappersEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  /** {@inheritDoc} */
  public void postMapper(MappersEdition pMappers, MapperEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pMappers != null && pMappers.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, MappersEdition> editionMap = new HashMap();
        String location = pMappers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        MappersEdition edition;
        // parcours des éléments à modifier
        for (MapperEditionType elementToUpdate: pMappers.getMapper())
        {
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, pMappers.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (MapperEditionType element: edition.getMapper())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getMapper().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getMapper().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, MappersEdition> entry: editionMap.entrySet())
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

  /** {@inheritDoc} */
  public void deleteMapper(MappersEdition pMappers, MapperEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pMappers != null && pMappers.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, MappersEdition> editionMap = new HashMap();
        String location = pMappers.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        MappersEdition edition;
        ProvidersEdition editionRef = null;
        ProviderEditionCriteriaType providerCrit = new ProviderEditionCriteriaType();
        ProviderEditionManagerServiceImpl providerServ = new ProviderEditionManagerServiceImpl();
        // parcours des éléments à supprimer
        for (MapperEditionType elementToDelete: pMappers.getMapper())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          providerCrit.setMapperRef(elementToDelete.getName());
          providerCrit.setResultSize(1);
          editionRef = providerServ.findProvider(providerCrit, null, pContext);
          if (!editionRef.getProvider().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.MAPPER_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getProvider().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, pMappers.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (MapperEditionType element: edition.getMapper())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getMapper().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, MappersEdition> entry: editionMap.entrySet())
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

  private MappersEdition getEditionFromMap(String pFileName, Map<String, MappersEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    MappersEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new MappersEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        MapperEditionScenarioType scenario = new MapperEditionScenarioType();
        scenario.setNotManageDomain(true);
        MappersEdition main = getMainMapper(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainMapper(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }
}
