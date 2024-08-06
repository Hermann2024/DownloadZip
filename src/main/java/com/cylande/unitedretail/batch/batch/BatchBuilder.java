package com.cylande.unitedretail.batch.batch;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.common.tools.SequenceValueSingleton;
import com.cylande.unitedretail.framework.security.AuthenticationValidator;
import com.cylande.unitedretail.framework.security.Crypto;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.sequence.SequenceKeyType;
import com.cylande.unitedretail.message.sequence.SequenceResultType;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.PropertiesTools;
import com.cylande.unitedretail.process.tools.VariablesRepository;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

/**
 * Builder from builder and loader pattern
 * <br>Build a 'batch' with informations initialized by the BatchLoader
 */
public class BatchBuilder
{
  /** security loggueur*/
  private static final Logger LOGGER = Logger.getLogger("LOG_SECURITY");

  /** definition du batch */
  private BatchType _batchDef = null;

  /** fail on error flag */
  private Boolean _failOnError = null;

  /** parent batch */
  private AbstractBatch _parentBatch;

  /** executive domain */
  private String _domain = null;

  /** alternative domain */
  private String _alternativeDomain = null;

  /**
   * Default constructor
   */
  public BatchBuilder()
  {
  }

  /**
   * Set batch definition to build
   * @param pBatchBean : the batch definition
   * @throws EUBuildException if définition invalid.
   */
  public void setBatchDefinition(BatchType pBatchBean) throws EUBuildException
  {
    if (pBatchBean == null)
    {
      throw new EUBuildException(BatchErrorDetail.SETBATCHINFO_PARAM);
    }
    _batchDef = pBatchBean;
  }

  /**
   * Build a batch implementation
   * @param pId : the wanted id for this batch implementation (can be null).
   * @return an implementation of batch
   * @throws EUBuildException if batch contruction failed
   */
  public BatchImpl build(Integer pId) throws EUBuildException
  {
    if (_batchDef == null)
    {
      throw new EUBuildException(BatchErrorDetail.BATCHBUILDER_BATCHDEF_NODEF);
    }
    BatchImpl result = new BatchImpl(_parentBatch, _batchDef, pId);
    result.setFailOnError(_failOnError);
    result.setDomain(_domain);
    result.setAlternativeDomain(_alternativeDomain);
    String userLogin = null;
    Subject userSubject = null;
    // User spécifié pour le batch
    if (_batchDef.getUser() != null)
    {
      try
      {
        // récupérer le login
        userLogin = _batchDef.getUser().getLogin();
        userLogin = PropertiesTools.replaceProperties(userLogin, result.getPropManager(), result.getDomain(), result.getAlternativeDomain());
        // récupérer le mot de passe
        String userPassword = _batchDef.getUser().getPassword();
        userPassword = PropertiesTools.replaceProperties(userPassword, result.getPropManager(), result.getDomain(), result.getAlternativeDomain());
        userPassword = Crypto.decryptInfo(userPassword);
        // s'authentifier
        AuthenticationValidator authValidator = new AuthenticationValidator(userLogin, userPassword);
        userSubject = authValidator.getSubject();
        result.setUser(authValidator.getUserPrincipal().getName());
        result.setAuthenticatedSubject(userSubject);
        LOGGER.info("user " + userLogin + " authenticated for the batch " + result.getSysPath());
      }
      catch (Exception e)
      {
        LOGGER.warn("Exception on programatic authentification ");
        LOGGER.debug("Exception on programatic authentification cause :", e);
      }
    }
    return result;
  }

  /**
   * Create Root Batch
   * @param pBatchName    : the name of batch definition
   * @param pActiveDomain : the launch domain
   * @param pAlternativeDomain : the alternative Domain
   * @param pVarENGrepo   : the variable engine repository
   * @param pPropENGrepo  : the property engine repository
   * @return the new batch Impl
   * @throws EUBuildException : if batch contruction failed
   */
  public static BatchImpl buildRoot(String pBatchName, String pActiveDomain, String pAlternativeDomain, VariablesRepository pVarENGrepo, PropertiesRepository pPropENGrepo) throws EUBuildException
  {
    return buildRoot(pBatchName, pActiveDomain, pAlternativeDomain, null, pVarENGrepo, pPropENGrepo);
  }

