package com.cylande.unitedretail.batch.task;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchExceptionManager;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.execution.EUJob;
import com.cylande.unitedretail.batch.execution.ExecutionUnit;
import com.cylande.unitedretail.batch.execution.UpdateRunKind;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.impl.FileProvider;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.batch.provider.rw.build.ProviderRWFactory;
import com.cylande.unitedretail.batch.provider.rw.impl.JMSProviderReader;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderWriterImpl;
import com.cylande.unitedretail.batch.repository.BatchRepository;
import com.cylande.unitedretail.batch.repository.TaskPropertiesRepository;
import com.cylande.unitedretail.batch.repository.TriggerRepository;
import com.cylande.unitedretail.batch.scheduler.MessageConsumer;
import com.cylande.unitedretail.batch.scheduler.MessageConsumerManager;
import com.cylande.unitedretail.batch.service.FileProviderTraceManagerServiceDelegate;
import com.cylande.unitedretail.batch.service.TaskAuditManagerServiceDelegate;
import com.cylande.unitedretail.batch.service.TaskRunManagerServiceDelegate;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.batch.transformer.coordinator.TaskRunCoordinator;
import com.cylande.unitedretail.common.tools.ConnectionManager;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.URException;
import com.cylande.unitedretail.framework.exception.FileManagementException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.FilenameUtil;
import com.cylande.unitedretail.framework.tools.filemanagement.DirectoryFileManager;
import com.cylande.unitedretail.message.batch.AbstractStream;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.ContentInfoType;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.batch.JMSPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.batch.TaskProviderType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.batch.TriggerKeyType;
import com.cylande.unitedretail.message.batch.TriggerType;
import com.cylande.unitedretail.message.batch.XMLSplitType;
import com.cylande.unitedretail.process.exception.ProcessException;
import com.cylande.unitedretail.process.service.ProcessEngine;
import com.cylande.unitedretail.process.service.ProcessEngineImpl;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Implementation of "Task" concept <p>Tasks offers a way to implement concrete exchanges orchestrations in java.
 */
public abstract class AbstractTask extends ExecutionUnit
{
  /** id de la tache exécutée */
  public static final String SYS_ID_KEY_NAME = "currentTaskId";
  /** path de la tache exécutée */
  public static final String SYS_PATH_KEY_NAME = "currentTaskPath";
  protected static final String SYS_START_TIME_DB_KEY_NAME = "currentTaskStartTimeDB";
  /** date de début d'exécution de la tache */
  private static final String SYS_START_TIME_KEY_NAME = "currentTaskStartTime";
  /** logger technique */
  private static final Logger LOGGER = Logger.getLogger(AbstractTask.class);
  /** définition de la tache */
  protected TaskType _taskDef;
  /** provider d'entrée pour la tache */
  protected transient ProviderReader _inputProviderReader = null;
  /** flag indiquant l'initialisation du provider d'entrée */
  protected transient boolean _inputProviderInitialized = false;
  /** provider de sortie pour la tache */
  protected transient ProviderWriter _responseProviderWriter = null;
  /** provider de rejet */
  protected transient ProviderWriter _rejectProviderWriter = null;
  /** flag indiquant une demande d'annulation du traitement */
  protected transient boolean _cancelAsked = false;
  /** valeur du commitFrequency */
  protected int _commitFrequency = 0;
  /** factory pour la génération des providers */
  protected transient ProviderRWFactory _providerFactory = null;
  /** niveau d'erreur identifié */
  protected int _errorLevel = 0;
  /** informations d'exécution pour la traçabilité */
  protected transient TaskRunType _currentRunDef = null;
  protected boolean _noProcessor = false;
  /** temps moyen de traitement d'un paquet de données en milliseconde */
  protected long _avgDataProcess = 0L;
  protected boolean _data = false;
  /** compteur de nombre de paquet traité */
  protected int _countProgress = 0;
  /** compteur de nombre de paquet traité utilisé par le xmlSplit pour gérer le cas où les paquets ne sont jamais complet par
   * exemple avec l'utisation d'un service spécifique filtrant les données d'un paquet renvoyé par le service standard
  */
  protected int _countProgressByFile = 0;
  protected String _initFileName = null;
  /** heure de dernière erreur - Bidouille pour résoudre pb de conflit ADF avec les history columns - */
  private Calendar _errorCalendar = null;
  /** ref du process que la tache va déclencher */
  private String _processRef = null;
  /** instance du process engine qui sera utilisé par la tache */
  private transient ProcessEngine _processEngine = null;
  /** service delegate de trace (task run) */
  private transient TaskRunManagerServiceDelegate _taskRunManagerService = null;
  /** service delegate de trace des erreurs (task audit) */
  private transient TaskAuditManagerServiceDelegate _taskAuditManagerService = null;
  /** service delegate de trace des fichiers utilisés (file_provider_trace) */
  private transient FileProviderTraceManagerServiceDelegate _fileProviderTraceManagerService = null;
  private int _splitNumber = 1;
  private MessageConsumer _consumer;

