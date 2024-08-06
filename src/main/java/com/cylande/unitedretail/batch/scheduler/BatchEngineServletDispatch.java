package com.cylande.unitedretail.batch.scheduler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.execution.quartz.JobManager;
import com.cylande.unitedretail.batch.mapper.flatmapper.FlatTemplate;
import com.cylande.unitedretail.batch.repository.BatchPropertiesRepository;
import com.cylande.unitedretail.batch.repository.BatchRepository;
import com.cylande.unitedretail.batch.service.BatchStatusEngineServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchStatusEngineService;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.common.service.AboutEngineServiceDelegate;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.dbsynchro.service.DbSynchroEngineServiceDelegate;
import com.cylande.unitedretail.framework.URException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchContentType;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchReportType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.TaskChildType;
import com.cylande.unitedretail.message.common.about.AboutListType;
import com.cylande.unitedretail.message.common.about.AboutScenarioType;
import com.cylande.unitedretail.message.common.about.ManifestAboutType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.dbsynchro.DBSynchroScenarioType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.message.network.businessunit.SiteType;
import com.cylande.unitedretail.message.process.ProcessorCriteriaListType;
import com.cylande.unitedretail.message.process.ProcessorListType;
import com.cylande.unitedretail.message.process.ProcessorType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.exception.ProcessException;
import com.cylande.unitedretail.process.repository.ProcessorRepository;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;
import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * Classe de traitement de la servlet BatchEngineServlet. Effectue le traitement demandé à la servlet
 */
public class BatchEngineServletDispatch
{
  /** paramètre de servlet batchName */
  public static final String BATCH_NAME = "batchName";
  /** paramètre de servlet batchId */
  public static final String BATCH_ID = "batchId";
  /** paramètre de servlet action */
  public static final String ACTION = "action";
  /** paramètre de servlet site */
  public static final String SITE = "site";
  /** paramètre de servlet domain */
  public static final String DOMAIN = "domain";
  /** paramètre de servlet domaine alternatif */
  public static final String DEFAULT_DOMAIN = "defaultDomain";
  /** paramètre de servlet xsl */
  public static final String XSL = "xsl";
  /** paramètre de servlet user */
  public static final String USER = "user";
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(BatchEngineServletDispatch.class);
  /** le code du site local */
  private static final String LOCAL_SITE_CODE = getLocalSite().getCode();
  private static final String ACTION_CONTENT_TYPE;
  /** instance du moteur de batch */
  private BatchEngine _batchEngine = null;
  /** instance du service de consultation d'avancement des batchs */
  private BatchStatusEngineService _statusService = new BatchStatusEngineServiceImpl();
  /** instance de jaxb manager */
  private JAXBManager _jaxbManager = new JAXBManager();
  /** writer de sortie */
  private PrintWriter _output = null;
  private HttpServletResponse _response;
  /** paramètres de servlet */
  private Map<String, String> _servletParams = null;
  /** marqueur de mise en forme xsl */
  private boolean _hasXSL = false;
  /** identifiant utilisateur */
  private String _user = null;
  static
  {
    String contentType = BatchEngineServlet.CONTENT_TYPE;
    try
    {
      contentType = ConfigEngineProperties.getInstance().getEngineProperty("batchaction.execute.contenttype");
      contentType = contentType == null || contentType.equals("") ? BatchEngineServlet.CONTENT_TYPE : contentType;
    }
    catch (ConfigEnginePropertiesException e)
    {
      LOGGER.error(e, e);
    }
    finally
    {
      ACTION_CONTENT_TYPE = contentType;
    }
  }

  /**
   * Constructeur
   * @throws IOException exception
   */
  public BatchEngineServletDispatch(Map<String, String> pParams, HttpServletResponse pResponse) throws ProcessException, BatchException, IOException
  {
    _servletParams = pParams;
    _response = pResponse;
    _output = _response.getWriter();
    _user = pParams.get(USER);
    _hasXSL = "true".equals(pParams.get(XSL));
    _batchEngine = new BatchEngine();
    if (_user != null)
    {
      _batchEngine.setUser(_user);
    }
  }

  /**
   * Méthode principale, lance les traitements en fonction du paramétrage
   */
  public void service()
  {
    actionLauncher();
    sendResponse();
  }

