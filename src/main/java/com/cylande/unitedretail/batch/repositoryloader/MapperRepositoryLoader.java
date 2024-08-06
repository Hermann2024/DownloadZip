package com.cylande.unitedretail.batch.repositoryloader;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.repository.MapperPropertiesRepository;
import com.cylande.unitedretail.batch.repository.ProviderPropertiesRepository;
import com.cylande.unitedretail.batch.service.MapperManagerServiceDelegate;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.message.batch.Mappers;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.engineproperties.PropertyListType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;

/**
 * Load Mappers and Populate it in MapperRepository
 */
public class MapperRepositoryLoader extends AbstractRepositoryLoader
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(MapperRepositoryLoader.class);
  /** nom de la propriété ciblant le fichiers de définitions de Mappers à charger */
  private static final String MAPPER_PROPERTY_NAME = "mapper.dir";
  /** l'ensemble des définitions de mappers */
  private Mappers _mappers = null;
  /** gestionnaire de définitions de mappers */
  private MapperManagerServiceDelegate _mapperManager = null;

  /**
   * Default Constructor : orchestraction of action (load and populate)
   * @throws ConfigEnginePropertiesException exception
   */
  public MapperRepositoryLoader() throws ConfigEnginePropertiesException
  {
    super();
    _mapperManager = new MapperManagerServiceDelegate();
    _repoPropertyName = MAPPER_PROPERTY_NAME;
    loadProperties();
  }

  /**
   * Load Mappers File and convert it in Mappers Object
   * @param pFileName : path of the Mappers File
   */
  public void load(String pFileName)
  {
    LOGGER.debug("Lecture des descriptions de mappers");
    if (pFileName != null && !pFileName.equals(""))
    {
      if (fileExists(pFileName))
      {
        _mappers = new Mappers();
        _mappers = (Mappers)_manager.read(pFileName, _mappers);
      }
    }
    if (_mappers == null)
    {
      LOGGER.warn("Aucun mapper n'a été défini");
      _mappers = new Mappers();
    }
    LOGGER.info(_mappers.getMapper().size() + " mappers ont été préchargés");
  }

  /**
   * Déclenche l'alimentation des référentiels liés aux mappers
   * @throws BatchException exception
   */
  public void populate() throws BatchException
  {
    if (_mappers == null)
    {
      throw new BatchException(BatchErrorDetail.MAPPER_REPO_NOLIST);
    }
    LOGGER.debug("Alimentation de la repository de mappers");
    populateMapper();
    LOGGER.debug("Alimentation de la repository des propriétés de mapper");
    populateMapperProperties();
  }

  /**
   * Alimente le référentiel de mapper
   * @throws BatchException exception
   */
  private void populateMapper() throws BatchException
  {
    try
    {
      ContextType ctx = ContextTransformer.fromLocale();
      for (MapperType bean: _mappers.getMapper())
      {
        _mapperManager.createMapper(bean, null, ctx);
      }
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.MAPPER_REPO_POPULATE, e);
    }
  }

  /**
   * Alimente le référentiel des propriétés de mapper
   */
  private void populateMapperProperties()
  {
    PropertyListType propertyList = _mappers.getProperties();
    if (propertyList == null)
    {
      LOGGER.debug("Pas de propriété de mapper à enregistrer");
      return;
    }
    ProviderPropertiesRepository.getInstance().putPropertyList(propertyList);
    LOGGER.debug("MapperPropertiesRepository possède " + MapperPropertiesRepository.getInstance().getSize() + " propriétés");
  }
}
