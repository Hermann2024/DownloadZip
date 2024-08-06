package com.cylande.unitedretail.batch.execution;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.framework.logging.FrameworkMDCEntries;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.process.tools.EngineSysObjectRepository;
import com.cylande.unitedretail.process.tools.PropertiesManager;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.PropertiesTools;
import com.cylande.unitedretail.process.tools.VariablesRepository;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * Unité d'exécution (E.U.) : Classe abstraite représentant un élément exécutable de batch
 */
public abstract class ExecutionUnit implements Serializable
{
  /** Utilisateur par défaut */
  public static final String UNKNOWN_USER = "Unknown";

  /** Id du batch Root */
  public static final String SYS_ROOT_ID_KEY_NAME = "batchRootId";

  /** Path du batch Root */
  public static final String SYS_ROOT_PATH_KEY_NAME = "batchRootPath";

  /** Timestamp de début d'exécution du batch root */
  public static final String SYS_ROOT_START_TIME_KEY_NAME = "batchRootStartTime";

  /** Timeout du batch root */
  public static final String SYS_ROOT_TIMEOUT_KEY_NAME = "batchRootTimeout";

  /** Code site */
  public static final String SYS_SITE_CODE_KEY_NAME = "siteCode";

  /** logger technique */
  private static final Logger LOGGER = Logger.getLogger(ExecutionUnit.class);

  /** Id du batch parent */
  private static final String SYS_PARENT_ID_KEY_NAME = "batchParentId";

  /** Path du batch parent */
  private static final String SYS_PARENT_PATH_KEY_NAME = "batchParentPath";

  /** Timestamp de début d'éxécution du batch parent */
  private static final String SYS_PARENT_START_TIME_KEY_NAME = "batchParentStartTime";

  /** Pour la sérialisation */
  private static final long serialVersionUID = 12345;

  /** identifie la classe pour les logs */
  private static final String DEBUG_CLASS_INFO = " [Execution Unit] ";

  /** properties manager pour récupérer les propriétés */
  protected PropertiesManager _propManager = null;

  /** batch est-ce que le batch est root ? */
  protected boolean _isRoot;

  /** chaine decrivant le contexte (path, id) */
  protected String _debugContextInfo = "";

  /** défini le comportement du batch en cas d'erreur sur cette unité d'exécution  */
  private Boolean _failOnError = false;

  /** La derniere exception */
  private EUExecutionException _exception = null;

  /** la repository de variables ENG utilisées par le moteur de batch */
  private VariablesRepository _varENGrepo = null;

  /** le nom de l'utilisateur ( = Principal.getName ) ayant demandé l'exécution du batch */
  private String _user = UNKNOWN_USER;

  /** l'utilisateur et ses droits (Subject) */
  private transient Subject _authSubject = null;

  /** flag d'état */
  private EUState _state = EUState.CREATED;

  /** date de fin d'execution */
  private Calendar _endTime = null;

  /** l'utilisateur (Principal) */
  private transient Principal _userPrincipal;

  /** Job Name */
  private transient EUJobManager _ueJobManager;

  /** loggueur fonctionnel */
  private transient Logger _executionLogger = null; // NOPMD

  /** the name of this execution unit */
  private String _name;

  /** the current domain */
  private String _domain = PropertiesManager.DEFAULT_DOMAIN;

  /** the alternative domain */
  private String _alternativeDomain = null;

  /**
   * Constructeur
   * @param pParent : l'E.U. mère
   * @param pName   : le nom de cette E.U.
   */
  protected ExecutionUnit(ExecutionUnit pParent, String pName)
  {
    super();
    init(pParent, pName, null);
  }

  /**
   * Constructeur
   * @param pParent : l'E.U. mère
   * @param pName   : le nom de cette E.U.
   * @param pId     : l'Id attribué à cette E.U. (défini dans la méthode before ultérieurement si null)
   */
  protected ExecutionUnit(ExecutionUnit pParent, String pName, Integer pId)
  {
    super();
    init(pParent, pName, pId);
  }

