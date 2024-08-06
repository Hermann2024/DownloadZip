package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * R�f�rentiel des propri�t�s globales de batchs
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
   * @return r�sultat
   */
  public static BatchPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
