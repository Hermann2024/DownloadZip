package com.cylande.unitedretail.batch.task;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.execution.UpdateRunKind;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.framework.compensation.transaction.URTransactionManagerImpl;
import com.cylande.unitedretail.framework.compensation.transaction.common.URTransactionManager;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.INTPARAM;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.exception.ProcessException;
import com.cylande.unitedretail.process.response.OutputResponse;
import com.cylande.unitedretail.process.response.ProcessorResponse;
import com.cylande.unitedretail.process.response.RejectResponse;
import com.cylande.unitedretail.process.service.ProcessEngine;
import com.cylande.unitedretail.process.service.ProcessEngineImpl;

/**
 * Impl�mentation de la tache d'int�gration
 */
public class TaskIntegrationImpl extends AbstractTask
{

  /** Numero de version pour la s�rialisation */
  private static final long serialVersionUID = 201002231415L;
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(TaskIntegrationImpl.class);
  /** Dernier paquet */
  protected boolean _endOfStream = false;
  /** compteur de lot */
  private int _countLot;
  /** Numero du thread */
  private int _threadNumber = -1;
  /** Numero du thread */
  private int _threadCount = 1;
  private boolean _singleTransaction = false;

  /**
   * Constructeur du tache d'int�gration
   * @param pParent le batch parent de la task
   * @param pBean la definition de la task
   * @throws TaskException exception
   */
  public TaskIntegrationImpl(AbstractBatch pParent, INTEGRATION pBean) throws TaskException
  {
    super(pParent, pBean);
    if (pBean != null)
    {
      if (pBean instanceof INTPARAM)
      {
        String dynaProviderLabel = ((INTPARAM)pBean).getLabel();
        if (dynaProviderLabel == null || dynaProviderLabel.equals(""))
        {
          dynaProviderLabel = getTaskName();
        }
        _providerFactory.setDynaProvidersLabel(dynaProviderLabel);
        _commitFrequency = 0;
      }
      else
      {
        // r�cup�ration de la valeur du commitFrequency
        if (pBean.getCommitFrequency() != null)
        {
          _commitFrequency = pBean.getCommitFrequency().intValue();
        }
        if (pBean.isSingleTransaction() != null)
        {
          _singleTransaction = pBean.isSingleTransaction();
        }
      }
    }
  }

  /**
   * Traite la r�ponse r�cup�r�e aupr�s du process, et �crit les donn�es dans les bons providers
   * Cette m�thode est sp�cifique pour chaque type de tache
   * @param pResponse
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  private void responseTreatment(ProcessorResponse pResponse) throws TaskException, ProviderException
  {
    if (pResponse == null)
    {
      return;
    }
    if (pResponse instanceof OutputResponse)
    {
      outputTreatment((OutputResponse)pResponse);
    }
    else if (pResponse instanceof RejectResponse)
    {
      rejectTreatment((RejectResponse)pResponse);
    }
  }

  /**
   * Traitement d'une r�ponse de type "r�ussite"
   * @param pResponse
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  private void outputTreatment(OutputResponse pResponse) throws TaskException, ProviderException
  {
    //mise � jour de l'�tat d'avancement
    _countProgress += pResponse.getSize();
    updateTaskRun(UpdateRunKind.STEP, "output");
    logInfo("STEP", "output");
    LOGGER.info(pResponse.getSize() + " elements was integrated with " + getSysPath());
    // enregistrement de la progression
    updateTaskRun(UpdateRunKind.WORK_PROGRESS, _countProgress);
    logInfo("PROGRESS", Integer.toString(_countProgress));
    //�criture de la r�ponse dans providerResponse
    if (pResponse.getValue() != null && !pResponse.getValue().equals("") && pResponse.getSize() > 0)
    {
      writeResponse(pResponse.getValue());
    }
  }

  /**
   * Traitement d'une r�ponse de type "erreur"
   * @param pResponse
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  protected void rejectTreatment(RejectResponse pResponse) throws TaskException, ProviderException
  {
    LOGGER.debug("Error for lot : " + _countLot);
    LOGGER.info(pResponse.getSize() + " elements was not integrated with " + getSysPath());
    _errorLevel = 1;
    // ajout de l'erreur en task_audit
    EUExecutionException euError;
    if (pResponse.getCause() instanceof EUExecutionException)
    {
      euError = (EUExecutionException)pResponse.getCause();
    }
    else
    {
      euError = new TaskException(BatchErrorDetail.TASK_INTEGRATION_ERR, new Object[] { getSysPath(), getSysId() }, pResponse.getCause());
    }
    setException(euError); // setException avant le writeReject pour ajout de la balise error dans le fichier
    // �criture de la r�ponse dans providerReject toujours avant TASK_AUDIT pour conserver une trace du rejet en cas de perte de connexion BDD
    if (pResponse.getValue() != null && !pResponse.getValue().equals("") && pResponse.getSize() > 0)
    {
      writeReject(pResponse.getValue());
    }
    traceError(euError); // ajout de l'erreur dans TASK_AUDIT
  }

  /**
   * Initialise le processEngine pour l'int�gration
   * @throws ProcessException exception
   */
  private void initProcessEngineToIntegration() throws ProcessException
  {
    setProcessEngine(new ProcessEngineImpl());
    if (!_noProcessor)
    {
      getProcessEngine().setProcessEngineConfiguration(getProcessRef(), getVarENGrepo(), getPropManager().getPropENGrepo(), getPropManager().getSYSrepo(), getDomainForProcess(), getAlternativeDomain());
    }
  }

