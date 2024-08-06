package com.cylande.unitedretail.batch.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;

import com.cylande.unitedretail.batch.batch.BatchEntityResolver;
import com.cylande.unitedretail.batch.exception.BatchEditionErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.common.BatchEditionEngineService;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.FilenameUtil;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.framework.tools.ZipManager;
import com.cylande.unitedretail.framework.tools.filemanagement.DirectoryFileManager;
import com.cylande.unitedretail.message.batch.AbstractParentEditionType;
import com.cylande.unitedretail.message.batch.AbstractStream;
import com.cylande.unitedretail.message.batch.BatchChildEnum;
import com.cylande.unitedretail.message.batch.BatchEditionChildAbstractType;
import com.cylande.unitedretail.message.batch.BatchEditionCriteriaType;
import com.cylande.unitedretail.message.batch.BatchEditionGenerateParameterType;
import com.cylande.unitedretail.message.batch.BatchEditionScenarioType;
import com.cylande.unitedretail.message.batch.BatchEditionType;
import com.cylande.unitedretail.message.batch.BatchesEdition;
import com.cylande.unitedretail.message.batch.CorpusEditionType;
import com.cylande.unitedretail.message.batch.DomainType;
import com.cylande.unitedretail.message.batch.EditionCopyActionType;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.InitBatchEditionType;
import com.cylande.unitedretail.message.batch.MapperEditionCriteriaType;
import com.cylande.unitedretail.message.batch.MapperEditionType;
import com.cylande.unitedretail.message.batch.MappersEdition;
import com.cylande.unitedretail.message.batch.OperationServiceType;
import com.cylande.unitedretail.message.batch.ProcessorEditionBlockType;
import com.cylande.unitedretail.message.batch.ProcessorEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProcessorEditionType;
import com.cylande.unitedretail.message.batch.ProcessorsEdition;
import com.cylande.unitedretail.message.batch.ProjectDescriptionListType;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertiesEdition;
import com.cylande.unitedretail.message.batch.PropertyEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProviderEditionCriteriaType;
import com.cylande.unitedretail.message.batch.ProviderEditionType;
import com.cylande.unitedretail.message.batch.ProvidersEdition;
import com.cylande.unitedretail.message.batch.ServiceType;
import com.cylande.unitedretail.message.batch.StylesheetEditionCriteriaType;
import com.cylande.unitedretail.message.batch.StylesheetEditionType;
import com.cylande.unitedretail.message.batch.StylesheetsEdition;
import com.cylande.unitedretail.message.batch.TaskEditionCriteriaType;
import com.cylande.unitedretail.message.batch.TaskEditionType;
import com.cylande.unitedretail.message.batch.TasksEdition;
import com.cylande.unitedretail.message.batch.TriggerEditionCriteriaType;
import com.cylande.unitedretail.message.batch.TriggerEditionType;
import com.cylande.unitedretail.message.batch.TriggersEdition;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.sales.upload.UploadFileType;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;

/** {@inheritDoc} */
public class BatchEditionEngineServiceImpl implements BatchEditionEngineService
{
  public static final String MAINFILE_LIST = ";Batches.xml;Batchs.xml;Tasks.xml;Triggers.xml;Providers.xml;Processors.xml;Mappers.xml;Stylesheets.xml;";
  public static final Map<String, String> TAGNAME_CONVERSION_MAP = new HashMap<String, String>();
  private static final Pattern ENTITY_PATTERN = Pattern.compile("\\s*<!ENTITY [\\w]+ SYSTEM \"([\\w/\\\\]+.xml)\">\\s*");
  private static final Pattern INCLUDE_PATTERN = Pattern.compile("\\s*&([\\w]+);\\s*");
  private static final Pattern TAGNAME_PATTERN = Pattern.compile("\\s*</?([\\w]+).*>\\s*");
  private static final String DEFAULT_HOME = System.getProperty("user.home") + "/batchesEdition";
  private static final String[] XSL_LIST = { "Batch.xsl", "Task.xsl", "Processor.xsl", "Provider.xsl", "Trigger.xsl", "Property.xsl", "Mapper.xsl", "Stylesheet.xsl" };
  static {
    TAGNAME_CONVERSION_MAP.put("batchs", "batchesEdition");
    TAGNAME_CONVERSION_MAP.put("tasks", "tasksEdition");
    TAGNAME_CONVERSION_MAP.put("providers", "providersEdition");
    TAGNAME_CONVERSION_MAP.put("processors", "processorsEdition");
    TAGNAME_CONVERSION_MAP.put("triggers", "triggersEdition");
    TAGNAME_CONVERSION_MAP.put("properties", "propertiesEdition");
    TAGNAME_CONVERSION_MAP.put("mappers", "mappersEdition");
    TAGNAME_CONVERSION_MAP.put("stylesheets", "stylesheetsEdition");
  }
  private JAXBManager _jaxbManager = new JAXBManager();
  private Map<String, BatchEditionType> _batchMap;
  private Map<String, TaskEditionType> _taskMap;
  private Map<String, TriggerEditionType> _triggerMap;
  private Map<String, ProcessorEditionType> _processorMap;
  private Map<String, ProviderEditionType> _providerMap;
  private Map<String, StylesheetEditionType> _xsltMap;
  private Map<String, MapperEditionType> _mapperMap;
  private Map<String, String> _domainMap;
  private PropertyEditionManagerServiceImpl _propertyServ = new PropertyEditionManagerServiceImpl();
  private BatchEditionManagerServiceImpl _batchServ = new BatchEditionManagerServiceImpl();
  private TaskEditionManagerServiceImpl _taskServ = new TaskEditionManagerServiceImpl();
  private TriggerEditionManagerServiceImpl _triggerServ = new TriggerEditionManagerServiceImpl();
  private ProcessorEditionManagerServiceImpl _processorServ = new ProcessorEditionManagerServiceImpl();
  private ProviderEditionManagerServiceImpl _providerServ = new ProviderEditionManagerServiceImpl();
  private StylesheetEditionManagerServiceImpl _xsltServ = new StylesheetEditionManagerServiceImpl();
  private MapperEditionManagerServiceImpl _mapperServ = new MapperEditionManagerServiceImpl();
  private CorpusEditionType _corpus;
  private ProjectDescriptionType _project = new ProjectDescriptionType();

