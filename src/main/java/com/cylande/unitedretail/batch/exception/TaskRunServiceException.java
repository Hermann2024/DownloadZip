package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.exception.ErrorDetail;
import com.cylande.unitedretail.framework.service.BusinessServiceException;

import java.util.Locale;

public class TaskRunServiceException extends BusinessServiceException
{
  /**
   * Constructor
   * @param pErrorDetail Detail of this error (codes)
   * @parma locale Localization used for message translation
   */
  public TaskRunServiceException(ErrorDetail pErrorDetail, Locale pLocale)
  {
    super(pErrorDetail, pLocale);
  }

  /**
   * Constructor
   * @param pErrorDetail Detail of this error (codes)
   * @param pCause The cause exception that will be encapsulated into this exception
   * @parma locale Localization used for message translation
   */
  public TaskRunServiceException(ErrorDetail pErrorDetail, Throwable pCause, Locale pLocale)
  {
    super(pErrorDetail, pCause, pLocale);
  }

  /**
   * Constructor
   * @param pErrorDetail Detail of this error (codes)
   * @param pParams An array of parameters to bind into the message associated with the code of this exception
   * @param pCause The cause exception that will be encapsulated into this exception
   * @parma locale Localization used for message translation
   */
  public TaskRunServiceException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause, Locale pLocale)
  {
    super(pErrorDetail, pParams, pCause, pLocale);
  }

  /**
   * Constructor
   * @param pErrorDetail Detail of this error (codes)
   * @param pParams An array of parameters to bind into the message associated with the code of this exception
   * @parma locale Localization used for message translation
   */
  public TaskRunServiceException(ErrorDetail pErrorDetail, Object[] pParams, Locale pLocale)
  {
    super(pErrorDetail, pParams, pLocale);
  }
}