  /**
   * invoque le processEngine avec le paquet pr�c�demment lu
   * @param pXmlStream
   * @return la reponse du process
   * @throws ProcessException exception
   */
  private ProcessorResponse invokeProcessEngine(String pXmlStream) throws ProcessException
  {
    ProcessEngine processor = getProcessEngine();
    ProcessorResponse result = null;
    // r�initialisation des variables locales
    processor.reloadLocalVariable();
    // d�claration du input dans la repoLOC de processEngine
    processor.addLocalVariableString("input", pXmlStream);
    if (_inputProviderReader != null)
    {
      getProcessEngine().addLocalVariableString("scenario", _inputProviderReader.getScenarioValue());
      getProcessEngine().addLocalVariableString("context", _inputProviderReader.getContextValue());
      getProcessEngine().addLocalVariableString("inputContext", _inputProviderReader.getContextValue());
    }
    // execution du process charg� dans processEngine
    result = processor.execute();
    return result;
  }

  /**
   * d�clenche l'integration des donn�es contenue dans le provider de lecture
   * @throws Exception exception
   */
  private void launchIntegration() throws Exception
  {
    LOGGER.debug("Integration par : " + _commitFrequency);
    String inputData;
    DataPackage dataPackage = null;
    while ((_inputProviderReader != null) && (!_endOfStream) && (!_cancelAsked))
    {
      // r�cup�ration d'un paquet aupr�s du provider de lecture
      try
      {
        dataPackage = readInput(null);
      }
      catch (Exception e)
      {
        // le fichier est mal form�
        if (_rejectProviderWriter == null)
        {
          _rejectProviderWriter = _providerFactory.generateProviderWriter("reject", _taskDef.getReject(), _currentRunDef);
        }
        if (_rejectProviderWriter != null)
        {
          // bloque l'archivage du fichier pour le placer dans le dossier de rejet
          _inputProviderReader.setReject(_rejectProviderWriter.getProviderFileType());
        }
        if (e instanceof ProviderException)
        {
          if (((ProviderException)e).getCanonicalCode().equalsIgnoreCase(BatchErrorDetail.STAXPARSER_INVALID_XML_FILE.getCanonicalCode()))
          {
            TaskAuditFactory.createTaskAudit((ProviderException)e, _propManager, getSysId(), _inputProviderReader.getProvider());
            _inputProviderReader.getProvider().nextTransformedBufferedReader(getSysId()); // permet de d�placer le fichier en rejet
          }
        }
        _inputProviderReader.read(true, getSysId()); // permet de forcer le passage au prochain fichier
        continue;
      }
      if (dataPackage != null)
      {
        inputData = dataPackage.getValue();
        _endOfStream = dataPackage.isLastPackage();
        // invocation du processEngine avec le paquet lu dans le provider d'entr�e
        // traitement de la r�ponse du process
        _countLot = dataPackage.getPackageNumber();
        if (!_noProcessor)
        {
          try
          {
            updateTaskRun(UpdateRunKind.STEP, "processor");
            logInfo("STEP", "processor");
            LOGGER.debug("Integration lot : " + _countLot);
            if (getException() == null || !isFailOnError())
            {
              responseTreatment(invokeProcessEngine(inputData));
            }
            else
            {
              writeReject(inputData);
            }
          }
          catch (Exception e)
          {
            writeReject(inputData);
            notifyTechnicalProblem(e);
          }
          if (_taskDef.getResponse() == null)
          {
            commitJMS(); // en l'absence de provider de sortie, commit JMS apr�s chaque message trait�
          }
        }
      }
      else
      {
        _endOfStream = true;
      }
      if (!isNotTimeOut())
      {
        _cancelAsked = true;
      }
    }
    if (!_endOfStream && _cancelAsked)
    {
      // en cas de timeOut ou de stop, le fichier en cours n'est pas archiv� afin de pouvoir le traiter ult�rieurement dans son int�gralit�
      _inputProviderReader.disableArchive();
    }
    LOGGER.debug("Totaly elements integrated with " + getSysPath() + " : " + _countProgress);
  }

