package com.cylande.unitedretail.batch.repositoryloader;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.TriggerManagerServiceImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.batch.TriggerType;
import com.cylande.unitedretail.message.batch.Triggers;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;

/**
 * Load Triggers and Populate them in TriggerRepository
 */
public class TriggerRepositoryLoader extends AbstractRepositoryLoader
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(TaskRepositoryLoader.class);
  /** nom de la propriété ciblant le fichiers de défintions de triggers à charger */
  private static final String TRIGGER_PROPERTY_NAME = "trigger.dir";
  /** l'ensemble des définitions de triggers */
  private Triggers _triggers = null;
  /** gestionnaire de définitions de triggers */
  private TriggerManagerServiceImpl _triggerManager = null;

  /**
   * Default Constructor : orchestraction of action (load and populate)
   * @throws ConfigEnginePropertiesException exception
   */
  public TriggerRepositoryLoader() throws ConfigEnginePropertiesException
  {
    super();
    _triggerManager = new TriggerManagerServiceImpl();
    _repoPropertyName = TRIGGER_PROPERTY_NAME;
    loadProperties();
  }

  /**
   * Load Tasks File and convert it in Tasks Object
   * @param pFileName : path of the Tasks File
   */
  public void load(String pFileName)
  {
    LOGGER.debug("Lecture des descriptions de triggers");
    if (pFileName != null && !pFileName.equals(""))
    {
      if (fileExists(pFileName))
      {
        _triggers = new Triggers();
        _triggers = (Triggers)_manager.read(pFileName, _triggers);
      }
    }
    if (_triggers == null)
    {
      LOGGER.warn("Aucun trigger n'a été défini");
      _triggers = new Triggers();
    }
    LOGGER.info(_triggers.getTrigger().size() + " triggers ont été préchargés");
  }

  /** Populate Triggers Object in TriggerRepository thanks to TriggerManager (contains Service CRUD of Repository)
   * @throws Exception exception
   */
  public void populate() throws BatchException
  {
    if (_triggers == null)
    {
      throw new BatchException(BatchErrorDetail.TRIGGER_REPO_NOLIST);
    }
    LOGGER.debug("Alimentation de la repository de trigger");
    try
    {
      ContextType ctx = ContextTransformer.fromLocale();
      for (TriggerType bean: _triggers.getTrigger())
      {
        _triggerManager.createTrigger(bean, null, ctx);
      }
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.TRIGGER_REPO_POPULATE, e);
    }
  }
}
