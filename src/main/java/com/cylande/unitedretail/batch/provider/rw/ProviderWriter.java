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
   * Ecrit une chaine de caract�res xml dans le provider
   * @param pValue
   * @throws ProviderException exception
   */
  public abstract void write(String pValue) throws ProviderException;

  /**
   * Valide l'�criture dans le outputStream et Release les ressources utilis�es
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
   * @return r�sultat
   */
  public String getCurrentFileName()
  {
    return null;
  }

  /**
   * getProviderFileType
   * @return r�sultat
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