  /**
   * Constructor
   */
  public AbstractTask(AbstractBatch pParent, TaskType pBean) throws TaskException
  {
    super(pParent, pBean == null ? null : pBean.getName());
    init(pBean);
    _providerFactory = new ProviderRWFactory(this);
  }

  /**
   * initInputProviderReader
   * @throws Exception exception
   */
  protected void initInputProviderReader() throws Exception
  {
    AbstractStream inputAbstractStream = _taskDef.getInput();
    if (inputAbstractStream != null)
    {
      _inputProviderReader = _providerFactory.generateProviderReader("input", decodeAbstractStreamProperties(inputAbstractStream), _currentRunDef, _commitFrequency);
      if (_inputProviderReader instanceof JMSProviderReader)
      {
        setMessageConsumer();
      }
      // Initialisation du descripteur de fichiers de rejet utilisé dans le cas de fichiers invalides
      if (_taskDef.getReject() != null)
      {
        ProviderWriter rejectProviderWriter = _providerFactory.generateProviderWriter("reject", _taskDef.getReject(), _currentRunDef);
        if (rejectProviderWriter != null)
        {
          try
          {
            _inputProviderReader.setReject(rejectProviderWriter.getProviderFileType());
          }
          catch (Exception e)
          {
            LOGGER.error("unable to initialize reject provider writer on task " + getName());
            throw e;
          }
        }
      }
    }
  }

  private void setMessageConsumer()
  {
    BatchKeyType key = new BatchKeyType();
    key.setName(getSysPath().split("\\.")[0]);
    BatchType batchRootDef = BatchRepository.getInstance().getBatch(key, null, null);
    if (batchRootDef.getTrigger() != null)
    {
      TriggerKeyType triggerKey = new TriggerKeyType();
      triggerKey.setName(batchRootDef.getTrigger().getRef());
      TriggerType triggerDef = TriggerRepository.getInstance().getTrigger(triggerKey, null, null);
      if (triggerDef != null)
      {
        _consumer = MessageConsumerManager.getInstance().getMessageConsumerMap().get(triggerKey.getName());
      }
    }
  }

  /**
   * Récupère le préfixe du nom de la tâche
   * @return le préfixe du nom de la tâche
   * @throws TaskException exception
   */
  protected String getTaskName() throws TaskException
  {
    String response = "";
    try
    {
      StringBuffer buf = new StringBuffer(_taskDef.getName());
      int lastindex = buf.lastIndexOf("-");
      response = buf.substring(lastindex + 1);
      return response;
    }
    catch (Exception e)
    {
      throw new TaskException(BatchErrorDetail.TASK_GETTASKNAME, e);
    }
  }

