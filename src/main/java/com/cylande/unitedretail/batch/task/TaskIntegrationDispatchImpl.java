package com.cylande.unitedretail.batch.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.tools.FileInUse;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.INTPARAM;
import com.cylande.unitedretail.message.batch.PathFileProviderListType;
import com.cylande.unitedretail.message.batch.PathFileProviderType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.process.response.RejectResponse;

/**
 * Impl�mentation de la t�che d'int�gration
 */
public class TaskIntegrationDispatchImpl extends AbstractTask
{

  /** Num�ro de version pour la s�rialisation */
  private static final long serialVersionUID = 201002231415L;
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(TaskIntegrationDispatchImpl.class);
  public CountDownLatch _countDownLatch = null;
  /** Dernier paquet */
  protected AtomicBoolean _endOfStream = new AtomicBoolean(false);
  /** Compteur de lot */
  private AtomicInteger _countLot = new AtomicInteger(0);
  private volatile Object _lock = new Object();
  /** Num�ro du thread */
  private int _threadNumber = -1;

  /**
   * Constructeur d'une t�che d'int�gration
   * @param pParent le batch parent de la t�che
   * @param pBean la d�finition de la t�che
   * @throws TaskException exception
   */
  public TaskIntegrationDispatchImpl(AbstractBatch pParent, INTEGRATION pBean) throws TaskException
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
      }
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
    LOGGER.debug("Error for lot : " + _countLot.get());
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
   * D�clenche l'int�gration des donn�es contenue dans le provider de lecture
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  private void launchIntegration()
  {
    // Tout ce que fait le thread dispatcher c'est attendre que les thread pooled aient finit d'int�grer tout le fichier
    try
    {
      _countDownLatch.await();
    }
    catch (Exception e)
    {
      LOGGER.error("unable to wait latch");
    }
  }

  /**
   * Impl�mentation de l'ex�cution de la t�che. Cet "execute" doit juste attendre avec son provider de lecture que les threads fils finissent leur
   * int�gration
   * @throws TaskException TaskException
   */
  public void execute() throws TaskException
  {
    launchIntegration();
    finalUpdateTaskRun();
  }

  /**
   * Ecrit un message dans le provider d'erreur
   * @param pXmlReject
   * @throws TaskException exception
   * @throws ProviderException exception
   */
  public void writeReject(String pXmlReject, TaskRunType pTaskRun, EUExecutionException pException) throws ProviderException
  {
    synchronized (_lock)
    {
      try
      {
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
          _rejectProviderWriter.setException(pException);
          _rejectProviderWriter.write(pXmlReject);
          // le write reject met � jour le rejectProvider du _currentRunDef de ce dispatcher
          // il faut donc redescendre le fichier en cours du rejectProvider de ce dispatcher au thread enfant s'il n'est pas d�j� pr�sent
          addCurrentFile(_currentRunDef.getRejectProvider(), pTaskRun.getRejectProvider(), _rejectProviderWriter.getCurrentFileName());
        }
      }
      catch (ProviderException e)
      {
        LOGGER.error("unable to write reject on task " + getName() + " package=\"" + pXmlReject + "\"");
        throw e;
      }
    }
  }

  /**
   * setThreadNumber
   * @param pThreadNumber thread number
   */
  public void setThreadNumber(int pThreadNumber)
  {
    _threadNumber = pThreadNumber;
  }

  /**
   * getThreadNumber
   * @return thread number
   */
  public int getThreadNumber()
  {
    return _threadNumber;
  }

  /**
   * setThreadCount
   * @param pThreadCount thread count
   */
  public void setThreadCount(int pThreadCount)
  {
    _countDownLatch = new CountDownLatch(pThreadCount);
  }

  /**
   * activateRemoteProvidersMode
   * @param pProviderPoolUrl
   * @param pProviderSessionId
   * @throws ProviderException exception
   */
  public void activateRemoteProvidersMode(String pProviderPoolUrl, String pProviderSessionId) throws ProviderException
  {
    _providerFactory.activateRemoteMode(pProviderPoolUrl, pProviderSessionId);
  }

  /**
   * R�cup�re le prochain paquet d'�l�ments pour les thread fils (thread pooled)
   * @return DataPackage
   * @throws TaskException TaskException
   * @throws ProviderException ProviderException
   */
  public synchronized DataPackage getNextDataPackage(TaskRunType pTaskRun) throws TaskException, ProviderException
  {
    DataPackage dataPackage = null;
    // r�cup�ration d'un paquet aupr�s du provider de lecture
    dataPackage = readInput(pTaskRun.getId());
    // le readInput met � jour l'inputProvider du _currentRunDef de ce dispatcher
    // il faut donc redescendre le fichier en cours de l'inputProvider de ce dispatcher au thread enfant s'il n'est pas d�j� pr�sent
    if (dataPackage != null)
    {
      addCurrentFile(_currentRunDef.getInputProvider(), pTaskRun.getInputProvider(), _inputProviderReader.getCurrentFileName());
      //Permet de flagger le fichier comme en cours d'utilisation par une task
      FileInUse fileInUse = FileInUse.getFileInUse(dataPackage.getFileName());
      if (fileInUse != null)
      {
        fileInUse.increment();
      }
      // le readInput met � jour l'inputProvider du _currentRunDef de ce dispatcher
      _endOfStream.set(dataPackage.isLastPackage());
      // invocation du processEngine avec le paquet lu dans le provider d'entr�e
      // traitement de la r�ponse du process
      _countLot.set(dataPackage.getPackageNumber());
    }
    else
    {
      _endOfStream.set(true);
    }
    return dataPackage;
  }

  private void addCurrentFile(PathFileProviderListType pFileListDispatch, PathFileProviderListType pFileList, String pCurrentFileName)
  {
    if (!pFileListDispatch.getList().isEmpty())
    {
      boolean addFile = true;
      for (PathFileProviderType file: pFileList.getList())
      {
        if (file.getFileName().equals(pCurrentFileName))
        {
          addFile = false;
          break;
        }
      }
      if (addFile)
      {
        for (PathFileProviderType file: pFileListDispatch.getList())
        {
          if (file.getFileName().equals(pCurrentFileName))
          {
            pFileList.getList().add(file);
            break;
          }
        }
      }
    }
  }

  /**
   * Getter pour le provider d'entr�e.
   * @return ProviderReader
   */
  protected ProviderReader getInputProviderReader()
  {
    return _inputProviderReader;
  }

  protected void manageXMLSplit()
  {
  }
}
