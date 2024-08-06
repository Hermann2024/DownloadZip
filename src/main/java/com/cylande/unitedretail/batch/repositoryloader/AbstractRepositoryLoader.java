package com.cylande.unitedretail.batch.repositoryloader;

import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;

import java.io.File;

import org.apache.log4j.Logger;

public abstract class AbstractRepositoryLoader
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(AbstractRepositoryLoader.class);
  /** nom de la propri�t� � r�cup�rer */
  protected String _repoPropertyName;
  /** JAXBManager pour les transformations */
  protected JAXBManager _manager = null;
  /** chemin du fichier XML � charger dans la repository */
  private String _scriptFilePath = null;

  /**
   * Constructor
   */
  public AbstractRepositoryLoader()
  {
    _manager = new JAXBManager();
  }

  /**
   * Chargement des scripts en m�moire
   * @throws BatchException exception
   */
  public void load() throws BatchException
  {
    if (getRepoDir() != null)
    {
      load(_scriptFilePath);
      populate();
    }
  }

  /**
   * Lit le fichier dont le path est pass� en param�tre, et cr�e les beans java
   * @param pFileName
   */
  public abstract void load(String pFileName);

  /**
   * contr�le si un fichier existe
   */
  protected boolean fileExists(String pFileName)
  {
    boolean result = false;
    if (pFileName == null)
    {
      return result;
    }
    File file = new File(pFileName);
    result = file.exists();
    if (!result)
    {
      LOGGER.warn("File " + pFileName + " doesn't exists");
    }
    return file.exists();
  }

  /**
   * Enregistre les beans java g�n�r�s dans la repository
   * @throws BatchException exception
   */
  public abstract void populate() throws BatchException;

  /**
   * R�cup�re le chemin du fichier xml � partir de la propri�t� en m�moire
   * @throws ConfigEnginePropertiesException exception
   */
  public void loadProperties() throws ConfigEnginePropertiesException
  {
    _scriptFilePath = ConfigEngineProperties.getInstance().getDirectoryEngineProperties(_repoPropertyName);
  }

  /**
   * Renvoie le nom du fichier qui alimente le r�f�rentiel
   * @return le nom du fichier qui alimente le r�f�rentiel
   */
  public String getRepoDir()
  {
    return _scriptFilePath;
  }
}
