package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * R�f�rentiel des propri�t�s globales de mapper
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
   * @return r�sultat
   */
  public static MapperPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
