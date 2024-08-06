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
  /** nom de la propriété à récupérer */
  protected String _repoPropertyName;
  /** JAXBManager pour les transformations */
  protected JAXBManager _manager = null;
  /** chemin du fichier XML à charger dans la repository */
  private String _scriptFilePath = null;

  /**
   * Constructor
   */
  public AbstractRepositoryLoader()
  {
    _manager = new JAXBManager();
  }

  /**
   * Chargement des scripts en mémoire
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
   * Lit le fichier dont le path est passé en paramètre, et crée les beans java
   * @param pFileName
   */
  public abstract void load(String pFileName);

  /**
   * contrôle si un fichier existe
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
   * Enregistre les beans java générés dans la repository
   * @throws BatchException exception
   */
  public abstract void populate() throws BatchException;

  /**
   * Récupère le chemin du fichier xml à partir de la propriété en mémoire
   * @throws ConfigEnginePropertiesException exception
   */
  public void loadProperties() throws ConfigEnginePropertiesException
  {
    _scriptFilePath = ConfigEngineProperties.getInstance().getDirectoryEngineProperties(_repoPropertyName);
  }

  /**
   * Renvoie le nom du fichier qui alimente le référentiel
   * @return le nom du fichier qui alimente le référentiel
   */
  public String getRepoDir()
  {
    return _scriptFilePath;
  }
}
