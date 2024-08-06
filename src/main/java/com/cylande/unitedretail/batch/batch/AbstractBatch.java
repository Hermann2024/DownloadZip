package com.cylande.unitedretail.batch.batch;

import java.util.Calendar;
import java.util.Enumeration;

import oracle.jbo.common.ampool.ApplicationPool;
import oracle.jbo.common.ampool.PoolMgr;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.execution.EUState;
import com.cylande.unitedretail.batch.execution.ExecutionUnit;
import com.cylande.unitedretail.batch.repository.BatchPropertiesRepository;
import com.cylande.unitedretail.batch.service.BatchRunManagerServiceDelegate;
import com.cylande.unitedretail.batch.task.TaskIntegrationDispatchImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.enginevariables.VariableType;
import com.cylande.unitedretail.process.repository.VariablesProcessGlobalRepository;

/**
 * Abstract batch class
 */
public abstract class AbstractBatch extends ExecutionUnit
{
  /**  Nom de clef du batch ID dans les paramètres systèmes (SysRepo) */
  public static final String SYS_ID_KEY_NAME = "currentBatchId";

  /**  Nom de clef du batch path dans les paramètres systèmes (SysRepo) */
  public static final String SYS_PATH_KEY_NAME = "currentBatchPath";

  /**  Nom de clef de l'heure de démarrage du batch dans les paramètres systèmes (SysRepo) */
  public static final String SYS_START_TIME_KEY_NAME = "currentBatchStartTime";

  /** identifie la classe pour les logs */
  private static final String DEBUG_CLASS_INFO = " [Abstract Batch] ";

  /** la definition du batch */
  protected BatchType _batchDefinition;

  /** le service CRUD permettant de tracer les batch */
  private transient BatchRunManagerServiceDelegate _batchRunManagerService = null;

  /** la valeur courante de batchRun */
  private BatchRunType _currentRunDef = null;

  /** Dispatcher */
  private TaskIntegrationDispatchImpl _dispatcher = null;

  /**
   * Constructeur
   * @param pParent : batch parent si existant
   * @param pBatchDef : la définition du batch
   */
  protected AbstractBatch(AbstractBatch pParent, BatchType pBatchDef)
  {
    super(pParent, pBatchDef.getName());
    _batchDefinition = pBatchDef;
    _propManager.setPropGLOrepo(BatchPropertiesRepository.getInstance());
  }

  /**
   * Constructeur
   * @param pParent : batch parent si existant
   * @param pBatchDef : la définition du batch
   * @param pId : l'Id de cette instance de batch
   */
  protected AbstractBatch(AbstractBatch pParent, BatchType pBatchDef, Integer pId)
  {
    super(pParent, pBatchDef.getName(), pId);
    _batchDefinition = pBatchDef;
    _propManager.setPropGLOrepo(BatchPropertiesRepository.getInstance());
  }