  /**
   * @return le loggueur pour les traces applicatives.
   */
  private Logger getExecutionLogger()
  {
    if (_executionLogger == null)
    {
      _executionLogger = Logger.getLogger(getSysPath());
    }
    return _executionLogger;
  }

  /**
   * Initialisation interne
   * @param pParent : l'E.U. (unité d'exécution) mère
   * @param pName   : le nom de cette E.U.
   * @param pId     : l'Id de cette E.U. (null si à définir).
   */
  private void init(ExecutionUnit pParent, String pName, Integer pId)
  {
    _name = pName;
    _propManager = new PropertiesManager();
    _propManager.setSYSrepo(new EngineSysObjectRepository());
    _isRoot = (pParent == null);
    if (_isRoot)
    {
      _domain = PropertiesManager.DEFAULT_DOMAIN;
      _debugContextInfo = pName + '(' + pId + ')';
      setSysPath(pName);
      setSysId(pId);
      _propManager.putSysObject(SYS_PARENT_ID_KEY_NAME, Integer.valueOf(-1));
      _propManager.putSysObject(SYS_PARENT_PATH_KEY_NAME, "");
    }
    else
    {
      _domain = pParent.getDomain();
      _alternativeDomain = pParent.getAlternativeDomain();
      String path = pParent.getSysPath() + '.' + pName;
      _debugContextInfo = path + '(' + pId + ')';
      setSysPath(path);
      setSysId(pId);
      setUser(pParent.getUser());
      setAuthenticatedSubject(pParent.getAuthenticatedSubject());
      updateSysInfo(pParent);
    }
  }

  /**
   * Getter de l'Id de l'unité d'exécution mère
   * (-1) si pas de parent (root)
   * @return Integer : l'id du parent
   */
  public Integer getParentId()
  {
    Integer result;
    result = (Integer)_propManager.getSysObject(SYS_PARENT_ID_KEY_NAME, -1);
    return result;
  }

  /**
   * Getter du path de cette E.U.
   * @return null si pas de parent
   */
  public String getParentPath()
  {
    String result;
    result = (String)_propManager.getSysObject(SYS_PARENT_PATH_KEY_NAME, null);
    return result;
  }

  /**
   * Setter du nom d'utilisateur de cette E.U.
   * @param pUser : le nom d'utilisateur (equivalent à getUserPrincipal.getName())
   */
  public void setUser(String pUser)
  {
    LOGGER.debug(DEBUG_CLASS_INFO + _debugContextInfo + " set user (" + pUser + ")");
    if ((pUser == null) || (pUser.trim().length() == 0))
    {
      _user = UNKNOWN_USER;
    }
    else
    {
      _user = pUser;
    }
    _userPrincipal = null;
  }

  /**
   * Getter du nom d'utilisateur de cette E.U.
   * @return String : _user who have send request to Servlet
   */
  public String getUser()
  {
    String result;
    if ((_user == null) || (_authSubject == null))
    {
      result = UNKNOWN_USER;
    }
    else
    {
      result = _user;
    }
    LOGGER.debug(DEBUG_CLASS_INFO + _debugContextInfo + " get User (" + result + ")");
    return result;
  }

  /**
   * Accesseur de la repository des variables Engine
   * @return la repository de variables Engine
   */
  public final VariablesRepository getVarENGrepo()
  {
    VariablesRepository result = _varENGrepo;
    LOGGER.debug(DEBUG_CLASS_INFO + _debugContextInfo + " get Engine Variable Repo (" + result + ")");
    return _varENGrepo;
  }

  /**
   * Mutateur du domaine de cette unité d'exécution
   * @param pDomain : le domaine
   */
  public final void setDomain(String pDomain)
  {
    LOGGER.debug(DEBUG_CLASS_INFO + _debugContextInfo + " set domain (" + pDomain + ")");
    if ((pDomain != null) && (pDomain.trim().length() > 0))
    {
      _domain = pDomain;
    }
  }

