package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * R�f�rentiel des propri�t�s globales de providers
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
   * @return r�sultat
   */
  public static ProviderPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
