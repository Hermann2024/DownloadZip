package com.cylande.unitedretail.batch.scheduler;

import com.cylande.unitedretail.batch.batch.BatchQueueManager;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.execution.ExecutionUnit;
import com.cylande.unitedretail.batch.repository.BatchPropertiesRepository;
import com.cylande.unitedretail.batch.repositoryloader.BatchRepositoryLoader;
import com.cylande.unitedretail.batch.repositoryloader.MapperRepositoryLoader;
import com.cylande.unitedretail.batch.repositoryloader.ProviderRepositoryLoader;
import com.cylande.unitedretail.batch.repositoryloader.TaskRepositoryLoader;
import com.cylande.unitedretail.batch.repositoryloader.TriggerRepositoryLoader;
import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.batch.service.TriggerManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.batch.tools.RegexPattern;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.common.tools.URParam;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.exception.FileManagementException;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.util.FileUtil;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchTriggerType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.TriggerKeyType;
import com.cylande.unitedretail.message.batch.TriggerType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.network.businessunit.SiteType;
import com.cylande.unitedretail.message.resource.BusinessParameterScenarioType;
import com.cylande.unitedretail.message.resource.BusinessParameterType;
import com.cylande.unitedretail.portal.service.BusinessParameterManagerServiceDelegate;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.exception.ProcessException;
import com.cylande.unitedretail.process.repositoryloader.ProcessorRepositoryLoader;
import com.cylande.unitedretail.process.repositoryloader.StylesheetRepositoryLoader;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;
import com.cylande.unitedretail.process.tools.EngineProperty;
import com.cylande.unitedretail.process.tools.PropertiesManager;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.PropertiesTools;
import com.cylande.unitedretail.process.tools.Property;
import com.cylande.unitedretail.process.tools.VariablesRepository;

import java.io.File;
import java.io.IOException;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.log4j.Logger;

import org.quartz.SchedulerException;

/**
 * Moteur de batch
 */
