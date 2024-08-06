package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.AbstractRuntimeException;
import com.cylande.unitedretail.framework.exception.ErrorDetail;

/**
 * Exception représentant une erreur lors de l'éxécution d'un batch
 */
public class FlatMapperXmlStreamException extends AbstractRuntimeException
{
  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   */
  public FlatMapperXmlStreamException(ErrorDetail pErrorDetail)
  {
    super(pErrorDetail);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pCause : la cause d'origine de cette exception
   */
  public FlatMapperXmlStreamException(ErrorDetail pErrorDetail, Throwable pCause)
  {
    super(pErrorDetail, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les paramètres décrivant le contexte de l'exception
   * @param pCause : la cause d'origine de cette exception
   */
  public FlatMapperXmlStreamException(ErrorDetail pErrorDetail, Object[] pParams, Throwable pCause)
  {
    super(pErrorDetail, pParams, pCause);
  }

  /**
   * Constructeur
   * @param pErrorDetail : le code erreur
   * @param pParams : les paramètres décrivant le contexte de l'exception
   */
  public FlatMapperXmlStreamException(ErrorDetail pErrorDetail, Object[] pParams)
  {
    super(pErrorDetail, pParams);
  }
}
