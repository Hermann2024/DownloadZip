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
import com.cylande.unitedretail.batch.service.common.PropertyEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.BatchEditionScenarioType;
import com.cylande.unitedretail.message.batch.BatchesEdition;
import com.cylande.unitedretail.message.batch.DomainType;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.MapperEditionScenarioType;
import com.cylande.unitedretail.message.batch.MappersEdition;
import com.cylande.unitedretail.message.batch.ProcessorEditionScenarioType;
import com.cylande.unitedretail.message.batch.ProcessorsEdition;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertiesEdition;
import com.cylande.unitedretail.message.batch.PropertyEditionCriteriaType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.PropertyEditionType;
import com.cylande.unitedretail.message.batch.ProviderEditionScenarioType;
import com.cylande.unitedretail.message.batch.ProvidersEdition;
import com.cylande.unitedretail.message.batch.StylesheetEditionScenarioType;
import com.cylande.unitedretail.message.batch.StylesheetsEdition;
import com.cylande.unitedretail.message.batch.TaskEditionScenarioType;
import com.cylande.unitedretail.message.batch.TasksEdition;
import com.cylande.unitedretail.message.batch.TstmpPropertyEditionType;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class PropertyEditionManagerServiceImpl implements PropertyEditionManagerService
{
  private JAXBManager _jaxbManager = new JAXBManager();
  private ContextType _context;

  /** {@inheritDoc} */
  public PropertiesEdition getDomainList(PropertiesEdition properties, PropertyEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    PropertiesEdition result = new PropertiesEdition();
    List<String> domainList = new ArrayList();
    if (properties != null && properties.getProjectDescription() != null)
    {
      try
      {
        PropertiesEdition edition;
        File batchFile;
        String location = properties.getProjectDescription().getLocation();
        File projectDir = new File(location);
        if (!projectDir.exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        DomainType domain;
        List<IncludeXMLType> includeList;
        if (Boolean.TRUE.equals(pScenario.isFromIncludeXML()))
        {
          includeList = properties.getIncludeXML();
        }
        else
        {
          includeList = new ArrayList();
          for (File file: projectDir.listFiles())
          {
            if (file.isFile() && file.getName().startsWith("Properties"))
            {
              IncludeXMLType include = new IncludeXMLType();
              include.setName(file.getName());
              includeList.add(include);
            }
          }
        }
        for (IncludeXMLType include: includeList)
        {
          if (include.getName().startsWith("Properties"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (PropertyEditionType element: edition.getProperty())
            {
              if (!domainList.contains(element.getDomain()))
              {
                domainList.add(element.getDomain());
                domain = new DomainType();
                domain.setName(element.getDomain());
                if (Boolean.TRUE.equals(pScenario.isDomainListOnInput()))
                {
                  properties.getDomain().add(domain);
                }
                else
                {
                  result.getDomain().add(domain);
                }
              }
            }
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

  /** {@inheritDoc} */
  public PropertiesEdition findProperty(PropertyEditionCriteriaType pCriteria, PropertyEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    PropertiesEdition result = new PropertiesEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        PropertiesEdition edition;
        File batchFile;
        String location = pCriteria.getProjectDescription().getLocation();
        File projectDir = new File(location);
        if (!projectDir.exists())
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
          includeList = new ArrayList();
          for (File file: projectDir.listFiles())
          {
            if (file.isFile() && file.getName().startsWith("Properties"))
            {
              IncludeXMLType include = new IncludeXMLType();
              include.setName(file.getName());
              includeList.add(include);
            }
          }
        }
        boolean stopFind = false;
        boolean testName, testDomain;
        for (IncludeXMLType include: includeList)
        {
          batchFile = new File(location + "/" + include.getName());
          if (!batchFile.exists())
          {
            throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
          }
          edition = readFile(new File(location + "/" + include.getName()));
          for (PropertyEditionType element: edition.getProperty())
          {
            testName = BatchEditionUtil.testString(pCriteria.getPropertyName(), element.getName());
            testDomain = pCriteria.getDomainName() == null || pCriteria.getDomainName().equals(element.getDomain());
            if (testName && testDomain)
            {
              result.getProperty().add(element);
            }
            if (pCriteria.getResultSize() != null && result.getProperty().size() == pCriteria.getResultSize())
            {
              stopFind = true;
              break;
            }
          }
          for (TstmpPropertyEditionType element: edition.getTstmp())
          {
            testName = BatchEditionUtil.testString(pCriteria.getPropertyName(), element.getName());
            testDomain = pCriteria.getDomainName() == null || pCriteria.getDomainName().equals(element.getDomain());
            if (testName && testDomain)
            {
              result.getTstmp().add(element);
            }
            if (pCriteria.getResultSize() != null && result.getTstmp().size() == pCriteria.getResultSize())
            {
              stopFind = true;
              break;
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

  private PropertiesEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (PropertiesEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new PropertiesEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  /** {@inheritDoc} */
  public void deleteProperty(PropertiesEdition properties, PropertyEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (properties != null && properties.getProjectDescription() != null)
    {
      try
      {
        Map<String, PropertiesEdition> editionMap = new HashMap();
        String location = properties.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        PropertiesEdition edition;
        // parcours des éléments à supprimer
        for (PropertyEditionType elementToDelete: properties.getProperty())
        {
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, properties.getProjectDescription());
          int i = 0; // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (PropertyEditionType element: edition.getProperty())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getProperty().remove(i);
              break;
            }
            i++;
          }
        }
        for (TstmpPropertyEditionType elementToDelete: properties.getTstmp())
        {
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, properties.getProjectDescription());
          int i = 0; // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (TstmpPropertyEditionType element: edition.getTstmp())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getTstmp().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, PropertiesEdition> entry: editionMap.entrySet())
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

  private PropertiesEdition getEditionFromMap(String pFileName, Map<String, PropertiesEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    PropertiesEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new PropertiesEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur les fichiers principaux
        BatchEditionManagerServiceImpl batchServ = new BatchEditionManagerServiceImpl();
        BatchEditionScenarioType scenario = new BatchEditionScenarioType();
        scenario.setNotManageDomain(true);
        BatchesEdition main = batchServ.getMainBatch(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        if (main.getPropertiesEdition() == null)
        {
          main.setPropertiesEdition(new PropertiesEdition());
        }
        main.getPropertiesEdition().getIncludeXML().add(include);
        batchServ.setMainBatch(main, null, _context);
        TaskEditionManagerServiceImpl taskServ = new TaskEditionManagerServiceImpl();
        TaskEditionScenarioType taskScenario = new TaskEditionScenarioType();
        taskScenario.setNotManageDomain(true);
        TasksEdition mainTask = taskServ.getMainTask(project, taskScenario, _context);
        if (mainTask.getPropertiesEdition() == null)
        {
          mainTask.setPropertiesEdition(new PropertiesEdition());
        }
        mainTask.getPropertiesEdition().getIncludeXML().add(include);
        taskServ.setMainTask(mainTask, null, _context);
        ProviderEditionManagerServiceImpl providerServ = new ProviderEditionManagerServiceImpl();
        ProviderEditionScenarioType providerScenario = new ProviderEditionScenarioType();
        providerScenario.setNotManageDomain(true);
        ProvidersEdition mainProvider = providerServ.getMainProvider(project, providerScenario, _context);
        if (mainProvider.getPropertiesEdition() == null)
        {
          mainProvider.setPropertiesEdition(new PropertiesEdition());
        }
        mainProvider.getPropertiesEdition().getIncludeXML().add(include);
        providerServ.setMainProvider(mainProvider, null, _context);
        ProcessorEditionManagerServiceImpl processorServ = new ProcessorEditionManagerServiceImpl();
        ProcessorEditionScenarioType processorScenario = new ProcessorEditionScenarioType();
        processorScenario.setNotManageDomain(true);
        ProcessorsEdition mainProcessor = processorServ.getMainProcessor(project, processorScenario, _context);
        if (mainProcessor.getPropertiesEdition() == null)
        {
          mainProcessor.setPropertiesEdition(new PropertiesEdition());
        }
        mainProcessor.getPropertiesEdition().getIncludeXML().add(include);
        processorServ.setMainProcessor(mainProcessor, null, _context);
        StylesheetEditionManagerServiceImpl stylesheetServ = new StylesheetEditionManagerServiceImpl();
        StylesheetEditionScenarioType stylesheetScenario = new StylesheetEditionScenarioType();
        stylesheetScenario.setNotManageDomain(true);
        StylesheetsEdition mainStylesheet = stylesheetServ.getMainStylesheet(project, stylesheetScenario, _context);
        if (mainStylesheet.getPropertiesEdition() == null)
        {
          mainStylesheet.setPropertiesEdition(new PropertiesEdition());
        }
        mainStylesheet.getPropertiesEdition().getIncludeXML().add(include);
        stylesheetServ.setMainStylesheet(mainStylesheet, null, _context);
        MapperEditionManagerServiceImpl mapperServ = new MapperEditionManagerServiceImpl();
        MapperEditionScenarioType mapperScenario = new MapperEditionScenarioType();
        mapperScenario.setNotManageDomain(true);
        MappersEdition mainMapper = mapperServ.getMainMapper(project, mapperScenario, _context);
        if (mainMapper.getPropertiesEdition() == null)
        {
          mainMapper.setPropertiesEdition(new PropertiesEdition());
        }
        mainMapper.getPropertiesEdition().getIncludeXML().add(include);
        mapperServ.setMainMapper(mainMapper, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void postProperty(PropertiesEdition properties, PropertyEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (properties != null && properties.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, PropertiesEdition> editionMap = new HashMap();
        String location = properties.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        PropertiesEdition edition;
        // parcours des éléments à modifier
        for (PropertyEditionType elementToUpdate: properties.getProperty())
        {
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, properties.getProjectDescription());
          boolean isCreate = true;
          int i = 0; // parcours des éléments du fichier pour trouver l'élément à modifier
          for (PropertyEditionType element: edition.getProperty())
          {
            if (element.getName().equals(elementToUpdate.getName()) && element.getDomain().equals(elementToUpdate.getDomain()))
            {
              edition.getProperty().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getProperty().add(elementToUpdate);
          }
        }
        for (TstmpPropertyEditionType elementToUpdate: properties.getTstmp())
        {
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, properties.getProjectDescription());
          boolean isCreate = true;
          int i = 0; // parcours des éléments du fichier pour trouver l'élément à modifier
          for (TstmpPropertyEditionType element: edition.getTstmp())
          {
            if (element.getName().equals(elementToUpdate.getName()) && element.getDomain().equals(elementToUpdate.getDomain()))
            {
              edition.getTstmp().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getTstmp().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, PropertiesEdition> entry: editionMap.entrySet())
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
