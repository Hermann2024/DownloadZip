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
   * @return DataPackage repr�sentant un lot, null si pas de donn�es
   * @throws ProviderException exception
   */
  public abstract DataPackage read(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException;

  /**
   * Lib�re la ressource allou�e
   * @throws ProviderException exception
   */
  public abstract void releaseProvider() throws ProviderException;

  /**
   * Nom du fichier en cours de traitement
   * @return r�sultat
   */
  public abstract String getCurrentFileName();

  /**
   * Retourne le contenu du fichier de sc�nario pr�cis� sur le provider
   * @return le contenu du fichier de sc�nario, nul si non pr�cis�
   */
  public abstract String getScenarioValue();

  /**
   * Sur JMSProviderReader, retourne le context lu sur message SOAP
   * @return
   */
  public abstract String getContextValue();

  /**
   * getProviderFileType
   * @return r�sultat
   * @throws Exception exception
   */
  public abstract ProviderFileType getProviderFileType() throws Exception;

  /**
   * setReject
   * @param pFileType
   */
  public abstract void setReject(FileType pFileType);

  /**
   * D�sactive l'archivage de fichier
   */
  public abstract void disableArchive();

  public boolean isInputJson()
  {
    return getProvider().isInputJson();
  }
}
