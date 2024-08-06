package com.cylande.unitedretail.batch.provider.pool;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.batch.provider.rw.build.ProviderRWFactory;
import com.cylande.unitedretail.batch.task.AbstractTask;
import com.cylande.unitedretail.message.batch.AbstractStream;

/**
 * Conteneur des providers distribués.
 */
class ProviderContainer implements HttpSessionBindingListener, Serializable
{

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(ProviderContainer.class);

  /** concurrent map for factory pool*/
  private ProviderRWFactory _factory = null;

  /** concurrent map for reader pool*/
  private ConcurrentHashMap<String, PooledProviderReader> _readerMap = new ConcurrentHashMap<String, PooledProviderReader>();

  /** concurrent map for writer pool*/
  private ConcurrentHashMap<String, PooledProviderWriter> _writerMap = new ConcurrentHashMap<String, PooledProviderWriter>();

  protected void initFactory(RemoteProviderProtocoleManager pProtocoleManager, AbstractTask pParentTask, Integer pPreLoadSize)
  {
    LOGGER.debug("Init Factory - start " + pParentTask);
    if (_factory == null)
    {
      _factory = new ProviderRWFactory(pParentTask);
      if (pProtocoleManager != null)
      {
        pProtocoleManager.onFactoryInitialized();
      }
      _factory.setPreLoadSize(pPreLoadSize);
    }
    else
    {
      if (pProtocoleManager != null)
      {
        pProtocoleManager.onFactoryInitialized();
      }
    }
    LOGGER.debug("Init Factory - end " + pParentTask);
  }

  /**
   * initReader
   * @param pReaderName
   * @param pAbstractStream
   * @param pPackageSize
   * @param pTaskId Identifiant de la task en cours
   * @throws ProviderException exception
   */
  protected void initReader(RemoteProviderProtocoleManager pListener, String pReaderName, AbstractStream pAbstractStream, Integer pPackageSize, Integer pTaskId) throws ProviderException
  {
    LOGGER.debug("Init Reader - start " + pAbstractStream);
    PooledProviderReader storedReader = _readerMap.get(pReaderName);
    if (storedReader == null)
    {
      ProviderReader pr = _factory.generateProviderReader(pReaderName, pAbstractStream, null, pPackageSize);
      if (pr != null)
      {
        PooledProviderReader newReader = new PooledProviderReader(pr);
        ProviderWriter reject = _factory.generateRejectProviderWriter(null);
        if (reject != null)
        {
          try
          {
            newReader.setReject(reject.getProviderFileType());
          }
          catch (Exception e)
          {
            LOGGER.warn(e);
          }
        }
        storedReader = _readerMap.putIfAbsent(pReaderName, newReader);
        if (storedReader == null)
        {
          storedReader = newReader;
          storedReader.setUsageCount(_factory.getPreLoadSize());
        }
        else
        {
          storedReader.notifyUsage();
        }
        try
        {
          storedReader.loadBuffer(pTaskId);
        }
        finally
        {
          if (pListener != null)
          {
            // Méthode onReaderInitialized appelée à la fin du initReader pour
            // s'assurer que le traitement d'init est terminé avant de redonner la
            // main côté client (RemoteProviderPoolClient). Si la méthode
            // onReaderInitialized est appelée avant la fin, le traitement reprend
            // côté client et génère alors parfois un RowInconsistentException
            // lors de la mise à jour de TaskRun.
            pListener.onReaderInitialized(pReaderName);
          }
        }
      }
    }
    else
    {
      if (pListener != null)
      {
        pListener.onReaderInitialized(pReaderName);
      }
    }
    LOGGER.debug("Init Reader - end " + pAbstractStream);
  }

  /**
   * read
   * @param pReaderName
   * @param pTaskId Identifiant de la task en cours
   * @return résultat
   */
  protected void read(RemoteProviderProtocoleManager pListener, String pReaderName, Integer pTaskId)
  {
    LOGGER.debug("read - start " + pReaderName);
    if (pListener != null)
    {
      PooledProviderReader reader = _readerMap.get(pReaderName);
      DataPackage data = null;
      if (reader != null)
      {
        data = reader.read(null, pTaskId);
        // plus de donnée à transmettre, on transmet un paquet vide;
        if (data == null)
        {
          data = new DataPackage();
          data.setLastPackage(true);
          pListener.onDataRead(pReaderName, data);
        }
        else if (pListener.onDataRead(pReaderName, data))
        {
          // donnée probablement transmise -> chargement du buffer
          reader.loadBuffer(pTaskId);
        }
        else
        {
          // donnée non transmise on la redonne au lecteur;
          reader.putData(data);
        }
      }
    }
    LOGGER.debug("read - end " + pReaderName);
  }

