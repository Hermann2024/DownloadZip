package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.exception.ErrorDetail;

public class RemoteProviderProtocolException extends ProviderException
{
  public RemoteProviderProtocolException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  public RemoteProviderProtocolException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  public RemoteProviderProtocolException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  public RemoteProviderProtocolException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
