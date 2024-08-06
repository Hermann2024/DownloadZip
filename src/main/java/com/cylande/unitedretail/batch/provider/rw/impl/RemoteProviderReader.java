package com.cylande.unitedretail.batch.provider.rw.impl;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.pool.RemoteProviderPoolClient;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.ProviderFileType;

public class RemoteProviderReader extends ProviderReader
{
  private RemoteProviderPoolClient _owner;
  private String _name;

  /**
   * Constructeur
   * @param pOwner
   * @param pProviderReaderName
   */
  public RemoteProviderReader(RemoteProviderPoolClient pOwner, String pProviderReaderName)
  {
    super(null);
    _owner = pOwner;
    _name = pProviderReaderName;
  }

  /** {@inheritDoc} */
  public DataPackage read(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException
  {
    DataPackage result = _owner.read(_name, pCurrentTaskId);
    return result;
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    _owner.releaseReader(_name);
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
    return null;
  }

  /** {@inheritDoc} */
  public void setReject(FileType pFileType)
  {
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
