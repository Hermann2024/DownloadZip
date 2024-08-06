package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.AbstractException;
import com.cylande.unitedretail.framework.exception.ErrorDetail;

public class ProviderException extends AbstractException
{
  public ProviderException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  public ProviderException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  public ProviderException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  public ProviderException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
