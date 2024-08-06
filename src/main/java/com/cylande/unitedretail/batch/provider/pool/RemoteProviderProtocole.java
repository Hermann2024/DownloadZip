package com.cylande.unitedretail.batch.provider.pool;

/**
 * liste des mots clef du protocole d'echange avec les providers distants
 */
public class RemoteProviderProtocole
{
  /** Constante pour le tag de début de flux*/
  public static final RemoteProviderProtocoleTag START_OF_STREAM = new RemoteProviderProtocoleTag("START_OF_STREAM");

  /** Constante pour le tag de fin de flux*/
  public static final RemoteProviderProtocoleTag END_OF_STREAM = new RemoteProviderProtocoleTag("END_OF_STREAM");

  /** Constante pour la commande d'initialisation de la session http*/
  public static final RemoteProviderProtocoleTag INIT_SESSION = new RemoteProviderProtocoleTag("INIT_SESSION");

  /** Constante pour la commande d'initialisation de la session http*/
  public static final RemoteProviderProtocoleTag INIT_SESSION_ACTION = new RemoteProviderProtocoleTag("INIT_SESSION_ACTION");

  /** Constante pour la commande de création du provider factory*/
  public static final RemoteProviderProtocoleTag INIT_FACTORY_ACTION = new RemoteProviderProtocoleTag("INIT_FACTORY_ACTION");

  /** Constante pour la commande de création du provider reader */
  public static final RemoteProviderProtocoleTag INIT_READER_ACTION = new RemoteProviderProtocoleTag("INIT_READER_ACTION");

  /** Constante pour la commande de création du provider writer */
  public static final RemoteProviderProtocoleTag INIT_WRITER_ACTION = new RemoteProviderProtocoleTag("INIT_WRITER_ACTION");

  /** Constante pour la commande de lecture*/
  public static final RemoteProviderProtocoleTag READ_ACTION = new RemoteProviderProtocoleTag("READ_ACTION");

  /** Constante pour la commande d'écriture */
  public static final RemoteProviderProtocoleTag WRITE_ACTION = new RemoteProviderProtocoleTag("WRITE_ACTION");

  /** Constante pour la commande de libération du provider reader */
  public static final RemoteProviderProtocoleTag RELEASE_READER_ACTION = new RemoteProviderProtocoleTag("RELEASE_READER_ACTION");

  /** Constante pour la commande de libération du provider writer */
  public static final RemoteProviderProtocoleTag RELEASE_WRITER_ACTION = new RemoteProviderProtocoleTag("RELEASE_WRITER_ACTION");

  /** Constante pour la commande de libération du provider factory */
  public static final RemoteProviderProtocoleTag RELEASE_SESSION_ACTION = new RemoteProviderProtocoleTag("RELEASE_SESSION_ACTION");
}