  /** {@inheritDoc} */
  public void generateBatch(BatchEditionGenerateParameterType param, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (param.getService() != null && param.getService().getRef() != null && param.getIncludeXMLSuffix() != null && param.getProjectDescription() != null && param.getXslDir() != null)
    {
      try
      {
        ServiceType service = _processorServ.getService(param.getService(), null, pContext);
        if (service != null)
        {
          File projectDir = new File(param.getProjectDescription().getLocation());
          if (!projectDir.exists())
          {
            throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { param.getProjectDescription().getLocation() });
          }
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          _jaxbManager.write(service, outputStream);
          byte[] xmlSourceBytes = outputStream.toString().getBytes();
          String result = transform(param, "/Batch.xsl", xmlSourceBytes);
          if (result != null)
          {
            BatchesEdition edition = new BatchesEdition();
            edition = (BatchesEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), edition);
            edition.setProjectDescription(param.getProjectDescription());
            _batchServ.postBatch(edition, null, pContext);
          }
          result = transform(param, "/Task.xsl", xmlSourceBytes);
          TasksEdition tasksEdition = new TasksEdition();
          if (result != null)
          {
            tasksEdition = (TasksEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), tasksEdition);
            tasksEdition.setProjectDescription(param.getProjectDescription());
            _taskServ.postTask(tasksEdition, null, pContext);
          }
          result = transform(param, "/Processor.xsl", xmlSourceBytes);
          if (result != null)
          {
            ProcessorsEdition edition = new ProcessorsEdition();
            edition = (ProcessorsEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), edition);
            edition.setProjectDescription(param.getProjectDescription());
            _processorServ.postProcessor(edition, null, pContext);
          }
          result = transform(param, "/Provider.xsl", xmlSourceBytes);
          ProvidersEdition providersEdition = new ProvidersEdition();
          if (result != null)
          {
            providersEdition = (ProvidersEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), providersEdition);
            providersEdition.setProjectDescription(param.getProjectDescription());
            _providerServ.postProvider(providersEdition, null, pContext);
          }
          result = transform(param, "/Trigger.xsl", xmlSourceBytes);
          if (result != null)
          {
            TriggersEdition edition = new TriggersEdition();
            edition = (TriggersEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), edition);
            edition.setProjectDescription(param.getProjectDescription());
            _triggerServ.postTrigger(edition, null, pContext);
          }
          result = transform(param, "/Stylesheet.xsl", xmlSourceBytes);
          if (result != null)
          {
            StylesheetsEdition edition = new StylesheetsEdition();
            edition = (StylesheetsEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), edition);
            edition.setProjectDescription(param.getProjectDescription());
            _xsltServ.postStylesheet(edition, null, pContext);
          }
          result = transform(param, "/Mapper.xsl", xmlSourceBytes);
          if (result != null)
          {
            MappersEdition edition = new MappersEdition();
            edition = (MappersEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), edition);
            edition.setProjectDescription(param.getProjectDescription());
            _mapperServ.postMapper(edition, null, pContext);
          }
          result = transform(param, "/Property.xsl", xmlSourceBytes);
          if (result != null)
          {
            PropertiesEdition edition = new PropertiesEdition();
            edition = (PropertiesEdition)_jaxbManager.readAndCloseStream(new ByteArrayInputStream(result.getBytes()), edition);
            edition.setProjectDescription(param.getProjectDescription());
            _propertyServ.postProperty(edition, null, pContext);
          }
          outputStream.close();
          if (Boolean.TRUE.equals(param.isGeneratePopulate()))
          {
            generateBatchFileData(param, service, tasksEdition, providersEdition);
          }
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  private void generateBatchFileData(BatchEditionGenerateParameterType param, ServiceType pService, TasksEdition pTasksEdition, ProvidersEdition providersEdition) throws Exception
  {
    if (!pTasksEdition.getTask().isEmpty() && !providersEdition.getProvider().isEmpty())
    {
      String module = "";
      String[] names = pService.getClassName().split("\\.");
      if (names.length > 3)
      {
        module = names[3].substring(0, 1).toUpperCase() + names[3].substring(1);
      }
      for (TaskEditionType task: pTasksEdition.getTask())
      {
        if (task.getOperation() != null && task.getInput() != null && task.getInput().getProvider() != null)
        {
          OperationServiceType operation = null;
          String ref = task.getInput().getProvider().getRef();
          // recherche de l'opération correspondant à la ref du provider
          for (OperationServiceType op: pService.getOperation())
          {
            if (op.getName().equals(task.getOperation()))
            {
              operation = op;
              break;
            }
          }
          if (operation != null)
          {
            // recherche du provider afin que le nom du fichier d'entrée corresponde au fileName défini sur le provider
            for (ProviderEditionType provider: providersEdition.getProvider())
            {
              if (provider.getFile() != null && ref.equals(provider.getName()))
              {
                BatchUtil.createXmlFile(getFileNameData(param.getXslDir(), module, provider.getFile().getFileName()), operation.getInputPopulate());
                BatchUtil.createXmlFile(getFileNameData(param.getXslDir(), module, provider.getFile().getScenarioFile()), operation.getScenarioPopulate());
                break;
              }
            }
          }
        }
      }
    }
  }

  private String getFileNameData(String pXslDir, String pModule, String pFileNameProvider)
  {
    if (pFileNameProvider != null)
    {
      new File(pXslDir + "/Data/default/In").mkdirs();
      String fileName = pFileNameProvider;
      fileName = fileName.replaceAll("\\*", pModule);
      fileName = fileName.replaceAll("\\$\\{.*\\}", "1");
      return pXslDir + "/Data/default/In/" + fileName;
    }
    return null;
  }

  private String transform(BatchEditionGenerateParameterType param, String pXsl, byte[] pXmlSourceByte) throws BatchException
  {
    String result = null;
    String xslDir = param.getXslDir().contains("batchGenerator") ? param.getXslDir() : param.getProjectDescription().getLocation() + "/../batchGenerator/" + param.getXslDir();
    File xsl = new File(xslDir + pXsl);
    if (xsl.exists())
    {
      try
      {
        Transformer fileTrans = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl));
        fileTrans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        fileTrans.setParameter("serviceName", param.getService().getRef());
        fileTrans.setParameter("fileName", param.getIncludeXMLSuffix().endsWith(".xml") ? param.getIncludeXMLSuffix() : param.getIncludeXMLSuffix() + ".xml");
        if (param.getService().getOperation().size() == 1)
        {
          fileTrans.setParameter("operationName", param.getService().getOperation().get(0).getName());
        }
        ByteArrayInputStream xmlSourceStream = new ByteArrayInputStream(pXmlSourceByte);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        fileTrans.transform(new StreamSource(xmlSourceStream), new StreamResult(outputStream));
        result = outputStream.toString();
        xmlSourceStream.close();
        outputStream.close();
      }
      catch (Exception e)
      {
        throw new BatchException(BatchEditionErrorDetail.XSL_ERROR, new Object[] { pXsl });
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  public CorpusEditionType findDependancies(CorpusEditionType pCorpus, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    CorpusEditionType result = new CorpusEditionType();
    _corpus = new CorpusEditionType();
    _corpus.setProperties(new PropertiesEdition());
    _corpus.setBatches(new BatchesEdition());
    _corpus.setTriggers(new TriggersEdition());
    _corpus.setTasks(new TasksEdition());
    _corpus.setProcessors(new ProcessorsEdition());
    _corpus.setProviders(new ProvidersEdition());
    _corpus.setMappers(new MappersEdition());
    _corpus.setStylesheets(new StylesheetsEdition());
    _project = pCorpus.getProjectDescription();
    _batchMap = new HashMap();
    _taskMap = new HashMap();
    _triggerMap = new HashMap();
    _processorMap = new HashMap();
    _providerMap = new HashMap();
    _xsltMap = new HashMap();
    _mapperMap = new HashMap();
    _domainMap = new HashMap();
    try
    {
      if (pCorpus.getBatches() != null)
      {
        if (pCorpus.getBatches().getInitBatch() != null)
        {
          InitBatchEditionType initBatch = pCorpus.getBatches().getInitBatch();
          findDependancies(initBatch.getChildSequence() != null ? initBatch.getChildSequence() : initBatch.getChildFork(), pScenario, pContext);
        }
        for (BatchEditionType batch: pCorpus.getBatches().getBatch())
        {
          findDependancies(batch, pScenario, pContext);
        }
      }
      if (pCorpus.getTasks() != null)
      {
        for (TaskEditionType task: pCorpus.getTasks().getTask())
        {
          findDependancies(task, pScenario, pContext);
        }
      }
      if (pCorpus.getProcessors() != null)
      {
        for (ProcessorEditionType processor: pCorpus.getProcessors().getProcessor())
        {
          findDependancies(processor, pScenario, pContext);
        }
      }
      if (pCorpus.getProviders() != null)
      {
        for (ProviderEditionType provider: pCorpus.getProviders().getProvider())
        {
          findDependancies(provider, pScenario, pContext);
        }
      }
      // recherche des properties par rapport à la liste des domaines trouvés
      PropertyEditionCriteriaType crit = new PropertyEditionCriteriaType();
      crit.setProjectDescription(_project);
      PropertiesEdition properties;
      for (DomainType domain: _corpus.getProperties().getDomain())
      {
        crit.setDomainName(domain.getName());
        properties = _propertyServ.findProperty(crit, null, pContext);
        _corpus.getProperties().getProperty().addAll(properties.getProperty());
        _corpus.getProperties().getTstmp().addAll(properties.getTstmp());
      }
      if (pScenario != null && pScenario.getAction() != null)
      {
        if (pScenario.getAction() instanceof EditionCopyActionType)
        {
          ProjectDescriptionType toProject = ((EditionCopyActionType)pScenario.getAction()).getToProject();
          // copie vers toProject des dépendances trouvées mais aussi des élements passés en paramètre
          if (toProject != null)
          {
            _corpus.getBatches().setProjectDescription(toProject);
            _batchServ.postBatch(_corpus.getBatches(), null, pContext);
            if (pCorpus.getBatches() != null)
            {
              pCorpus.getBatches().setProjectDescription(toProject);
              _batchServ.postBatch(pCorpus.getBatches(), null, pContext);
            }
            _corpus.getTriggers().setProjectDescription(toProject);
            _triggerServ.postTrigger(_corpus.getTriggers(), null, pContext);
            if (pCorpus.getTriggers() != null)
            {
              pCorpus.getTriggers().setProjectDescription(toProject);
              _triggerServ.postTrigger(pCorpus.getTriggers(), null, pContext);
            }
            _corpus.getTasks().setProjectDescription(toProject);
            _taskServ.postTask(_corpus.getTasks(), null, pContext);
            if (pCorpus.getTasks() != null)
            {
              pCorpus.getTasks().setProjectDescription(toProject);
              _taskServ.postTask(pCorpus.getTasks(), null, pContext);
            }
            _corpus.getProcessors().setProjectDescription(toProject);
            _processorServ.postProcessor(_corpus.getProcessors(), null, pContext);
            if (pCorpus.getProcessors() != null)
            {
              pCorpus.getProcessors().setProjectDescription(toProject);
              _processorServ.postProcessor(pCorpus.getProcessors(), null, pContext);
            }
            _corpus.getProviders().setProjectDescription(toProject);
            _providerServ.postProvider(_corpus.getProviders(), null, pContext);
            if (pCorpus.getProviders() != null)
            {
              pCorpus.getProviders().setProjectDescription(toProject);
              _providerServ.postProvider(pCorpus.getProviders(), null, pContext);
            }
            _corpus.getMappers().setProjectDescription(toProject);
            _mapperServ.postMapper(_corpus.getMappers(), null, pContext);
            if (pCorpus.getMappers() != null)
            {
              pCorpus.getMappers().setProjectDescription(toProject);
              _mapperServ.postMapper(pCorpus.getMappers(), null, pContext);
            }
            _corpus.getStylesheets().setProjectDescription(toProject);
            _xsltServ.postStylesheet(_corpus.getStylesheets(), null, pContext);
            copyXSL(_corpus.getStylesheets(), _project, toProject);
            if (pCorpus.getStylesheets() != null)
            {
              pCorpus.getStylesheets().setProjectDescription(toProject);
              _xsltServ.postStylesheet(pCorpus.getStylesheets(), null, pContext);
              copyXSL(pCorpus.getStylesheets(), _project, toProject);
            }
            // les properties doivent être traitées en dernier par rapport à l'ajout des include sur les fichiers principaux
            _corpus.getProperties().setProjectDescription(toProject);
            _propertyServ.postProperty(_corpus.getProperties(), null, pContext);
            if (pCorpus.getProperties() != null)
            {
              pCorpus.getProperties().setProjectDescription(toProject);
              _propertyServ.postProperty(pCorpus.getProperties(), null, pContext);
            }
          }
          else
          {
            // TODO lever exception projet de destination non spécifié
          }
        }
      }
      else
      {
        // Ne renvoie que les dépendances trouvées
        result.setProperties(new PropertiesEdition());
        result.getProperties().setProperty(_corpus.getProperties().getProperty());
        result.getProperties().setTstmp(_corpus.getProperties().getTstmp());
        result.getProperties().setDomain(_corpus.getProperties().getDomain());
        result.getProperties().setProjectDescription(_project);
        result.setBatches(new BatchesEdition());
        result.getBatches().setInitBatch(_corpus.getBatches().getInitBatch());
        result.getBatches().setBatch(_corpus.getBatches().getBatch());
        result.getBatches().setProjectDescription(_project);
        result.setTriggers(new TriggersEdition());
        result.getTriggers().setTrigger(_corpus.getTriggers().getTrigger());
        result.getTriggers().setProjectDescription(_project);
        result.setTasks(new TasksEdition());
        result.getTasks().setTask(_corpus.getTasks().getTask());
        result.getTasks().setProjectDescription(_project);
        result.setProcessors(new ProcessorsEdition());
        result.getProcessors().setProcessor(_corpus.getProcessors().getProcessor());
        result.getProcessors().setProjectDescription(_project);
        result.setProviders(new ProvidersEdition());
        result.getProviders().setProvider(_corpus.getProviders().getProvider());
        result.getProviders().setProjectDescription(_project);
        result.setMappers(new MappersEdition());
        result.getMappers().setMapper(_corpus.getMappers().getMapper());
        result.getMappers().setProjectDescription(_project);
        result.setStylesheets(new StylesheetsEdition());
        result.getStylesheets().setStylesheet(_corpus.getStylesheets().getStylesheet());
        result.getStylesheets().setProjectDescription(_project);
      }
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  private void copyXSL(StylesheetsEdition pStylesheets, ProjectDescriptionType pFromProject, ProjectDescriptionType pToProject) throws Exception
  {
    DirectoryFileManager dirFileManager;
    for (StylesheetEditionType stylesheet: pStylesheets.getStylesheet())
    {
      dirFileManager = new DirectoryFileManager(pFromProject.getLocation() + "/" + stylesheet.getFile().getDir());
      dirFileManager.copyFile(pToProject.getLocation() + "/" + stylesheet.getFile().getDir(), stylesheet.getFile().getKwInclude() != null ? stylesheet.getFile().getKwInclude() : stylesheet.getFile().getFileName());
    }
  }

  /**
   * Recherche des triggers, batchs et tasks et mémorisation des domaines utilisés
   * @param pBatch
   * @param pScenario
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void findDependancies(BatchEditionType pBatch, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatch.getTrigger() != null && pBatch.getTrigger().getRef() != null)
    {
      TriggerEditionCriteriaType crit = new TriggerEditionCriteriaType();
      crit.setProjectDescription(_project);
      crit.setTriggerName(new CriteriaStringType());
      crit.getTriggerName().setEquals(pBatch.getTrigger().getRef());
      TriggersEdition triggers = _triggerServ.findTrigger(crit, null, pContext);
      // une recherche sur un name peut ramener 2 éléments à cause du custom
      for (TriggerEditionType trigger: triggers.getTrigger())
      {
        if (_triggerMap.get(trigger.getName() + "-" + trigger.getFileName()) == null)
        {
          _triggerMap.put(trigger.getName() + "-" + trigger.getFileName(), trigger);
          _corpus.getTriggers().getTrigger().add(trigger);
        }
      }
    }
    findDependancies(pBatch.getChildSequence() != null ? pBatch.getChildSequence() : pBatch.getChildFork(), pScenario, pContext);
  }

  /**
   * Recherche des batchs et des tasks et mémorisation des domaines utilisés
   * @param pBatchContent
   * @param pScenario
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void findDependancies(List<BatchEditionChildAbstractType> pBatchContent, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatchContent != null)
    {
      for (BatchEditionChildAbstractType ref: pBatchContent)
      {
        if (ref.getDefaultDomain() != null && _domainMap.get(ref.getDefaultDomain()) == null)
        {
          _domainMap.put(ref.getDefaultDomain(), ref.getDefaultDomain());
          DomainType domain = new DomainType();
          domain.setName(ref.getDefaultDomain());
          _corpus.getProperties().getDomain().add(domain);
        }
        if (ref.getActiveDomain() != null && _domainMap.get(ref.getActiveDomain()) == null)
        {
          _domainMap.put(ref.getActiveDomain(), ref.getActiveDomain());
          DomainType domain = new DomainType();
          domain.setName(ref.getActiveDomain());
          _corpus.getProperties().getDomain().add(domain);
        }
        if (BatchChildEnum.BATCH.equals(ref.getType()) && ref.getRef() != null)
        {
          BatchEditionCriteriaType crit = new BatchEditionCriteriaType();
          crit.setProjectDescription(_project);
          crit.setBatchName(new CriteriaStringType());
          crit.getBatchName().setEquals(ref.getRef());
          BatchesEdition batches = _batchServ.findBatch(crit, null, pContext);
          // une recherche sur un name peut ramener 2 éléments à cause du custom
          for (BatchEditionType batch: batches.getBatch())
          {
            if (_batchMap.get(batch.getName() + "-" + batch.getFileName()) == null)
            {
              _batchMap.put(batch.getName() + "-" + batch.getFileName(), batch);
              _corpus.getBatches().getBatch().add(batch);
              findDependancies(batch, pScenario, pContext);
            }
          }
        }
        else if (BatchChildEnum.TASK.equals(ref.getType()) && ref.getRef() != null)
        {
          TaskEditionCriteriaType crit = new TaskEditionCriteriaType();
          crit.setProjectDescription(_project);
          crit.setTaskName(new CriteriaStringType());
          crit.getTaskName().setEquals(ref.getRef());
          TasksEdition tasks = _taskServ.findTask(crit, null, pContext);
          // une recherche sur un name peut ramener 2 éléments à cause du custom
          for (TaskEditionType task: tasks.getTask())
          {
            if (_taskMap.get(task.getName() + "-" + task.getFileName()) == null)
            {
              _taskMap.put(task.getName() + "-" + task.getFileName(), task);
              _corpus.getTasks().getTask().add(task);
              findDependancies(task, pScenario, pContext);
            }
          }
        }
      }
    }
  }

  /**
   * Recherche des processors et des providers
   * @param pTask
   * @param pScenario
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void findDependancies(TaskEditionType pTask, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pTask.getProcessor() != null && pTask.getProcessor().getRef() != null)
    {
      ProcessorEditionCriteriaType crit = new ProcessorEditionCriteriaType();
      crit.setProjectDescription(_project);
      crit.setProcessorName(new CriteriaStringType());
      crit.getProcessorName().setEquals(pTask.getProcessor().getRef());
      ProcessorsEdition processors = _processorServ.findProcessor(crit, null, pContext);
      // une recherche sur un name peut ramener 2 éléments à cause du custom
      for (ProcessorEditionType processor: processors.getProcessor())
      {
        if (_processorMap.get(processor.getName() + "-" + processor.getFileName()) == null)
        {
          _processorMap.put(processor.getName() + "-" + processor.getFileName(), processor);
          _corpus.getProcessors().getProcessor().add(processor);
          findDependancies(processor, pScenario, pContext);
        }
      }
    }
    findDependancies(pTask.getInput(), pScenario, pContext);
    findDependancies(pTask.getResponse(), pScenario, pContext);
    findDependancies(pTask.getReject(), pScenario, pContext);
  }

  /**
   * Recherche des stylesheets
   * @param processor
   * @param pScenario
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void findDependancies(ProcessorEditionType processor, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    for (ProcessorEditionBlockType block: processor.getBlockEdition())
    {
      if (block.getXslt() != null && block.getXslt().getStyle() != null && block.getXslt().getStyle().getRef() != null)
      {
        StylesheetEditionCriteriaType crit = new StylesheetEditionCriteriaType();
        crit.setProjectDescription(_project);
        crit.setStylesheetName(new CriteriaStringType());
        crit.getStylesheetName().setEquals(block.getXslt().getStyle().getRef());
        StylesheetsEdition stylesheets = _xsltServ.findStylesheet(crit, null, pContext);
        // une recherche sur un name peut ramener 2 éléments à cause du custom
        for (StylesheetEditionType xslt: stylesheets.getStylesheet())
        {
          if (_xsltMap.get(xslt.getName() + "-" + xslt.getFileName()) == null)
          {
            _xsltMap.put(xslt.getName() + "-" + xslt.getFileName(), xslt);
            _corpus.getStylesheets().getStylesheet().add(xslt);
          }
        }
      }
    }
  }

  /**
   * Recherche des providers
   * @param pStream
   * @param pScenario
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void findDependancies(AbstractStream pStream, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pStream != null && pStream.getProvider() != null && pStream.getProvider().getRef() != null)
    {
      ProviderEditionCriteriaType crit = new ProviderEditionCriteriaType();
      crit.setProjectDescription(_project);
      crit.setProviderName(new CriteriaStringType());
      crit.getProviderName().setEquals(pStream.getProvider().getRef());
      ProvidersEdition providers = _providerServ.findProvider(crit, null, pContext);
      // une recherche sur un name peut ramener 2 éléments à cause du custom
      for (ProviderEditionType provider: providers.getProvider())
      {
        if (_providerMap.get(provider.getName() + "-" + provider.getFileName()) == null)
        {
          _providerMap.put(provider.getName() + "-" + provider.getFileName(), provider);
          _corpus.getProviders().getProvider().add(provider);
          findDependancies(provider, pScenario, pContext);
        }
      }
    }
  }

  /**
   * Recherche des mappers
   * @param provider
   * @param pScenario
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void findDependancies(ProviderEditionType provider, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (provider.getMapper() != null && provider.getMapper().getRef() != null)
    {
      MapperEditionCriteriaType crit = new MapperEditionCriteriaType();
      crit.setProjectDescription(_project);
      crit.setMapperName(new CriteriaStringType());
      crit.getMapperName().setEquals(provider.getMapper().getRef());
      MappersEdition mappers = _mapperServ.findMapper(crit, null, pContext);
      // une recherche sur un name peut ramener 2 éléments à cause du custom
      for (MapperEditionType mapper: mappers.getMapper())
      {
        if (_mapperMap.get(mapper.getName() + "-" + mapper.getFileName()) == null)
        {
          _mapperMap.put(mapper.getName() + "-" + mapper.getFileName(), mapper);
          _corpus.getMappers().getMapper().add(mapper);
        }
      }
    }
  }

  /** {@inheritDoc} */
  public UploadFileType exportArchive(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    UploadFileType result = new UploadFileType();
    File exportDir = new File(new File(project.getLocation()).getParentFile().getPath() + "/export/" + project.getName());
    try
    {
      FileUtils.deleteDirectory(exportDir);
      if (exportDir.mkdirs())
      {
        ProjectDescriptionType projectRepo = new ProjectDescriptionType();
        projectRepo.setLocation(project.getLocation());
        projectRepo.setFromDir(exportDir.getPath());
        exportRepository(projectRepo, pScenario, pContext);
        File zip = new File(projectRepo.getFromDir() + ".zip");
        zip.delete();
        ZipManager zipMgr = new ZipManager();
        zipMgr.zipFiles(zip, exportDir);
        result.setName(zip.getName());
        result.setPath(zip.getPath());
      }
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /** {@inheritDoc} */
  public void exportRepository(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (project != null && project.getFromDir() != null && project.getLocation() != null)
    {
      try
      {
        String location = project.getLocation();
        File projectDir = new File(location);
        if (!projectDir.exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        processIncludeEmpty(project, pContext);
        Transformer mainFileTrans = TransformerFactory.newInstance().newTransformer(new StreamSource(this.getClass().getResourceAsStream("/xsl/mainFileEditionToMainFile.xsl")));
        mainFileTrans.setParameter(OutputKeys.INDENT, "yes");
        mainFileTrans.setOutputProperty(OutputKeys.INDENT, "yes");
        mainFileTrans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        mainFileTrans.setParameter("indent-number", Integer.valueOf(2));
        Transformer includeTrans = TransformerFactory.newInstance().newTransformer(new StreamSource(this.getClass().getResourceAsStream("/xsl/includeEditionToInclude.xsl")));
        includeTrans.setOutputProperty(OutputKeys.INDENT, "yes");
        includeTrans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        for (File file: projectDir.listFiles())
        {
          if (file.isFile() && file.getName().endsWith(".xml"))
          {
            if (MAINFILE_LIST.contains(";" + file.getName() + ";"))
            {
              FileOutputStream fos = new FileOutputStream(new File(project.getFromDir() + "/" + (file.getName().equals("Batches.xml") ? "Batchs.xml" : file.getName())));
              mainFileTrans.transform(new StreamSource(file), new StreamResult(fos));
              fos.flush();
              fos.close();
            }
            else if (file.getName().contains("_") || file.getName().startsWith("Properties") || file.getName().equals("Variables.xml"))
            {
              String suffix = file.getName().split("_")[0];
              if (MAINFILE_LIST.contains(";" + suffix + ".xml;") || "Properties.xml".equals(suffix) || "Variables.xml".equals(suffix) || "Properties.xml".equals(suffix + ".xml"))
              {
                FileOutputStream fos = new FileOutputStream(new File(project.getFromDir() + "/" + file.getName()));
                includeTrans.transform(new StreamSource(file), new StreamResult(fos));
                fos.flush();
                fos.close();
              }
            }
          }
        }
        DirectoryFileManager dirFileManager = new DirectoryFileManager(location + "/xsl");
        dirFileManager.copyFile(project.getFromDir() + "/xsl", "*.xsl");
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /**
   * L'inclusion de fichier xml vide empêche la lecture des fichiers inclus suivant. Cette méthode place donc les fichiers inclus vide après les
   * fichiers inclus non vide.
   * @param project
   * @param pContext
   * @throws WrapperServiceException exception
   */
  private void processIncludeEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    processIncludeBatchEmpty(project, pContext);
    processIncludeTaskEmpty(project, pContext);
    processIncludeTriggerEmpty(project, pContext);
    processIncludeProcessorEmpty(project, pContext);
    processIncludeProviderEmpty(project, pContext);
    processIncludeMapperEmpty(project, pContext);
    processIncludeStylesheetEmpty(project, pContext);
  }

  private void processIncludeBatchEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    BatchEditionManagerServiceImpl serv = new BatchEditionManagerServiceImpl();
    BatchesEdition edition = serv.getMainBatch(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    BatchesEdition includeEdition;
    BatchEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Batchs"))
      {
        crit = new BatchEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findBatch(crit, null, pContext);
        if (includeEdition.getBatch().isEmpty() && includeEdition.getInitBatch() == null)
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainBatch(edition, null, pContext);
  }

  private void processIncludeTaskEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    TaskEditionManagerServiceImpl serv = new TaskEditionManagerServiceImpl();
    TasksEdition edition = serv.getMainTask(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    TasksEdition includeEdition;
    TaskEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Tasks"))
      {
        crit = new TaskEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findTask(crit, null, pContext);
        if (includeEdition.getTask().isEmpty())
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainTask(edition, null, pContext);
  }

  private void processIncludeProcessorEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    ProcessorEditionManagerServiceImpl serv = new ProcessorEditionManagerServiceImpl();
    ProcessorsEdition edition = serv.getMainProcessor(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    ProcessorsEdition includeEdition;
    ProcessorEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Processors"))
      {
        crit = new ProcessorEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findProcessor(crit, null, pContext);
        if (includeEdition.getProcessor().isEmpty())
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainProcessor(edition, null, pContext);
  }

  private void processIncludeTriggerEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    TriggerEditionManagerServiceImpl serv = new TriggerEditionManagerServiceImpl();
    TriggersEdition edition = serv.getMainTrigger(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    TriggersEdition includeEdition;
    TriggerEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Triggers"))
      {
        crit = new TriggerEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findTrigger(crit, null, pContext);
        if (includeEdition.getTrigger().isEmpty())
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainTrigger(edition, null, pContext);
  }

  private void processIncludeProviderEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    ProviderEditionManagerServiceImpl serv = new ProviderEditionManagerServiceImpl();
    ProvidersEdition edition = serv.getMainProvider(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    ProvidersEdition includeEdition;
    ProviderEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Providers"))
      {
        crit = new ProviderEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findProvider(crit, null, pContext);
        if (includeEdition.getProvider().isEmpty())
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainProvider(edition, null, pContext);
  }

  private void processIncludeMapperEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    MapperEditionManagerServiceImpl serv = new MapperEditionManagerServiceImpl();
    MappersEdition edition = serv.getMainMapper(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    MappersEdition includeEdition;
    MapperEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Mappers"))
      {
        crit = new MapperEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findMapper(crit, null, pContext);
        if (includeEdition.getMapper().isEmpty())
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainMapper(edition, null, pContext);
  }

  private void processIncludeStylesheetEmpty(ProjectDescriptionType project, ContextType pContext) throws WrapperServiceException
  {
    List<IncludeXMLType> includeList = new ArrayList();
    List<IncludeXMLType> includeEmptyList = new ArrayList();
    StylesheetEditionManagerServiceImpl serv = new StylesheetEditionManagerServiceImpl();
    StylesheetsEdition edition = serv.getMainStylesheet(project, null, pContext);
    if (edition.getPropertiesEdition() != null)
    {
      edition.getPropertiesEdition().setDomain(null);
    }
    StylesheetsEdition includeEdition;
    StylesheetEditionCriteriaType crit;
    for (IncludeXMLType include: edition.getIncludeXML())
    {
      if (include.getName().startsWith("Stylesheets"))
      {
        crit = new StylesheetEditionCriteriaType();
        crit.setProjectDescription(project);
        crit.setFileName(include.getName());
        includeEdition = serv.findStylesheet(crit, null, pContext);
        if (includeEdition.getStylesheet().isEmpty())
        {
          includeEmptyList.add(include);
        }
        else
        {
          includeList.add(include);
        }
      }
      else
      {
        includeList.add(include);
      }
    }
    edition.setIncludeXML(new ArrayList());
    edition.getIncludeXML().addAll(includeList);
    edition.getIncludeXML().addAll(includeEmptyList);
    serv.setMainStylesheet(edition, null, pContext);
  }

  /** {@inheritDoc} */
  public ProjectDescriptionListType getProjectDescriptionList(ProjectDescriptionType pRootOfAllProject, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProjectDescriptionListType result = new ProjectDescriptionListType();
    try
    {
      String location = pRootOfAllProject == null || pRootOfAllProject.getLocation() == null ? getDefaultHome() : pRootOfAllProject.getLocation();
      File rootDir = new File(location);
      if (!rootDir.exists())
      {
        throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
      }
      updateProjectXSL(result, location);
      BatchesEdition edition;
      for (File projectDir: rootDir.listFiles())
      {
        if (projectDir.isDirectory())
        {
          File batchFile = new File(projectDir.getPath() + "/Batches.xml");
          if (batchFile.exists())
          {
            edition = new BatchesEdition();
            edition = (BatchesEdition)_jaxbManager.readAndCloseStream(new FileInputStream(batchFile), edition);
            result.getProjectDescription().add(edition.getProjectDescription());
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

  private void updateProjectXSL(ProjectDescriptionListType project, String pLocation) throws Exception, IOException
  {
    File batchGeneratorDir = new File(pLocation + "/batchGenerator");
    batchGeneratorDir.mkdir();
    // récupération des modèles XSL de génération contenu dans le jar
    JarFile jar = BatchUtil.getCurrentJarFile();
    if (jar != null)
    {
      String jarPath = "xsl/batchGenerator/";
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements())
      {
        String name = entries.nextElement().getName();
        if (name.startsWith(jarPath))
        {
          String relativeName = name.substring(jarPath.length());
          int checkSubdir = relativeName.indexOf('/');
          File xslDir = new File(pLocation + "/batchGenerator/" + relativeName);
          // si c'est un dossier, transfert de son contenu
          if (checkSubdir > 1)
          {
            xslDir.mkdir();
            for (String xsl: XSL_LIST)
            {
              JarEntry xslEntry = jar.getJarEntry("xsl/batchGenerator/" + relativeName + xsl);
              if (xslEntry != null)
              {
                BatchUtil.copyInputStream(jar.getInputStream(xslEntry), new File(xslDir.getAbsolutePath() + "/" + xsl));
              }
            }
          }
        }
      }
    }
    for (File dir: batchGeneratorDir.listFiles())
    {
      if (dir.isDirectory())
      {
        project.getXslDir().add(dir.getAbsolutePath());
      }
    }
  }

  /** {@inheritDoc} */
  public void importArchive(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ZipManager zipMgr = new ZipManager();
    File zip = new File(project.getFromDir());
    if (zip.exists())
    {
      try
      {
        File unzipDir = new File(zip.getParentFile() + "/" + project.getName());
        zipMgr.unZipFiles(zip, unzipDir);
        ProjectDescriptionType projectRepo = new ProjectDescriptionType();
        projectRepo.setName(project.getName());
        projectRepo.setLocation(project.getLocation());
        File[] files = unzipDir.listFiles();
        // fromDir = dossier root contenu dans le zip si présent
        projectRepo.setFromDir(files.length == 1 && files[0].isDirectory() ? files[0].getPath() : unzipDir.getPath());
        importRepository(projectRepo, pScenario, pContext);
        zip.delete();
        FileUtils.deleteDirectory(unzipDir);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public void importRepository(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (project != null && project.getName() != null)
    {
      try
      {
        project.setFromDir(project.getFromDir() == null ? FilenameUtil.getWebAppRootPath() : project.getFromDir());
        if (!new File(project.getFromDir()).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { project.getFromDir() });
        }
        project.setLocation(project.getLocation() == null ? getDefaultHome() + "/" + project.getName() : project.getLocation());
        File homeDir = new File(project.getLocation());
        if (!homeDir.exists() && !homeDir.mkdirs())
        {
          throw new BatchException(BatchEditionErrorDetail.IMPOSSIBLE_TO_CREATE_DIRECTORY, new Object[] { project.getLocation() });
        }
        importBatches(project);
        importTasks(project);
        importProviders(project);
        importProcessors(project);
        importTriggers(project);
        importMappers(project);
        importStylesheets(project);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  private String getDefaultHome() throws Exception
  {
    String defaultHome = ConfigEngineProperties.getInstance().getEngineProperty("batchesedition.home.dir");
    defaultHome = defaultHome == null || defaultHome.equals("") ? DEFAULT_HOME : defaultHome;
    File homeDir = new File(defaultHome);
    if (!homeDir.exists() && !homeDir.mkdirs())
    {
      throw new BatchException(BatchEditionErrorDetail.IMPOSSIBLE_TO_CREATE_DIRECTORY, new Object[] { defaultHome });
    }
    return defaultHome;
  }

  private void importBatches(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Batchs", "Batches");
    // Ajout de projectDescription au fichier principal
    BatchesEdition edition = new BatchesEdition();
    edition = (BatchesEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setBatch(new ArrayList());
      edition.setInitBatch(null);
      retrieveAllInclude(project, edition, "Batchs_");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
  }

  private void importTasks(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Tasks", "Tasks");
    // Ajout de projectDescription au fichier principal
    TasksEdition edition = new TasksEdition();
    edition = (TasksEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setTask(new ArrayList());
      retrieveAllInclude(project, edition, "Tasks_");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
  }

  private void importProviders(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Providers", "Providers");
    // Ajout de projectDescription au fichier principal
    ProvidersEdition edition = new ProvidersEdition();
    edition = (ProvidersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setProvider(new ArrayList());
      retrieveAllInclude(project, edition, "Providers_");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
  }

  private void importProcessors(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Processors", "Processors");
    // Ajout de projectDescription au fichier principal
    ProcessorsEdition edition = new ProcessorsEdition();
    edition = (ProcessorsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setProcessor(new ArrayList());
      retrieveAllInclude(project, edition, "Processors_");
      retrieveAllInclude(project, edition, "Variables");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
  }

  private void importTriggers(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Triggers", "Triggers");
    // Ajout de projectDescription au fichier principal
    TriggersEdition edition = new TriggersEdition();
    edition = (TriggersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setTrigger(new ArrayList());
      retrieveAllInclude(project, edition, "Triggers_");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
  }

  private void importMappers(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Mappers", "Mappers");
    // Ajout de projectDescription au fichier principal
    MappersEdition edition = new MappersEdition();
    edition = (MappersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setMapper(new ArrayList());
      retrieveAllInclude(project, edition, "Mappers_");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
  }

  private void importStylesheets(ProjectDescriptionType project) throws Exception
  {
    File mainFile = importMainFileAndIncludes(project, "Stylesheets", "Stylesheets");
    // Ajout de projectDescription au fichier principal
    StylesheetsEdition edition = new StylesheetsEdition();
    edition = (StylesheetsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(mainFile), edition);
    edition.setProjectDescription(project);
    if (edition.getIncludeXML().isEmpty())
    {
      edition.setStylesheet(new ArrayList());
      retrieveAllInclude(project, edition, "Stylesheets_");
    }
    edition.setProjectDescription(getNewProjectDescription(project));
    _jaxbManager.write(edition, mainFile.getPath());
    DirectoryFileManager dirFileManager = new DirectoryFileManager(project.getFromDir() + "/xsl");
    dirFileManager.copyFile(project.getLocation() + "/xsl", "*.xsl");
  }

  private File importMainFileAndIncludes(ProjectDescriptionType project, String pFromFileName, String pToFileName) throws Exception
  {
    BufferedReader reader = null;
    PrintWriter writer = null;
    try
    {
      File toMainFile = new File(project.getLocation() + "/" + pToFileName + ".xml");
      SAXParserFactory fabrique = SAXParserFactory.newInstance();
      SAXParser parser = fabrique.newSAXParser();
      BatchEntityResolver entityResolver = new BatchEntityResolver(project);
      parser.setProperty("http://xml.org/sax/properties/declaration-handler", entityResolver);
      File mainFile = new File(project.getFromDir() + "/" + pFromFileName + ".xml");
      parser.parse(mainFile, entityResolver); // récupération des fichiers inclus
      reader = new BufferedReader(new FileReader(mainFile));
      writer = new PrintWriter(new FileWriter(toMainFile, false));
      String line;
      while ((line = reader.readLine()) != null)
      {
        line = getTagName(line);
        line = line.replaceFirst("xsi:type=\"(.*)\"", "type=\"$1\"");
        Matcher matcher = INCLUDE_PATTERN.matcher(line);
        writer.println(matcher.find() ? "<includeXML name=\"" + entityResolver.getEntityFileMap().get(matcher.group(1)) + "\"/>" : line);
      }
      if (!entityResolver.getEntityFileMap().isEmpty())
      {
        // sur Windows, le parsing SAX verrouille tous les fichiers associés aux entités trouvées
        // forcer l'appel au garbage collector permet de les déverrouiller
        // CHECKSTYLE:OFF
        System.gc();
        // CHECKSTYLE:ON
      }
      return toMainFile;
    }
    finally
    {
      try
      {
        if (reader != null)
        {
          reader.close();
        }
      }
      finally
      {
        if (writer != null)
        {
          writer.close();
        }
      }
    }
  }

  /**
   * force la récupération, pour un fichier principal, des sous-fichiers existant si le fichier principal n'utilise pas l'insertion d'entités XML.
   * C'est le cas lorsque le fichier principal s'est vu appliquer une transformation XSL (comme dans GlobalTest lors de la génération du livrable) ce
   * qui a pour effet de retranscrire le fichier en un seul bloc et ainsi de rendre invisible l'insertion des différents sous-fichiers qui étaient
   * utilisés
   * @param project
   * @param pEdition
   * @param pFilePrefix
   * @throws IOException exception
   * @throws Exception exception
   * @throws FileNotFoundException exception
   */
  private void retrieveAllInclude(ProjectDescriptionType project, AbstractParentEditionType pEdition, String pFilePrefix) throws IOException, Exception, FileNotFoundException
  {
    pEdition.setIncludeXML(new ArrayList());
    if (pEdition.getPropertiesEdition() != null)
    {
      pEdition.getPropertiesEdition().setIncludeXML(new ArrayList());
      pEdition.getPropertiesEdition().setProperty(new ArrayList());
      pEdition.getPropertiesEdition().setTstmp(new ArrayList());
    }
    BatchEntityResolver entityResolver = new BatchEntityResolver(project);
    File dir = new File(project.getFromDir());
    for (File file: dir.listFiles())
    {
      if (file.isFile() && file.getName().endsWith(".xml"))
      {
        if (file.getName().startsWith(pFilePrefix))
        {
          IncludeXMLType include = new IncludeXMLType();
          include.setName(file.getName());
          pEdition.getIncludeXML().add(include);
          entityResolver.importIncludeFile(file.getName());
        }
        else if (pEdition.getPropertiesEdition() != null && file.getName().startsWith("Properties"))
        {
          IncludeXMLType include = new IncludeXMLType();
          include.setName(file.getName());
          pEdition.getPropertiesEdition().getIncludeXML().add(include);
          entityResolver.importIncludeFile(file.getName());
        }
      }
    }
  }

  private String getTagName(String pLine) throws Exception
  {
    String line = pLine.trim();
    Matcher matcher = TAGNAME_PATTERN.matcher(line);
    if (matcher.find())
    {
      String tagName = matcher.group(1);
      if (TAGNAME_CONVERSION_MAP.get(tagName) != null)
      {
        return (line.startsWith("</") ? "</" : "<") + TAGNAME_CONVERSION_MAP.get(tagName) + (line.endsWith("/>") ? "/>" : ">");
      }
    }
    return pLine;
  }

  /** {@inheritDoc} */
  public void deleteProject(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    try
    {
      if (new File(project.getLocation() + BatchEditionManagerServiceImpl.MAIN_FILE_NAME).exists())
      {
        FileUtils.deleteDirectory(new File(project.getLocation()));
      }
    }
    catch (IOException e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
  }

  /** {@inheritDoc} */
  public ProjectDescriptionType createProject(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    try
    {
      project.setLocation(project.getLocation() == null ? getDefaultHome() + "/" + project.getName() : project.getLocation());
      File homeDir = new File(project.getLocation());
      if (!homeDir.exists() && !homeDir.mkdirs())
      {
        throw new BatchException(BatchEditionErrorDetail.IMPOSSIBLE_TO_CREATE_DIRECTORY, new Object[] { project.getLocation() });
      }
      postBatches(project, true);
      postTasks(project, true);
      postProviders(project, true);
      postProcessors(project, true);
      postTriggers(project, true);
      postMappers(project, true);
      postStylesheets(project, true);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return project;
  }

  /** {@inheritDoc} */
  public void duplicateProject(ProjectDescriptionListType projectList, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    try
    {
      ProjectDescriptionType projectFrom = projectList.getProjectDescription().get(0);
      ProjectDescriptionType projectTo = projectList.getProjectDescription().get(1);
      File dir = new File(projectFrom.getLocation());
      if (!dir.exists())
      {
        throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { projectFrom.getLocation() });
      }
      DirectoryFileManager dirFileManager = new DirectoryFileManager(projectFrom.getLocation());
      dirFileManager.copyFile(projectTo.getLocation(), "*.xml");
      dirFileManager = new DirectoryFileManager(projectFrom.getLocation() + "/xsl");
      dirFileManager.copyFile(projectTo.getLocation() + "/xsl", "*.xsl");
      postBatches(projectTo, false);
      postTasks(projectTo, false);
      postProviders(projectTo, false);
      postProcessors(projectTo, false);
      postTriggers(projectTo, false);
      postMappers(projectTo, false);
      postStylesheets(projectTo, false);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
  }

  private void postBatches(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    BatchesEdition edition = new BatchesEdition();
    String fileName = project.getLocation() + BatchEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (BatchesEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private void postTasks(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    TasksEdition edition = new TasksEdition();
    String fileName = project.getLocation() + TaskEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (TasksEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private void postProviders(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    ProvidersEdition edition = new ProvidersEdition();
    String fileName = project.getLocation() + ProviderEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (ProvidersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private void postProcessors(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    ProcessorsEdition edition = new ProcessorsEdition();
    String fileName = project.getLocation() + ProcessorEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (ProcessorsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private void postTriggers(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    TriggersEdition edition = new TriggersEdition();
    String fileName = project.getLocation() + TriggerEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (TriggersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private void postMappers(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    MappersEdition edition = new MappersEdition();
    String fileName = project.getLocation() + MapperEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (MappersEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private void postStylesheets(ProjectDescriptionType project, boolean pCreate) throws IOException
  {
    StylesheetsEdition edition = new StylesheetsEdition();
    String fileName = project.getLocation() + StylesheetEditionManagerServiceImpl.MAIN_FILE_NAME;
    if (!pCreate)
    {
      edition = (StylesheetsEdition)_jaxbManager.readAndCloseStream(new FileInputStream(fileName), edition);
    }
    edition.setProjectDescription(project);
    _jaxbManager.write(edition, fileName);
  }

  private ProjectDescriptionType getNewProjectDescription(ProjectDescriptionType project)
  {
    ProjectDescriptionType result = new ProjectDescriptionType();
    result.setName(project.getName());
    result.setDescription(project.getDescription());
    result.setLocation(project.getLocation());
    result.setFromDir(FilenameUtil.getWebAppRootPath()); // doit être positionné ainsi pour le futur export dans l'application
    return result;
  }
}
