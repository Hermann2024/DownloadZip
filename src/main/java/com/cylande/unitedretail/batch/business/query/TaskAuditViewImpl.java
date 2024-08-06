package com.cylande.unitedretail.batch.business.query;

import oracle.jbo.Key;
import oracle.jbo.Row;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.business.entity.TaskAuditImpl;
import com.cylande.unitedretail.batch.business.query.common.TaskAuditView;
import com.cylande.unitedretail.batch.business.query.common.TaskAuditViewRow;
import com.cylande.unitedretail.common.transformer.CriteriaTransformer;
import com.cylande.unitedretail.framework.business.BusinessException;
import com.cylande.unitedretail.framework.business.jbo.server.ViewObjectImpl;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaType;
import com.cylande.unitedretail.message.batch.TaskAuditKeyType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.sequence.SequenceKeyType;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class TaskAuditViewImpl extends ViewObjectImpl implements TaskAuditView
{
  private static final Logger LOGGER = Logger.getLogger(TaskAuditViewImpl.class);

  /**
   * This is the default constructor (do not remove)
   */
  public TaskAuditViewImpl()
  {
  }

  /**
   * getRow
   * @param pKey
   * @return TaskAuditViewRow
   */
  public TaskAuditViewRow getRow(TaskAuditKeyType pKey)
  {
    if (pKey != null)
    {
      return getRowById(pKey.getId());
    }
    return null;
  }

  /**
   * getRowByTaskId
   * @param pId
   * @return TaskAuditViewRow
   */
  public TaskAuditViewRow getRowById(Integer pId)
  {
    TaskAuditViewRow result = null;
    if (pId != null)
    {
      Key key = TaskAuditImpl.createPrimaryKey(new com.cylande.unitedretail.framework.business.jbo.domain.common.Integer(pId));
      Row[] rows = findByKey(key, -1);
      int recordCount = rows.length;
      if (recordCount == 1)
      {
        result = (TaskAuditViewRow)rows[0];
      }
      else
      {
        LOGGER.info("getRowById : " + pId + " : " + recordCount);
      }
    }
    else
    {
      LOGGER.warn("getRowById");
    }
    return result;
  }

  /**
   * findByCriterias
   * @param pCriterias
   */
  public void findByCriterias(TaskAuditCriteriaListType pCriterias)
  {
    if (pCriterias != null)
    {
      setWhereClause(null);
      setWhereClauseParams(null);
      CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
      for (TaskAuditCriteriaType taskAuditCriteria: pCriterias.getList())
      {
        criteriaTransformer.addElement("ID", taskAuditCriteria.getId());
        criteriaTransformer.addElement("TASK_CODE", taskAuditCriteria.getPath());
        criteriaTransformer.addElement("TASK", taskAuditCriteria.getTaskId());
        criteriaTransformer.addElement("EVENT_TIME", taskAuditCriteria.getEventTime());
        if (taskAuditCriteria.getSite() != null && taskAuditCriteria.getSite().getCode() != null)
        {
          criteriaTransformer.addElement("SITE_CODE", taskAuditCriteria.getSite().getCode());
        }
        criteriaTransformer.closeCurrentCriteria();
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

  public void findTaskAuditOfBatch(BatchRunCriteriaType pBatchRunCriteria)
  {
    if (pBatchRunCriteria != null)
    {
      setWhereClause(null);
      setWhereClauseParams(null);
      CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
      CriteriaStringType batchRunPathCriteria;
      CriteriaStringType taskAuditPathCriteria;
      batchRunPathCriteria = pBatchRunCriteria.getPath();
      taskAuditPathCriteria = new CriteriaStringType();
      if (batchRunPathCriteria.getEquals() != null)
      {
        taskAuditPathCriteria.setStartsWith(batchRunPathCriteria.getEquals());
      }
      if (batchRunPathCriteria.getContains() != null)
      {
        taskAuditPathCriteria.setContains(batchRunPathCriteria.getContains());
      }
      criteriaTransformer.addElement("TASK_CODE", taskAuditPathCriteria);
      if (pBatchRunCriteria.getSite() != null && pBatchRunCriteria.getSite().getCode() != null)
      {
        criteriaTransformer.addElement("SITE_CODE", pBatchRunCriteria.getSite().getCode());
      }
      if (pBatchRunCriteria.getStartDate() != null)
      {
        criteriaTransformer.addElement("CREATION_TIME", pBatchRunCriteria.getStartDate());
      }
      criteriaTransformer.closeCurrentCriteria();
      setWhereClause(criteriaTransformer.getWhereClause());
      setWhereClauseParams(criteriaTransformer.getParams());
    }
    executeQuery();
  }

  /**
   * atLeastFindResult
   * @param pCriterias
   * @return boolean
   */
  public boolean atLeastFindResult(TaskAuditCriteriaListType pCriterias)
  {
    long count = 0;
    if (pCriterias != null)
    {
      setWhereClause(null);
      setWhereClauseParams(null);
      CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
      for (TaskAuditCriteriaType taskAuditCriteria: pCriterias.getList())
      {
        criteriaTransformer.addElement("ID", taskAuditCriteria.getId());
        criteriaTransformer.addElement("TASK_CODE", taskAuditCriteria.getPath());
        criteriaTransformer.addElement("TASK", taskAuditCriteria.getTaskId());
        if (taskAuditCriteria.getSite() != null && taskAuditCriteria.getSite().getCode() != null)
        {
          criteriaTransformer.addElement("SITE_CODE", taskAuditCriteria.getSite().getCode());
        }
        criteriaTransformer.closeCurrentCriteria();
      }
      setWhereClause(criteriaTransformer.getWhereClause());
      setWhereClauseParams(criteriaTransformer.getParams());
      clearCache();
      count = getEstimatedRowCount();
    }
    if (count > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * insertRow
   * @param pRow
   */
  public void insertRow(TaskAuditViewRow pRow)
  {
    if (pRow.getId() == null || pRow.getId().intValue() == 0)
    {
      SequenceKeyType key = new SequenceKeyType();
      key.setCode(getName());
      try
      {
        insertSequenceId(pRow, new TaskAuditSequenceHandler(pRow, key));
      }
      catch (Exception e)
      {
        LOGGER.error(e.getMessage(), e);
        throw new BusinessException(500000, e.getMessage());
      }
    }
    else
    {
      super.insertRow(pRow);
    }
  }
}