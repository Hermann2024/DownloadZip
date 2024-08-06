package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * Référentiel des propriétés globales de batchs
 */
public final class BatchPropertiesRepository extends PropertiesRepository
{
  /** singleton */
  private static final BatchPropertiesRepository INSTANCE;
  static {
    INSTANCE = new BatchPropertiesRepository();
  }

  /**
   * Constructor
   */
  private BatchPropertiesRepository()
  {
    super();
  }

  /**
   * Restitue le singleton
   * @return résultat
   */
  public static BatchPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
