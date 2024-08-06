package com.cylande.unitedretail.batch.business.query;

import java.util.concurrent.atomic.AtomicBoolean;

import oracle.jbo.JboException;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.business.query.common.BatchRunViewRow;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.common.tools.SequenceValueSingleton;
import com.cylande.unitedretail.common.utils.SequenceUtils;
import com.cylande.unitedretail.framework.business.jbo.server.SequenceHandler;
import com.cylande.unitedretail.message.sequence.SequenceKeyType;
import com.cylande.unitedretail.message.sequence.SequenceResultType;

public class BatchRunSequenceHandler implements SequenceHandler<BatchRunViewRow>
{
  private static final Logger LOGGER = Logger.getLogger(BatchRunSequenceHandler.class);
  private static AtomicBoolean _fullSequence = new AtomicBoolean(false);
  private BatchRunViewRow _row;
  private SequenceKeyType _key;
  private SequenceResultType _sequenceResult;
  private Integer _initId;

  public BatchRunSequenceHandler(BatchRunViewRow pRow, SequenceKeyType pKey)
  {
    _row = pRow;
    _key = pKey;
    pKey.setCode(pKey.getCode().toUpperCase());
    _initId = null;
  }

  /**
   * Contructeur pour g�rer le cas particulier o� l'identifiant est fourni pr�alablement sur la cr�ation d'un batch_run lors du lancement d'un batch
   * @param pRow row
   * @param pKey cl� de s�quence
   * @param pInitId identifiant d'initialisation
   */
  public BatchRunSequenceHandler(BatchRunViewRow pRow, SequenceKeyType pKey, Integer pInitId)
  {
    _row = pRow;
    _key = pKey;
    pKey.setCode(pKey.getCode().toUpperCase());
    _initId = pInitId;
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

  /**
   * lors du 1er appel � nextValue, la s�quence est initialis�e avec l'identifiant d'initialisation
   */
  public void nextValue()
  {
    if (_initId != null)
    {
      _sequenceResult = new SequenceResultType();
      _sequenceResult.setIntValue(_initId);
      _initId = null;
    }
    else
    {
      _sequenceResult = SequenceValueSingleton.getInstance().nextValue(_key, null);
    }
  }

  /** {@inheritDoc} */
  public void setId(BatchRunViewRow pRow)
  {
    pRow.setId(SequenceUtils.createIdFromSequence(_sequenceResult));
  }

  public boolean isMaxValueReached(BatchRunViewRow pRow)
  {
    return _sequenceResult.getIntValue().intValue() < pRow.getId().intValue();
  }

  /** {@inheritDoc} */
  public Exception getFullSequenceException()
  {
    return new BatchException(BatchErrorDetail.BATCHRUN_FULL_SEQUENCE);
  }

  /** {@inheritDoc} */
  public void log(JboException pException)
  {
    LOGGER.info(pException.toString() + " : BatchRun " + _row.getId());
  }
}