  /**
   * Mutateur du domaine alternatif de l'unité d'exécution
   * @param pAlternativeDomain : le domaine alternatif
   */
  public final void setAlternativeDomain(String pAlternativeDomain)
  {
    if ((pAlternativeDomain != null) && (pAlternativeDomain.trim().length() > 0))
    {
      _alternativeDomain = pAlternativeDomain;
    }
  }

  /**
   * Get the Domain of this unit Execution.
   * @return the domain name
   */
  public String getDomain()
  {
    return _domain;
  }

  /**
   * Get the alternative Domain
   * @return the alternative domain name
   */
  public String getAlternativeDomain()
  {
    return _alternativeDomain;
  }

  /**
   * Getter for fail on error option
   * @return true si cette unité produit des exceptions blocantes pour la suite du batch.
   */
  public boolean isFailOnError()
  {
    boolean result = _failOnError.booleanValue();
    LOGGER.debug(DEBUG_CLASS_INFO + _debugContextInfo + " get fail on error (" + result + ")");
    return result;
  }

  /**
   * Set the fail on error attribute
   * true if we wants to exit to the parent batch if an error occurs during execution
   * false or null otherwise
   * @param pFailOnError the option value
   */
  public void setFailOnError(Boolean pFailOnError)
  {
    LOGGER.debug(DEBUG_CLASS_INFO + _debugContextInfo + " set fail on Error (" + pFailOnError + ")");
    if (pFailOnError == null)
    {
      _failOnError = false;
    }
    else
    {
      _failOnError = pFailOnError;
    }
  }

  /**
   * Retournes le gestionnaire de propriétés
   * @return PropertiesManager
   */
  public PropertiesManager getPropManager()
  {
    return _propManager;
  }

  /**
   * Enregistre la référence de la propENGrepo utilisée par l'instance de batchEngine
   * @param pPropENGrepo : le repository des propriété de batch
   */
  public final void setPropertiesEngRepo(PropertiesRepository pPropENGrepo)
  {
    _propManager.setPropENGrepo(pPropENGrepo);
  }

  /**
   * Enregistre la référence de la varENGrepo utilisée par l'instance de batchEngine
   * @param pVarENGrepo : le repository des variables de batch
   */
  public final void setVariablesEngRepo(VariablesRepository pVarENGrepo)
  {
    _varENGrepo = pVarENGrepo;
  }

  /**
   * Construit une chaine paramétrée par des propritées de batch
   * @param pString la chaine paramétrée
   * @return la chaine construite
   */
  public String getFilteredString(String pString)
  {
    if (_propManager == null)
    {
      return null;
    }
    return PropertiesTools.replaceProperties(pString, _propManager, _domain, _alternativeDomain);
  }

  /**
   * Retourne vrai si une exception s'est produite
   * @return vrai ou faux
   */
  public boolean hasException()
  {
    return (_exception != null);
  }

  /**
   * Retourne l'exception ayant empeché le fonctionement de l'unite d'execution.
   * @return l'exception
   */
  public EUExecutionException getException()
  {
    return _exception;
  }

  /**
   * Défini l'exception ayant interrompu l'exécution de l'unite d'execution.
   * @param pExecutionException : l'exception
   */
  protected void setException(EUExecutionException pExecutionException)
  {
    _exception = pExecutionException;
  }

  /**
   * Sets the subject associated to this batch instance (builder only)
   * @param pSubject : the subject
   */
  public void setAuthenticatedSubject(Subject pSubject)
  {
    _authSubject = pSubject;
    _userPrincipal = null;
  }

