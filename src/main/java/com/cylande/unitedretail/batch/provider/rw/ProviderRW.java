package com.cylande.unitedretail.batch.provider.rw;

import com.cylande.unitedretail.batch.provider.Provider;

/**
 * Parent Class for provider Reader or Writer
 */
public abstract class ProviderRW
{
  private Provider _provider = null;

  /**
   * Constructeur
   * @param pProvider
   */
  public ProviderRW(Provider pProvider)
  {
    _provider = pProvider;
  }

  /**
   * getProvider
   * @return résultat
   */
  public Provider getProvider()
  {
    return _provider;
  }
}