  /**
   * Add a new event (error)
   * @param pCode
   * @param pDesc
   */
  private void addEvent(String pCode, String pDesc)
  {
    // Log event
    LOGGER.debug("ADDEVENT -> " + pCode + ":" + pDesc);
    // Add event
    TaskAuditType error = new TaskAuditType();
    try
    {
      // Bidouille pour éviter des problemes d'enregistrement avec les history colums
      if (_errorCalendar == null || _errorCalendar.before(Calendar.getInstance()))
      {
        _errorCalendar = Calendar.getInstance();
      }
      else if (_errorCalendar.compareTo(Calendar.getInstance()) >= 0)
      {
        _errorCalendar.add(Calendar.MILLISECOND, 1);
      }
      error.setPath(getSysPath().replaceAll(EUJob.THREAD_POOLED_NAME + "\\d+", ""));
      error.setTask(getSysId());
      error.setErrorCode(pCode);
      error.setErrorMessage(pDesc);
      error.setEventTime(_errorCalendar);
      error.setSite(getSiteKey());
      error.setFileId(getFileId());
      getTaskAuditManagerService().createTaskAudit(error, null, ContextTransformer.fromLocale());
    }
    catch (Exception e)
    {
      // probable perte de connexion BDD
      _inputProviderReader.disableArchive(); // on désactive l'archivage afin que le fichier soit rejoué plus tard
      LOGGER.warn("Failed to add event using TaskAuditManagerService", e);
    }
  }

  private Integer getFileId() throws Exception
  {
    String currentFileName;
    if (this instanceof TaskIntegrationThreadPooledImpl)
    {
      currentFileName = ((TaskIntegrationThreadPooledImpl)this)._currentFile;
    }
    else
    {
      currentFileName = _inputProviderReader != null ? _inputProviderReader.getCurrentFileName() : null;
    }
    Integer result = null;
    if (currentFileName != null)
    {
      for (FileProviderTraceType file: _currentRunDef.getInputProvider().getFileList())
      {
        if (file.getFileName().equals(currentFileName))
        {
          file.setInError(true);
          getFileProviderTraceManagerService().updateFileProviderTrace(file, null, ContextTransformer.fromLocale());
          result = file.getId();
          break;
        }
      }
    }
    return result;
  }

  /**
   * Update the number of exchanged records into the run
   * @param pUpdateRunKind
   * @param pValue
   * @throws TaskException exception
   */
  protected void updateTaskRun(UpdateRunKind pUpdateRunKind, Object pValue) throws TaskException
  {
    if (!(this instanceof TaskIntegrationDispatchImpl))
    {
      if (pUpdateRunKind == null || pValue == null)
      {
        throw new TaskException(BatchErrorDetail.TASK_UPDATETASKRUN_PARAM);
      }
      // update run
      switch (pUpdateRunKind)
      {
        case WORK_LOAD:
          _currentRunDef.setWorkLoad((Integer)pValue);
          break;
        case WORK_PROGRESS:
          _currentRunDef.setWorkProgress((Integer)pValue);
          break;
        case STEP:
          _currentRunDef.setStep(pValue.toString());
          break;
      }
      try
      {
        getTaskRunManagerService().updateTaskRun(_currentRunDef, null, ContextTransformer.fromLocale());
      }
      catch (Exception e)
      {
        throw new TaskException(BatchErrorDetail.UPDATE_TASKRUN_ERR, new Object[] { getSysPath(), getSysId() }, e);
      }
    }
  }

  /**
   * {@inheritDoc}
   * @throws TaskException exception
   */
  protected void before() throws TaskException
  {
    try
    {
      createTaskRun();
      if (!(this instanceof TaskIntegrationDispatchImpl))
      {
        _currentRunDef = getTaskRunManagerService().createTaskRun(_currentRunDef, null, ContextTransformer.fromLocale());
        setSysId(_currentRunDef.getId());
      }
      else
      {
        setSysId(Integer.valueOf((int)(System.currentTimeMillis() / 100)));
      }
      initInputProviderReader();
    }
    catch (Exception e)
    {
      throw new TaskException(BatchErrorDetail.TASKRUN_BEFORE_ERR, new Object[] { getSysPath(), getParentId() }, e);
    }
  }

