package com.cylande.unitedretail.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cylande.unitedretail.batch.exception.BatchEditionErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.common.StylesheetEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.ProcessorEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProcessorsEdition;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.StylesheetEditionCriteriaType;
import com.cylande.unitedretail.message.batch.StylesheetEditionScenarioType;
import com.cylande.unitedretail.message.batch.StylesheetEditionType;
import com.cylande.unitedretail.message.batch.StylesheetsEdition;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class StylesheetEditionManagerServiceImpl implements StylesheetEditionManagerService
{
  public static final String MAIN_FILE_NAME = "/Stylesheets.xml";
  private static final PropertyEditionScenarioType DOMAIN_PROPERTY_SCENARIO = new PropertyEditionScenarioType();
  private JAXBManager _jaxbManager = new JAXBManager();
  private PropertyEditionManagerServiceImpl _propertyServ = new PropertyEditionManagerServiceImpl();
  static
  {
    DOMAIN_PROPERTY_SCENARIO.setFromIncludeXML(true);
    DOMAIN_PROPERTY_SCENARIO.setDomainListOnInput(true);
  }
  private ContextType _context;

  /** {@inheritDoc} */
  public StylesheetsEdition getMainStylesheet(ProjectDescriptionType project, StylesheetEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    StylesheetsEdition result = new StylesheetsEdition();
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
        StylesheetsEdition edition = readFile(batchFile);
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
  public void setMainStylesheet(StylesheetsEdition pStylesheets, StylesheetEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pStylesheets != null && pStylesheets.getProjectDescription() != null)
    {
      try
      {
        String location = pStylesheets.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(pStylesheets, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public StylesheetsEdition findStylesheet(StylesheetEditionCriteriaType pCriteria, StylesheetEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    StylesheetsEdition result = new StylesheetsEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        StylesheetsEdition edition;
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
          if (include.getName().startsWith("Stylesheets_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (StylesheetEditionType element: edition.getStylesheet())
            {
              testName = BatchEditionUtil.testString(pCriteria.getStylesheetName(), element.getName());
              if (testName)
              {
                result.getStylesheet().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getStylesheet().size() == pCriteria.getResultSize())
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

  private StylesheetsEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (StylesheetsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new StylesheetsEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  /** {@inheritDoc} */
  public void deleteStylesheet(StylesheetsEdition pStylesheets, StylesheetEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pStylesheets != null && pStylesheets.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, StylesheetsEdition> editionMap = new HashMap();
        String location = pStylesheets.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        StylesheetsEdition edition;
        ProcessorsEdition editionRef = null;
        ProcessorEditionCriteriaType processCrit = new ProcessorEditionCriteriaType();
        ProcessorEditionManagerServiceImpl processServ = new ProcessorEditionManagerServiceImpl();
        // parcours des éléments à supprimer
        for (StylesheetEditionType elementToDelete: pStylesheets.getStylesheet())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          processCrit.setStylesheetRef(elementToDelete.getName());
          processCrit.setResultSize(1);
          editionRef = processServ.findProcessor(processCrit, null, pContext);
          if (!editionRef.getProcessor().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.STYLESHEET_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getProcessor().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, pStylesheets.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (StylesheetEditionType element: edition.getStylesheet())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getStylesheet().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, StylesheetsEdition> entry: editionMap.entrySet())
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

  private StylesheetsEdition getEditionFromMap(String pFileName, Map<String, StylesheetsEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    StylesheetsEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new StylesheetsEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        StylesheetEditionScenarioType scenario = new StylesheetEditionScenarioType();
        scenario.setNotManageDomain(true);
        StylesheetsEdition main = getMainStylesheet(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainStylesheet(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void postStylesheet(StylesheetsEdition pStylesheets, StylesheetEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pStylesheets != null && pStylesheets.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, StylesheetsEdition> editionMap = new HashMap();
        String location = pStylesheets.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        StylesheetsEdition edition;
        // parcours des éléments à modifier
        for (StylesheetEditionType elementToUpdate: pStylesheets.getStylesheet())
        {
          if (elementToUpdate.getContent() != null)
          {
            writeFile(location, elementToUpdate);
            elementToUpdate.setContent(null);
          }
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, pStylesheets.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (StylesheetEditionType element: edition.getStylesheet())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getStylesheet().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getStylesheet().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, StylesheetsEdition> entry: editionMap.entrySet())
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

  private void writeFile(String pLocation, StylesheetEditionType pElementToUpdate) throws IOException
  {
    FileWriter fw = null;
    try
    {
      fw = new FileWriter(pLocation + "/" + pElementToUpdate.getFile().getDir() + "/" + pElementToUpdate.getFile().getKwInclude());
      fw.write(pElementToUpdate.getContent());
    }
    finally
    {
      if (fw != null)
      {
        fw.close();
      }
    }
  }
}
