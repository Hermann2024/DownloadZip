package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.AbstractException;
import com.cylande.unitedretail.framework.exception.ErrorDetail;

public class StatusServiceException extends AbstractException
{
  public StatusServiceException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  public StatusServiceException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  public StatusServiceException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  public StatusServiceException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
