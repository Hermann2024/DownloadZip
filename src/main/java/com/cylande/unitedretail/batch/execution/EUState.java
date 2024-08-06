package com.cylande.unitedretail.batch.execution;

/**
 * Etat d'une unit� d'ex�cution
 */
public enum EUState
{
  /** Cr�� */
  CREATED,
  /** En cours d'initialisation */
  INITITIALIZING,
  /** En cours d'ex�cution */
  RUNNING,
  /** En cours de finalisation (Liberation des ressources) */
  FINALIZING,
  /** Annulation en cours */
  CANCELING,
  /** Termin�e */
  ENDED;
}
