package com.cylande.unitedretail.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.cylande.unitedretail.batch.exception.BatchEditionErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.common.ProcessorEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.batch.tools.Populate;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.ServiceLocator;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.OperationServiceType;
import com.cylande.unitedretail.message.batch.ProcessorEditionBlockType;
import com.cylande.unitedretail.message.batch.ProcessorEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProcessorEditionScenarioType;
import com.cylande.unitedretail.message.batch.ProcessorEditionType;
import com.cylande.unitedretail.message.batch.ProcessorsEdition;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.ServiceListType;
import com.cylande.unitedretail.message.batch.ServiceType;
import com.cylande.unitedretail.message.batch.TaskEditionCriteriaType;
import com.cylande.unitedretail.message.batch.TasksEdition;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.process.ProcessorAbstractBlockContentType;
import com.cylande.unitedretail.message.process.ProcessorBlockType;
import com.cylande.unitedretail.message.process.ProcessorCopyType;
import com.cylande.unitedretail.message.process.ProcessorServiceType;
import com.cylande.unitedretail.message.process.ProcessorXsltType;

/** {@inheritDoc} */
public class ProcessorEditionManagerServiceImpl implements ProcessorEditionManagerService
{
  public static final String MAIN_FILE_NAME = "/Processors.xml";
  private static final PropertyEditionScenarioType DOMAIN_PROPERTY_SCENARIO = new PropertyEditionScenarioType();
  static
  {
    DOMAIN_PROPERTY_SCENARIO.setFromIncludeXML(true);
    DOMAIN_PROPERTY_SCENARIO.setDomainListOnInput(true);
  }
  private JAXBManager _jaxbManager = new JAXBManager();
  private PropertyEditionManagerServiceImpl _propertyServ = new PropertyEditionManagerServiceImpl();
  private ContextType _context;
  private DocumentBuilderFactory _docBuilderFactory = DocumentBuilderFactory.newInstance();
  private TransformerFactory _transFactory = TransformerFactory.newInstance();

