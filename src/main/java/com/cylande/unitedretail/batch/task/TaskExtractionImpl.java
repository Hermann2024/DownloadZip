package com.cylande.unitedretail.batch.task;

import java.math.BigInteger;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.execution.UpdateRunKind;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.service.BatchReportingEngineServiceImpl;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.compensation.transaction.URTransactionManagerImpl;
import com.cylande.unitedretail.framework.compensation.transaction.common.URTransactionManager;
import com.cylande.unitedretail.framework.tools.FilenameUtil;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.BatchReportingScenarioType;
import com.cylande.unitedretail.message.batch.EXTPARAM;
import com.cylande.unitedretail.message.batch.EXTRACTION;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.TaskRunDetailsType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.sales.common.VariedMovementKeyType;
import com.cylande.unitedretail.message.sales.common.VariedMovementType;
import com.cylande.unitedretail.process.exception.ProcessException;
import com.cylande.unitedretail.process.response.OutputResponse;
import com.cylande.unitedretail.process.response.ProcessorResponse;
import com.cylande.unitedretail.process.response.RejectResponse;
import com.cylande.unitedretail.process.service.ProcessEngineImpl;
import com.cylande.unitedretail.salesnetwork.service.VariedMovementServiceDelegate;

/**
 * Implementation de la tache d'extraction
 */
public class TaskExtractionImpl extends AbstractTask
{
  /**  date et heure de l'heure de démarrage de la dernière extraction correcte de la tâche dans les paramètres systèmes (SysRepo) */
  private static final String SYS_LAST_START_TIME_KEY_NAME = "lastTaskOKStartTime";
  private static final String SYS_LAST_START_TIME_NULL_IF_EMPTY_KEY_NAME = "lastTaskOKStartTimeNullIfEmpty";
  private static final String SYS_LAST_START_TIME_DB_KEY_NAME = "lastTaskOKStartTimeDB";
  private static final BatchReportingScenarioType TASKRUN_DETAILS_SCENARIO;
  /** logger technique */
  private static final Logger LOGGER = Logger.getLogger(TaskExtractionImpl.class);
  /** Numero de version pour la sérialization YYYYMMDDhhmm Doit être mis à jour à chaque modif de la classe */
  private static final long serialVersionUID = 201002231415L;
  /** fetch size */
  protected int _maxFetchSize = 0;
  /** indique si le premier appel au process a déjà été effectuée */
  private boolean _firstCallingIsDone = false;
  /** indique s'il faut continuer l'extraction*/
  private boolean _continueExtraction = false;
  private VariedMovementServiceDelegate _varMvtServ = new VariedMovementServiceDelegate();
  private boolean _singleTransaction = false;
  static
  {
    TASKRUN_DETAILS_SCENARIO = new BatchReportingScenarioType();
    TASKRUN_DETAILS_SCENARIO.setErrorInDepth(true);
    TASKRUN_DETAILS_SCENARIO.setManageErrorDetails(true);
    TASKRUN_DETAILS_SCENARIO.setManageFileProvider(true);
  }

  /**
   * constructor
   */
  public TaskExtractionImpl(AbstractBatch pParent, EXTRACTION pBean) throws TaskException
  {
    super(pParent, pBean);
    if (pBean instanceof EXTPARAM)
    {
      String dynaProvidersLabel = ((EXTPARAM)pBean).getLabel();
      if (dynaProvidersLabel == null || dynaProvidersLabel.equals(""))
      {
        dynaProvidersLabel = getTaskName();
      }
      _providerFactory.setDynaProvidersLabel(dynaProvidersLabel);
      _maxFetchSize = 0;
    }
    else
    {
      BigInteger maxFetchSize = pBean.getMaxFetchSize();
      if (maxFetchSize != null)
      {
        _maxFetchSize = maxFetchSize.intValue();
      }
    }
    if (pBean.isSingleTransaction() != null)
    {
      _singleTransaction = pBean.isSingleTransaction();
    }
  }