  /** {@inheritDoc} */
  protected void after()
  {
    this.leaveShadowDomain();
    if (!(this instanceof TaskIntegrationDispatchImpl))
    {
      // Update the run for this task (and specify endTime)
      try
      {
        // update run
        _currentRunDef.setStatus(true);
        _currentRunDef.setEndTime(Calendar.getInstance());
        getTaskRunManagerService().updateTaskRun(_currentRunDef, null, ContextTransformer.fromLocale());
      }
      catch (Exception e)
      {
        new TaskException(BatchErrorDetail.TASKRUN_AFTER_ERR, new Object[] { getSysPath(), getSysId() }, e).log();
      }
    }
  }

  protected void generateFlag()
  {
  }

  /**
   * Créé un bean TaskRun dans la variable _currentRunDef à partir de la définition de la tâche et des informations d'exécution
   */
  private void createTaskRun()
  {
    if (_currentRunDef == null)
    {
      _currentRunDef = new TaskRunType();
      _currentRunDef.setId(getSysId());
      // On retire notre -Thread pour taskRun
      _currentRunDef.setPath(getSysPath().replaceAll(EUJob.THREAD_POOLED_NAME + "\\d+", ""));
      _currentRunDef.setSite(getSiteKey());
      _currentRunDef.setParentId(getParentId());
      _currentRunDef.setStartTime(getSysStartTime());
      _currentRunDef.setStep("start");
      _currentRunDef.setStatus(false);
      _currentRunDef.setDomain(getDomain());
      // informations execution de processor
      ContentInfoType processInfo = new ContentInfoType();
      if (!_noProcessor)
      {
        processInfo.setName(getTaskDef().getProcessor().getRef());
        processInfo.setDomain(getDomainForProcess());
      }
      _currentRunDef.setProcessInfo(processInfo);
      // informations utilisation input provider
      TaskRunCoordinator.setProvider(_currentRunDef, getTaskDef());
      // type de tache
      _currentRunDef.setTaskType(getTaskDef().getClass().getSimpleName());
    }
  }

  /**
   * init
   * @param pTaskType la définition de la tâche
   * @throws TaskException exception
   */
  private void init(TaskType pTaskType) throws TaskException
  {
    _taskDef = pTaskType;
    if (pTaskType == null)
    {
      throw new TaskException(BatchErrorDetail.INIT_TASK_PARAM);
    }
    _propManager.setPropGLOrepo(TaskPropertiesRepository.getInstance());
    // récupération de la ref du process à déclencher
    if (pTaskType.getProcessor() == null || pTaskType.getProcessor().getRef() == null)
    {
      _noProcessor = true;
    }
    else
    {
      // récupération de la ref de process utilisé par la task
      String processRefNoFiltered = pTaskType.getProcessor().getRef();
      _processRef = getFilteredString(processRefNoFiltered);
    }
  }

  /**
   * Get nested TaskRunManagerService
   * @return TaskRunManagerServiceDelegate
   */
  public TaskRunManagerServiceDelegate getTaskRunManagerService()
  {
    if (_taskRunManagerService == null)
    {
      _taskRunManagerService = new TaskRunManagerServiceDelegate();
    }
    return _taskRunManagerService;
  }

  /**
   * Get nested TaskAuditManagerService
   * @return TaskAuditManagerServiceDelegate
   */
  public TaskAuditManagerServiceDelegate getTaskAuditManagerService()
  {
    if (_taskAuditManagerService == null)
    {
      _taskAuditManagerService = new TaskAuditManagerServiceDelegate();
    }
    return _taskAuditManagerService;
  }

  /**
   * Get nested TaskAuditManagerService
   * @return FileProviderTraceManagerServiceDelegate
   */
  public FileProviderTraceManagerServiceDelegate getFileProviderTraceManagerService()
  {
    if (_fileProviderTraceManagerService == null)
    {
      _fileProviderTraceManagerService = new FileProviderTraceManagerServiceDelegate();
    }
    return _fileProviderTraceManagerService;
  }

