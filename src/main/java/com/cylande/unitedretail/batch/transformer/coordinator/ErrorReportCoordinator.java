package com.cylande.unitedretail.batch.transformer.coordinator;

import com.cylande.unitedretail.message.batch.ErrorType;
import com.cylande.unitedretail.message.batch.TaskAuditType;

/**
 * Coordonne les informations pour construire les �l�ments d'erreurs du rapports
 */
public final class ErrorReportCoordinator
{
  /**
   * Constructeur priv� car toutes les m�thodes sont statics
   */
  private ErrorReportCoordinator()
  {
  }

  /**
   * Compl�te le bean ErrorType � partir d'un TaskAuditType
   * @param pError
   * @param pTaskAudit
   */
  public static void fillErrorType(ErrorType pError, TaskAuditType pTaskAudit)
  {
    pError.setErrorCode(pTaskAudit.getErrorCode());
    pError.setErrorLevel(pTaskAudit.getErrorMessage());
  }
}