  protected void write(RemoteProviderProtocoleManager pListener, String pWriterName, DataPackage pPackageToWrite) throws ProviderException
  {
    LOGGER.debug("write - start " + pWriterName);
    ProviderWriter writer = _writerMap.get(pWriterName);
    if (writer != null)
    {
      synchronized (writer)
      {
        writer.write(pPackageToWrite.getValue());
      }
    }
    if (pListener != null)
    {
      pListener.onDataWrote(pWriterName);
    }
    LOGGER.debug("write - end " + pWriterName);
  }

  protected void initWriter(RemoteProviderProtocoleManager pListener, String pWriterName, AbstractStream pAbstractStream) throws ProviderException
  {
    LOGGER.debug("init writer - start " + pWriterName);
    PooledProviderWriter storedWriter = _writerMap.get(pWriterName);
    if (storedWriter == null)
    {
      ProviderWriter pw = _factory.generateProviderWriter(pWriterName, pAbstractStream, null);
      if (pw != null)
      {
        PooledProviderWriter newWriter = new PooledProviderWriter(pw);
        storedWriter = _writerMap.putIfAbsent(pWriterName, newWriter);
      }
    }
    if (storedWriter != null)
    {
      storedWriter.notifyUsage();
    }
    if (pListener != null)
    {
      pListener.onWriterInitialized(pWriterName);
    }
    LOGGER.debug("init writer - end " + pWriterName);
  }

  protected void releaseReader(RemoteProviderProtocoleManager pManager, String pReaderName, boolean pCloseResponseStream) throws ProviderException
  {
    LOGGER.debug("release reader - start " + pReaderName);
    PooledProviderReader reader = _readerMap.get(pReaderName);
    if (reader != null)
    {
      //reader.releaseProvider();
      reader.forceReleaseProvider(); // TK46254
      if (reader.isClosed())
      {
        _readerMap.remove(pReaderName);
      }
    }
    if (pManager != null)
    {
      pManager.onReaderReleased(pReaderName, pCloseResponseStream);
    }
    LOGGER.debug("release reader - end " + pReaderName);
  }

  protected void releaseWriter(RemoteProviderProtocoleManager pManager, String pWriterName, boolean pCloseResponseStream) throws ProviderException
  {
    LOGGER.debug("release writer - start " + pWriterName);
    PooledProviderWriter writer = _writerMap.get(pWriterName);
    if (writer != null)
    {
      //writer.releaseProvider();
      writer.forceReleaseProvider(); // TK46254
      if (writer.isClosed())
      {
        _readerMap.remove(pWriterName);
      }
    }
    if (pManager != null)
    {
      pManager.onWriterReleased(pWriterName, pCloseResponseStream);
    }
    LOGGER.debug("release writer - end " + pWriterName);
  }

  /**
   * Ferme tous les lecteurs
   * @param pManager : le listener des évenements de fermeture
   */
  private void releaseAllReaders(RemoteProviderProtocoleManager pManager)
  {
    LOGGER.debug("release all readers - start ");
    if (!_readerMap.isEmpty())
    {
      Enumeration<String> readerEnum = _readerMap.keys();
      boolean bHasNext = readerEnum.hasMoreElements();
      String readerName;
      while (bHasNext)
      {
        try
        {
          readerName = readerEnum.nextElement();
          bHasNext = readerEnum.hasMoreElements();
          releaseReader(pManager, readerName, !bHasNext);
        }
        catch (ProviderException e)
        {
          LOGGER.warn(e);
        }
      }
    }
    LOGGER.debug("release all readers - end ");
  }

  /**
   * Ferme tous les écrivains
   * @param pListener : le listener des évenements de fermeture
   */
  private void releaseAllWriters(RemoteProviderProtocoleManager pManager)
  {
    LOGGER.debug("release all writers - start ");
    if (!_writerMap.isEmpty())
    {
      Enumeration<String> writerEnum = _writerMap.keys();
      boolean bHasNext = writerEnum.hasMoreElements();
      String writerName;
      while (bHasNext)
      {
        try
        {
          writerName = writerEnum.nextElement();
          bHasNext = writerEnum.hasMoreElements();
          releaseWriter(pManager, writerName, !bHasNext);
        }
        catch (ProviderException e)
        {
          LOGGER.warn(e);
        }
      }
    }
    LOGGER.debug("release all writers - end ");
  }

  protected void releaseFactory(RemoteProviderProtocoleManager pManager)
  {
    LOGGER.debug("release factory - start ");
    releaseAllReaders(pManager);
    releaseAllWriters(pManager);
    _factory = null;
    if (pManager != null)
    {
      pManager.onFactoryReleased();
    }
    LOGGER.debug("release factory - end ");
  }

  /**
   * @implements HttpSessionBindingListener
   */
  public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent)
  {
  }

  /**
   * @implements HttpSessionBindingListener
   */
  public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent)
  {
    releaseFactory(null);
  }
}