public class BatchEngine
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(BatchEngine.class);

  /** extensions pour les fichiers de batch */
  private static final List<String> BATCH_FILES_EXTENSION = Arrays.asList("xsl", "txt", "xml", "properties");

  /** utilisateur */
  public String _user = ExecutionUnit.UNKNOWN_USER;

  /** service CRUD des définitions de Batch */
  private BatchManagerService _batchManager = null;

  /** nom du batch a exécuté */
  private String _batchName = null;

  /** Référentiel de variables ENG */
  private VariablesRepository _varEngRepo = null;

  /** Référentiel de propriétés ENG */
  private PropertiesRepository _propEngRepo = null;

  /** Domaine actif pour l'exécution */
  private String _activeDomain = PropertiesManager.DEFAULT_DOMAIN; // default par défaut

  /** Domain alternatif */
  private String _alternativeDomain = null;

  /** le site local défini */
  private SiteType _localSite = null;

  /** PropertiesManager interne à la classe BatchEngine pour évaluer les éventuelles properties */
  private PropertiesManager _innerBatchEnginePropertiesManager = null;

  /**
   * Default Constructor.
   * @throws SchedulerException exception
   * @throws Exception exception
   */
  public BatchEngine() throws BatchException
  {
    _batchManager = new BatchManagerServiceImpl();
    _innerBatchEnginePropertiesManager = new PropertiesManager();
    initSiteInSYSrepo();
  }

  /**
   * Ajoute la propriété SYS siteCode correspondant au site local.
   */
  private void initSiteInSYSrepo() throws BatchException
  {
    // Ajoute la propriété SYS siteCode correspondant au site local.
    initLocalSite();
    if (_localSite != null)
    {
      _innerBatchEnginePropertiesManager.putSysObject("siteCode", _localSite.getCode());
    }
    // déclaration de la repository de properties pour le manager de propriété interne à BatchEngine
    _innerBatchEnginePropertiesManager.setPropGLOrepo(BatchPropertiesRepository.getInstance());
  }

  /**
   * Charge les repositories de données
   * @throws ProcessException exception
   * @throws BatchException exception
   */
  public void loadRepository() throws ProcessException, BatchException, ConfigEnginePropertiesException
  {
    checkCustomDir();
    BatchRepositoryLoader batchRepositoryLoader = new BatchRepositoryLoader();
    batchRepositoryLoader.load();
    TaskRepositoryLoader taskRepositoryLoader = new TaskRepositoryLoader();
    taskRepositoryLoader.load();
    MapperRepositoryLoader mapperRepositoryLoader = new MapperRepositoryLoader();
    mapperRepositoryLoader.load();
    ProviderRepositoryLoader providerRepositoryLoader = new ProviderRepositoryLoader();
    providerRepositoryLoader.load();
    ProcessorRepositoryLoader processorRepositoryLoader = new ProcessorRepositoryLoader();
    processorRepositoryLoader.load();
    StylesheetRepositoryLoader stylesheetRepositoryLoader = new StylesheetRepositoryLoader();
    stylesheetRepositoryLoader.load();
    TriggerRepositoryLoader triggerRepositoryLoader = new TriggerRepositoryLoader();
    triggerRepositoryLoader.load();
  }

  /**
   * Remplis et relance les files d'attente avec les éléments présent dans le dossier de travail
   * @throws FileManagementException exception
   * @throws ConfigEnginePropertiesException exception
   */
  public void loadQueues() throws FileManagementException, ConfigEnginePropertiesException
  {
    new BatchQueueManager().load();
  }

  /**
   * Execute Batch thanks to his definition switch this type : STATELESS or STATEFULL.
   * @return : InstanceId of Batch, null if batch unknown
   * @throws ConfigEnginePropertiesException exception
   */
  public String execute() throws InterruptedException, SchedulerException, EUBuildException, EULaunchException
  {
    Integer batchId = null;
    batchId = new EUJobManager().launchRoot(_batchName, _activeDomain, _alternativeDomain, _varEngRepo, _propEngRepo, _localSite);
    return batchId != null ? batchId.toString() : null;
  }

  /**
   * Programme un batch en fonction de sa définition (et de son trigger associé)
   * @param pJobManager
   * @param pBatchDef
   * @return 0 si le batch n'a pas de trigger actif
   *         -1 si programmation KO
   *          1 si programmation OK
   * @throws EULaunchException exception
   * @throws ConfigEnginePropertiesException exception
   */
  private int scheduleBatch(EUJobManager pJobManager, BatchType pBatchDef) throws EULaunchException
  {
    int result = 0;
    if (pBatchDef != null && pBatchDef.getTrigger() != null) // if the batch has an already defined trigger
    {
      try
      {
        TriggerType triggerDef = loadTrigger(pBatchDef.getTrigger().getRef());
        setActiveDomain(pBatchDef.getTrigger().getActiveDomain());
        if (triggerDef != null && Boolean.TRUE.equals(triggerDef.isActive()) && evaluateBatchTrigger(pBatchDef))
        {
          LOGGER.info("Programmation du batch " + pBatchDef.getName() + " selon " + triggerDef.getName() + "...");
          if (triggerDef.getCronExpression() != null)
          {
            pJobManager.scheduleBatch(pBatchDef.getName(), triggerDef, _activeDomain, _alternativeDomain, _varEngRepo, _propEngRepo, _localSite);
          }
          else
          {
            MessageConsumerManager.getInstance().addConsumer(triggerDef, pBatchDef);
          }
          result = 1;
        }
      }
      catch (EUBuildException e)
      { //load batch impl
        result = -1;
        LOGGER.warn("Warning while loading Batch Implementation... The batch [" + pBatchDef.getName() + "] is not scheduled... :" + e.getCause());
      }
      catch (WrapperServiceException e2)
      { //load trigger
        result = -1;
        LOGGER.warn("Warning while loading Trigger [" + pBatchDef.getName() + "]... The batch [" + pBatchDef.getName() + "] is not scheduled... :" + e2.getCause());
      }
    }
    return result;
  }

  public void launchInitBatch()
  {
    EUJobManager batchJobManager = new EUJobManager();
    try
    {
      BatchManagerService batchManagerService = new BatchManagerServiceImpl();
      BatchKeyType initBatchKey = new BatchKeyType();
      initBatchKey.setName(BatchRepositoryLoader.INIT_BATCH_NAME);
      BatchType initBatchDef = batchManagerService.getBatch(initBatchKey, null, null);
      if (initBatchDef != null)
      {
        LOGGER.info("lancement du batch d'initialisation ");
        EngineProperty waitEndProp = BatchPropertiesRepository.getInstance().getProperty(BatchRepositoryLoader.WAIT_INIT_BATCH_END_PROPERTY_NAME);
        batchJobManager.launchRoot(BatchRepositoryLoader.INIT_BATCH_NAME, _activeDomain, _alternativeDomain, _varEngRepo, _propEngRepo, _localSite, "true".equals(waitEndProp.getValue()));
      }
    }
    catch (Exception e)
    {
      LOGGER.warn("Warning while launching the init batch... No batch is started:" + e);
    }
  }

  /**
   * Schedule Batchs.
   * @throws SchedulerException exception
   */
  public void scheduleBatchs() throws SchedulerException
  {
    EUJobManager batchJobManager = new EUJobManager();
    try
    {
      LOGGER.info("programmation des batchs automatiques");
      // Find Batchs to schedule
      BatchManagerService batchManagerService = new BatchManagerServiceImpl();
      LOGGER.debug("Find Batchs...");
      BatchCriteriaListType batchCriteriaList = new BatchCriteriaListType();
      BatchListType batchList = batchManagerService.findBatch(batchCriteriaList, null, ContextTransformer.fromLocale());
      // Schedule each Batch Job
      int succesCount = 0;
      int failedCount = 0;
      for (BatchType batch: batchList.getValues())
      {
        int launchRes = scheduleBatch(batchJobManager, batch);
        if (launchRes == 1)
        {
          succesCount++;
        }
        else if (launchRes == -1)
        {
          failedCount++;
        }
      }
      MessageConsumerManager.getInstance().startConnection();
      LOGGER.info(succesCount + " batchs automatiques ont été programmés avec succès");
      if (failedCount > 0)
      {
        LOGGER.info(failedCount + " batchs automatiques n'ont pas pu être programmés");
      }
    }
    catch (Exception e)
    {
      EUJobManager.unScheduleAllBatchs();
      LOGGER.warn("Warning while building BatchListType... No batch is scheduled :", e);
    }
  }

  /**
   *  Evalue si le trigger du batch doit être lancé en fonction des conditions sur le domaine
   */
  private boolean evaluateBatchTrigger(BatchType pBatchDef)
  {
    boolean response = true;
    BatchTriggerType batchTrigger = pBatchDef.getTrigger();
    if (batchTrigger == null)
    {
      return response;
    }
    boolean testInclude = true;
    boolean testExclude = true;
    // test sur l'inclusion de domaine
    String ifDomainValue = batchTrigger.getIfDomain();
    String ifNotDomainValue = batchTrigger.getIfNotDomain();
    if (ifDomainValue == null)
    {
      testInclude = true;
    }
    else
    {
      String includeDomainPattern = RegexPattern.getRegexPattern(ifDomainValue);
      if (includeDomainPattern != null && _activeDomain != null)
      {
        testInclude = _activeDomain.matches(includeDomainPattern);
      }
    }
    // test sur l'exclustion de domaine
    if (ifNotDomainValue == null)
    {
      testExclude = true;
    }
    else
    {
      ifNotDomainValue = replaceProperties(ifNotDomainValue);
      String excludeDomainPattern = RegexPattern.getRegexPattern(ifNotDomainValue);
      if (excludeDomainPattern != null && _activeDomain != null)
      {
        testExclude = !_activeDomain.matches(excludeDomainPattern);
      }
    }
    return testInclude && testExclude;
  }

  /**
   * Stop Scheduler on JobScheduler Instance.
   * @throws SchedulerException exception
   * @throws InterruptedException exception
   */
  public void stopScheduler() throws SchedulerException, InterruptedException
  {
    EUJobManager.unScheduleAllBatchs();
    MessageConsumerManager.getInstance().stopAll();
  }

  /**
   * Setter of user attribute.
   * @param pUser : user who have send request to Servlet
   */
  public void setUser(String pUser)
  {
    if (pUser == null)
    {
      _user = ExecutionUnit.UNKNOWN_USER;
    }
    else
    {
      _user = pUser;
    }
  }

  /**
   * Getter of batch definition.
   * @param pBatchName
   * @return BatchType
   */
  public BatchType getBatch(String pBatchName) throws RemoteException, ServiceException
  {
    BatchType batch = null;
    if (pBatchName != null)
    {
      BatchKeyType key = new BatchKeyType();
      key.setName(pBatchName);
      batch = _batchManager.getBatch(key, null, ContextTransformer.fromLocale());
    }
    return batch;
  }

  /**
   * Positionne le nom du batch à exécuter
   * @param pBatchName
   */
  public void setBatchNameToExe(String pBatchName)
  {
    _batchName = pBatchName;
  }

  /**
   * Spécifie la repo de variables de type Engine
   * @param pVarEngRepo
   */
  public void setVarEngRepo(VariablesRepository pVarEngRepo)
  {
    _varEngRepo = pVarEngRepo;
  }

  /**
   * Spécifie la repo de propriétés de type Engine
   * @param pPropEngRepo
   */
  public void setPropEngRepo(PropertiesRepository pPropEngRepo)
  {
    _propEngRepo = pPropEngRepo;
    if (_innerBatchEnginePropertiesManager != null)
    {
      _innerBatchEnginePropertiesManager.setPropENGrepo(pPropEngRepo);
    }
  }

  /**
   * Positionne le domaine d'exécution
   * @param pActiveDomain
   */
  public void setActiveDomain(String pActiveDomain)
  {
    if (pActiveDomain != null && (pActiveDomain.trim().length() > 0))
    {
      _activeDomain = replaceProperties(pActiveDomain);
    }
  }

  public void setAlternativeDomain(String pAlternativeDomain)
  {
    if (pAlternativeDomain != null && (pAlternativeDomain.trim().length() > 0))
    {
      _alternativeDomain = replaceProperties(pAlternativeDomain);
    }
  }

  /**
   * Enregistre une propriété ENG
   * @param pKey
   * @param pPropDomain
   * @param pValue
   */
  public void putEngineProperty(String pKey, String pPropDomain, String pValue)
  {
    if (_propEngRepo != null)
    {
      Property prop = new Property(pValue);
      _propEngRepo.putProperty(pKey, pPropDomain, prop);
    }
  }

  /**
   * Enregistre une liste de propriétés ENG
   * @param pPropDomain
   * @param pValues
   */
  public void putEngineProperties(String pPropDomain, Map<String, String> pValues)
  {
    if (_propEngRepo != null && pValues != null && !pValues.isEmpty())
    {
      for (Entry<String, String> entry: pValues.entrySet())
      {
        putEngineProperty(entry.getKey(), pPropDomain, entry.getValue());
      }
    }
  }

  /**
   * Load trigger implementation
   * @param pTriggerName : name of the Trigger
   * @return TriggerType : Implementation
   * @throws Exception exception
   */
  private TriggerType loadTrigger(String pTriggerName) throws WrapperServiceException
  {
    TriggerManagerServiceImpl triggerManagerService = new TriggerManagerServiceImpl();
    LOGGER.debug("Looking for Trigger : " + pTriggerName);
    TriggerKeyType key = new TriggerKeyType();
    key.setName(pTriggerName);
    return triggerManagerService.getTrigger(key, null, ContextTransformer.fromLocale());
  }

  /**
   * Retourne le site d'exploitation local
   * @return SiteType
   */
  private SiteType initLocalSite() throws BatchException
  {
    if (_localSite == null)
    {
      _localSite = SiteUtils.getLocalSite();
    }
    return _localSite;
  }

  /**
   * Remplace les propriétés d'une chaîne de caractères
   * @param pString
   * @return résultat
   */
  private String replaceProperties(String pString)
  {
    if (_innerBatchEnginePropertiesManager != null)
    {
      return PropertiesTools.replaceProperties(pString, _innerBatchEnginePropertiesManager, _activeDomain, _alternativeDomain);
    }
    return pString;
  }

  public void setLockedBatchExecution(boolean pValue) throws Exception
  {
    ContextType context = ContextTransformer.fromLocale();
    BusinessParameterType param = URParam.LOCKED_BATCH_EXECUTION;
    param.setIntValue1(pValue ? 1 : 0);
    new BusinessParameterManagerServiceDelegate().postBusinessParameter(param, new BusinessParameterScenarioType(), context);
  }

  /**
   * Vérifie si un répertoire custom a été paramétré et si c'est le cas, écrase les fichiers standard.
   */
  private void checkCustomDir()
  {
    Optional<File> customFolderOptFile = getCustomBatchDirectory();
    Optional<File> repositoryOptFile = getRepoBatchDirectory();
    if (!customFolderOptFile.isPresent() || !repositoryOptFile.isPresent())
    {
      return;
    }
    File customFolderFile = customFolderOptFile.get();
    File repositoryFile = repositoryOptFile.get();

    try
    {
      FileUtil.copyDirectoryFilesToDirectory(customFolderFile, repositoryFile, BATCH_FILES_EXTENSION);
      FileUtil.copySubDirectoryToDirectory(customFolderFile, repositoryFile, "xsl", BATCH_FILES_EXTENSION);
      FileUtil.copySubDirectoryToDirectory(customFolderFile, repositoryFile, "template", BATCH_FILES_EXTENSION);
    }
    catch (IOException e)
    {
      LOGGER.debug(e);
    }
  }

  /**
   * Récupère le dossier de batchs custom s'il existe
   * @return Dossier custom ou vide
   */
  private Optional<File> getCustomBatchDirectory()
  {
    String customFolder = System.getProperty("CUSTOM_DIR");
    if (customFolder == null)
    {
      LOGGER.debug("Impossible de récupérer la propriété \"CUSTOM_DIR\"");
      return Optional.empty();
    }

    File customFolderFile = new File(customFolder + File.separator + "batchs");
    if (!customFolderFile.isDirectory())
    {
      LOGGER.debug("Impossible de trouver le dossier désigné par la propriété \"CUSTOM_DIR\"");
      return Optional.empty();
    }

    return Optional.of(customFolderFile);
  }

  /**
   * Récupère le dossier de batchs serveur s'il existe
   * @return Dossier serveur ou vide
   */
  private Optional<File> getRepoBatchDirectory()
  {
    String batchDir = null;
    try
    {
      batchDir = ConfigEngineProperties.getInstance().getDirectoryEngineProperties("batch.dir");
    }
    catch (ConfigEnginePropertiesException e)
    {
      LOGGER.debug(e);
      return Optional.empty();
    }

    if (batchDir == null)
    {
      LOGGER.debug("Impossible de récupérer la propriété moteur \"batch.dir\"");
      return Optional.empty();
    }

    File repositoryFile = new File(batchDir).getParentFile();
    if (repositoryFile == null || !repositoryFile.isDirectory())
    {
      LOGGER.debug("Impossible de trouver le dossier désigné par la propriété moteur \"batch.dir\"");
      return Optional.empty();
    }

    return Optional.of(repositoryFile);
  }
}