  /**
   * Méthode de sélection de l'action à exécuter
   */
  private void actionLauncher()
  {
    String action = _servletParams.get(ACTION);
    if ("execute".equals(action))
    {
      executeBatchAction();
    }
    else if ("status".equals(action))
    {
      getBatchStatusAction();
    }
    else if ("reload".equals(action))
    {
      reloadAction();
    }
    else if ("stopBatch".equals(action))
    {
      stopBatchAction();
    }
    else if ("stopScheduler".equals(action))
    {
      stopSchedulerAction();
    }
    else if ("about".equals(action))
    {
      getAboutAction();
    }
    else if ("servletName".equals(action))
    {
      getServletNameAction();
    }
    else if ("disableAutoLock".equals(action))
    {
      disableAutoLockAction();
    }
    else if ("getExecutingJobList".equals(action))
    {
      getExecutingJobList(false);
    }
    else if ("getExecutingJobCount".equals(action))
    {
      getExecutingJobList(true);
    }
    else
    {
      getInterfaceAction();
    }
  }

  private void getExecutingJobList(boolean pCount)
  {
    try
    {
      _response.setContentType("text/plain; charset=ISO-8859-1");
      if (pCount)
      {
        _output.println(JobManager.getInstance().getExecutingJobList().size());
      }
      else
      {
        for (JobExecutionContext job: JobManager.getInstance().getExecutingJobList())
        {
          _output.println(job.getJobDetail().getName());
        }
      }
    }
    catch (SchedulerException e)
    {
      LOGGER.error(e, e);
    }
  }