  /**
   * Cancel the execution unit
   * @param pForced : if true stop immediatly, false stop after current execution finished
   */
  public final void cancel(boolean pForced)
  {
    logInfo("INFO", "user interruption");
    if (_ueJobManager != null)
    {
      _ueJobManager.cancelChilds();
      if (pForced)
      {
        _ueJobManager.cancelChilds();
      }
    }
    doCancel(pForced);
  }

  /**
   * Methode designed for init operations
   * Invocated before execute() call
   * @throws EUExecutionException if an exception occurs during this step (initialisation)
   */
  protected abstract void before() throws EUExecutionException;

  /**
   * Methode designed for release operations
   * Invocated after execute() call
   */
  protected abstract void after();

  /**
   * Execution implementation     : need to be override inside batch or task implementation
   * @throws EUExecutionException : if exception occurs during Execution
   */
  protected abstract void execute() throws EUExecutionException;

  /**
   * Fired if a cancellation is asked
   * @param pForced : true if stop need to be immediate
   */
  protected abstract void doCancel(boolean pForced);

  /**
   * Met à jour les informations systeme avec les infos du parent
   * @param pParentUnit : unité d'execution parent
   */
  private void updateSysInfo(ExecutionUnit pParentUnit)
  {
    if (pParentUnit != null)
    {
      // le site
      _propManager.putSysObject(SYS_SITE_CODE_KEY_NAME, pParentUnit.getSiteKey().getCode());
      // info de l'ue parent.
      _propManager.putSysObject(SYS_PARENT_ID_KEY_NAME, pParentUnit.getSysId());
      _propManager.putSysObject(SYS_PARENT_PATH_KEY_NAME, pParentUnit.getSysPath());
      _propManager.putSysObject(SYS_PARENT_START_TIME_KEY_NAME, pParentUnit.getSysStartTime());
      // info de l'ue root.
      Object obj;
      PropertiesManager parentPropertiesManager = pParentUnit.getPropManager();
      obj = parentPropertiesManager.getSysObject(SYS_ROOT_ID_KEY_NAME);
      _propManager.putSysObject(SYS_ROOT_ID_KEY_NAME, obj);
      obj = parentPropertiesManager.getSysObject(SYS_ROOT_PATH_KEY_NAME);
      _propManager.putSysObject(SYS_ROOT_PATH_KEY_NAME, obj);
      obj = parentPropertiesManager.getSysObject(SYS_ROOT_START_TIME_KEY_NAME);
      _propManager.putSysObject(SYS_ROOT_START_TIME_KEY_NAME, obj);
      obj = parentPropertiesManager.getSysObject(SYS_ROOT_TIMEOUT_KEY_NAME, null);
      if (obj != null)
      {
        _propManager.putSysObject(SYS_ROOT_TIMEOUT_KEY_NAME, obj);
      }
      // repositories global et engine
      _propManager.setPropGLOrepo(parentPropertiesManager.getPropGLOrepo());
      _propManager.setPropENGrepo(parentPropertiesManager.getPropENGrepo());
      // variables
      _varENGrepo = pParentUnit.getVarENGrepo();
    }
  }

  /**
   * Getter pour le Principal associé à l'utilisateur authentifié
   * @return le principal utilisateur
   */
  protected Principal getUserPrincipal()
  {
    if ((_userPrincipal == null) && (_authSubject != null) && (_user != null) && (!_user.equals(UNKNOWN_USER)))
    {
      Set<Principal> principals = _authSubject.getPrincipals();
      Iterator<Principal> it = principals.iterator();
      Principal tmp;
      while (it.hasNext())
      {
        tmp = it.next();
        if (tmp.getName().equals(_user))
        {
          _userPrincipal = tmp;
          break;
        }
      }
    }
    return _userPrincipal;
  }

  /**
   * Getter pour le subject authentifié
   * @return le sujet authentifié
   */
  public Subject getAuthenticatedSubject()
  {
    return _authSubject;
  }