  /**
   * Impl�mentation de l'ex�cution de la tache
   * @throws TaskException exception
   */
  public void execute() throws TaskException
  {
    LOGGER.debug("-------------> Execution de la task " + getSysPath() + " dans le domaine " + getDomain());
    logInfo("START", "");
    LOGGER.debug("Execution de la Tache d'Integration : " + getSysPath() + " id : " + getSysId());
    // INITIALISATION
    ContextType ctx = new ContextType();
    URTransactionManager transactionManager = null;
    try
    {
      // init providers, seulement au moment de l'ex�cution pour �viter la prise inutile de ressources
      updateTaskRun(UpdateRunKind.WORK_LOAD, -1);
      logInfo("LOAD", "-1");
      initProcessEngineToIntegration(); //processException
      // INTEGRATION
      if (_taskDef.getInput() == null)
      {
        // pas de reader -> rien � int�grer !
        logInfo("PROGRESS", "0");
      }
      else
      {
        if (_singleTransaction)
        {
          transactionManager = new URTransactionManagerImpl();
          transactionManager.initContextTransaction(ctx);
          getPropManager().getSYSrepo().setSingleTransaction(true);
          getPropManager().getSYSrepo().setContext(ctx);
        }
        // lancement de l'int�gration
        launchIntegration();
      }
    }
    catch (Throwable e)
    {
      // une exception s'est produite hors traitement -> erreur technique, interruption du traitement
      notifyTechnicalProblem(e);
    }
    finally
    {
      if (_singleTransaction)
      {
        if (transactionManager != null)
        {
          transactionManager.release(ctx);
        }
        getPropManager().getSYSrepo().removeSingleTransaction();
        getPropManager().getSYSrepo().removeContext();
      }
      finalUpdateTaskRun();
    }
  }

  /**
   * @param pThreadNumber
   */
  public void setThreadNumber(int pThreadNumber)
  {
    _threadNumber = pThreadNumber;
  }

  /**
   * @return r�sultat
   */
  public int getThreadNumber()
  {
    return _threadNumber;
  }

  /**
   * @param pThreadCount
   * @throws ProviderException exception
   */
  public void setThreadCount(int pThreadCount) throws ProviderException
  {
    _threadCount = pThreadCount;
    _providerFactory.setPreLoadSize(pThreadCount);
  }

  /**
   * @param pProviderPoolUrl
   * @param pProviderSessionId
   * @throws ProviderException exception
   */
  public void activateRemoteProvidersMode(String pProviderPoolUrl, String pProviderSessionId) throws ProviderException
  {
    _providerFactory.activateRemoteMode(pProviderPoolUrl, pProviderSessionId);
  }

  /**
   * @return r�sultat
   */
  public int getThreadCount()
  {
    return _threadCount;
  }
}