  /**
   * Traitement de la réponse du service
   * @param pResponse
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  private void responseTreatment(ProcessorResponse pResponse) throws TaskException, ProviderException
  {
    //TODO à revoir avec les tests d'évaluation de traitement
    if (pResponse == null)
    {
      LOGGER.debug("L'extraction est terminée !");
      _continueExtraction = false;
    }
    else if (pResponse instanceof OutputResponse)
    {
      // redirection du traitement en fonction du type de réponse
      LOGGER.debug("la réponse est positive");
      if (evaluateOutputResponse(pResponse))
      {
        // traitement de la réponse
        outputTreatment((OutputResponse)pResponse);
      }
    }
    else if (pResponse instanceof RejectResponse)
    {
      LOGGER.debug("La réponse est en erreur");
      rejectTreatment((RejectResponse)pResponse);
      _continueExtraction = false;
    }
    // le premier traitement a été effectué
    _firstCallingIsDone = true;
  }

  private boolean evaluateOutputResponse(ProcessorResponse pResponse)
  {
    // évaluation de la réponse positive
    boolean treatment = false;
    // détermine si la réponse doit être traitée
    if (!pResponse.getNullMainService())
    {
      treatment = true;
      _continueExtraction = true;
    }
    else
    {
      // nullService
      LOGGER.debug("le service principal a renvoyé NULL");
      _continueExtraction = false;
      treatment = !_firstCallingIsDone;
    }
    return treatment;
  }

  /**
   * Traitement de la réponse positive
   * @param pResponse
   */
  private void outputTreatment(OutputResponse pResponse) throws TaskException, ProviderException
  {
    // incrémenter le compteur d'éléments correctement traités
    _countProgress += pResponse.getSize();
    _countProgressByFile += pResponse.getSize();
    // mettre à jour la traçabilité
    updateTaskRun(UpdateRunKind.STEP, "output");
    logInfo("STEP", "output");
    LOGGER.info(pResponse.getSize() + " elements was extracted with " + getSysPath());
    updateTaskRun(UpdateRunKind.WORK_PROGRESS, _countProgress);
    logInfo("PROGRESS", String.valueOf(_countProgress));
    //écrire la réponse dans le provider s'il existe
    if (pResponse.getValue() != null && !pResponse.getValue().equals("") && pResponse.getSize() > 0)
    {
      writeResponse(pResponse.getValue());
    }
  }

  /**
   * Traitement de la réponse en rejet
   * @param pResponse
   */
  private void rejectTreatment(RejectResponse pResponse)
  {
    LOGGER.info("error during extraction " + getSysPath());
    _errorLevel = 1;
    EUExecutionException euError;
    if (pResponse.getCause() instanceof EUExecutionException)
    {
      euError = (EUExecutionException)pResponse.getCause();
    }
    else
    {
      euError = new TaskException(BatchErrorDetail.TASK_EXTRACTION_ERR, new Object[] { getSysPath(), getSysId() }, pResponse.getCause());
    }
    traceError(euError); // ajout de l'erreur dans TASK_AUDIT
  }

  /**
   * Extraction par paquet
   * @throws Exception exception
   */
  private void fetchByRange(String pInputParams) throws Exception
  {
    //TODO à revoir après évolution du URTransactionManager
    LOGGER.debug("récupération par paquet");
    // préparer un numéro de transaction
    // envoyer le rangesize et le numéro de transaction au process par propriété ENG
    URTransactionManager transactionManager = new URTransactionManagerImpl();
    ContextType ctx = new ContextType();
    transactionManager.initContextTransaction(ctx);
    // préparation message transaction et rangesize
    getPropManager().getSYSrepo().setRangeSize(_maxFetchSize);
    getPropManager().getSYSrepo().setContext(ctx);
    if (_singleTransaction)
    {
      getPropManager().getSYSrepo().setSingleTransaction(true);
    }
    try
    {
      int nbExtract = 0;
      while (fetchData(pInputParams))
      {
        nbExtract++;
        _avgDataProcess = ((Calendar.getInstance().getTimeInMillis() - this.getSysStartTime().getTimeInMillis()) / nbExtract) + 1;
      }
    }
    finally
    {
      // une fois terminé, libérer la transaction et retirer le rangesize et la transaction du sysobject
      transactionManager.release(ctx);
      getPropManager().getSYSrepo().removeRangeSize();
      getPropManager().getSYSrepo().removeContext();
      if (_singleTransaction)
      {
        getPropManager().getSYSrepo().removeSingleTransaction();
      }
    }
  }