  /**
   * Libère les ressources utilisées par les providers
   * @throws ProviderException exception
   */
  protected void releaseProviderResources() throws ProviderException
  {
    // provoque la finalisation (renommage archivage...)
    if (_inputProviderReader != null)
    {
      LOGGER.debug("Release providerInput");
      commitJMS(); // commit JMS à la fin de la task juste avant la fermeture de la connexion effectuée par le releaseProvider de l'input JMS
      _inputProviderReader.releaseProvider();
    }
    if (_responseProviderWriter != null)
    {
      LOGGER.debug("Release providerResponse");
      _responseProviderWriter.releaseProvider();
    }
    if (_rejectProviderWriter != null)
    {
      LOGGER.debug("Release providerReject");
      _rejectProviderWriter.releaseProvider();
    }
  }

  /**
   * traceError
   * @param pException
   */
  protected void traceError(EUExecutionException pException)
  {
    setException(pException);
    Throwable calledProcessException = BatchExceptionManager.getFirstInternalException(pException);
    if (calledProcessException == null)
    {
      calledProcessException = pException;
    }
    Throwable cause = BatchExceptionManager.getCause(calledProcessException);
    if ((cause != null) && (cause instanceof URException))
    {
      calledProcessException = cause;
    }
    cause = BatchExceptionManager.getCause(calledProcessException);
    while ((cause != null) && (cause instanceof WrapperServiceException))
    {
      calledProcessException = cause;
      cause = BatchExceptionManager.getCause(calledProcessException);
    }
    String code = ((URException)calledProcessException).getCanonicalCode();
    String message = calledProcessException.getLocalizedMessage();
    if (calledProcessException instanceof WrapperServiceException)
    {
      String detail = ((WrapperServiceException)calledProcessException).getDetailMessage();
      if ((detail != null) && (detail.trim().length() > 0))
      {
        message += " : " + detail;
      }
    }
    LOGGER.debug("ajout de l'erreur de task d'extraction en base : " + code);
    logInfo("ERROR", code + ":" + message);
    addEvent(code, message);
  }

  /**
   * traceTechnicalError
   * @param pException
   */
  protected void traceTechnicalError(EUExecutionException pException)
  {
    if (pException == null)
    {
      return;
    }
    setException(pException);
    Throwable explainedException = BatchExceptionManager.getFirstInternalException(pException);
    if (explainedException == null)
    {
      explainedException = pException;
    }
    Throwable cause = BatchExceptionManager.getCause(explainedException);
    if ((cause != null) && (cause instanceof URException))
    {
      explainedException = cause;
    }
    cause = BatchExceptionManager.getCause(explainedException);
    String code = ((URException)explainedException).getCanonicalCode();
    String message = explainedException.getLocalizedMessage();
    if ((cause != null) && (cause != explainedException))
    {
      String detail = cause.getLocalizedMessage();
      if ((detail != null) && (detail.trim().length() > 0))
      {
        message += " : " + detail;
      }
    }
    LOGGER.debug("ajout de l'erreur de task d'extraction en base : " + code + message);
    logInfo("ERROR", code + ":" + message);
    addEvent(code, message);
  }

  /**
   * Restitue la définition de la tâche associée
   * @return définition de la tâche
   */
  public TaskType getTaskDef()
  {
    return _taskDef;
  }

  /**
   * setProcessEngine
   * @param pProcessEngine the processor Engine
   */
  protected void setProcessEngine(ProcessEngine pProcessEngine)
  {
    _processEngine = pProcessEngine;
  }

  /**
   * getProcessEngine
   * @return the processor Engine
   */
  protected ProcessEngine getProcessEngine() throws ProcessException
  {
    if (_processEngine == null)
    {
      _processEngine = new ProcessEngineImpl();
    }
    return _processEngine;
  }

  /**
   * Renvoie la référence du processor associé à la tâche
   * @return référence du processor
   */
  protected String getProcessRef()
  {
    return _processRef;
  }

