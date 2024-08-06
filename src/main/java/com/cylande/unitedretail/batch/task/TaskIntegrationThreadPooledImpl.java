package com.cylande.unitedretail.batch.task;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.execution.UpdateRunKind;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.tools.FileInUse;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.INTPARAM;
import com.cylande.unitedretail.process.exception.ProcessException;
import com.cylande.unitedretail.process.response.OutputResponse;
import com.cylande.unitedretail.process.response.ProcessorResponse;
import com.cylande.unitedretail.process.response.RejectResponse;
import com.cylande.unitedretail.process.service.ProcessEngine;
import com.cylande.unitedretail.process.service.ProcessEngineImpl;

import org.apache.log4j.Logger;

/**
 * Impl�mentation de la tache d'int�gration
 */
public class TaskIntegrationThreadPooledImpl extends AbstractTask
{

  /** Numero de version pour la s�rialisation */
  private static final long serialVersionUID = 201002231415L;

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(TaskIntegrationThreadPooledImpl.class);

  /** Dernier paquet */
  protected boolean _endOfStream = false;

  /** Parent */
  protected AbstractBatch _parent = null;

  /** fichier en cours d'utilisation*/
  protected String _currentFile;

  /** compteur de lot */
  private int _countLot;

  /** compteur d'�l�ments trait�s */
  private int _countProgress = 0;

  /** Numero du thread */
  private int _threadNumber = -1;

  /** Numero du thread */
  private int _threadCount = 1;

