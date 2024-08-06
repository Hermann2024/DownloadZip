package com.cylande.unitedretail.batch.provider.pool;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.message.batch.ProviderFileType;

public class PooledProviderWriter extends ProviderWriter
{
  private ProviderWriter _providerWriter;
  private int _usageCount = 0;

  public PooledProviderWriter(ProviderWriter pProviderWriter)
  {
    super(null);
    _providerWriter = pProviderWriter;
  }

  /** {@inheritDoc} */
  public void write(String pValue) throws ProviderException
  {
    _providerWriter.write(pValue);
  }

  public void notifyUsage()
  {
    _usageCount++;
  }

  public boolean isClosed()
  {
    return _usageCount <= 0; // TK46254
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    _usageCount--;
    if (_usageCount == 0)
    {
      synchronized (_providerWriter)
      {
        _providerWriter.releaseProvider();
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
    synchronized (_providerWriter)
    {
      _providerWriter.releaseProvider();
      _usageCount = 0;
    }
  }

  /** {@inheritDoc} */
  public ProviderFileType getProviderFileType()
  {
    ProviderFileType result = null;
    synchronized (_providerWriter)
    {
      result = _providerWriter.getProviderFileType();
    }
    return result;
  }
}
