package com.cylande.unitedretail.batch.provider.rw.build;

import com.cylande.unitedretail.batch.service.ProviderManagerServiceImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.ProviderKeyType;
import com.cylande.unitedretail.message.batch.ProviderType;

/**
 * Loader from builder and loader pattern
 * <br>Load a 'provider' definition and initialize the ProviderLoader
 */
public class ProviderRWLoader
{
  private ProviderRWBuilder _builder = null;

  /**
   * Constructor
   * @param pBuilder the builder to initialize
   */
  public ProviderRWLoader(ProviderRWBuilder pBuilder)
  {
    _builder = pBuilder;
  }

  /**
   * Load Provider and then initialize the builder
   * @param pProviderName the provider id to load
   * @throws Exception exception
   */
  public void loadProviderDef(String pProviderName) throws WrapperServiceException
  {
    ProviderManagerServiceImpl providerManager = new ProviderManagerServiceImpl();
    // Load provider definition
    ProviderKeyType pKey = new ProviderKeyType();
    pKey.setName(pProviderName);
    // Initialize the builder
    ProviderType providerDef = providerManager.getProvider(pKey, null, ContextTransformer.fromLocale());
    _builder.setProviderInformations(providerDef);
  }
}
