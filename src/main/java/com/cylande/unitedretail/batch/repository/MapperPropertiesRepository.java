package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * Référentiel des propriétés globales de mapper
 */
public final class MapperPropertiesRepository extends PropertiesRepository
{
  /** singleton */
  private static final MapperPropertiesRepository INSTANCE;
  static {
    INSTANCE = new MapperPropertiesRepository();
  }

  /**
   * constructor
   */
  private MapperPropertiesRepository()
  {
    super();
  }

  /**
   * restitue l'instance du singleton
   * @return résultat
   */
  public static MapperPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