  /**
   * Extraction de données
   * @return true s'il faut continuer l'extraction, false sinon
   * @throws TaskException exception
   * @throws ProcessException exception
   * @throws ProviderException exception
   */
  private boolean fetchData(String pInputParams) throws TaskException, ProcessException, ProviderException
  {
    LOGGER.debug("récupération");
    // réinitialisation des variables locales
    getProcessEngine().reloadLocalVariable();
    getProcessEngine().addLocalVariableString("input", pInputParams);
    if (_inputProviderReader != null)
    {
      getProcessEngine().addLocalVariableString("scenario", _inputProviderReader.getScenarioValue());
      getProcessEngine().addLocalVariableString("inputContext", _inputProviderReader.getContextValue());
    }
    updateTaskRun(UpdateRunKind.STEP, "processor");
    logInfo("STEP", "processor");
    // exécution du processor
    ProcessorResponse response = getProcessEngine().execute();
    responseTreatment(response);
    if (!isNotTimeOut())
    {
      _cancelAsked = true;
    }
    if (_cancelAsked)
    {
      // l'annulation de la tache est demandée.
      LOGGER.debug("L'extraction est interrompue !");
      return false;
    }
    // l'extraction peut continuer
    return _continueExtraction;
  }

  /**
   * Initialise le processEngine pour l'extraction
   * @throws ProcessException exception
   */
  private void initProcessEngineToExtraction() throws ProcessException
  {
    setProcessEngine(new ProcessEngineImpl());
    getProcessEngine().setProcessEngineConfiguration(getProcessRef(), getVarENGrepo(), getPropManager().getPropENGrepo(), getPropManager().getSYSrepo(), getDomainForProcess(), getAlternativeDomain());
  }

  /**
   * Lance l'execution de la tâche
   * @throws TaskException exception
   */
  protected void execute() throws TaskException
  {
    LOGGER.debug("-------------> Execution de la task " + getSysPath() + " dans le domaine " + getDomain());
    logInfo("START", "");
    LOGGER.debug("Execution de la Tache d'Extraction : " + getSysPath() + " id : " + getSysId());
    String xmlParams = null;
    try
    {
      setSysLastStartTime();
      initProcessEngineToExtraction();
      DataPackage dataParams = readInput(null);
      if (dataParams != null)
      {
        xmlParams = dataParams.getValue();
      }
      _responseProviderWriter = _providerFactory.generateProviderWriter("response", _taskDef.getResponse(), _currentRunDef);
      setTimeZone();
      ProviderFileType providerFile = _responseProviderWriter != null ? _responseProviderWriter.getProviderFileType() : null;
      if (providerFile != null && providerFile.getHeader() != null)
      {
        writeResponse(providerFile.getHeader());
      }
      if (_maxFetchSize > 0)
      {
        // extraction par paquet
        fetchByRange(xmlParams);
      }
      else
      {
        // extraction complète
        fetchData(xmlParams);
      }
      if (providerFile != null && providerFile.getFooter() != null)
      {
        writeResponse(providerFile.getFooter());
      }
      if (!_data && providerFile != null && providerFile.getDefaultContent() != null)
      {
        _responseProviderWriter.getProvider().getTransformedOutputStream();
        _responseProviderWriter.getProvider().closeOutputStream();
      }
    }
    catch (Throwable e)
    {
      // une exception s'est produite hors traitement -> erreur technique, interruption du traitement
      notifyTechnicalProblem(e);
    }
    finally
    {
      finalUpdateTaskRun();
    }
  }

  private void setTimeZone()
  {
    if (_responseProviderWriter != null)
    {
      String timeZone = _responseProviderWriter.getProvider().getProviderDef().getTimeZone();
      if (timeZone != null)
      {
        _propManager.getSYSrepo().setTimeZone(timeZone);
      }
    }
  }

  private String getCode(String path)
  {
    return path.length() < 513 ? path : String.valueOf(path.hashCode());
  }