  /**
   * Batch and task's objects execution building orchestration
   * @param pBatchName     : the name of batch definition
   * @param pActiveDomain  : the launch domain
   * @param pAlternativeDomain : the alternative Domain
   * @param pId            : the new batch Id
   * @param pVarENGrepo    : the variable engine repository
   * @param pPropENGrepo   : the property engine repository
   * @return the new batch implementation
   * @throws EUBuildException : if batch contruction failed
   */
  public static BatchImpl buildRoot(String pBatchName, String pActiveDomain, String pAlternativeDomain, Integer pId, VariablesRepository pVarENGrepo, PropertiesRepository pPropENGrepo) throws EUBuildException
  {
    // Build Batch
    BatchImpl result;
    BatchBuilder batchBuilder = new BatchBuilder();
    BatchLoader batchLoader = new BatchLoader(batchBuilder);
    batchLoader.loadRoot(pBatchName, pActiveDomain, pAlternativeDomain);
    result = batchBuilder.build(pId);
    result.setEngineProperties(pPropENGrepo);
    result.setEngineVariables(pVarENGrepo);
    return result;
  }

  /**
   * Build a child batch
   * @param pParentBatch   : parent batch
   * @param pBatchChildRef : the child batch reference
   * @param pId            : the new batch Id
   * @return the new batch implementation
   * @throws EUBuildException : if batch contruction failed
   */
  public static BatchImpl buildChild(AbstractBatch pParentBatch, BatchChildrenAbstractType pBatchChildRef, Integer pId) throws EUBuildException
  {
    BatchImpl result;
    BatchBuilder batchBuilder = new BatchBuilder();
    BatchLoader batchLoader = new BatchLoader(batchBuilder);
    batchLoader.loadChild(pParentBatch, pBatchChildRef);
    result = batchBuilder.build(pId);
    return result;
  }

  /**
   * Sets the Parent batch
   * @param pParentBatch for the batch to build
   */
  public void setParentBatch(AbstractBatch pParentBatch)
  {
    _parentBatch = pParentBatch;
  }

  /**
   * Sets the active domain
   * @param pActiveDomain : the active domain for the batch to build
   */
  public void setActiveDomain(String pActiveDomain)
  {
    _domain = pActiveDomain;
  }

  /**
   * Mutateur sur le domaine alternatif
   * @param pAlternativeDomain
   */
  public void setAlternativeDomain(String pAlternativeDomain)
  {
    _alternativeDomain = pAlternativeDomain;
  }

  /**
   * Sets the fail on error property for the batch to build
   * @param pFailOnError : true if we want that batch exit on first child execution error
   */
  public void setFailOnError(Boolean pFailOnError)
  {
    _failOnError = pFailOnError;
  }

  /**
   * Build a child batch
   * @param pBatchDef     : a batch Definition
   * @param pActiveDomain : the launch domain
   * @param pAlternativeDomain : the alternative Domain
   * @param pId           : the Id for new Batch
   * @param pParentBatch  : the parent batch
   * @param pFailOnError  : true if fail on error option is setted
   * @return a new batch implementation
   * @throws EUBuildException : if error during batch building
   */
  public BatchImpl buildBatch(BatchType pBatchDef, String pActiveDomain, String pAlternativeDomain, Integer pId, AbstractBatch pParentBatch, boolean pFailOnError) throws EUBuildException
  {
    BatchImpl result;
    setBatchDefinition(pBatchDef);
    setParentBatch(pParentBatch);
    setActiveDomain(pActiveDomain);
    setAlternativeDomain(pAlternativeDomain);
    setFailOnError(pFailOnError);
    result = build(pId);
    return result;
  }

  /**
   * Retrieve new batch Id
   * @return a new batch Id
   */
  public static Integer getNewId()
  {
    Integer result = null;
    SequenceKeyType key = new SequenceKeyType();
    key.setCode("BATCHRUNVIEW");
    try
    {
      SequenceResultType seqResult = SequenceValueSingleton.getInstance().nextValue(key, null);
      result = seqResult.getIntValue();
    }
    catch (Exception e)
    {
      result = null;
    }
    return result;
  }
}
