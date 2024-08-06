package com.cylande.unitedretail.batch.business.query;

import java.util.concurrent.atomic.AtomicBoolean;

import oracle.jbo.JboException;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.business.query.common.TaskAuditViewRow;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.common.tools.SequenceValueSingleton;
import com.cylande.unitedretail.common.utils.SequenceUtils;
import com.cylande.unitedretail.framework.business.jbo.server.SequenceHandler;
import com.cylande.unitedretail.message.sequence.SequenceKeyType;
import com.cylande.unitedretail.message.sequence.SequenceResultType;

public class TaskAuditSequenceHandler implements SequenceHandler<TaskAuditViewRow>
{
  private static final Logger LOGGER = Logger.getLogger(TaskAuditSequenceHandler.class);
  private static AtomicBoolean _fullSequence = new AtomicBoolean(false);
  private TaskAuditViewRow _row;
  private SequenceKeyType _key;
  private SequenceResultType _sequenceResult;

  public TaskAuditSequenceHandler(TaskAuditViewRow pRow, SequenceKeyType pKey)
  {
    _row = pRow;
    _key = pKey;
    pKey.setCode(pKey.getCode().toUpperCase());
  }

  /** {@inheritDoc} */
  public boolean getFullSequence()
  {
    return _fullSequence.get();
  }

  /** {@inheritDoc} */
  public void setFullSequence(boolean pValue)
  {
    _fullSequence.set(pValue);
  }

  /** {@inheritDoc} */
  public void nextValue()
  {
    _sequenceResult = SequenceValueSingleton.getInstance().nextValue(_key, null);
  }

  /** {@inheritDoc} */
  public void setId(TaskAuditViewRow pRow)
  {
    pRow.setId(SequenceUtils.createIdFromSequence(_sequenceResult));
  }

  /** {@inheritDoc} */
  public boolean isMaxValueReached(TaskAuditViewRow pRow)
  {
    return _sequenceResult.getIntValue().intValue() < pRow.getId().intValue();
  }

  /** {@inheritDoc} */
  public Exception getFullSequenceException()
  {
    return new BatchException(BatchErrorDetail.TASKAUDIT_FULL_SEQUENCE);
  }

  /** {@inheritDoc} */
  public void log(JboException pException)
  {
    LOGGER.info(pException.toString() + " : TaskAudit " + _row.getId());
  }
}
