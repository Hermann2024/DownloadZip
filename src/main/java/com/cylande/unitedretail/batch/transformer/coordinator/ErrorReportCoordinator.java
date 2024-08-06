package com.cylande.unitedretail.batch.transformer.coordinator;

import com.cylande.unitedretail.message.batch.ErrorType;
import com.cylande.unitedretail.message.batch.TaskAuditType;

/**
 * Coordonne les informations pour construire les éléments d'erreurs du rapports
 */
public final class ErrorReportCoordinator
{
  /**
   * Constructeur privé car toutes les méthodes sont statics
   */
  private ErrorReportCoordinator()
  {
  }

  /**
   * Complète le bean ErrorType à partir d'un TaskAuditType
   * @param pError
   * @param pTaskAudit
   */
  public static void fillErrorType(ErrorType pError, TaskAuditType pTaskAudit)
  {
    pError.setErrorCode(pTaskAudit.getErrorCode());
    pError.setErrorLevel(pTaskAudit.getErrorMessage());
  }
}
