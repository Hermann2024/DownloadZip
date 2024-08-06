package com.cylande.unitedretail.batch.batch;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchType;

/**
 * Loader from builder and loader pattern
 * <br>Load a 'batch' definition and initialize the BatchLoader
 */
public class BatchLoader
{
  /** the builder assaciated to this loader */
  private BatchBuilder _builder = null;

  /** the service to get acces to batch repository */
  private BatchManagerService _batchManager = new BatchManagerServiceImpl();

  /**
   * Constructor
   */
  public BatchLoader()
  {
    _batchManager = new BatchManagerServiceImpl();
  }

  /**
   * Constructor
   * @param pBuilder the builder to initialize
   * @throws EUBuildException  : if invalid builder setted
   */
  public BatchLoader(BatchBuilder pBuilder) throws EUBuildException
  {
    if (pBuilder == null)
    {
      throw new EUBuildException(BatchErrorDetail.BATCHLOADER_PARAM);
    }
    _builder = pBuilder;
  }

  /**
   * Load batch root
   * @param pBatchName         : the name of the batch to load
   * @param pActiveDomain      : the domain for the batch to load
   * @param pAlternativeDomain : the alternative domain
   * @throws EUBuildException  : if an error occure while retrieving batch définition
   */
  public void loadRoot(String pBatchName, String pActiveDomain, String pAlternativeDomain) throws EUBuildException
  {
    // load the definition from repository
    BatchType batchDef = loadBatchDef(pBatchName);
    // Initialize the builder
    _builder.setBatchDefinition(batchDef);
    _builder.setParentBatch(null);
    _builder.setActiveDomain(pActiveDomain);
    _builder.setAlternativeDomain(pAlternativeDomain);
    _builder.setFailOnError(false);
  }

  /**
   * Load a child batch
   * @param pParentBatch       : the parent batch that load this batch
   * @param pBatchChildRef     : the child batch reference (with launch options)
   * @throws EUBuildException  : if an error occure while retrieving batch définition
   */
  public void loadChild(AbstractBatch pParentBatch, BatchChildrenAbstractType pBatchChildRef) throws EUBuildException
  {
    if (_builder == null)
    {
      throw new EUBuildException(BatchErrorDetail.BATCHLOADER_BUILDER_NOTINIT, new Object[] { pBatchChildRef.getRef() });
    }
    String batchRef = pParentBatch.getFilteredString(pBatchChildRef.getRef());
    BatchType batchDef = loadBatchDef(batchRef);
    // Initialize the builder
    _builder.setBatchDefinition(batchDef);
    _builder.setParentBatch(pParentBatch);
    _builder.setActiveDomain(pBatchChildRef.getActiveDomain());
    _builder.setAlternativeDomain(pBatchChildRef.getDefaultDomain());
    _builder.setFailOnError(pBatchChildRef.getFailOnError());
  }

  /**
   * Load the batch definition
   * @param pBatchName : the name of the batch
   * @return the batch definition
   * @throws EUBuildException if an error occure while retrieving batch définition
   */
  public BatchType loadBatchDef(String pBatchName) throws EUBuildException
  {
    BatchType result = null;
    // REMARQUE : path chaine vide autorisé
    if (pBatchName == null)
    {
      throw new EUBuildException(BatchErrorDetail.BATCHLOADER_LOAD_PARAM);
    }
    try
    {
      // Load batch definition
      BatchKeyType batchKey = new BatchKeyType();
      batchKey.setName(pBatchName);
      result = _batchManager.getBatch(batchKey, null, ContextTransformer.fromLocale());
    }
    catch (Exception e)
    {
      throw new EUBuildException(BatchErrorDetail.BATCHLOADER_LOAD_ERR, new Object[] { pBatchName }, e);
    }
    if (result == null)
    {
      // le batch n'est pas défini
      throw new EUBuildException(BatchErrorDetail.BATCHLOADER_LOAD_NOTDEFFOUND, new Object[] { pBatchName });
    }
    return result;
  }
}