  /**
   * Libère les ressources
   */
  protected void releaseResources()
  {
    setProcessEngine(null);
    try
    {
      releaseProviderResources();
    }
    catch (ProviderException f)
    {
      LOGGER.warn("Impossible de libérer les ressources");
    }
    generateFlag();
    _taskAuditManagerService = null;
    _taskRunManagerService = null;
    _currentRunDef = null;
    if (_consumer != null)
    {
      _consumer.resetConnection();
    }
  }

  /**
   * Evalue le domaine qui sera positionné pour le processor
   * @return domaine du processor
   */
  protected String getDomainForProcess()
  {
    String result = getTaskDef().getProcessor().getActiveDomain();
    if (result == null || (result.trim().length() == 0))
    {
      result = getDomain();
    }
    return result;
  }

  /**
   * doCancel
   * @param pForced true if forced stop asked
   */
  protected void doCancel(boolean pForced)
  {
    _cancelAsked = true;
  }

  /**
   * setSysId
   * @param pValue
   */
  public void setSysId(Integer pValue)
  {
    if (pValue != null)
    {
      _propManager.putSysObject(SYS_ID_KEY_NAME, pValue);
      _debugContextInfo = getSysPath() + '[' + pValue + ']';
    }
  }

  /**
   * Accesseur pour l'id courant (de SysRepo)
   * @return l'id courant
   */
  public Integer getSysId()
  {
    return (Integer)_propManager.getSysObject(SYS_ID_KEY_NAME, -1);
  }

  /**
   * setSysPath
   * @param pValue
   */
  public void setSysPath(String pValue)
  {
    if (pValue != null)
    {
      _propManager.putSysObject(SYS_PATH_KEY_NAME, pValue);
    }
  }

  /**
   * getSysPath
   * @return sysPath
   */
  public String getSysPath()
  {
    return (String)_propManager.getSysObject(SYS_PATH_KEY_NAME, "");
  }

  /**
   * setSysStartTime
   * @param pValue
   */
  public void setSysStartTime(Calendar pValue)
  {
    if (pValue != null)
    {
      _propManager.putSysObject(SYS_START_TIME_KEY_NAME, pValue);
    }
    _propManager.putSysObject(SYS_START_TIME_DB_KEY_NAME, getCurrentDBTime());
  }

  /**
   * getSysStartTime
   * @return Calendar
   */
  public Calendar getSysStartTime()
  {
    return (Calendar)_propManager.getSysObject(SYS_START_TIME_KEY_NAME, null);
  }

  /**
   * decodeAbstractStreamProperties
   * @param pAbstractStream
   * @return AbstractStream
   */
  private AbstractStream decodeAbstractStreamProperties(AbstractStream pAbstractStream)
  {
    AbstractStream result = new AbstractStream();
    TaskProviderType provider = pAbstractStream.getProvider();
    if (provider != null)
    {
      TaskProviderType clonedProvider = new TaskProviderType();
      clonedProvider.setRef(getFilteredString(provider.getRef()));
      clonedProvider.setActiveDomain(getFilteredString(provider.getActiveDomain()));
      result.setProvider(clonedProvider);
    }
    return result;
  }

  /**
   * Lecture de données dans le provider de lecture, lit le prochain paquet tant qu'il y en a dans le provider
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return DataPackage
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  protected DataPackage readInput(Integer pCurrentTaskId) throws TaskException, ProviderException
  {
    DataPackage result = null;
    if (_inputProviderReader != null)
    {
      updateTaskRun(UpdateRunKind.STEP, "input");
      logInfo("STEP", "input");
      Integer currentTaskId = pCurrentTaskId;
      if ((currentTaskId == null) || (currentTaskId.intValue() == 0))
      {
        currentTaskId = getSysId();
      }
      result = _inputProviderReader.read(_noProcessor, currentTaskId);
      if (result != null)
      {
        result.setFileName(_inputProviderReader.getCurrentFileName());
      }
    }
    return result;
  }

  /**
   * Ecrit un message dans le provider de réponse
   * @param pXmlResponse
   * @throws ProviderException exception
   */
  protected final void writeResponse(String pXmlResponse) throws ProviderException
  {
    try
    {
      if (_responseProviderWriter == null)
      {
        _responseProviderWriter = _providerFactory.generateProviderWriter("response", _taskDef.getResponse(), _currentRunDef);
      }
      if (_responseProviderWriter != null)
      {
        _data = true;
        _responseProviderWriter.write(pXmlResponse);
        manageXMLSplit();
      }
    }
    catch (ProviderException e)
    {
      LOGGER.error("unable to write response on task " + getName() + " package=\"" + pXmlResponse + "\"");
      throw e;
    }
  }

