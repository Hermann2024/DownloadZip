package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.exception.ErrorDetail;

/**
 * Exception repr�sentant une erreur lors de l'�x�cution d'un batch
 */
public class BatchException extends EUExecutionException
{
  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   */
  public BatchException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pCause : la cause d'origine de cette exception
   */
  public BatchException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les param�tres d�crivant le contexte de l'exception
   * @param pCause : la cause d'origine de cette exception
   */
  public BatchException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les param�tres d�crivant le contexte de l'exception
   */
  public BatchException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
