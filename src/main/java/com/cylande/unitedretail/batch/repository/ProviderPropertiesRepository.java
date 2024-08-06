package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * Référentiel des propriétés globales de providers
 */
public final class ProviderPropertiesRepository extends PropertiesRepository
{
  /** singleton */
  private static final ProviderPropertiesRepository INSTANCE;
  static {
    INSTANCE = new ProviderPropertiesRepository();
  }

  /**
   * Constructor
   */
  private ProviderPropertiesRepository()
  {
    super();
  }

  /**
   * restitue l'instance du singleton
   * @return résultat
   */
  public static ProviderPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
