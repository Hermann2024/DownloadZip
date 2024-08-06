package com.cylande.unitedretail.batch.execution;

/**
 * Etat d'une unité d'exécution
 */
public enum EUState
{
  /** Créé */
  CREATED,
  /** En cours d'initialisation */
  INITITIALIZING,
  /** En cours d'exécution */
  RUNNING,
  /** En cours de finalisation (Liberation des ressources) */
  FINALIZING,
  /** Annulation en cours */
  CANCELING,
  /** Terminée */
  ENDED;
}