  /** {@inheritDoc} */
  public ProcessorsEdition getMainProcessor(ProjectDescriptionType project, ProcessorEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProcessorsEdition result = new ProcessorsEdition();
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
          throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { location });
        }
        ProcessorsEdition edition = readFile(batchFile);
        Transformer trans = _transFactory.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        for (IncludeXMLType include: edition.getIncludeXML())
        {
          if (include.getName().equals("Variables.xml"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            ProcessorsEdition vars = new ProcessorsEdition();
            vars = (ProcessorsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(location + "/" + include.getName()), vars);
            BatchEditionUtil.valueVarProcessToString(vars.getVariables(), trans, null);
            edition.setVariables(vars.getVariables());
            break;
          }
        }
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
  public void setMainProcessor(ProcessorsEdition processors, ProcessorEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (processors != null && processors.getProjectDescription() != null)
    {
      try
      {
        String location = processors.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(processors, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public ServiceListType findService(CriteriaStringType pCriteria, ProcessorEditionScenarioType pScenario, ContextType pContext)
  {
    ServiceListType result = new ServiceListType();
    ServiceType serv;
    boolean find;
    for (String ref: ServiceLocator.getInstance().getServiceKeySet())
    {
      find = (pCriteria.getStartsWith() != null && ref.startsWith(pCriteria.getStartsWith())) || (pCriteria.getContains() != null && ref.contains(pCriteria.getContains()))
          || ref.equals(pCriteria.getEquals());
      if (find)
      {
        serv = new ServiceType();
        serv.setRef(ref);
        result.getValues().add(serv);
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  public ServiceType getService(ServiceType pService, ProcessorEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ServiceType result = null;
    Class servClass = null;
    try
    {
      if (pService.getRef() != null)
      {
        Object obj = ServiceLocator.getInstance().getService(pService.getRef());
        servClass = obj != null ? obj.getClass() : null;
      }
      else if (pService.getClassName() != null)
      {
        try
        {
          servClass = Class.forName(pService.getClassName());
        }
        catch (ClassNotFoundException e)
        {
          throw new BatchException(BatchEditionErrorDetail.CLASS_NOT_FOUND, new Object[] { pService.getClassName() });
        }
      }
      if (servClass != null)
      {
        result = new ServiceType();
        result.setRef(pService.getRef());
        result.setClassName(servClass.getName());
        Class[] classes = servClass.getInterfaces();
        if (classes.length > 0)
        {
          Method[] methods = classes[0].getMethods();
          Method method = null;
          OperationServiceType operation;
          Populate populate = new Populate();
          for (int i = 0; i < methods.length; i++)
          {
            method = methods[i];
            operation = new OperationServiceType();
            operation.setName(method.getName());
            Class[] params = method.getParameterTypes();
            if (params.length == 3)
            {
              operation.setInputClassName(params[0].getName());
              operation.setInputPopulate(populate.create(params[0]));
              operation.setScenarioClassName(params[1].getName());
              operation.setScenarioPopulate(populate.create(params[1]));
            }
            else if (params.length == 2)
            {
              operation.setScenarioClassName(params[0].getName());
              operation.setScenarioPopulate(populate.create(params[0]));
            }
            Class returnClass = method.getReturnType();
            operation.setOutputClassName(returnClass != Void.TYPE ? returnClass.getName() : null);
            result.getOperation().add(operation);
          }
        }
      }
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProcessorsEdition findProcessor(ProcessorEditionCriteriaType pCriteria, ProcessorEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProcessorsEdition result = new ProcessorsEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        ProcessorsEdition edition;
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
            throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { location });
          }
          edition = readFile(batchFile);
          includeList = edition.getIncludeXML();
        }
        Transformer trans = _transFactory.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        boolean stopFind = false;
        boolean testName, testXsltRef, testPattern;
        Boolean pattern;
        for (IncludeXMLType include: includeList)
        {
          if (include.getName().startsWith("Processors_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            for (ProcessorEditionType element: edition.getProcessor())
            {
              testName = BatchEditionUtil.testString(pCriteria.getProcessorName(), element.getName());
              testXsltRef = pCriteria.getStylesheetRef() == null || isBlockUseStylesheetRef(element.getBlock(), pCriteria.getStylesheetRef());
              pattern = element.isPattern() == null ? Boolean.FALSE : element.isPattern();
              testPattern = pCriteria.getPattern() == null || pCriteria.getPattern().getEquals().equals(pattern);
              if (testName && testXsltRef && testPattern)
              {
                setBlockEdition(element);
                if (element.getVariables() != null)
                {
                  BatchEditionUtil.valueVarProcessToString(element.getVariables(), trans, element.getName());
                }
                result.getProcessor().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getProcessor().size() == pCriteria.getResultSize())
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

  private ProcessorsEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (ProcessorsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new ProcessorsEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  private boolean isBlockUseStylesheetRef(ProcessorBlockType pBlock, String pValueRef)
  {
    if (pValueRef != null && pBlock != null)
    {
      for (ProcessorAbstractBlockContentType content: pBlock.getCopyOrXsltOrService())
      {
        if (content instanceof ProcessorXsltType)
        {
          ProcessorXsltType xslt = (ProcessorXsltType)content;
          if (xslt.getStyle() != null && pValueRef.equals(xslt.getStyle().getRef()))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Permet de rendre le block du processor lisible pour le Flex
   * @param processor
   */
  private void setBlockEdition(ProcessorEditionType processor)
  {
    if (processor.getBlock() != null)
    {
      ProcessorEditionBlockType blockEdition;
      for (ProcessorAbstractBlockContentType blockContent: processor.getBlock().getCopyOrXsltOrService())
      {
        blockEdition = new ProcessorEditionBlockType();
        if (blockContent instanceof ProcessorCopyType)
        {
          blockEdition.setCopy((ProcessorCopyType)blockContent);
        }
        else if (blockContent instanceof ProcessorXsltType)
        {
          blockEdition.setXslt((ProcessorXsltType)blockContent);
        }
        else if (blockContent instanceof ProcessorServiceType)
        {
          blockEdition.setService((ProcessorServiceType)blockContent);
        }
        processor.getBlockEdition().add(blockEdition);
      }
      processor.setBlock(null);
    }
  }

  /**
   * Alimentation des balises standards car blockEdition a été créé pour la compatibilité avec Flex
   * qui n'est pas compatible avec des beans utilisant un choice dans les schémas XSD
   * @param processor
   */
  private void setCopyOrXsltOrService(ProcessorEditionType processor)
  {
    if (!processor.getBlockEdition().isEmpty())
    {
      processor.setBlock(new ProcessorBlockType());
      for (ProcessorEditionBlockType blockEdition: processor.getBlockEdition())
      {
        if (blockEdition.getCopy() != null)
        {
          processor.getBlock().getCopyOrXsltOrService().add(blockEdition.getCopy());
        }
        else if (blockEdition.getXslt() != null)
        {
          processor.getBlock().getCopyOrXsltOrService().add(blockEdition.getXslt());
        }
        else if (blockEdition.getService() != null)
        {
          processor.getBlock().getCopyOrXsltOrService().add(blockEdition.getService());
        }
      }
      processor.setBlockEdition(null);
    }
  }

  /** {@inheritDoc} */
  public void postProcessor(ProcessorsEdition processors, ProcessorEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (processors != null && processors.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, ProcessorsEdition> editionMap = new HashMap();
        String location = processors.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        ProcessorsEdition edition;
        // parcours des éléments à modifier
        for (ProcessorEditionType elementToUpdate: processors.getProcessor())
        {
          BatchEditionUtil.valueVarToNode(elementToUpdate.getVariables(), _docBuilderFactory);
          setCopyOrXsltOrService(elementToUpdate);
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, processors.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (ProcessorEditionType element: edition.getProcessor())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getProcessor().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getProcessor().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, ProcessorsEdition> entry: editionMap.entrySet())
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
  public void deleteProcessor(ProcessorsEdition processors, ProcessorEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (processors != null && processors.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, ProcessorsEdition> editionMap = new HashMap();
        String location = processors.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        ProcessorsEdition edition;
        TasksEdition editionRef = null;
        TaskEditionCriteriaType taskCrit = new TaskEditionCriteriaType();
        TaskEditionManagerServiceImpl taskServ = new TaskEditionManagerServiceImpl();
        // parcours des éléments à supprimer
        for (ProcessorEditionType elementToDelete: processors.getProcessor())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          taskCrit.setProviderRef(elementToDelete.getName());
          taskCrit.setResultSize(1);
          editionRef = taskServ.findTask(taskCrit, null, pContext);
          if (!editionRef.getTask().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.PROCESSOR_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getTask().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, processors.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (ProcessorEditionType element: edition.getProcessor())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getProcessor().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, ProcessorsEdition> entry: editionMap.entrySet())
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

  private ProcessorsEdition getEditionFromMap(String pFileName, Map<String, ProcessorsEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    ProcessorsEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new ProcessorsEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        ProcessorEditionScenarioType scenario = new ProcessorEditionScenarioType();
        scenario.setNotManageDomain(true);
        ProcessorsEdition main = getMainProcessor(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainProcessor(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }
}