  /**
   * execution
   */
  public final void run()
  {
    try
    {
      setSysStartTime(Calendar.getInstance());
      if (_state != EUState.CANCELING)
      {
        try
        {
          before();
        }
        catch (Exception e)
        {
          LOGGER.warn(DEBUG_CLASS_INFO + _debugContextInfo + " exception lors de l'execution du before");
          logInfo("ERROR", e);
        }
        if (_state != EUState.CANCELING)
        {
          _state = EUState.RUNNING;
          execute();
          if (_ueJobManager != null)
          {
            _ueJobManager.waitChilds(null);
          }
        }
      }
    }
    catch (EUExecutionException e)
    {
      _exception = e;
    }
    finally
    {
      _endTime = Calendar.getInstance();
      _state = EUState.FINALIZING;
      after();
      _state = EUState.ENDED;
      releaseResources();
      setSysId(null);
    }
  }

  /**
   * Déclenche l'exécution d'une unité fille
   * @param pBatchChildRef : la reférence de l'unité fille
   * @param pWaitEnd : true si on doit attendre la fin d'exécution de cette unité avant de sortir de la méthode
   * @param pNbChildMax
   * @throws EUBuildException : si une erreur se produit lors de la création du job enfant
   * @throws EULaunchException : si une erreur se produit lors du lancement du job enfant
   * @throws EUExecutionException : si une erreur se produit durant l'exécution.
   */
  protected void launchChild(BatchChildrenAbstractType pBatchChildRef, boolean pWaitEnd, Integer pNbChildMax) throws EUBuildException, EULaunchException, EUExecutionException
  {
    if (_ueJobManager != null)
    {
      try
      {
        _ueJobManager.launchChild(this, pBatchChildRef);
        if (pWaitEnd || pNbChildMax != null)
        {
          _ueJobManager.waitChilds(pNbChildMax);
        }
      }
      catch (Exception e)
      {
        EUExecutionException e2;
        if (e instanceof EUExecutionException)
        {
          e2 = (EUExecutionException)e;
        }
        else
        {
          e2 = new EUExecutionException(BatchErrorDetail.BATCH_LAUNCH_ERR, new Object[] { this.getSysPath() + '.' + pBatchChildRef.getRef() }, e);
        }
        setException(e2);
        Boolean bFailOnError = pBatchChildRef.getFailOnError();
        logInfo("ERROR", e);
        if (Boolean.TRUE.equals(bFailOnError))
        {
          throw e2;
        }
      }
    }
  }

  /**
   * acesseur de l'état d'exécution
   * @return l'etat d'exécution
   */
  public EUState getState()
  {
    return _state;
  }

  /**
   * setteur interne de l'Id dans le SysRepo
   * @param pValue : l'Id (Integer)
   */
  protected abstract void setSysId(Integer pValue);

  /**
   * setteur interne du path dans le SysRepo
   * @param pValue : le path (String)
   */
  protected abstract void setSysPath(String pValue);

  /**
   * setteur interne de l'heure de démarrage dans le SysRepo
   * @param pValue l'heure de démarrage du batch (Calendar)
   */
  protected abstract void setSysStartTime(Calendar pValue);


  /** Methode interne de libération des ressources (apellée en fin d'exécution même en cas d'erreur) */
  protected abstract void releaseResources();

  /**
   * Accesseur de l'Id de batch
   * @return l'id de batch
   */
  public abstract Integer getSysId();

  /**
   * Accesseur du path du batch
   * @return le path
   */
  public abstract String getSysPath();

  /**
   * Accesseur du timeStamp de démarrage
   * @return timeStamp de démarrage (Calendar)
   */
  public abstract Calendar getSysStartTime();

  /**
   * Accesseur du timestamp de fin d'execution
   * @return timestamp de fin d'execution
   */
  public Calendar getEndTime()
  {
    return _endTime;
  }

