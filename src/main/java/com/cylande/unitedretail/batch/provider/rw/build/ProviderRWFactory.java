package com.cylande.unitedretail.batch.provider.rw.build;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.pool.RemoteProviderPoolClient;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.batch.task.AbstractTask;
import com.cylande.unitedretail.message.batch.AbstractStream;
import com.cylande.unitedretail.message.batch.TaskProviderType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

public class ProviderRWFactory
{
  /** logger technique */
  private static final Logger LOGGER = Logger.getLogger(ProviderRWFactory.class);
  /** tache parente */
  private PropertiesManager _parentPropManager;
  private String _dynaProvidersLabel = null;
  private RemoteProviderPoolClient _remoteClient = null;
  private boolean _remoteModeActivated = false;
  private String _remoteURLStr = null;
  private AbstractTask _parentTask;
  private int _preLoadSize = 1;

  public ProviderRWFactory(AbstractTask pParentTask)
  {
    _parentTask = pParentTask;
    _parentPropManager = pParentTask.getPropManager();
  }

  /**
   * generateLocalProviderReader
   * @param pAbstractStream description de l'abstract stream
   * @param pBufferSize taille du buffer
   * @return résultat
   */
  private ProviderReader generateLocalProviderReader(AbstractStream pAbstractStream, String pProviderName, TaskRunType pTaskRun, int pBufferSize) throws ProviderException
  {
    ProviderReader result = null;
    TaskProviderType provider = pAbstractStream.getProvider();
    LOGGER.debug("Generating provider " + provider.getRef() + " in read mode");
    try
    {
      ProviderRWBuilder providerBuilder = new ProviderReaderBuilder(pBufferSize);
      providerBuilder.setSystemRepository(_parentPropManager.getSYSrepo());
      providerBuilder.setPropertiesRepository(_parentPropManager.getPropENGrepo());
      providerBuilder.setCurrentDomain(_parentTask.getDomain());
      providerBuilder.setCurrentDomain(provider.getActiveDomain());
      providerBuilder.setAlternativeDomain(_parentTask.getAlternativeDomain());
      providerBuilder.setAlternativeDomain(provider.getDefaultDomain());
      providerBuilder.setName(_dynaProvidersLabel);
      ProviderRWLoader providerLoader = new ProviderRWLoader(providerBuilder);
      providerLoader.loadProviderDef(provider.getRef());
      result = (ProviderReader)providerBuilder.buildProvider(pProviderName, pTaskRun);
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.INIT_PROVIDER_READER_ERR, new Object[] { provider.getRef() }, e);
    }
    return result;
  }

  /**
   * generateLocalProviderWriter
   * @param pAbstractStream description de l'abstract stream
   * @return résultat
   */
  private ProviderWriter generateLocalProviderWriter(AbstractStream pAbstractStream, String pProviderName, TaskRunType pTaskRun) throws ProviderException
  {
    ProviderWriter result = null;
    TaskProviderType provider = pAbstractStream.getProvider();
    LOGGER.debug("Generating provider " + provider.getRef() + " in write mode");
    try
    {
      ProviderRWBuilder providerBuilder = new ProviderWriterBuilder();
      providerBuilder.setSystemRepository(_parentPropManager.getSYSrepo());
      providerBuilder.setPropertiesRepository(_parentPropManager.getPropENGrepo());
      providerBuilder.setCurrentDomain(_parentTask.getDomain());
      providerBuilder.setCurrentDomain(provider.getActiveDomain());
      providerBuilder.setAlternativeDomain(_parentTask.getAlternativeDomain());
      providerBuilder.setAlternativeDomain(provider.getDefaultDomain());
      providerBuilder.setName(_dynaProvidersLabel);
      ProviderRWLoader providerLoader = new ProviderRWLoader(providerBuilder);
      providerLoader.loadProviderDef(provider.getRef());
      result = (ProviderWriter)providerBuilder.buildProvider(pProviderName, pTaskRun);
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.INIT_PROVIDER_WRITER_ERR, new Object[] { provider.getRef() }, e);
    }
    return result;
  }

  /**
   * Construit un lecteur de flux.
   * @param pAbstractStream description du flux d'entrée.
   * @param pBufferSize taille du buffer.
   * @return le lecteur de flux.
   */
  public ProviderReader generateProviderReader(String pProviderName, AbstractStream pAbstractStream, TaskRunType pTaskRun, int pBufferSize) throws ProviderException
  {
    ProviderReader result = null;
    if ((pAbstractStream != null) && (pAbstractStream.getProvider() != null))
    {
      if (_remoteModeActivated)
      {
        result = _remoteClient.initReader(pProviderName, pAbstractStream, pTaskRun, pBufferSize);
      }
      else
      {
        result = generateLocalProviderReader(pAbstractStream, pProviderName, pTaskRun, pBufferSize);
      }
    }
    return result;
  }

  /**
   * Construit un ecrivain de flux.
   * @param pAbstractStream description du flux de sortie.
   * @return le writer de provider ou null si flux non défini.
   */
  public ProviderWriter generateProviderWriter(String pProviderName, AbstractStream pAbstractStream, TaskRunType pTaskRun) throws ProviderException
  {
    ProviderWriter result = null;
    if (pAbstractStream != null && pAbstractStream.getProvider() != null && !Boolean.TRUE.equals(pAbstractStream.isDisabled()))
    {
      if (_remoteModeActivated)
      {
        result = _remoteClient.initWriter(pProviderName, pAbstractStream);
      }
      else
      {
        result = generateLocalProviderWriter(pAbstractStream, pProviderName, pTaskRun);
      }
    }
    return result;
  }

  /**
   * Retourne le provider de rejet correspondant à la task en cours.
   * @param pTaskRun Task
   * @return ProviderWriter
   * @throws ProviderException Erreur de provider
   */
  public ProviderWriter generateRejectProviderWriter(TaskRunType pTaskRun) throws ProviderException
  {
    ProviderWriter result = null;
    if ((_parentTask != null) && (_parentTask.getTaskDef() != null) && (_parentTask.getTaskDef().getReject() != null))
    {
      result = generateProviderWriter("reject", _parentTask.getTaskDef().getReject(), pTaskRun);
    }
    return result;
  }

  private void initRemoteFactory(String pProvidersPoolUrl, String pProviderSessionId) throws ProviderException
  {
    _remoteClient = new RemoteProviderPoolClient(pProvidersPoolUrl);
    _remoteClient.setSessionCookieValue(pProviderSessionId);
    _remoteClient.initFactory(_parentTask, _preLoadSize);
  }

  private void releaseRemoteFactory()
  {
  }

  public void activateRemoteMode(String pProvidersPoolUrl, String pProviderSessionId) throws ProviderException
  {
    if (!_remoteModeActivated)
    {
      initRemoteFactory(pProvidersPoolUrl, pProviderSessionId);
    }
    _remoteModeActivated = true;
  }

  public void desactivateRemoteMode() throws ProviderException
  {
    if (_remoteModeActivated)
    {
      releaseRemoteFactory();
    }
    _remoteModeActivated = false;
  }

  public boolean isRemoteModeActivated()
  {
    return _remoteModeActivated;
  }

  public void setRemoteURLStr(String pRemoteURLStr)
  {
    this._remoteURLStr = pRemoteURLStr;
  }

  public String getRemoteURLStr()
  {
    return _remoteURLStr;
  }

  public void setDynaProvidersLabel(String pDynaProvidersLabel)
  {
    this._dynaProvidersLabel = pDynaProvidersLabel;
  }

  public String getDynaProvidersLabel()
  {
    return _dynaProvidersLabel;
  }

  public void setPreLoadSize(int preLoadSize)
  {
    this._preLoadSize = preLoadSize;
  }

  public int getPreLoadSize()
  {
    return _preLoadSize;
  }
}