  protected void manageXMLSplit() throws ProviderException
  {
    ProviderType providerDef = _responseProviderWriter.getProvider().getProviderDef();
    if (providerDef instanceof FILEPROVIDER)
    {
      XMLSplitType xmlSplit = ((FILEPROVIDER)providerDef).getFile().getXmlSplit();
      boolean jsonArray = ((FILEPROVIDER)providerDef).getFile().getJsonArray() == null ? false : Boolean.TRUE.equals(((FILEPROVIDER)providerDef).getFile().getJsonArray());
      if (xmlSplit != null && ((_countProgress % xmlSplit.getSize().intValue() == 0) || _countProgressByFile >= xmlSplit.getSize().intValue()))
      {
        _countProgressByFile = 0;
        _responseProviderWriter.releaseProvider();
        commitJMS(); // si provider de sortie de type FILE, permet de faire un commit JMS à chaque fichier intermédiaire généré
        ProviderFileType providerFile = _responseProviderWriter.getProviderFileType();
        if (_initFileName == null)
        {
          _initFileName = _responseProviderWriter.getProvider().getCurrentFileName();
          try
          {
            String dir = new FilenameUtil().addRelativePath(providerFile.getDir());
            DirectoryFileManager directoryFileManager = new DirectoryFileManager(dir);
            directoryFileManager.moveFile(_initFileName, dir, getSplitNameFile(xmlSplit, _splitNumber++), false);
          }
          catch (FileManagementException e)
          {
            LOGGER.error(e, e);
          }
        }
        providerFile.setFileName(getSplitNameFile(xmlSplit, _splitNumber++));
        providerFile.setJsonArray(jsonArray);
        ((FileProvider)_responseProviderWriter.getProvider()).setFileProviderDef(providerFile);
        _responseProviderWriter = new ProviderWriterImpl(_responseProviderWriter.getProvider());
      }
    }
    else if (providerDef instanceof JMSPROVIDER)
    {
      commitJMS(); // si provider de sortie de type JMS, permet de faire un commit JMS à chaque message traité
    }
  }

  private String getSplitNameFile(XMLSplitType pXmlSplit, int pNumber)
  {
    String splitNameFile = pXmlSplit.getFileName().replaceFirst("\\$\\{SYSTEM_outputFileName\\}", _initFileName);
    splitNameFile = splitNameFile.replaceFirst("\\$\\{SYSTEM_outputFileNameNoExt\\}", BatchUtil.getFileNameNoExt(_initFileName));
    return splitNameFile.replaceAll("\\$\\{SYSTEM_splitNumber\\}", String.valueOf(pNumber));
  }

  /**
   * Ecrit un message dans le provider d'erreur
   * @param pXmlReject
   * @throws ProviderException exception
   */
  protected void writeReject(String pXmlReject) throws ProviderException
  {
    try
    {
      // rollback JMS du message en erreur : dans ce cas, la définition d'un provider de rejet est inutile,
      // le message rollbacké sera relivré un maximum de 6 fois si toujours en erreur avant d'être placé en DLQ (norme ActiveMQ par défaut)
      rollbackJMS();
      if (_rejectProviderWriter == null)
      {
        _rejectProviderWriter = _providerFactory.generateProviderWriter("reject", _taskDef.getReject(), _currentRunDef);
        if (_inputProviderReader.isInputJson())
        {
          _rejectProviderWriter.setOutputJson(true);
        }
      }
      if (_rejectProviderWriter != null)
      {
        _rejectProviderWriter.setException(getException());
        _rejectProviderWriter.setTaskCode(getSysPath().replaceAll(EUJob.THREAD_POOLED_NAME + "\\d+", ""));
        _rejectProviderWriter.write(pXmlReject);
      }
    }
    catch (ProviderException e)
    {
      LOGGER.error("unable to write reject on task " + getName() + " package=\"" + pXmlReject + "\"");
      throw e;
    }
  }