  /**
   * {@inheritDoc}
   */
  protected void finalUpdateTaskRun()
  {
    super.finalUpdateTaskRun();
    logInfo("END", String.valueOf(_errorLevel));
    if (_errorLevel == 0)
    {
      if (!Boolean.TRUE.equals(((EXTRACTION)_taskDef).isSkipSetSysLastStartTime()))
      {
        // aucun problème
        VariedMovementType varMvt = new VariedMovementType();
        varMvt.setCode(getCode(_currentRunDef.getPath() + "_" + getAlternativeDomain() + "_" + getDomain()));
        varMvt.setMovementCode(SYS_LAST_START_TIME_KEY_NAME);
        varMvt.setDateTimeMvt(getSysStartTime());
        try
        {
          ContextType ctx = ContextTransformer.fromLocale();
          _varMvtServ.postVariedMovement(varMvt, null, ctx);
          varMvt.setMovementCode(SYS_LAST_START_TIME_DB_KEY_NAME);
          varMvt.setDateTimeMvt((Calendar)_propManager.getSysObject(AbstractTask.SYS_START_TIME_DB_KEY_NAME, null));
          _varMvtServ.postVariedMovement(varMvt, null, ctx);
        }
        catch (Exception e)
        {
          LOGGER.error("Erreur lors de la mise à jour de la propriété système " + SYS_LAST_START_TIME_KEY_NAME, e);
        }
      }
    }
  }

  /**
   * Initialise les date et heure de début de la dernière extraction correcte de la tâche dans le SYSRepo
   * @throws Exception
   */
  private void setSysLastStartTime() throws Exception
  {
    if (!Boolean.TRUE.equals(((EXTRACTION)_taskDef).isSkipSetSysLastStartTime()))
    {
      ContextType ctx = ContextTransformer.fromLocale();
      VariedMovementKeyType varMvtKey = new VariedMovementType();
      varMvtKey.setCode(getCode(_currentRunDef.getPath() + "_" + getAlternativeDomain() + "_" + getDomain()));
      varMvtKey.setMovementCode(SYS_LAST_START_TIME_KEY_NAME);
      VariedMovementType varMvt = _varMvtServ.getVariedMovement(varMvtKey, null, ctx);
      if (varMvt != null && varMvt.getDateTimeMvt() != null)
      {
        _propManager.putSysObject(SYS_LAST_START_TIME_KEY_NAME, varMvt.getDateTimeMvt());
        _propManager.putSysObject(SYS_LAST_START_TIME_NULL_IF_EMPTY_KEY_NAME, varMvt.getDateTimeMvt());
      }
      else
      {
        _propManager.putSysObject(SYS_LAST_START_TIME_KEY_NAME, getSysStartTime());
      }
      varMvtKey.setMovementCode(SYS_LAST_START_TIME_DB_KEY_NAME);
      varMvt = _varMvtServ.getVariedMovement(varMvtKey, null, ctx);
      _propManager.putSysObject(SYS_LAST_START_TIME_DB_KEY_NAME, varMvt != null && varMvt.getDateTimeMvt() != null ? varMvt.getDateTimeMvt() : getCurrentDBTime());
    }
  }

  protected void generateFlag()
  {
    try
    {
      if (_responseProviderWriter == null)
      {
        _responseProviderWriter = _providerFactory.generateProviderWriter("response", _taskDef.getResponse(), _currentRunDef);
      }
      if (_responseProviderWriter != null)
      {
        ProviderFileType providerFile = _responseProviderWriter.getProviderFileType();
        if (providerFile != null && providerFile.getFlagName() != null && providerFile.getDir() != null)
        {
          String fileName = _initFileName == null ? providerFile.getFileName() : _initFileName;
          String flagName = ((FILEPROVIDER)_responseProviderWriter.getProvider().getProviderDef()).getFile().getFlagName();
          flagName = flagName.replaceFirst("\\$\\{SYSTEM_outputFileName\\}", fileName);
          flagName = flagName.replaceFirst("\\$\\{SYSTEM_outputFileNameNoExt\\}", BatchUtil.getFileNameNoExt(fileName));
          String dir = new FilenameUtil().addRelativePath(_responseProviderWriter.getProviderFileType().getDir());
          BatchReportingEngineServiceImpl serv = new BatchReportingEngineServiceImpl();
          TaskRunDetailsType taskDetail = serv.getTaskRunDetails(_currentRunDef, TASKRUN_DETAILS_SCENARIO, ContextTransformer.fromLocale());
          new JAXBManager().write(taskDetail, dir + "/" + flagName);
        }
        _responseProviderWriter.releaseProvider();
      }
    }
    catch (Exception e)
    {
      new TaskException(BatchErrorDetail.TASKRUN_AFTER_ERR, new Object[] { getSysPath(), getSysId() }, e).log();
    }
  }
}