  /**
   * Constructeur d'une tache d'int�gration
   * @param pParent le batch parent de la task
   * @param pBean la definition de la task
   * @throws TaskException exception
   */
  public TaskIntegrationThreadPooledImpl(AbstractBatch pParent, INTEGRATION pBean) throws TaskException
  {
    super(pParent, pBean);
    _parent = pParent;
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
      }
    }
  }

  /**
   * Pas de reader pour les task ThreadPooled
   * la lecture est d�l�gu�e a la task parent de type Dispatch
   */
  protected void initInputProviderReader() throws ProviderException
  {
  }

  /**
   * Traite la r�ponse r�cup�r�e aupr�s du process, et �crit les donn�es dans les bons providers. Cette m�thode est sp�cifique pour chaque type de
   * tache
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
    // mise � jour de l'�tat d'avancement
    _countProgress += pResponse.getSize();
    updateTaskRun(UpdateRunKind.STEP, "output");
    logInfo("STEP", "output");
    LOGGER.info(pResponse.getSize() + " elements was integrated with " + getSysPath());
    // enregistrement de la progression
    updateTaskRun(UpdateRunKind.WORK_PROGRESS, _countProgress);
    logInfo("PROGRESS", Integer.toString(_countProgress));
    // �criture de la r�ponse dans providerResponse
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
      _parent.getDispatcher().writeReject(pResponse.getValue(), _currentRunDef, getException());
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
    getProcessEngine().setProcessEngineConfiguration(getProcessRef(), getVarENGrepo(), getPropManager().getPropENGrepo(), getPropManager().getSYSrepo(), getDomainForProcess(), _parent.getDispatcher().getAlternativeDomain());
  }

  /**
   * invoque le processEngine avec le paquet pr�c�demment lu
   * @param pXmlStream
   * @return la reponse du process exception
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
    }
    // execution du process charg� dans processEngine
    result = processor.execute();
    return result;
  }

  /**
   * Lecture de donn�es dans le provider de lecture.
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return DataPackage
   * @throws TaskException Erreur de task
   * @throws ProviderException Erreur de provider
   */
  protected DataPackage readInput(Integer pCurrentTaskId) throws TaskException, ProviderException
  {
    releaseFileInUse();
    updateTaskRun(UpdateRunKind.STEP, "input");
    logInfo("STEP", "input");
    DataPackage dataPackage = _parent.getDispatcher().getNextDataPackage(_currentRunDef);
    if (dataPackage != null)
    {
      _currentFile = dataPackage.getFileName();
    }
    else
    {
      _currentFile = null;
    }
    return dataPackage;
  }

  /**
   * Permet de liberer un pointeur sur l'utilisation du fichier en cours
   */
  private void releaseFileInUse()
  {
    if (_currentFile != null)
    {
      FileInUse fileInUse = FileInUse.getFileInUse(_currentFile);
      if (fileInUse != null)
      {
        fileInUse.decrement();
      }
    }
  }

  /**
   * d�clenche l'integration des donn�es contenue dans le provider de lecture
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  private void launchIntegration() throws TaskException, ProviderException
  {
    LOGGER.debug("Integration par : " + _commitFrequency);
    String inputData = null;
    DataPackage dataPackage = null;
    dataPackage = readInput(null);
    while (dataPackage != null && (!_cancelAsked)) // Tant qu'on a quelque chose � lire
    {
      // r�cup�ration d'un paquet aupr�s du provider de lecture (dans le dispatcher)
      try
      {
        inputData = dataPackage.getValue();
        _endOfStream = true;
        // invocation du processEngine avec le paquet lu dans le provider d'entr�e
        // traitement de la r�ponse du process
        _countLot = dataPackage.getPackageNumber();
        updateTaskRun(UpdateRunKind.STEP, "processor");
        logInfo("STEP", "processor");
        LOGGER.debug("Integration lot : " + _countLot);
        if (getException() == null || !isFailOnError())
        {
          responseTreatment(invokeProcessEngine(inputData));
        }
        else
        {
          _parent.getDispatcher().writeReject(inputData, _currentRunDef, getException());
        }
        dataPackage = readInput(null);
      }
      catch (Exception e)
      {
        releaseFileInUse();
        _parent.getDispatcher().writeReject(inputData, _currentRunDef, getException());
        notifyTechnicalProblem(e);
        // S'il s'agit d'une exception de cl�ture de flux (readInput), on
        // remet � null dataPackage pour ne pas traiter la m�me trame
        // ind�finiment.
        if (isCloseInputStreamError(e, 0))
        {
          dataPackage = null;
        }
      }
      if (!isNotTimeOut())
      {
        _cancelAsked = true;
      }
    } // END WHILE //EOS
    if (!_parent.getDispatcher()._endOfStream.get() && _cancelAsked)
    {
      // en cas de timeOut ou de stop, le fichier en cours n'est pas archiv� afin de pouvoir le traiter ult�rieurement dans son int�gralit�
      _parent.getDispatcher()._inputProviderReader.disableArchive();
    }
    LOGGER.debug("Totaly elements integrated with " + getSysPath() + " : " + _countProgress);
  }

  /**
   * V�rifie s'il s'agit d'une erreur de fermeture de flux.
   * @param pException Exception
   * @param pDepth Profondeur de recherche
   * @return boolean
   */
  private boolean isCloseInputStreamError(Exception pException, int pDepth)
  {
    boolean result = false;
    if (pException != null)
    {
      String errorCode = BatchErrorDetail.FILEPROVIDERMANAGER_CLOSECURRENTINPUTSTREAM_ERROR.getCanonicalCode();
      ProviderException providerException = null;
      if (pException instanceof ProviderException)
      {
        providerException = (ProviderException)pException;
      }
      if ((providerException != null) && (providerException.returnErrorDetail() != null) && (errorCode.equalsIgnoreCase(providerException.returnErrorDetail().getCanonicalCode())))
      {
        result = true;
      }
      // La m�thode �tant r�cursive, on limite la profondeur de recherche afin
      // d'�tre s�r de ne pas boucler ind�finiment en cas d'anomalie.
      // La valeur 10 est totalement arbitraire mais ne devrait, dans un cas
      // normal, pas �tre d�pass�e.
      else if ((pDepth < 10) && (pException.getCause() != null) && (pException.getCause() instanceof Exception) && (pException != pException.getCause()))
      {
        result = isCloseInputStreamError((Exception)pException.getCause(), pDepth + 1);
      }
    }
    return result;
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
    try
    {
      // init providers, seulement au moment de l'ex�cution pour �viter la prise inutile de ressources
      updateTaskRun(UpdateRunKind.WORK_LOAD, -1);
      logInfo("LOAD", "-1");
      initProcessEngineToIntegration(); // processException
      // INTEGRATION
      // lancement de l'int�gration
      launchIntegration();
    }
    catch (Throwable e)
    {
      // une exception s'est produite hors traitement -> erreur technique, interruption du traitement
      notifyTechnicalProblem(e);
    }
    finally
    {
      finalUpdateTaskRun();
      _parent.getDispatcher()._countDownLatch.countDown();
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

  protected void manageXMLSplit()
  {
  }
}