  /**
   * Notifie une erreur technique : code 2
   * @param pThrowable
   */
  protected void notifyTechnicalProblem(Throwable pThrowable)
  {
    _errorLevel = 2;
    TaskException taskException = new TaskException(BatchErrorDetail.TASK_EXTRACTION_ERR, new Object[] { getSysPath(), getSysId() }, pThrowable);
    traceTechnicalError(taskException);
  }

  /**
   * Mise à jour de la traçabilité
   */
  protected void finalUpdateTaskRun()
  {
    logInfo("END", String.valueOf(_errorLevel));
    _currentRunDef.setStep(_errorLevel == 0 ? "complete" : "failed");
    // l'update final en base est effectué via la méthode after de AbstractTask
  }

  protected boolean isNotTimeOut()
  {
    boolean result = true;
    Integer timeOut = null;
    Calendar startTime;
    if (_propManager.getSysObject(SYS_ROOT_TIMEOUT_KEY_NAME, null) != null)
    {
      // le timeOut défini sur le batch root est prioritaire
      timeOut = (Integer)_propManager.getSysObject(SYS_ROOT_TIMEOUT_KEY_NAME);
      startTime = (Calendar)_propManager.getSysObject(SYS_ROOT_START_TIME_KEY_NAME);
    }
    else
    {
      timeOut = _taskDef.getTimeOut();
      startTime = this.getSysStartTime();
    }
    if ((timeOut != null) && ((Calendar.getInstance().getTimeInMillis() + _avgDataProcess) > (startTime.getTimeInMillis() + timeOut * 60000L)))
    {
      LOGGER.debug("Task " + getSysPath() + " is time out");
      result = false;
    }
    return result;
  }

  protected Calendar getCurrentDBTime()
  {
    ConnectionManager con = null;
    try
    {
      con = new ConnectionManager();
      Timestamp time = con.getCurrentDBTime();
      Calendar result = Calendar.getInstance();
      int gmtOffset = result.get(Calendar.DST_OFFSET) + result.get(Calendar.ZONE_OFFSET);
      result.setTimeInMillis(time.getTime() + gmtOffset);
      return result;
    }
    catch (Exception e)
    {
      LOGGER.error(e, e);
      return null;
    }
    finally
    {
      if (con != null)
      {
        con.close();
      }
    }
  }

  /**
   * Commit JMS sur le(s) message(s) lu(s) depuis le provider JMS d'entrée
   */
  protected void commitJMS()
  {
    if (_inputProviderReader instanceof JMSProviderReader)
    {
      try
      {
        ((JMSProviderReader)_inputProviderReader).getConnection().getSession().commit();
      }
      catch (JMSException e)
      {
        LOGGER.error(e, e);
      }
    }
  }

  /**
   * Rollback JMS sur le(s) message(s) lu(s) depuis le provider JMS d'entrée
   */
  private void rollbackJMS()
  {
    if (_inputProviderReader instanceof JMSProviderReader)
    {
      try
      {
        ((JMSProviderReader)_inputProviderReader).getConnection().getSession().rollback();
      }
      catch (JMSException e)
      {
        LOGGER.error(e, e);
      }
    }
  }

  private void leaveShadowDomain()
  {
    if (_propManager == null || _propManager.getSYSrepo() == null)
    {
      return;
    }
    _propManager.getSYSrepo().removeShadowDomain();
  }
}
