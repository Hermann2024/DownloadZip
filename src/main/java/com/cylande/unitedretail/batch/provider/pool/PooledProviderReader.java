package com.cylande.unitedretail.batch.provider.pool;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.ProviderFileType;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *  Buffered Provider Reader
 *  this class is a provider reader that bufferize content
 *  this class allow multiple thread access
 */
public class PooledProviderReader extends ProviderReader
{

  /** the reader */
  private ProviderReader _providerReader;

  /** the queue to bufferize strings */
  private ConcurrentLinkedQueue<DataPackage> _queue;

  /** number of client that used this provider */
  private int _usageCount = 1;

  /** number of client that used this provider */
  private int _currentUsageCount = 1;

  /**
   * Constructor
   * @param pProviderReader : the reader
   */
  public PooledProviderReader(ProviderReader pProviderReader)
  {
    super(null);
    _providerReader = pProviderReader;
    _queue = new ConcurrentLinkedQueue<DataPackage>();
  }

  /** {@inheritDoc} */
  public DataPackage read(Boolean pNoProcessor, Integer pCurrentTaskId)
  {
    DataPackage result = _queue.poll();
    if (result == null)
    {
      // plus de donnée : on verifie la bonne alimentation de la file.
      loadBuffer(pCurrentTaskId);
      result = _queue.poll();
    }
    return result;
  }

  /**
   * inject an external data to this stream
   */
  public void putData(DataPackage pValue)
  {
    _queue.offer(pValue);
  }

  /**
   * @param pCurrentTaskId Identifiant de la task en cours
   * Load the next(s) String to be read
   */
  public void loadBuffer(Integer pCurrentTaskId)
  {
    if (_queue.size() < _usageCount)
    {
      synchronized (_providerReader)
      {
        boolean read = true;
        while (read)
        {
          try
          {
            DataPackage readValue = _providerReader.read(null, pCurrentTaskId);
            if (readValue != null)
            {
              _queue.offer(readValue);
              read = (_queue.size() < _usageCount);
            }
            else
            {
              read = false;
            }
          }
          catch (ProviderException e)
          {
            read = false;
          }
        }
      }
    }
  }

  /**
   * test if provider stream is empty
   * @return résultat
   */
  public boolean endOfStream()
  {
    return false;
  }

  /**
   * called by provider container.
   * increase the user number, so the pre-read queue
   */
  protected void notifyUsage()
  {
    _currentUsageCount++;
    if (_usageCount < _currentUsageCount)
    {
      _usageCount = _currentUsageCount;
    }
  }

  /**
   * test if all users had notify to release provider Reader
   * @return true if this reader is not used anymore
   */
  protected boolean isClosed()
  {
    return _currentUsageCount <= 0; // TK46254
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    _currentUsageCount--;
    if (_currentUsageCount == 0)
    {
      synchronized (_providerReader)
      {
        _providerReader.releaseProvider();
      }
    }
  }

  /**
   * Méthode pour forcer la cloture propre du provider
   * TK46254
   * @throws ProviderException exception
   */
  public void forceReleaseProvider() throws ProviderException
  {
    synchronized (_providerReader)
    {
      _providerReader.releaseProvider();
      _currentUsageCount = 0;
    }
  }

  /**
   * set the number of users.
   * @param pUsageCount : the number of users
   */
  protected void setUsageCount(int pUsageCount)
  {
    this._usageCount = pUsageCount;
  }

  /**
   * get the number of users
   * @return the number of users.
   */
  public int getUsageCount()
  {
    return _usageCount;
  }

  /** {@inheritDoc} */
  public String getCurrentFileName()
  {
    return null;
  }

  /** {@inheritDoc} */
  public String getScenarioValue()
  {
    return null;
  }

  /** {@inheritDoc} */
  public ProviderFileType getProviderFileType() throws Exception
  {
    ProviderFileType result = null;
    synchronized (_providerReader)
    {
      result = _providerReader.getProviderFileType();
    }
    return result;
  }

  /** {@inheritDoc} */
  public void setReject(FileType pFileType)
  {
    synchronized (_providerReader)
    {
      _providerReader.setReject(pFileType);
    }
  }

  /** {@inheritDoc} */
  public void disableArchive()
  {
  }

  @Override
  public String getContextValue()
  {
    return null;
  }
}
