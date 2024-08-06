package com.cylande.unitedretail.batch.task;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.impl.XMLStringProvider;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderReaderImpl;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.process.response.RejectResponse;

import org.apache.log4j.Logger;

/**
 * task d'integration avec recyclage de paquet rejeté
 *
 * lorsque un paquet de taille supperieur a 1 est rejeter cette implementation
 * re-soumet ligne par ligne les elements du paquet rejeté pour ne rejeter que les elements
 * qui pose probleme.
 */
public class TaskIntegrationUnitRejectThreadPooled extends TaskIntegrationThreadPooledImpl
{
  /**
   * Numero de version pour la sérialisation
   */
  private static final long serialVersionUID = 201002231415L;

  /**
   * logger
   */
  private static final Logger LOGGER = Logger.getLogger(TaskIntegrationUnitRejectThreadPooled.class);

  /**
   * provider utilisé pour le retry ligne/ligne des paquet rejetés
   */
  private transient ProviderReader _retryProviderReader;

  /**
   * marqueur de l'etat du dernier paquet lut dans le provider "normal"
   */
  private boolean _lastReadPackageIsLastPackage = false;

  /**
   * index du dernier paquet lut dans le provider "normal"
   */
  private int _lastReadPackageNumber = 0;

  /** Parent */
  private AbstractBatch _parent = null;

  /**
   * Constructeur
   * @param pParent le batch parent
   * @param pBean la definition de la task
   * @throws TaskException exception sur le super constructeur
   */
  public TaskIntegrationUnitRejectThreadPooled(AbstractBatch pParent, INTEGRATION pBean) throws TaskException
  {
    super(pParent, pBean);
    _parent = pParent;
  }

  /**
   * override
   * Traitement d'une réponse de type "erreur"
   * Attention a ne pas tourner en boucle (paquet de 1 rejeté)
   * @param pResponse la reject response
   * @throws TaskException exception sur la "super" methode
   * @throws ProviderException echec de la construction du providerreader de recycling
   */
  protected void rejectTreatment(RejectResponse pResponse) throws TaskException, ProviderException
  {
    // si les paquets sont de taille <1 pas de retry evite de tourner en boucle
    if (pResponse.getSize() < 2)
    {
      super.rejectTreatment(pResponse);
    }
    else
    {
      // re-injection des données dans le retry provider mode LIFO avec paquet de 1
      _endOfStream = false;
      buildRetryProvider(pResponse);
    }
  }

  /**
   * override avec gestion retry provider
   * Lecture de données dans le provider de lecture, lit le prochain paquet dans le retry provider tant qu'il y en a puis lit dans le provider
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return le paquet de donnée suivant
   * @throws TaskException exception sur la lecture dans le provider
   * @throws ProviderException exception sur la lecture dans le provider
   */
  protected DataPackage readInput(Integer pCurrentTaskId) throws TaskException, ProviderException
  {
    DataPackage result = null;
    // si le retry provider est vide/null, lecture dans le provider d'origine
    if (_retryProviderReader == null)
    {
      result = super.readInput(pCurrentTaskId);
      if (result != null)
      {
        _lastReadPackageIsLastPackage = result.isLastPackage();
        _lastReadPackageNumber = result.getPackageNumber();
      }
    }
    // sinon lecture dans le retry provider
    else
    {
      // lecture dans retry provider
      Integer currentTaskId = (pCurrentTaskId != null ? pCurrentTaskId : getSysId());
      result = _retryProviderReader.read(null, currentTaskId);
      //si le provider retry est vide on le libere
      if (result == null || result.isLastPackage())
      {
        //liberation du provider retry
        _retryProviderReader.releaseProvider();
        // supprime la reference
        _retryProviderReader = null;
        // null = provider vide, on lit dans le provider normal
        if (result == null)
        {
          //lecture provider d'origine
          LOGGER.debug(new StringBuilder("End recycling element of lot :").append(_lastReadPackageNumber));
          result = super.readInput(pCurrentTaskId);
        }
        else if (result.isLastPackage())
        {
          // a la fin du provider retry on set le flag lastPackage avec le valeur
          // du meme flag du paquet en rejet
          LOGGER.debug(new StringBuilder("Last recycling element of lot :").append(_lastReadPackageNumber));
          result.setLastPackage(_lastReadPackageIsLastPackage);
        }
      }
      else if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug(new StringBuilder("Step recycling element ").append(result.getPackageNumber()).append(" of lot : ").append(_lastReadPackageNumber).append(""));
      }
    }
    return result;
  }

  /**
   * construit le provider de retry
   * @param pResponse la reject response fournit les datas pour construir le provider de reject
   * @throws ProviderException echec de la construction du provider de reject
   */
  private void buildRetryProvider(RejectResponse pResponse) throws ProviderException
  {
    //log creation/start retry provider
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(new StringBuilder("Error append during integration of lot :").append(_lastReadPackageNumber));
      LOGGER.debug(new StringBuilder("Start recycling of lot :").append(_lastReadPackageNumber));
    }
    Provider myXmlStringProvider = new XMLStringProvider(null, _propManager, getDomain(), getAlternativeDomain(), pResponse.getValue());
    _retryProviderReader = new ProviderReaderImpl(myXmlStringProvider, 1, BatchUtil.getKeepRootElementPrefix(getTaskIntegrationDispatchInputProviderReader()), BatchUtil.getIgnoreRootNamespace(_inputProviderReader));
  }

  /**
   * Retourne le provider d'entrée de la tache d'intégration.
   * @return ProviderReader
   */
  private ProviderReader getTaskIntegrationDispatchInputProviderReader()
  {
    ProviderReader result = null;
    if ((_parent != null) && (_parent.getDispatcher() != null))
    {
      result = _parent.getDispatcher().getInputProviderReader();
    }
    return result;
  }

  /** {@inheritDoc} */
  protected void releaseProviderResources() throws ProviderException
  {
    //liberation du provider retry
    if (_retryProviderReader != null)
    {
      _retryProviderReader.releaseProvider();
    }
    super.releaseProviderResources();
  }
}
