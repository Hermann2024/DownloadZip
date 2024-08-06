package com.cylande.unitedretail.batch.business.query;

import oracle.jbo.Key;
import oracle.jbo.Row;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.business.entity.TaskRunImpl;
import com.cylande.unitedretail.batch.business.query.common.TaskRunView;
import com.cylande.unitedretail.batch.business.query.common.TaskRunViewRow;
import com.cylande.unitedretail.common.transformer.CriteriaTransformer;
import com.cylande.unitedretail.framework.business.BusinessException;
import com.cylande.unitedretail.framework.business.jbo.server.ViewObjectImpl;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaType;
import com.cylande.unitedretail.message.batch.TaskRunKeyType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.message.sequence.SequenceKeyType;

public class TaskRunViewImpl extends ViewObjectImpl implements TaskRunView
{
  /**
   * Logger
   */
  private static final Logger LOGGER = Logger.getLogger(TaskRunViewImpl.class);

  /**
   * This is the default constructor (do not remove)
   */
  public TaskRunViewImpl()
  {
  }

  /**
   * getRow
   *
   * @param pKey
   * @return TaskRunViewRow
   */
  public TaskRunViewRow getRow(TaskRunKeyType pKey)
  {
    TaskRunViewRow result = null;
    if (pKey != null)
    {
      return getRowByTaskId(pKey.getPath(), pKey.getId(), pKey.getSite());
    }
    return result;
  }

  /**
   * getRowByTaskId
   *
   * @param pPath
   * @param pId
   * @param pSite
   * @return TaskRunViewRow
   */
  public TaskRunViewRow getRowByTaskId(String pPath, Integer pId, SiteKeyType pSite)
  {
    TaskRunViewRow result = null;
    if (pId != null)
    {
      Key key = TaskRunImpl.createPrimaryKey(new com.cylande.unitedretail.framework.business.jbo.domain.common.Integer(pId));
      Row[] rows = findByKey(key, -1);
      int recordCount = rows.length;
      if (recordCount == 1)
      {
        result = (TaskRunViewRow)rows[0];
      }
      else
      {
        LOGGER.info("getRowByTaskId : " + pPath + "/" + pId + "/" + pSite.getCode() + " : " + recordCount);
      }
    }
    else
    {
      LOGGER.warn("getRowByTaskId");
    }
    return result;
  }

  /**
   * findByCriterias
   *
   * @param pCriterias
   */
  public void findByCriterias(TaskRunCriteriaListType pCriterias)
  {
    if (pCriterias != null)
    {
      setWhereClause(null);
      setWhereClauseParams(null);
      CriteriaTransformer criteriaTransformer = CriteriaTransformer.createCriteriaWhereClause(this);
      for (TaskRunCriteriaType taskRunCriteria: pCriterias.getList())
      {
        criteriaTransformer.addElement("TASK_CODE", taskRunCriteria.getPath());
        criteriaTransformer.addElement("ID", taskRunCriteria.getId());
        criteriaTransformer.addElement("PARENT_ID", taskRunCriteria.getParentId());
        criteriaTransformer.addElement("DOMAIN", taskRunCriteria.getDomain());
        criteriaTransformer.addElement("START_TIME", taskRunCriteria.getStartDate());
        criteriaTransformer.addElement("END_TIME", taskRunCriteria.getEndDate());
        if (taskRunCriteria.getSite() != null && taskRunCriteria.getSite().getCode() != null)
        {
          criteriaTransformer.addElement("SITE_CODE", taskRunCriteria.getSite().getCode());
        }
        if (taskRunCriteria.getCompleted() != null)
        {
          if (Boolean.TRUE.equals(taskRunCriteria.getCompleted().getEquals()))
          {
            criteriaTransformer.addElement("STEP", "complete");
          }
          else if (Boolean.FALSE.equals(taskRunCriteria.getCompleted().getEquals()))
          {
            CriteriaStringType crit = new CriteriaStringType();
            crit.setNotEquals("complete");
            criteriaTransformer.addElement("STEP", crit);
            CriteriaStringType crit2 = new CriteriaStringType();
            crit2.setNotEquals("failed");
            criteriaTransformer.addElement("STEP", crit2);
          }
        }
        if (taskRunCriteria.getInError() != null && Boolean.TRUE.equals(taskRunCriteria.getInError().getEquals()))
        {
          criteriaTransformer.addElement("IN_ERROR", Integer.valueOf(1));
        }
        criteriaTransformer.closeCurrentCriteria();
      }
      setWhereClause(criteriaTransformer.getWhereClause());
      setWhereClauseParams(criteriaTransformer.getParams());
    }
    executeQuery();
  }

  /**
   * insertRow
   *
   * @param pRow
   */
  public void insertRow(TaskRunViewRow pRow)
  {
    if (pRow.getId() == null || pRow.getId().intValue() < 0)
    {
      SequenceKeyType key = new SequenceKeyType();
      key.setCode(getName());
      try
      {
        insertSequenceId(pRow, new TaskRunSequenceHandler(pRow, key));
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
