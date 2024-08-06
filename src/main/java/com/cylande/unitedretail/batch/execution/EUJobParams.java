package com.cylande.unitedretail.batch.execution;

import com.cylande.unitedretail.batch.execution.quartz.JobParamKey;
import com.cylande.unitedretail.batch.execution.quartz.JobParamMap;

/**
 * conteneur des paramètres de creation de batch ou taches filles
 */
public class EUJobParams extends JobParamMap
{

  /** Clef pour le batch parent */
  public static final JobParamKey PARENT_BATCH_KEY = new JobParamKey("parentBatch", true);

  /** Clef pour la définition de bacth */
  public static final JobParamKey BATCH_DEFINITION_KEY = new JobParamKey("batchDef", true);

  /** Clef pour la référence de tache */
  public static final JobParamKey TASK_DEFINITION_KEY = new JobParamKey("taskDef", true);

  /** Clef pour le site */
  public static final JobParamKey SITE_KEY = new JobParamKey("site", true);

  /** Clef pour le domain d'execution */
  public static final JobParamKey ACTIVE_DOMAIN_KEY = new JobParamKey("activeDomain", true);

  /** Clef pour le domain alternatif */
  public static final JobParamKey ALT_DOMAIN_KEY = new JobParamKey("defaultDomain", true);

  /** Clef pour les propriété de batch */
  public static final JobParamKey ENGINE_PROPERTIES_KEY = new JobParamKey("propEngRepo", true);

  /** Clef pour les variables de batch */
  public static final JobParamKey ENGINE_VARIABLES_KEY = new JobParamKey("varEngRepo", true);

  /** Clef pour l'option fail on error */
  public static final JobParamKey FAIL_ON_ERROR_KEY = new JobParamKey("failOnError", true);

  /** Clef pour l'id de batch */
  public static final JobParamKey BATCH_ID_KEY = new JobParamKey("batchId", true);

  /** Clef pour le login de l'utilisateur authentifié ( UserPrincipal.getName() ) */
  public static final JobParamKey UE_LOGIN_KEY = new JobParamKey("userName", false);

  /** Clef pour le sujet authentifié par JAAS */
  public static final JobParamKey UE_SUBJECT_KEY = new JobParamKey("subject", false);

  /** Clef pour la référence de tache */
  public static final JobParamKey THREAD_NUMBER_KEY = new JobParamKey("threadNumber", true);

  /** Clef pour la référence de tache */
  public static final JobParamKey THREAD_COUNT_KEY = new JobParamKey("threadCount", true);

  /** Référence de session pour les providers distribués */
  public static final JobParamKey PROVIDER_POOL_SESSION_ID_KEY = new JobParamKey("providerSessionID", true);

  /** Référence de session pour les providers distribués */
  public static final JobParamKey PROVIDER_POOL_URL_KEY = new JobParamKey("providerPoolUrl", true);

  /**
   * Constructeur
   */
  public EUJobParams()
  {
    super();
  }
}
