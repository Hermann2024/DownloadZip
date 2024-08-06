package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.AbstractException;
import com.cylande.unitedretail.framework.exception.ErrorDetail;

/**
 * Exception représentant une erreur de lancement d'une unité d'éxécution (batch ou tache)
 */
public class EULaunchException extends AbstractException
{
  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   */
  public EULaunchException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pCause : la cause d'origine de cette exception
   */
  public EULaunchException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les paramètres décrivant le contexte de l'exception
   * @param pCause : la cause d'origine de cette exception
   */
  public EULaunchException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les paramètres décrivant le contexte de l'exception
   */
  public EULaunchException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