  private void getInterfaceAction()
  {
    try
    {
      _response.setContentType("text/html; charset=ISO-8859-1");
      ContextType ctx = ContextTransformer.fromLocale();
      BatchRepository batchRepo = BatchRepository.getInstance();
      BatchListType batchList = batchRepo.findBatch(new BatchCriteriaListType(), null, ctx);
      TreeSet<String> complexBatchSet = new TreeSet();
      TreeSet<String> simpleBatchSet = new TreeSet();
      BatchContentType content;
      for (BatchType batch: batchList.getValues())
      {
        content = batch.getSequence() != null ? batch.getSequence() : batch.getFork();
        if (content != null && !content.getTaskOrBatchOrComment().isEmpty())
        {
          BatchChildrenAbstractType child = content.getTaskOrBatchOrComment().get(0);
          if (child instanceof TaskChildType)
          {
            simpleBatchSet.add(batch.getName());
          }
          else
          {
            complexBatchSet.add(batch.getName());
          }
        }
      }
      StringWriter complexBatchSW = new StringWriter();
      StringWriter simpleBatchSW = new StringWriter();
      PrintWriter complexBatchPW = new PrintWriter(complexBatchSW);
      PrintWriter simpleBatchPW = new PrintWriter(simpleBatchSW);
      complexBatchPW.println("<option/>");
      for (String name: complexBatchSet)
      {
        complexBatchPW.println("<option value='" + name + "'>" + name + "</option>");
      }
      simpleBatchPW.println("<option/>");
      for (String name: simpleBatchSet)
      {
        simpleBatchPW.println("<option value='" + name + "'>" + name + "</option>");
      }
      Scanner scan = new Scanner(this.getClass().getResourceAsStream("/Batchs.html"));
      scan.useDelimiter("\\Z");
      String html = scan.next();
      html = html.replaceFirst("<select name=\"batchName\" id=\"complexBatch\">", "<select name='batchName'>" + complexBatchSW.toString());
      html = html.replaceFirst("<select name=\"batchName\" id=\"simpleBatch\">", "<select name='batchName'>" + simpleBatchSW.toString());
      ProcessorRepository processRepo = ProcessorRepository.getInstance();
      ProcessorListType processList = processRepo.findProcessor(new ProcessorCriteriaListType(), null, ctx);
      TreeSet<String> processSet = new TreeSet();
      for (ProcessorType process: processList.getValues())
      {
        processSet.add(process.getName());
      }
      StringWriter processSW = new StringWriter();
      PrintWriter processPW = new PrintWriter(processSW);
      processPW.println("<option/>");
      for (String name: processSet)
      {
        processPW.println("<option value='" + name + "'>" + name + "</option>");
      }
      html = html.replaceFirst("<select name=\"processName\">", "<select name='processName'>" + processSW.toString());
      Set<String> propsSet = BatchPropertiesRepository.getInstance().getKeySet();
      TreeSet<String> domainSet = new TreeSet();
      for (String key: propsSet)
      {
        if (key.endsWith("//BUSINESS_UNIT"))
        {
          domainSet.add(key.split("//")[0]);
        }
      }
      StringWriter domainSW = new StringWriter();
      PrintWriter domainPW = new PrintWriter(domainSW);
      for (String name: domainSet)
      {
        domainPW.println("<option value='" + name + "'" + ("default".equals(name) ? " selected" : "") + ">" + name + "</option>");
      }
      html = html.replaceAll("<select name=\"domain\">", "<select name='domain'>" + domainSW.toString());
      _output.println(html);
    }
    catch (Exception e)
    {
      LOGGER.error("Une erreur s'est produite durant la contruction de l'interface", e);
      _output.println("<error>");
      _output.println("<info>An error has occurred during interface building</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }

  /**
   * Exécute un batch
   */
  private void executeBatchAction()
  {
    _response.setContentType(ACTION_CONTENT_TYPE);
    LOGGER.debug("Execution d'un batch par la servlet");
    String batchName = _servletParams.get(BATCH_NAME);
    String domain = _servletParams.get(DOMAIN);
    String alternativeDomain = _servletParams.get(DEFAULT_DOMAIN);
    Map<String, String> extraParams = extractRequestEngineExtraParams();
    _output.println(BatchUtil.XML_HEADER);
    if (_hasXSL)
    {
      _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
    }
    try
    {
      if (domain == null || domain.equals(""))
      {
        domain = "default";
      }
      if (batchName != null && !batchName.equals("") && !MessageConsumerManager.getInstance().getBatchJMSReader().contains(batchName))
      {
        LOGGER.info("execution du batch " + batchName);
        PropertiesRepository propRepo = new PropertiesRepository();
        _batchEngine.setPropEngRepo(propRepo);
        _batchEngine.setActiveDomain(domain);
        _batchEngine.setAlternativeDomain(alternativeDomain);
        _batchEngine.setBatchNameToExe(batchName);
        _batchEngine.putEngineProperties(domain, extraParams);
        String resId = _batchEngine.execute();
        _output.println("<batch>");
        _output.println("<name>" + batchName + "</name>");
        _output.println("<id>" + resId + "</id>");
        _output.println("</batch>");
      }
      else if (MessageConsumerManager.getInstance().getBatchJMSReader().contains(batchName))
      {
        LOGGER.error("le batch " + batchName + "est connecté à un provider de lecture JMS");
        _output.println("<problem>unauthorized launch for batch " + batchName + " : batch connected to a JMS reader</problem>");
      }
      else
      {
        LOGGER.error("le paramètre batchName est null ou vide");
        _output.println("<problem>batchName is null or empty</problem>");
      }
    }
    catch (Exception e)
    {
      if (e instanceof EULaunchException && ((EULaunchException) e).getCanonicalCode().equals(BatchErrorDetail.LOCKED_BATCH_EXECUTION.getCanonicalCode()))
      {
        // inutile d'afficher la trace complète de l'erreur quand le moteur est verrouillé
        LOGGER.error("Une erreur s'est produite durant l'action executeBatch");
      }
      else
      {
        LOGGER.error("Une erreur s'est produite durant l'action executeBatch", e);
      }
      _output.println("<error>");
      _output.println("<info>An error has occurred during execution of batch " + batchName + "</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }

  /**
   * Cette action permet de récupérer le statut d'un batch
   */
  private void getBatchStatusAction()
  {
    LOGGER.debug("Consultation du status d'un batch par la servlet");
    BatchReportType batchReport = null;
    String batchPath = _servletParams.get(BATCH_NAME);
    String batchId = _servletParams.get(BATCH_ID);
    String siteCode = _servletParams.get(SITE);
    _output.println(BatchUtil.XML_HEADER);
    if (_hasXSL)
    {
      _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
    }
    try
    {
      if (siteCode == null || siteCode.equals(""))
      {
        siteCode = LOCAL_SITE_CODE;
      }
      if (batchPath != null && !batchPath.equals("") && batchId != null && siteCode != null && !siteCode.equals("") && !batchId.equals(""))
      {
        BatchRunKeyType key = new BatchRunKeyType();
        key.setId(Integer.valueOf(batchId));
        key.setPath(batchPath);
        SiteKeyType siteKey = new SiteKeyType();
        siteKey.setCode(siteCode);
        key.setSite(siteKey);
        batchReport = _statusService.getBatchRunStatus(key, null, ContextTransformer.fromLocale());
        if (batchReport != null)
        {
          LOGGER.debug("Création du rapport de batch");
          String result = _jaxbManager.toString(batchReport);
          if (result.contains("?>"))
          {
            result = result.substring(result.indexOf("?>") + 2);
          }
          _output.println(result);
        }
        else
        {
          // aucun rapport n'a pu être généré, le run correspondant n'existe certainement pas
          LOGGER.error("BatchRun " + batchPath + " introuvable ou inexistant");
          _output.println("<problem>batch " + batchPath + " with ID " + batchId + " was not found</problem>");
        }
      }
      else
      {
        LOGGER.error("le paramètre batchName et/ou batchId est null ou vide");
        _output.println("<problem>batchName or batchId or site parameters is null or empty</problem>");
      }
    }
    catch (Exception e)
    {
      LOGGER.error("une erreur s'est produite durant l'action getBatchStatus", e);
      _output.println("<error>");
      _output.println("<info>An error has occurred during execution getBatchStatus for " + batchPath + " with id " + batchId + "</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }

  /**
   * Cette action arrête un batch
   */
  private void stopBatchAction()
  {
    LOGGER.debug("Demande d'arrêt d'un batch par la servlet");
    String batchPath = _servletParams.get(BATCH_NAME);
    String batchId = _servletParams.get(BATCH_ID);
    _output.println(BatchUtil.XML_HEADER);
    if (_hasXSL)
    {
      _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
    }
    try
    {
      if (batchPath != null && !batchPath.equals("") && batchId != null && !batchId.equals(""))
      {
        try
        {
          EUJobManager eu = new EUJobManager();
          eu.stopBatch(batchPath, Integer.valueOf(batchId));
          _output.println("<info>The batch " + batchPath + '(' + batchId + ") has been interrupt and is stopping</info>");
        }
        catch (Exception e)
        {
          // le batch n'a pas pu être arrêté : il n'existe probablement pas.
          LOGGER.error("BatchRun " + batchPath + '(' + batchId + ") introuvable ou inexistant");
          _output.println("<problem>" + e.getLocalizedMessage() + "</problem>");
        }
      }
      else
      {
        LOGGER.error("le paramètre batchName et/ou batchId est nul ou vide");
        _output.println("<problem>batchName or batchId is null or empty</problem>");
      }
    }
    catch (Exception e)
    {
      LOGGER.error("une erreur s'est produite durant l'action stopBatch", e);
      _output.println("<error>");
      _output.println("<info>An error has occurred during execution stopBatch for " + batchPath + " with id " + batchId + "</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }

  /**
   * Cette action arrête le scheduler Quartz
   */
  private void stopSchedulerAction()
  {
    LOGGER.debug("Demande d'arrêt des triggers par la servlet");
    _output.println(BatchUtil.XML_HEADER);
    if (_hasXSL)
    {
      _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
    }
    try
    {
      LOGGER.info("Arrêt des triggers");
      _batchEngine.stopScheduler();
      _output.println("<info>Scheduler Quartz has been stopped</info>");
    }
    catch (Exception e)
    {
      LOGGER.error("An error has occurred during stopping Quartz Scheduler", e);
    }
  }

  /**
   * Cette action recharge les définitions de batchs
   */
  private void reloadAction()
  {
    LOGGER.debug("Demande de rechargement des batchs par la servlet");
    _output.println(BatchUtil.XML_HEADER);
    if (_hasXSL)
    {
      _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
    }
    try
    {
      LOGGER.info("Rechargement des batchs");
      _batchEngine.loadRepository();
      FlatTemplate.purgeCache();
      LOGGER.info("Rechargement des triggers");
      _batchEngine.stopScheduler();
      _batchEngine.loadQueues();
      _batchEngine.scheduleBatchs();
      _output.println("<info>Reloading Successful</info>");
    }
    catch (Exception e)
    {
      _output.println("<error>");
      _output.println("<info>An error has occurred during reloading batchEngine files descriptor</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }

  /**
   * permet de récupérer le numéro de version du moteur de batch
   */
  private void getAboutAction()
  {
    LOGGER.debug("Demande d'informations par la servlet");
    _output.println(BatchUtil.XML_HEADER);
    if (_hasXSL)
    {
      _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
    }
    try
    {
      AboutEngineServiceDelegate aboutEngine = new AboutEngineServiceDelegate();
      AboutScenarioType aboutScenario = new AboutScenarioType();
      aboutScenario.setWithJars(true);
      aboutScenario.setWithApp(false);
      aboutScenario.setWithSWFs(false);
      AboutListType list = aboutEngine.findModules(aboutScenario, new ContextType());
      for (ManifestAboutType myJar: list.getAboutManifestValues())
      {
        if (myJar.getModuleName().equals("Batch"))
        {
          _output.println("<info>");
          _output.println("<moduleName>" + myJar.getModuleName() + "</moduleName>");
          _output.println("<manifestVersion>" + myJar.getManifestVersion() + "</manifestVersion>");
          _output.println("<archiverVersion>" + myJar.getArchiverVersion() + "</archiverVersion>");
          _output.println("<createdBy>" + myJar.getCreatedBy() + "</createdBy>");
          _output.println("<buildJDK>" + myJar.getBuildJdk() + "</buildJDK>");
          _output.println("<implementationTitle>" + myJar.getImplementationTitle() + "</implementationTitle>");
          _output.println("<implementationVendor>" + myJar.getImplementationVendor() + "</implementationVendor>");
          _output.println("<implementationVersion>" + myJar.getImplementationVersion() + "</implementationVersion>");
          _output.println("<implementationBuild>" + myJar.getImplementationBuild() + "</implementationBuild>");
          _output.println("<specificationTitle>" + myJar.getSpecificationTitle() + "</specificationTitle>");
          _output.println("<specificationVendor>" + myJar.getSpecificationVendor() + "</specificationVendor>");
          _output.println("<specificationVersion>" + myJar.getSpecificationVersion() + "</specificationVersion>");
          _output.println("<generationTime>" + myJar.getGenerationTime() + "</generationTime>");
          _output.println("</info>");
        }
      }
    }
    catch (Exception e)
    {
      _output.println("<error>");
      _output.println("<info>An error has occurred while trying to get version</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }

  /**
   * Restitue le nom de la servlet
   */
  private void getServletNameAction()
  {
    LOGGER.debug("Demande du nom de la servlet");
    _output.println(BatchUtil.XML_HEADER);
    _output.println("<info><servletName>BatchEngineServlet</servletName></info>");
  }

  /**
   * Extraction des paramètres de la requête autres que ceux identifiés par le traitement principal de la servlet.
   * @return la map de paramètres extraits
   */
  private Map<String, String> extractRequestEngineExtraParams()
  {
    Map<String, String> result = new HashMap<String, String>();
    result.putAll(_servletParams);
    result.remove(ACTION);
    result.remove(BATCH_NAME);
    result.remove(BATCH_ID);
    result.remove(SITE);
    result.remove(DOMAIN);
    result.remove(USER);
    result.remove(XSL);
    return result;
  }

  /**
   * Affiche l'erreur récupérée dans le flux de sortie de la servlet
   * @param pError l'exception à afficher
   */
  private void printError(Exception pError)
  {
    if (pError instanceof URException)
    {
      URException exception = (URException)pError;
      _output.println("<code>" + exception.getCanonicalCode() + "</code>");
    }
    _output.println("<message>" + pError.getLocalizedMessage() + "</message>");
  }

  /**
   * Récupère le site local
   * @return le site local
   */
  private static synchronized SiteType getLocalSite()
  {
    return SiteUtils.getLocalSite();
  }

  /**
   * Flush le flux de réponse
   */
  private void sendResponse()
  {
    _output.flush();
  }

  /**
   * Désactive le verrouillage automatique de l'exécution des batchs
   */
  private void disableAutoLockAction()
  {
    try
    {
      new DbSynchroEngineServiceDelegate().forceSynchronizeDatabase(new DBSynchroScenarioType(), ContextTransformer.fromLocale());
      new BatchEngine().setLockedBatchExecution(false);
      _output.println(BatchUtil.XML_HEADER);
      if (_hasXSL)
      {
        _output.println(BatchUtil.XSL_ENGINE_INCLUDE);
      }
      _output.println("<info>Unlock successful</info>");
    }
    catch (Exception e)
    {
      LOGGER.error("une erreur s'est produite durant l'action unlockBatch", e);
      _output.println("<error>");
      _output.println("<info>An error has occurred during execution unlockBatch</info>");
      this.printError(e);
      _output.println("</error>");
    }
  }
}