  /**
   * Permet d'associer un job Manager à cette unité d'exécution
   * @param pEuJobManager : le job manager
   */
  public void setUeJobManager(EUJobManager pEuJobManager)
  {
    _ueJobManager = pEuJobManager;
  }

  /**
   * RAZ MDC entries
   */
  public void razMDC()
  {
    // Standard MDCEntries
    MDC.remove(FrameworkMDCEntries.BATCH_TRACE_TYPE.getLabel());
    MDC.remove(FrameworkMDCEntries.BATCH_TRACE_PATH.getLabel());
    MDC.remove(FrameworkMDCEntries.BATCH_TRACE_ID.getLabel());
    MDC.remove(FrameworkMDCEntries.BATCH_TRACE_USER.getLabel());
  }

  /** Write Informations on Batch avancement in Logs
   * @param pType : START,STEP,LOAD,PROGRESS,ERROR,END
   * @param pError : error that eventually occurs
   */
  public void logInfo(String pType, Throwable pError)
  {
    String message;
    if (pError == null)
    {
      message = "";
    }
    else
    {
      message = pError.getLocalizedMessage();
    }
    logInfo(pType, message);
  }

  /** Write Informations on Batch avancement in Logs
   * @param pType : START,STEP,LOAD,PROGRESS,ERROR,END
   * @param pMessage : the message to log
   */
  public void logInfo(String pType, String pMessage)
  {
    try
    {
      MDC.put(FrameworkMDCEntries.BATCH_TRACE_TYPE.getLabel(), pType);
      MDC.put(FrameworkMDCEntries.BATCH_TRACE_PATH.getLabel(), getSysPath());
      MDC.put(FrameworkMDCEntries.BATCH_TRACE_ID.getLabel(), getSysId());
      MDC.put(FrameworkMDCEntries.BATCH_TRACE_USER.getLabel(), getUser());
      getExecutionLogger().info(pMessage);
      LOGGER.info(pMessage);
    }
    catch (Exception e)
    {
      LOGGER.warn("Failed to trace informations on Batch in log", e);
    }
    razMDC();
  }

  /**
   * Setter for site key
   * @param pSiteKey the site key
   */
  public void setSiteKey(SiteKeyType pSiteKey)
  {
    if ((pSiteKey != null) && (pSiteKey.getCode() != null))
    {
      _propManager.putSysObject(SYS_SITE_CODE_KEY_NAME, pSiteKey.getCode());
    }
  }

  /**
   * Getter for Site key
   * @return the site key
   */
  public SiteKeyType getSiteKey()
  {
    SiteKeyType result = null;
    String siteCode = (String)_propManager.getSysObject(SYS_SITE_CODE_KEY_NAME, null);
    if (siteCode != null)
    {
      result = new SiteKeyType();
      result.setCode(siteCode);
    }
    return result;
  }

  /**
   * return the batch name
   * @return the name.
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Surcharge de la sérialisation Java pour le mode multithread
   * @param pOut : le stream pour l'écriture de l'objet
   * @throws IOException exception
   */
  private void writeObject(java.io.ObjectOutputStream pOut) throws IOException
  {
    pOut.writeObject(_propManager);
    pOut.writeObject(_varENGrepo);
    // TK47050
    pOut.writeObject(_name);
    pOut.writeObject(_domain);
    pOut.writeObject(_alternativeDomain);
  }

  /**
   * Surcharge de la sérialisation Java pour le mode multithread
   * @param pIn : le stream pour la lecture de l'objet
   * @throws IOException exception
   * @throws ClassNotFoundException exception
   */
  private void readObject(java.io.ObjectInputStream pIn) throws IOException, ClassNotFoundException
  {
    _propManager = (PropertiesManager)pIn.readObject();
    _varENGrepo = (VariablesRepository)pIn.readObject();
    // TK47050
    _name = (String)pIn.readObject();
    _domain = (String)pIn.readObject();
    _alternativeDomain = (String)pIn.readObject();
  }
}
