package com.cylande.unitedretail.batch.mapper;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.StreamMapperException;
import com.cylande.unitedretail.framework.tools.FilenameUtil;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;
import com.cylande.unitedretail.process.tools.PropertiesManager;
import com.cylande.unitedretail.process.tools.PropertiesTools;

import java.io.File;

import org.apache.log4j.Logger;

public class MapperFactory
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(MapperFactory.class);

  /** nom de la propri�t� renseignant le r�pertoire temporaire */
  private static String _propertyTemplateDirectoryName = "template.dir";

  /** gestionnaire de d�finitions de mappers */
  private PropertiesManager _propertiesManager;

  /** domaine d'ex�cution */
  private String _currentDomain;

  /** domaine alternative */
  private String _alternativeDomain;

  /**
   * Constructor
   */
  public MapperFactory()
  {
  }

  /**
   * create new StreamMapper by pMapperType
   * @param pMapperType
   * @return r�sultat
   */
  public StreamMapper createMapper(MapperType pMapperType, PropertiesManager pPropertiesManager, String pCurrentDomain, String pAlternativeDomain) throws StreamMapperException
  {
    StreamMapper result = null;
    _propertiesManager = pPropertiesManager;
    _currentDomain = pCurrentDomain;
    _alternativeDomain = pAlternativeDomain;
    // selon le type de mapper, impl�mentation diff�rente
    try
    {
      if (pMapperType == null)
      {
        // aucune r�f�rence au mapper trouv�
        throw new StreamMapperException(BatchErrorDetail.MAPPER_REF_ERROR);
      }
      if (pMapperType.getTemplate() != null)
      {
        //mapper Cylande
        result = new CylandeTemplateImpl(getPath(pMapperType.getTemplate().getDir(), pMapperType.getTemplate().getFile()), pMapperType.getTemplate());
      }
      else if (pMapperType.getXslfile() != null)
      {
        // mapper xslt
        result = new XslTemplateImpl(getPath(pMapperType.getXslfile().getDir(), pMapperType.getXslfile().getFile()));
      }
      else if (pMapperType.getJavaclass() != null)
      {
        // mapper javaclass
        result = new JavaclassTemplateImpl(pMapperType.getJavaclass());
      }
      if (result == null)
      {
        throw new StreamMapperException(BatchErrorDetail.MAPPER_DEFINITION_INCORRECT);
      }
    }
    catch (ProviderException e)
    {
      throw new StreamMapperException();
    }
    return result;
  }

  /**
   * get directory of template define in Engine_cong.properties
   * @return directory
   */
  public String getTemplateDir()
  {
    if ("".equals(_propertyTemplateDirectoryName))
    {
      return _propertyTemplateDirectoryName;
    }
    try
    {
      return ConfigEngineProperties.getInstance().getDirectoryEngineProperties(_propertyTemplateDirectoryName);
    }
    catch (ConfigEnginePropertiesException e)
    {
      LOGGER.warn("impossible de r�cup�rer l'emplacement sp�cifi� pour les fichiers templates");
      return new FilenameUtil().addRelativePath("/");
    }
  }

  private String getPath(String pDir, String pFile) throws StreamMapperException
  {
    String result = null;
    if (pDir != null)
    {
      String filteredString = getFilteredStringByDomain(pDir);
      result = new FilenameUtil().addRelativePath(filteredString);
    }
    if (result == null)
    {
      result = getTemplateDir();
    }
    result += File.separatorChar;
    String file = getFilteredStringByDomain(pFile);
    if (file == null)
    {
      throw new StreamMapperException(BatchErrorDetail.MAPPER_FILE_ERROR); // aucun fichier d�fini
    }
    return result + file;
  }

  /**
   * Lit une cha�ne de caract�res en filtrant les properties selon le domaine
   * @param pString
   * @return String
   */
  protected String getFilteredStringByDomain(String pString)
  {
    if (_propertiesManager == null || pString == null)
    {
      return null;
    }
    return PropertiesTools.replaceProperties(pString, _propertiesManager, _currentDomain, _alternativeDomain);
  }
}
