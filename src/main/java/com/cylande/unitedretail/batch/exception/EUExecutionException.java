package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.AbstractException;
import com.cylande.unitedretail.framework.exception.ErrorDetail;

/**
 * Exception représentant une erreur lors de l'éxécution d'une unité d'éxécution (batch ou tache)
 */
public class EUExecutionException extends AbstractException
{
  private int _sysId;

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   */
  public EUExecutionException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pCause : la cause d'origine de cette exception
   */
  public EUExecutionException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les paramètres décrivant le contexte de l'exception
   * @param pCause : la cause d'origine de cette exception
   */
  public EUExecutionException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les paramètres décrivant le contexte de l'exception
   */
  public EUExecutionException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }

  public int getSysId()
  {
    return _sysId;
  }

  public void setSysId(int pSysId)
  {
    _sysId = pSysId;
  }
}
