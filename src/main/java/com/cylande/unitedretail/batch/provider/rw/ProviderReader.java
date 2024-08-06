package com.cylande.unitedretail.batch.provider.rw;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.ProviderFileType;

public abstract class ProviderReader extends ProviderRW
{

  public ProviderReader(Provider provider)
  {
    super(provider);
  }

  /**
   * Lit le flux pour constituer un lot et le renvoyer
   * @param pNoProcessor
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return DataPackage représentant un lot, null si pas de données
   * @throws ProviderException exception
   */
  public abstract DataPackage read(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException;

  /**
   * Libère la ressource allouée
   * @throws ProviderException exception
   */
  public abstract void releaseProvider() throws ProviderException;

  /**
   * Nom du fichier en cours de traitement
   * @return résultat
   */
  public abstract String getCurrentFileName();

  /**
   * Retourne le contenu du fichier de scénario précisé sur le provider
   * @return le contenu du fichier de scénario, nul si non précisé
   */
  public abstract String getScenarioValue();

  /**
   * Sur JMSProviderReader, retourne le context lu sur message SOAP
   * @return
   */
  public abstract String getContextValue();

  /**
   * getProviderFileType
   * @return résultat
   * @throws Exception exception
   */
  public abstract ProviderFileType getProviderFileType() throws Exception;

  /**
   * setReject
   * @param pFileType
   */
  public abstract void setReject(FileType pFileType);

  /**
   * Désactive l'archivage de fichier
   */
  public abstract void disableArchive();

  public boolean isInputJson()
  {
    return getProvider().isInputJson();
  }
}
