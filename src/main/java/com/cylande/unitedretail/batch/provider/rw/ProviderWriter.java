package com.cylande.unitedretail.batch.provider.rw;

import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.message.batch.ProviderFileType;

public abstract class ProviderWriter extends ProviderRW
{

  public ProviderWriter(Provider provider)
  {
    super(provider);
  }

  /**
   * Ecrit une chaine de caractères xml dans le provider
   * @param pValue
   * @throws ProviderException exception
   */
  public abstract void write(String pValue) throws ProviderException;

  /**
   * Valide l'écriture dans le outputStream et Release les ressources utilisées
   * @throws ProviderException exception
   */
  public abstract void releaseProvider() throws ProviderException;

  /**
   * setException
   * @param pException
   */
  public void setException(EUExecutionException pException)
  {
  }

  /**
   * Nom du fichier en cours de traitement
   * @return résultat
   */
  public String getCurrentFileName()
  {
    return null;
  }

  /**
   * getProviderFileType
   * @return résultat
   */
  public abstract ProviderFileType getProviderFileType();

  public void setTaskCode(String pTaskCode)
  {
  }

  public void setOutputJson(boolean pJson)
  {
    getProvider().setOutputJson(pJson);
  }
}
