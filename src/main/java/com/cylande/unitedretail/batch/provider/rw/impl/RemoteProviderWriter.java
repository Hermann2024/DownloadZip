package com.cylande.unitedretail.batch.provider.rw.impl;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.pool.RemoteProviderPoolClient;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.message.batch.ProviderFileType;

public class RemoteProviderWriter extends ProviderWriter
{
  private RemoteProviderPoolClient _owner;
  private String _name;

  /**
   * Remote provider
   */
  public RemoteProviderWriter(RemoteProviderPoolClient pOwner, String pProviderReaderName)
  {
    super(null);
    _owner = pOwner;
    _name = pProviderReaderName;
  }

  /** {@inheritDoc} */
  public void write(String pValue) throws ProviderException
  {
    _owner.write(_name, pValue);
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    _owner.releaseWriter(_name);
  }

  /** {@inheritDoc} */
  public ProviderFileType getProviderFileType()
  {
    return null;
  }
}
