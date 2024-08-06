package com.cylande.unitedretail.batch.repositoryloader;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.repository.ProviderPropertiesRepository;
import com.cylande.unitedretail.batch.service.ProviderManagerServiceImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.batch.Providers;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.engineproperties.PropertyListType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;

/**
 * Load Providers and Populate it in ProviderRepository
 */
public class ProviderRepositoryLoader extends AbstractRepositoryLoader
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(ProviderRepositoryLoader.class);
  /** nom de la propriété ciblant le fichiers de définitions de providers à charger */
  private static final String PROVIDER_PROPERTY_NAME = "provider.dir";
  /** l'ensemble des définitions de providers */
  private Providers _providers = null;
  /** gestionnaire de définitions de providers */
  private ProviderManagerServiceImpl _providerManager = null;

  /**
   * Constructor
   * @throws ConfigEnginePropertiesException exception
   */
  public ProviderRepositoryLoader() throws ConfigEnginePropertiesException
  {
    super();
    _providerManager = new ProviderManagerServiceImpl();
    _repoPropertyName = PROVIDER_PROPERTY_NAME;
    loadProperties();
  }

  /**
   * Load Providers File and convert it in Providers Object
   * @param pFileName : path of the Providers File
   */
  public void load(String pFileName)
  {
    LOGGER.debug("Lecture des descriptions de providers");
    if (pFileName != null && !pFileName.equals(""))
    {
      if (fileExists(pFileName))
      {
        _providers = new Providers();
        _providers = (Providers)_manager.read(pFileName, _providers);
      }
    }
    if (_providers == null)
    {
      LOGGER.warn("Aucun provider n'a été défini");
      _providers = new Providers();
    }
    LOGGER.info(_providers.getProvider().size() + " providers ont été préchargés");
  }

  /**
   * Déclenche l'alimentation des référentiels liés aux providers
   * @throws BatchException exception
   */
  public void populate() throws BatchException
  {
    if (_providers == null)
    {
      throw new BatchException(BatchErrorDetail.PROVIDER_REPO_NOLIST);
    }
    LOGGER.debug("Alimentation de la repository de providers");
    this.populateProvider();
    LOGGER.debug("Alimentation de la repository de Properties de provider");
    this.populateProviderProperties();
  }

  /**
   * Alimente le référentiel de provider
   * @throws BatchException exception
   */
  private void populateProvider() throws BatchException
  {
    try
    {
      ContextType ctx = ContextTransformer.fromLocale();
      for (ProviderType bean: _providers.getProvider())
      {
        _providerManager.createProvider(bean, null, ctx);
      }
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.PROVIDER_REPO_POPULATE, e);
    }
  }

  /**
   * Alimente le référentiel des propriétés de provider
   */
  private void populateProviderProperties()
  {
    PropertyListType propertyList = _providers.getProperties();
    if (propertyList == null)
    {
      LOGGER.debug("Pas de propriété de provider à enregistrer");
      return;
    }
    ProviderPropertiesRepository.getInstance().putPropertyList(propertyList);
    LOGGER.debug("ProviderPropertiesRepository possède " + ProviderPropertiesRepository.getInstance().getSize() + " propriétés");
  }
}
