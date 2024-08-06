package com.cylande.unitedretail.batch.business.query;

import java.util.Iterator;

import oracle.jbo.JboException;
import oracle.jbo.Key;
import oracle.jbo.Row;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.business.entity.BatchRunImpl;
import com.cylande.unitedretail.batch.business.query.common.BatchRunView;
import com.cylande.unitedretail.batch.business.query.common.BatchRunViewRow;
import com.cylande.unitedretail.common.transformer.CriteriaTransformer;
import com.cylande.unitedretail.framework.business.jbo.server.ViewObjectImpl;
import com.cylande.unitedretail.message.batch.BatchCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.common.criteria.CriteriaBooleanType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.message.sequence.SequenceKeyType;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class BatchRunViewImpl extends ViewObjectImpl implements BatchRunView
{
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(BatchRunViewImpl.class);

  /**This is the default constructor (do not remove)
   */
  public BatchRunViewImpl()
  {
  }

  /**
   * GetRow
   * @param pKey Key.
   * @return BatchRunViewRow
   */
  public BatchRunViewRow getRow(BatchRunKeyType pKey)
  {
    if (pKey == null)
    {
      return null;
    }
    return getRowByBatchId(pKey.getPath(), pKey.getId(), pKey.getSite());
  }

  /**
   * Recherche par Id
   * @param pPath Path du batch
   * @param pId Id du batch
   * @param pSite Site associ� au batch
   * @return Row
   */
  public BatchRunViewRow getRowByBatchId(String pPath, Integer pId, SiteKeyType pSite)
  {
    BatchRunViewRow result = null;
    if (pId != null)
    {
      Key key = BatchRunImpl.createPrimaryKey(new com.cylande.unitedretail.framework.business.jbo.domain.common.Integer(pId));
      Row[] rows = findByKey(key, -1);
      int recordCount = rows.length;
      if (recordCount == 1)
      {
        result = (BatchRunViewRow)rows[0];
      }
      else
      {
        LOGGER.info("getRowByBatchId : " + pPath + "/" + pId + " : " + recordCount);
      }
    }
    else
    {
      LOGGER.warn("getRowByBatchId");
    }
    return result;
  }

  /**
   * add criteria to criteriaTransformer to build where clause
   * @param pCriteriaTransformer
   * @param pCriteria
   */
  private void addCriteriaToTransformer(CriteriaTransformer pCriteriaTransformer, BatchRunCriteriaType pCriteria)
  {
    if (pCriteria.getPath() != null)
    {
      pCriteriaTransformer.addElement("BATCH_CODE", pCriteria.getPath());
    }
    if (pCriteria.getId() != null)
    {
      pCriteriaTransformer.addElement("ID", pCriteria.getId());
    }
    if (pCriteria.getSite() != null && pCriteria.getSite().getCode().length() > 0)
    {
      pCriteriaTransformer.addElement("SITE_CODE", pCriteria.getSite().getCode());
    }
    if (pCriteria.getParentId() != null)
    {
      pCriteriaTransformer.addElement("PARENT_ID", pCriteria.getParentId());
    }
    if (pCriteria.getStartDate() != null)
    {
      pCriteriaTransformer.addElement("START_TIME", pCriteria.getStartDate());
    }
    if (pCriteria.getEndDate() != null)
    {
      pCriteriaTransformer.addElement("END_TIME", pCriteria.getEndDate());
    }
    if (pCriteria.getCompleted() != null)
    {
      pCriteriaTransformer.addElement("STATE", pCriteria.getCompleted());
    }
    if (pCriteria.getDomain() != null && pCriteria.getDomain().getEquals() != null && pCriteria.getDomain().getEquals().length() > 0)
    {
      pCriteriaTransformer.addElement("DOMAIN", pCriteria.getDomain());
    }
    pCriteriaTransformer.addElement("IN_ERROR", pCriteria.getInError());
    pCriteriaTransformer.closeCurrentCriteria();
  }

  /**
   * Recherche par crit�res
   * @param pCriterias Crit�re.
   */
  public void findByCriterias(BatchRunCriteriaListType pCriterias)
  {
    if (pCriterias != null)
    {
      setWhereClause(null);
      setWhereClauseParams(null);
      CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
      Iterator<BatchRunCriteriaType> it = pCriterias.getList().iterator();
      while (it.hasNext())
      {
        BatchRunCriteriaType batchRunCriteria = it.next();
        addCriteriaToTransformer(criteriaTransformer, batchRunCriteria);
      }
      if (pCriterias.getParameter() != null)
      {
        if (pCriterias.getParameter().getSort() != null)
        {
          setOrderByClause(pCriterias.getParameter().getSort());
        }
        if (pCriterias.getParameter().getSize() != null)
        {
          setMaxFetchSize(pCriterias.getParameter().getSize());
        }
      }
      setWhereClause(criteriaTransformer.getWhereClause());
      setWhereClauseParams(criteriaTransformer.getParams());
    }
    executeQuery();
  }

  /**
   * recherche les batchrun correspondants au crit�re sur path et les trie en commencant par le batch execut� le plus recemment
   * @param pCriteria
   * @deprecated
   */
  public void findLastActivationOfBatch(BatchCriteriaType pCriteria)
  {
    if (pCriteria == null || pCriteria.getName() == null)
    {
      return;
    }
    setWhereClause(null);
    setWhereClauseParams(null);
    CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
    criteriaTransformer.addElement("BATCH_CODE", pCriteria.getName());
    if (pCriteria.getSite() != null && pCriteria.getSite().getCode().length() > 0)
    {
      criteriaTransformer.addElement("SITE_CODE", pCriteria.getSite().getCode());
    }
    if (pCriteria.getDomain() != null && pCriteria.getDomain().length() > 0)
    {
      criteriaTransformer.addElement("DOMAIN", pCriteria.getDomain());
    }
    criteriaTransformer.closeCurrentCriteria();
    setWhereClause(criteriaTransformer.getWhereClause());
    setWhereClauseParams(criteriaTransformer.getParams());
    setOrderByClause("START_TIME DESC");
    executeQuery();
  }

  /**
   * recherche les batchrun correspondants au crit�res et les trie en commencant par le batch execut� le plus recemment
   * @param pCriteria
   */
  public void findLastActivationOfBatch(BatchRunCriteriaType pCriteria)
  {
    if (pCriteria == null || pCriteria.getPath() == null)
    {
      return;
    }
    setWhereClause(null);
    setWhereClauseParams(null);
    CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
    addCriteriaToTransformer(criteriaTransformer, pCriteria);
    setWhereClause(criteriaTransformer.getWhereClause());
    setWhereClauseParams(criteriaTransformer.getParams());
    setOrderByClause("START_TIME DESC");
    executeQuery();
  }

  /**
   * Contr�le si au moins un des run correspondants au crit�res ne s'est pas termin�
   * @param pCriteria
   * @return r�sultat
   */
  public boolean atLeastInstanceIsRunning(BatchRunCriteriaType pCriteria)
  {
    if (pCriteria == null)
    {
      return false;
    }
    setWhereClause(null);
    setWhereClauseParams(null);
    CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
    addCriteriaToTransformer(criteriaTransformer, pCriteria);
    setWhereClause(criteriaTransformer.getWhereClause());
    setWhereClauseParams(criteriaTransformer.getParams());
    long count = getEstimatedRowCount();
    if (count > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * Contr�le si au moins un des run correspondants au crit�re sur path et en cours d'ex�cution
   * @param pCriteria
   * @return r�sultat
   * @deprecated
   */
  public boolean atLeastInstanceIsRunning(BatchCriteriaType pCriteria)
  {
    if (pCriteria == null)
    {
      return false;
    }
    setWhereClause(null);
    setWhereClauseParams(null);
    CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
    criteriaTransformer.addElement("BATCH_CODE", pCriteria.getName());
    if (pCriteria.getSite() != null && pCriteria.getSite().getCode().length() > 0)
    {
      criteriaTransformer.addElement("SITE_CODE", pCriteria.getSite().getCode());
    }
    CriteriaBooleanType critBoolState = new CriteriaBooleanType();
    critBoolState.setEquals(false);
    criteriaTransformer.addElement("STATE", critBoolState);
    criteriaTransformer.closeCurrentCriteria();
    setWhereClause(criteriaTransformer.getWhereClause());
    setWhereClauseParams(criteriaTransformer.getParams());
    long count = getEstimatedRowCount();
    if (count > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * Insert Row
   * @param pRow Row
   */
  public void insertRow(BatchRunViewRow pRow)
  {
    try
    {
      SequenceKeyType key = new SequenceKeyType();
      key.setCode(getName());
      if (pRow.getId() == null || pRow.getId().intValue() < 0)
      {
        insertSequenceId(pRow, new BatchRunSequenceHandler(pRow, key));
      }
      else
      {
        insertSequenceId(pRow, new BatchRunSequenceHandler(pRow, key, pRow.getId().intValue()));
      }
    }
    catch (Exception e)
    {
      LOGGER.error(e.getMessage(), e);
      throw new JboException(e);
    }
  }
}