  /**
   * Met à jours les info via le service batch run manager
   * @throws TechnicalServiceException : Exception technique lors de l'enregistrement
   * @throws ServiceException          : Exception du service d'enregistrement.
   */
  private void updateBatchRun() throws TechnicalServiceException, ServiceException
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " update batch run - START");
    boolean bCreate = false;
    if (_currentRunDef == null)
    {
      bCreate = true;
      _currentRunDef = new BatchRunType();
    }
    if (_batchRunManagerService == null)
    {
      _batchRunManagerService = new BatchRunManagerServiceDelegate();
    }
    _currentRunDef.setId(getSysId());
    _currentRunDef.setPath(getSysPath());
    _currentRunDef.setSite(getSiteKey());
    _currentRunDef.setParentId(getParentId());
    _currentRunDef.setStartTime(getSysStartTime());
    _currentRunDef.setEndTime(getEndTime());
    _currentRunDef.setStatus(((getState() == EUState.FINALIZING)) || (getState() == EUState.ENDED));
    _currentRunDef.setDomain(getDomain());
    _currentRunDef.setBatchType(_batchDefinition.getType().value());
    _currentRunDef.setMode(getBatchMode());
    if (bCreate)
    {
      _currentRunDef = _batchRunManagerService.createBatchRun(_currentRunDef, new BatchRunScenarioType(), ContextTransformer.fromLocale());
    }
    else
    {
      if (getException() != null)
      {
        _currentRunDef.setInError(Boolean.TRUE);
      }
      _currentRunDef = _batchRunManagerService.updateBatchRun(_currentRunDef, new BatchRunScenarioType(), ContextTransformer.fromLocale());
    }
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " update batch run - END");
  }

  /**
   * Renvoie la chaîne de caractères correspondant au mode d'exécution du moteur de batch
   * @return résultat
   */
  private String getBatchMode()
  {
    if (_batchDefinition.getFork() != null)
    {
      return "fork";
    }
    if (_batchDefinition.getSequence() != null)
    {
      return "sequence";
    }
    return null;
  }

  /**
   * Avant exécution : enregistrement du batch
   * @throws BatchException en cas d'erreur de référencement du batch
   */
  protected void before() throws BatchException
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " before - START");
    try
    {
      logInfo("START", "");
      updateBatchRun();
      setSysId(_currentRunDef.getId());
      if (_batchDefinition.getVariables() != null)
      {
        // permet de rendre accessible aux sous tasks les variables définies sur le batch
        for (VariableType var: _batchDefinition.getVariables().getVariable())
        {
          // les variables de batch n'existent que pendant le temps d'exécution du batch
          VariablesProcessGlobalRepository.getInstance().putVariable("#" + getSysId() + var.getName(), var);
        }
      }
    }
    catch (Exception e)
    {
      Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " before - FAILED");
      throw new BatchException(BatchErrorDetail.BATCHRUN_BEFORE_ERR, new Object[] { getSysPath(), getParentId() }, e);
    }
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " before - END");
  }

  /** {@inheritDoc} */
  protected void after()
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " after - START");
    try
    {
      if (_batchDefinition.getVariables() != null)
      {
        // purge des variables de batch
        for (VariableType var: _batchDefinition.getVariables().getVariable())
        {
          VariablesProcessGlobalRepository.getInstance().remove("#" + getSysId() + var.getName());
        }
      }
      _currentRunDef.setStatus(true);
      updateBatchRun();
      logConnectionLeak();
      EUJobManager.STOPPING_BATCH.remove(getSysId());
    }
    catch (Exception e)
    {
      new BatchException(BatchErrorDetail.BATCHRUN_AFTER_ERR, new Object[] { getSysPath(), getSysId() }, e).log();
    }
    finally
    {
      logInfo("END", getException());
      Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " after - END");
    }
  }

  private void logConnectionLeak()
  {
    if (Logger.getLogger(AbstractBatch.class).getEffectiveLevel().equals(Level.DEBUG))
    {
      Enumeration pools = PoolMgr.getInstance().getResourcePools();
      if (pools != null)
      {
        while (pools.hasMoreElements())
        {
          ApplicationPool pool = (ApplicationPool)pools.nextElement();
          if (pool != null)
          {
            long checkIn = pool.getStatistics().mNumOfCheckins;
            long checkOut = pool.getStatistics().mNumOfCheckouts;
            if ((getParentId() == -1 && checkIn != checkOut) || (checkOut - checkIn) > 1)
            {
              Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + "CONNECTION LEAK ON APPLICATION POOL " + pool.getName() + " (Number of CHECKIN / CHECKOUT : " + checkIn + " / " + checkOut + ")");
            }
          }
        }
      }
    }
  }

  /**
   * Libère les ressources et réinitialise pour une future execution
   */
  protected void releaseResources()
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " releaseResources");
    _batchRunManagerService = null;
    _currentRunDef = null;
  }

  /**
   * Setter interne : Id système courant (met à jour SysRepo)
   * @param pValue : le nouvel Id
   */
  protected void setSysId(Integer pValue)
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " set Sys Id (" + pValue + ")");
    if (pValue != null)
    {
      _propManager.putSysObject(SYS_ID_KEY_NAME, pValue);
      if (_isRoot)
      {
        _propManager.putSysObject(SYS_ROOT_ID_KEY_NAME, pValue);
      }
      _debugContextInfo = getSysPath() + '(' + pValue + ')';
    }
  }

  /**
   * Setter interne : Path courant dans le SYSRepo
   * @param pValue le nouveau Path
   */
  protected void setSysPath(String pValue)
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " set Sys Path (" + pValue + ")");
    if (pValue != null)
    {
      _propManager.putSysObject(SYS_PATH_KEY_NAME, pValue);
      if (_isRoot)
      {
        _propManager.putSysObject(SYS_ROOT_PATH_KEY_NAME, pValue);
      }
    }
  }

  /** {@inheritDoc} */
  protected void setSysStartTime(Calendar pValue)
  {
    Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " set Sys Start Time (" + pValue + ")");
    if (pValue != null)
    {
      _propManager.putSysObject(SYS_START_TIME_KEY_NAME, pValue);
      if (_isRoot)
      {
        _propManager.putSysObject(SYS_ROOT_START_TIME_KEY_NAME, pValue);
        if (_batchDefinition.getTimeOut() != null)
        {
          _propManager.putSysObject(SYS_ROOT_TIMEOUT_KEY_NAME, _batchDefinition.getTimeOut());
        }
      }
    }
  }

  /**
   * Retourne l'Id courant du batch (passé en paramètre du constructeur ou obtenu au lancement du batch)
   * @return Id : l'Id (Integer)
   */
  public Integer getSysId()
  {
    Integer result = (Integer)_propManager.getSysObject(SYS_ID_KEY_NAME, -1);
    //Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " get Sys Id (" + result + ")");
    return result;
  }

  /**
   * Retourne le path courant du batch (nom complet dans l'arborescence)
   * @return le path
   */
  public String getSysPath()
  {
    String result = (String)_propManager.getSysObject(SYS_PATH_KEY_NAME, "");
    //Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " get Sys Path (" + result + ")");
    return result;
  }

  /** {@inheritDoc} */
  public Calendar getSysStartTime()
  {
    Calendar result = (Calendar)_propManager.getSysObject(SYS_START_TIME_KEY_NAME, null);
    //Logger.getLogger(AbstractBatch.class).debug(DEBUG_CLASS_INFO + _debugContextInfo + " get Sys Start Time (" + result + ")");
    return result;
  }

  public TaskIntegrationDispatchImpl getDispatcher()
  {
    return _dispatcher;
  }

  public void setDispatcher(TaskIntegrationDispatchImpl pDispatcher)
  {
    _dispatcher = pDispatcher;
  }

  public BatchType getBatchDefinition()
  {
    return _batchDefinition;
  }
}
