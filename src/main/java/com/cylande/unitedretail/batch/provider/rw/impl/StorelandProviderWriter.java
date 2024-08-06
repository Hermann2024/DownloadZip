package com.cylande.unitedretail.batch.provider.rw.impl;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.framework.tools.FilenameUtil;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.STORELANDPROVIDER;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.storelandwsproxy.ur2stortools.Ur2StorProcessDataType;
import com.cylande.unitedretail.message.storelandwsproxy.ur2stortools.Ur2StorProcessScenarioType;
import com.cylande.unitedretail.storelandamq.service.Ur2StorProcessManagerServiceDelegate;
import org.apache.log4j.Logger;


public class StorelandProviderWriter extends ProviderWriter
{
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(StorelandProviderWriter.class);

  protected STORELANDPROVIDER _providerDef = null;

  private ProviderFileType _providerFile = null;

  /**
   * Constructeur
   * @param pProvider : provider dans lequel il faut écrire
   * @throws ProviderException : exception
   */
  public StorelandProviderWriter(Provider pProvider) throws ProviderException
  {
    super(pProvider);
    _providerDef = (STORELANDPROVIDER) getProvider().getProviderDef();
  }

  @Override
  public void write(String pValue) throws ProviderException
  {
    LOGGER.debug("Writing : " + pValue);
    Ur2StorProcessDataType ur2StorProcessData = new Ur2StorProcessDataType();
    ur2StorProcessData.setXmlMessage(pValue);
    if (_providerDef.getMapping() != null)
    {
      ur2StorProcessData.setMappingFileName(new FilenameUtil().addRelativePath(getProvider().getFilteredStringByDomain(_providerDef.getMapping())));
    }
    ur2StorProcessData.setDatasourceName(getProvider().getFilteredStringByDomain(_providerDef.getDataSourceName()));
    ur2StorProcessData.setManageObjectReturn(false);


    Ur2StorProcessManagerServiceDelegate ur2StorProcessManagerServiceDelegate = new Ur2StorProcessManagerServiceDelegate();
    try
    {
      ur2StorProcessManagerServiceDelegate.execute(ur2StorProcessData, new Ur2StorProcessScenarioType(), new ContextType());
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.STORELAND_PROVIDER_WRITER_ERR, e);
    }
  }

  @Override
  public void releaseProvider() throws ProviderException
  {
    if (getProvider() != null)
    {
      getProvider().closeInputStream();
    }
  }

  @Override
  public ProviderFileType getProviderFileType()
  {
    return _providerFile;
  }
}
