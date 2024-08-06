package com.cylande.unitedretail.batch.scheduler;

import com.cylande.unitedretail.batch.batch.BatchBuilder;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.execution.ExecutionUnit;
import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.common.tools.SiteUtils;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.Property;
import com.cylande.unitedretail.process.tools.VariablesRepository;

import java.rmi.RemoteException;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Permet de lancer une instance de batch synchrone
 */
public class SynchronousBatch
{
  public String _instanceId = null;
  public String _user = "Unknown";
  private String _batchName = null;
  private VariablesRepository _varEngRepo = null;
  private PropertiesRepository _propEngRepo = null;
  private String _activedContext = null;
  private String _alternativeDomain = null;
  private BatchManagerService _batchManager = null;

  /**
   * Constructor
   */
  public SynchronousBatch()
  {
    _batchManager = new BatchManagerServiceImpl();
  }

  /**
   * Construit une Unité d'exécution associée au batch
   * @return résultat
   * @throws EUBuildException exception
   */
  private ExecutionUnit buildBatchRoot() throws EUBuildException
  {
    // construction du batchRoot
    ExecutionUnit result = BatchBuilder.buildRoot(_batchName, _activedContext, _alternativeDomain, _varEngRepo, _propEngRepo);
    result.setSiteKey(SiteUtils.getLocalSite());
    result.setUeJobManager(new EUJobManager());
    return result;
  }

  /**
   * Execute le batch de manière synchrone
   * @return le numéro d'instance
   * @throws EUBuildException exception
   * @throws EUExecutionException exception
   */
  public String execute() throws EUBuildException, EUExecutionException
  {
    BatchType batchDef = null;
    try
    {
      batchDef = getBatch(_batchName);
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.ENGINE_EXEC_GETBATCH_ERROR, new Object[] { _batchName }, e);
    }
    if (batchDef == null)
    {
      throw new BatchException(BatchErrorDetail.ENGINE_EXEC_BATCH_NOTFOUND, new Object[] { _batchName });
    }
    ExecutionUnit batchImpl = buildBatchRoot();
    batchImpl.run();
    if (batchImpl.hasException())
    {
      batchImpl.getException().setSysId(batchImpl.getSysId());
      throw batchImpl.getException();
    }
    return batchImpl.getSysId().toString();
  }

  /**
   * Récupère une définition de batch à partir de son nom
   * @param pBatchName
   * @return résultat
   * @throws RemoteException exception
   */
  private BatchType getBatch(String pBatchName) throws RemoteException, ServiceException
  {
    BatchType batch = null;
    if (pBatchName != null)
    {
      BatchKeyType key = new BatchKeyType();
      key.setName(pBatchName);
      batch = _batchManager.getBatch(key, null, ContextTransformer.fromLocale());
    }
    return batch;
  }

  /**
   * Positionne un utilisateur sur l'unité d'execution associée au batch à executer
   * @param pUser
   */
  public void setUser(String pUser)
  {
    if (pUser == null)
    {
      _user = ExecutionUnit.UNKNOWN_USER;
    }
    else
    {
      _user = pUser;
    }
  }

  /**
   * spécifie le nom du batch à exécuter
   * @param pBatchName
   */
  public void setBatchNameToExe(String pBatchName)
  {
    _batchName = pBatchName;
  }

  /**
   * spécifie la repository de variables qui sera utilisée lors de l'exécution du batch
   * @param pVarEngRepo
   */
  public void setVarEngRepo(VariablesRepository pVarEngRepo)
  {
    _varEngRepo = pVarEngRepo;
  }

  /**
   * spécifie la repository de properties qui sera utilisée lors de l'exécution du batch
   * @param pPropEngRepo
   */
  public void setPropEngRepo(PropertiesRepository pPropEngRepo)
  {
    _propEngRepo = pPropEngRepo;
  }

  /**
   * positionne le domaine qui sera activé lors de l'exécution du batch synchrone
   * @param pActivedContext
   */
  public void setActivedContext(String pActivedContext)
  {
    _activedContext = pActivedContext;
  }

  /**
   * Mutateur sur alternativeDomain
   * @param pAlternativeDomain
   */
  public void setAlternativeDomain(String pAlternativeDomain)
  {
    _alternativeDomain = pAlternativeDomain;
  }

  /**
   * ajoute une Engine Property
   * @param pKey
   * @param pPropDomain
   * @param pValue
   */
  public void putEngineProperty(String pKey, String pPropDomain, String pValue)
  {
    if (_propEngRepo != null)
    {
      Property prop = new Property(pValue);
      _propEngRepo.putProperty(pKey, pPropDomain, prop);
    }
  }

  /**
   * ajoute une liste d'Engine Property
   * @param pPropDomain
   * @param pValues
   */
  public void putEngineProperties(String pPropDomain, Map<String, String> pValues)
  {
    if (_propEngRepo != null && pValues != null && !pValues.isEmpty())
    {
      for (Entry<String, String> entry: pValues.entrySet())
      {
        putEngineProperty(entry.getKey(), pPropDomain, entry.getValue());
      }
    }
  }
}
