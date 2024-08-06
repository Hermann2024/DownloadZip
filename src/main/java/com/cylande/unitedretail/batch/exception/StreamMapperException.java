package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.AbstractException;
import com.cylande.unitedretail.framework.exception.ErrorDetail;

/**
 * StreamMapperException.
 */
public class StreamMapperException extends AbstractException
{

  /**
   * Default constructor
   */
  public StreamMapperException()
  {
    super();
  }

  /**
   * @param pErrorDetail
   */
  public StreamMapperException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  /**
   * @param pErrorDetail
   * @param pCause
   */
  public StreamMapperException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  /**
   * @param pErrorDetail
   * @param pParams
   * @param pCause
   */
  public StreamMapperException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  public StreamMapperException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
