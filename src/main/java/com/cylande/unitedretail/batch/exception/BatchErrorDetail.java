package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.exception.ErrorDetail;

/**
 * Enuméré des erreurs du moteur de batch.
 */
public enum BatchErrorDetail implements ErrorDetail
{
  // SERVICE
  //INIT_DEFAULT_CONTEXT_ERR("ST0001"),
  //BATCH_MANAGER_ERR("ST0002"),
  //PROVIDER_MANAGER_ERR("ST0003"),
  //TASK_MANAGER_ERR("ST0004"),
  //STATUS_MANAGER_ERR("ST0005"),
  //BATCHRUN_MANAGER_ERR("ST0016"),
  //TASKRUN_MANAGER_ERR("ST0017"),
  /** PROVIDERS */
  INIT_PROVIDER_READER_ERR("IT0006"),
  /** PROVIDERS */
  INIT_PROVIDER_WRITER_ERR("IT0012"),
  /** PROVIDERS */
  PROVIDER_CRC_READ_ERROR("IT0009"),
  /** PROVIDERS */
  PROVIDER_CRC_WRITE_ERROR("IT0010"),
  /** PROVIDERS */
  PROVIDER_CRC_ALREADY_EXISTS("IT0011"),
  /** PROVIDERS */
  PROVIDERREADER_RW_EVENT_PROB("IT0007"),
  /** PROVIDERS */
  PROVODERWRITER_RW_EVENT_PROB("IT0074"),
  /** PROVIDERS */
  NODEFPROVIDER_PROVIDER_READER_ERR("IT0075"),
  /** PROVIDERS */
  NODEFPROVIDER_PROVIDER_WRITER_ERR("IT0076"),
  /** PROVIDERS */
  FILE_PROVIDER_READER_NOTFOUND("IT0077"),
  /** PROVIDERS */
  FILE_PROVIDER_WRITER_NOTFOUND("IT0078"),
  /** PROVIDERS */
  BUILD_PROVIDER_READER_ERR("IT0079"),
  /** PROVIDERS */
  BUILD_PROVIDER_WRITER_ERR("IT0080"),
  /** PROVIDERS */
  STORELAND_PROVIDER_WRITER_ERR("IT0081"),
  /** PROVIDERS */
  UNSUPPORTED_ENCODING("IT0013"),
  /** STATUS */
  BATCH_REPORT_ERR("IT0014"),
  /** STATUS */
  TASK_REPORT_ERR("IT0015"),
  /** STATUS */
  GETTASKREPORT_PARAM("IT0045"),
  /** STATUS */
  GETBATCHREPORT_PARAM("SB0001"),
  /** BATCHS */
  BATCHBUILDER_BATCHDEF_NODEF("IT0046"),
  /** BATCHS */
  SETBATCHINFO_PARAM("IT0047"),
  /** BATCHS */
  BATCHBUILDER_PATH_NOTINIT("IT0048"),
  /** BATCHS */
  BATCHBUILDER_BUILD_ERR("IT0049"),
  /** BATCHS */
  BATCHLOADER_PARAM("IT0050"),
  /** BATCHS */
  BATCHLOADER_LOAD_PARAM("IT0051"),
  /** BATCHS */
  BATCHLOADER_BUILDER_NOTINIT("IT0052"),
  /** BATCHS */
  BATCHLOADER_LOAD_ERR("IT0053"),
  /** BATCHS */
  BATCHLOADER_LOAD_NOTDEFFOUND("IT0081"),
  /** BATCHS */
  BATCHRUN_BEFORE_PARAM("IT0054"),
  /** BATCHS */
  BATCHRUN_BEFORE_NOPATH("IT0055"),
  /** BATCHS */
  BATCHRUN_BEFORE_ERR("IT0056"),
  /** BATCHS */
  BATCHRUN_AFTER_ERR("IT0057"),
  /** BATCHS */
  BATCH_EXEC_ERR("IT0059"),
  /** BATCHS */
  BATCH_SHEDULING_ERR("IT0097"),
  /** BATCHS */
  BATCH_LAUNCH_ERR("IT0098"),
  /** BATCHS */
  BATCH_EXCLUSION_ERR("IT0099"),
  /** TASKS */
  LOAD_TASK_DEF_ERR("IT0018"),
  /** TASKS */
  LOAD_TASK_ERR("IT0019"),
  /** TASKS */
  LOAD_TASKRUN_ERR("IT0020"),
  /** Le type de tache {0} n'est pas supportée */
  UNSUPORTED_TASKTYPE("IT0200"),
  /** TASKS */
  UPDATE_TASKRUN_ERR("IT0021"),
  /** TASKS */
  TASKRUN_BEFORE_ERR("IT0022"),
  /** TASKS */
  TASKRUN_AFTER_ERR("IT0023"),
  /** TASKS */
  TASKRUN_BEFORE_NOPATH("IT0058"),
  /** TASKS */
  TASKRUN_BEFORE_PARAM("IT0045"),
  //TASK_IMPL_PROCESS_NOTFOUND("IT0024"),
  //TASK_INIT_PROCESS("IT0025"),
  /** TASKS */
  TASK_GEN_PROVIDER_ERR("IT0026"),
  /** TASKS */
  TASK_GEN_PROVIDER_PARAM("IT0040"),
  /** TASKS */
  TASK_GEN_PROVIDER_IO("IT0041"),
  /** TASKS */
  TASK_GETTASKRUN_VAR("IT0042"),
  /** TASKS */
  TASK_UPDATETASKRUN_PARAM("IT0043"),
  /** TASKS */
  TASK_EXTRACTION_ERR("IT0060"),
  /** TASKS */
  TASK_INTEGRATION_ERR("IT0061"),
  /** TASKS */
  INIT_TASK_PARAM("IT0038"),
  //INIT_PROC_PARAM("IT0039"),
  /** code erreur */
  INIT_PROC_REF("IT0039"),
  /** TRACABILITY */
  GETBATCHRUN_PARAM("IT0027"),
  /** TRACABILITY */
  GETBATCHRUN_ERR("IT0028"),
  /** TRACABILITY */
  FINDBATCHCHILDRUN_PARAM("IT0029"),
  /** TRACABILITY */
  FINDBATCHCHILDRUN_ERR("IT0030"),
  /** TRACABILITY */
  FINDTASKCHILDRUN_PARAM("IT0031"),
  /** TRACABILITY */
  FINDTASKCHILDRUN_ERR("IT0032"),
  /** TRACABILITY */
  GETTASKAUDIT_PARAM("IT0033"),
  /** TRACABILITY */
  GETTASKAUDIT_ERR("IT0034"),
  /** TRACABILITY */
  FINDTASKAUDIT_PARAM("IT0035"),
  /** TRACABILITY */
  FINDTASKAUDIT_ERR("IT0036"),
  /** TRACABILITY */
  GETBATCH_PARAM("IT0037"),
  // OTHERS
  //ABSTRACT_REPO_PROPERTY_NOTDEF("IT100"),
  //RELEASE_RESOURCES_ERR("IT0008"),
  /** code erreur */
  RELEASE_RESOURCES_ERR("IT0008"),
  //LOAD_ENGINE_PROPERTIES_NOTFOUND("IT0062"),
  //LOAD_ENGINE_PROPERTIES_ERR("IT0063"),
  //BATCH_PROPERTY_NOTDEF("IT0064"),
  /** code erreur */
  BATCH_REPO_POPULATE("IT0065"),
  /** code erreur */
  BATCH_REPO_NOLIST("IT0066"),
  //TASK_PROPERTY_NOTDEF("IT0067"),
  /** code erreur */
  TASK_REPO_POPULATE("IT0068"),
  /** code erreur */
  TASK_REPO_NOLIST("IT0069"),
  //PROVIDER_PROPERTY_NOTDEF("IT0070"),
  /** code erreur */
  PROVIDER_REPO_POPULATE("IT0071"),
  /** code erreur */
  PROVIDER_REPO_NOLIST("IT0072"),
  /** ENGINE */
  ENGINE_EXEC_PARAM("SB0002"),
  /** ENGINE */
  ENGINE_EXEC_BATCH_NOTFOUND("IT0073"),
  /** ENGINE */
  ENGINE_EXEC_GETBATCH_ERROR("IT0087"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_EXEC_NOPARAMS("SB0004"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_EXEC_NOBATCHNAME("SB0005"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_EXEC_NOSITE("SB0015"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_EXEC_ERROR("IT0088"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_STOP_NOPARAMS("SB0011"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_STOP_NOBATCHNAME("SB0012"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_STOP_BATCHNOTFOUND("SB0013"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_STOP_ERROR("SB0014"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_CONSTRUCTOR("IT0089"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_LOAD_ERROR("IT0090"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_STARTSCHEDULE_ERROR("IT0091"),
  /** ENGINE SERVICE */
  ENGINE_SERVICE_STOPSCHEDULE_ERROR("IT0092"),
  /** TRIGGER */
  TRIGGER_REPO_POPULATE("IT0083"),
  /** TRIGGER */
  TRIGGER_PROPERTY_NOTDEF("IT0084"),
  /** TRIGGER */
  TRIGGER_PARSE_CRON_ERROR("IT0085"),
  /** TRIGGER */
  TRIGGER_REPO_NOLIST("IT0086"),
  /** SCHEDULER */
  SCHEDULER_CREATE_QUARTZ_JOB("IT0093"),
  /** SCHEDULER */
  SCHEDULER_STATEFULLJOB_RUNNING("IT0094"),
  /** SCHEDULER */
  SCHEDULER_DELETE_QUARTZ_JOB("IT0095"),
  /** SCHEDULER */
  SCHEDULER_JOB_EXECUTION("IT0096"),
  /** SCHEDULER */
  SCHEDULER_JOB_LAUNCH_FAILED("IT0100"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_SERVICE_KEY_NULL("SB0006"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_SERVICE_KEY_ID_NULL("SB0007"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_SERVICE_KEY_PATH_NULL("SB0008"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_SERVICE_KEY_PATH_EMPTY("SB0009"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_SERVICE_KEY_SITE_NULL("SB0016"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_SERVICE_BATCHRUNKEY_UNKNOWN("SB0010"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_GEN_BATCH_REPORT_ERROR("IT0122"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_GEN_TASK_REPORT_ERROR("IT0123"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_BATCH_TEST_ERROR("IT0124"),
  /** BATCH_STATUS_ENGINE_SERVICE */
  BATCH_STATUS_GETBATCHRUN_ERROR("IT0125"),
  /** impossible d'extraire le nom de la tache */
  TASK_GETTASKNAME("IT0120"),
  /** impossible de générer le DYNAFILEPROVIDER car le nom de fichier n'est pas spécifié */
  DYNAFILEPROVIDER_NO_FILENAME("IT0121"),
  /** le type de message utilisé pour le provider de sortie est différent du précédent */
  PROVIDERWRITER_REWRITE_DIFF_TYPE("IT0126"),
  /** un problème est survenu durant la cloture du fichier {0} en lecture du répertoire {1} */
  FILEPROVIDERMANAGER_CLOSECURRENTINPUTSTREAM_ERROR("IT0127"),
  /** un problème est survenu durant la récupération d'un flux de lecture sur le fichier {0} du répertoire {1} */
  FILEPROVIDERMANAGER_GETINPUTSTREAM_ERROR("IT0128"),
  /** un problème est survenu durant la récupération d'un flux de sortie pour le fichier {0} du répertoire {1} */
  FILEPROVIDERMANAGER_GETOUTPUTSTREAM_ERROR("IT0129"),
  /** jokers utilisés pour la désignation du fichiers d'écriture */
  FILEPROVIDERMANAGER_JOKERS_FORBIDDEN("IT0130"),
  /** un problème est survenu durant la cloture du fichier {0} en écriture du répertoire {1} */
  FILEPROVIDERMANAGER_CLOSEOUTPUTSTREAM_ERROR("IT0131"),
  /** un problème est survenu durant la construction du providerReader pour le provider {0} */
  PROVIDERREADER_CONSTRUCTION_ERROR("IT0132"),
  /** un problème est survenu durant la lecture du provider {0} */
  PROVIDERREADER_READ_ERROR("IT0133"),
  /** une erreur s'est produite pendant l'initialisation du providerwriter pour le provider {0} */
  PROVIDERWRITER_INIT_ERROR("IT0134"),
  /** un problème est survenu durant l'écriture sur le provider {0} */
  PROVIDERWRITER_WRITE_ERROR("IT0135"),
  /** erreur durant la récupération du gestionnaire de fichier pour le provider {0} */
  FILEPROVIDER_GETFILEMANAGER_ERROR("IT0136"),
  /** pas de flux à lire pour le parseur XML */
  STAXPARSER_NO_INPUTSTREAM("IT0137"),
  /** erreur durant le test sur la mise à jour de la définition du provider */
  PROVIDER_DEFINITIONUPDATED_ERROR("IT0138"),
  /** MAPPER */
  MAPPER_REPO_POPULATE("IT0139"),
  /** MAPPER */
  MAPPER_REPO_NOLIST("IT0140"),
  /** erreur durant la récupération du gestionnaire de fichier pour le provider {0} */
  FILEPROVIDER_GETMAPPERMANAGER_ERROR("IT0141"),
  /** aucun noeud root dans le flux fournit a STAX */
  PROVIDERREADER_NO_ROOT("IT0142"),
  /** erreur lors de l'instantiation de la classe transformer pour le mapper */
  PROVIDER_INSTANTIATEMAPPER_ERROR("IT0143"),
  /** aucun mapper trouvé pour la référence donnée dans le provider */
  PROVIDER_NO_MAPPER_FOUND_ERROR("IT0144"),
  /** definition mapper incorrecte */
  MAPPER_DEFINITION_INCORRECT("IT0145"),
  /** erreur lors de l'encodage */
  MAPPER_UNSUPORTED_ENCODING("IT0146"),
  /** code erreur */
  MAPPER_REF_ERROR("IT0147"),
  /** aucun fichier de définit dans le mapper */
  MAPPER_FILE_ERROR("IT0148"),
  /** erreur de communication avec le provider distant - pool read error. */
  REMOTE_PROVIDER_PROTOCOLE_SERVLET_READ_ERROR("IT0170"),
  /** erreur de communication avec le provider distant - pool write error. */
  REMOTE_PROVIDER_PROTOCOLE_SERVLET_WRITE_ERROR("IT0171"),
  /** erreur lors de la communication avec le provider distant : session timeOut; */
  REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR("IT0172"),
  /** erreur de communication avec le provider distant - client read error. */
  REMOTE_PROVIDER_PROTOCOLE_CLIENT_READ_ERROR("IT0175"),
  /** erreur de communication avec le provider distant - client write error. */
  REMOTE_PROVIDER_PROTOCOLE_CLIENT_WRITE_ERROR("IT0176"),
  /** erreur de communication avec le provider distant - initialisation error. */
  REMOTE_PROVIDER_PROTOCOLE_CLIENT_INIT_ERROR("IT0177"),
  /** aucun login de connexion définit pour le site */
  NO_LOGIN_SITE("IT0154"),
  /** aucun nom de de définit pour le site */
  NO_NAME_SITE("IT0155"),
  /** aucun mot de passe de définit pour le site */
  NO_PASSWORD_SITE("IT0156"),
  /** aucune URL de définit pour le site */
  NO_URL_SITE("IT0157"),
  /** erreur lors de la récupération du site */
  NO_SITE_DEF("IT0158"),
  /** URL mal formée */
  SITE_MALFORMED_URL("IT0159"),
  /** MAPPER : Aucun template correspondant trouvé */
  FLATMAPPER_TEMPLATE_NOT_FIND("IT0160"),
  /** MAPPER : Le nombre de champs du modèle (template) ne correspond pas à celui des données */
  FLATMAPPER_TOO_MUCH_FIELDS("IT0161"),
  /** MAPPER : Problème dans la lecture du fichier xml. */
  FLATMAPPER_XML_EXCEPTION("IT0162"),
  /** MAPPER : Problème dans le cache du service. */
  FLATMAPPER_TECHNICAL_EXCEPTION("IT0163"),
  /** aucun site local défini en base */
  BATCHENGINE_NO_LOCALSITE_IN_DB("IT0164"),
  /** Erreur lors de la récupération du "BuildNumber" */
  BUILDNUMBER_INPUT_ERROR("IT0165"),
  /** Erreur lors de la création du fichier "BuildNumber" */
  BUILDNUMBER_CREATE_ERROR("IT0166"),
  /** Erreur lors de l'écriture dans le fichier "BuildNumber" */
  BUILDNUMBER_WRITE_ERROR("IT0167"),
  /** Aucune instance de batch trouvée avec le numéro {0} */
  NO_BATCH_RUN_FOUND("IT0168"),
  /** Impossible de modifier le fichier "{0}" */
  CAN_NOT_MODIFY_FILE("IT0169"),
  /** Impossible de lire le fichier "{0}" */
  CAN_NOT_READ_FILE("IT0178"),
  /** code erreur */
  CAN_NOT_READ_FILE_PROVIDER("IT0179"),
  /** code erreur */
  LOCKED_BATCH_EXECUTION("IT0180"),
  /** Fichier "{0}" invalide : {1} */
  STAXPARSER_INVALID_XML_FILE("IT0181"),
  /** Impossible de déplacer le fichier {0} dans le répertoire de rejet. */
  FILEPROVIDERMANAGER_MOVE_REJECT_ERROR("IT0182"),
  /** Impossible de créer le batch car tous les identifiants fournis par la séquence associée ont été attribués */
  BATCHRUN_FULL_SEQUENCE("IT0183"),
  /** Impossible de créer la tâche car tous les identifiants fournis par la séquence associée ont été attribués */
  TASKRUN_FULL_SEQUENCE("IT0184"),
  /** Impossible de créer l'erreur de batch car tous les identifiants fournis par la séquence associée ont été attribués */
  TASKAUDIT_FULL_SEQUENCE("IT0185"),
  /** Impossible d'ajouter le fichier de batch car tous les identifiants fournis par la séquence associée ont été attribués */
  FILEPROVIDERTRACE_FULL_SEQUENCE("IT0186"),
  /** Erreur lors de la suppression des données associées au batch {0}. */
  BATCH_DELETE_ASSOCIATED_ROW_ERROR("IT0187"),
  /** Erreur lors de la suppression des données associées à la task {0}. */
  TASK_DELETE_ASSOCIATED_ROW_ERROR("IT0188"),
  /** Erreur lors de la récupération du provider. */
  GET_PROVIDER_FILE_TYPE_ERROR("IT0189"),
  /** La requête doit être une lecture de données exclusivement */
  QUERY_SQL_NOT_READONLY_ERROR("IT0190"),
  /** La requête demandée n'a pas fonctionné */
  QUERY_SQL_EXECUTE_ERROR("IT0191"),
  /** La requête ne peut contenir que 200 colonnes */
  QUERY_SQL_MAX_COLUMN_ERROR("IT0192"),
  /**
   * Service non implémenté pour ce type de base de données
   */
  QUERY_SQL_NOT_IMPLEMENTED_YET("IT0193"),
  /**
   * IT0194 = Paramètres d'appel du service incorrects
   */
  QUERY_SQL_INVALID_PARAMS("IT0194"),
  /**
   * La requête n’a pu être exécutée car son coût est trop élevé ou sa complexité est trop importante
   */
  QUERY_SQL_HIGHER_COST("IT0195"),
  /**
   * Une erreur à eu lieu pendant l'analyse de la requête
   */
  QUERY_SQL_ERROR_COST_ANALYZE("IT0196"),
  /**
   * Une erreur à eu lieu pendant le contrôle du lock pour le batch
   */
  CHECK_LOCKED_BATCH_ERROR("IT0197");


  /**
   * Product code
   */
  public static final String PRODUCT = "URHO";

  /**
   * Module code
   */
  public static final String MODULE = "ENG";

  /**
   * Project code
   */
  public static final String PROJECT = "BAT";

  /**
   * Attribute containing the value for this enum : the error code
   */
  private String _code;

  /**
   * Constructor to associate a value to this enum
   * @param pCode : The error code
   */
  BatchErrorDetail(String pCode)
  {
    this._code = pCode;
  }

  /**
   * Sample error code : 00001
   * @return a String containing the error code (value of one of the constants defined into this Enum)
   */
  public String getCode()
  {
    return _code;
  }

  /**
   * Sample error code : IT0001
   * @param pCode Code
   */
  public void setCode(String pCode)
  {
    this._code = pCode;
  }

  /**
   * Sample product code : URHO
   * @return a String containing the product code
   */
  public String getProduct()
  {
    return PRODUCT;
  }

  /**
   * Sample module code : Foundation
   * @return a String containing the module code
   */
  public String getModule()
  {
    return MODULE;
  }

  /**
   * Sample project code : Framework
   * @return a String containing the project code
   */
  public String getProject()
  {
    return PROJECT;
  }

  /**
   * Sample canonical code : URHO-Foundation-Framework-00001
   * @return a String containing the canonical code
   */
  public String getCanonicalCode()
  {
    return getProduct() + "-" + getModule() + "-" + getProject() + "-" + getCode();
  }
}
