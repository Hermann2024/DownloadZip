package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.process.tools.PropertiesRepository;

/**
 * Référentiel des propriétés globales des tasks
 */
public final class TaskPropertiesRepository extends PropertiesRepository
{
  /** singleton */
  private static final TaskPropertiesRepository INSTANCE;
  static {
    INSTANCE = new TaskPropertiesRepository();
  }

  /**
   * Constructor
   */
  private TaskPropertiesRepository()
  {
    super();
  }

  /**
   * Restitue l'instance singleton
   * @return résultat
   */
  public static TaskPropertiesRepository getInstance()
  {
    return INSTANCE;
  }
}
