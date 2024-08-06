package com.cylande.unitedretail.batch.provider.rw.impl;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;

import de.odysseus.staxon.json.JsonXMLInputFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

/**
 * Classe utilisant le parser STAX.
 * Code Complexe !
 */
public class StaxXMLParser
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(StaxXMLParser.class);

  /** évènement début de root element */
  private static final int START_ROOT_ELEMENT = 0;

  /** évènement fin de root element */
  private static final int END_ROOT_ELEMENT = 1;

  /** évènement début element */
  private static final int START_ELEMENT = 2;

  /** évènement fin element */
  private static final int END_ELEMENT = 3;

  /** évènement fin document */
  private static final int END_DOCUMENT = 5;

  /** fabrique de lecteur d'événements XML */
  private XMLInputFactory _inputFactory;

  /** fabrique de l'écrivain d'événements XML */
  private XMLOutputFactory _outputFactory;

  /** lecteur d'évènements XML */
  private XMLEventReader _eventReader;

  /** écrivain d'évènements XML */
  private XMLEventWriter _eventWriter;

  /** provider fournissant le inputStream à lire */
  private Provider _inputProvider;

  /** résultat de la lecture du paquet courant par le parser */
  private StringWriter _packResult;

  /** niveau de profondeur de la lecture */
  private int _currentReadingDepthLevel = -1;

  /** rootElement principal des flux, doit être commun à tous */
  private RootElement _mainRootElement;

  /** rootElementCourant, doit être identique au mainroot */
  private RootElement _currentInputRootElement;

  /** taille des paquets, 0 par défaut : tout récupérer */
  private int _packSize = 0;

  /** BufferedReader externe */
  private BufferedReader _externalBufferedReader = null;

  /** BufferedReader courant */
  private Reader _currentBufferedReader = null;

  /** marqueur de fin de l'inputStream courant */
  private boolean _bufferedReaderEos = false;

  /** compteur d'élément pour la constitution du paquet */
  private int _counterParsedChildrenElements = 0;

  /** marqueur indiquant que le parsing est terminé */
  private boolean _globalEos = false;

  /** marqueur du mode partiel */
  private boolean _partialMode = false;

  /** marqueur indiquant que le premier paquet a été généré */
  private boolean _firstPackIsDone = false;

  /** Indique s'il faut conserver le préfixe de l'élément root */
  private final boolean _keepRootElementPrefix;

  /** Indique s'il faut ignorer l'attribut xmlns de l'élément root */
  private boolean _ignoreRootNamespace;

  private boolean _subElement;

  private String _virtualRoot;

  /* CONSTRUCTORS */

  /**
   * le provider en lecture est précisé ainsi que la taille des paquets à récupérer (renseigné dans la task)
   * @param pInputProvider
   * @param pPackSize
   * @param pKeepRootElementPrefix Indique s'il faut conserver le préfixe de
   *                               l'élément root.
   */
  public StaxXMLParser(Provider pInputProvider, int pPackSize, boolean pPartialMode, boolean pKeepRootElementPrefix, boolean pIgnoreRootNamespace) throws XMLStreamException, ProviderException
  {
    this(pInputProvider, pPackSize, pPartialMode, pKeepRootElementPrefix, pIgnoreRootNamespace, null);
  }

  /**
   * constructeur
   * @param pInputProvider
   * @param pPackSize
   * @param pKeepRootElementPrefix Indique s'il faut conserver le préfixe de
   *                               l'élément root.
   * @param pIgnoreRootNamespace
   * @param pWrapValue Valeur de l'élément root à rajouter
   */
  public StaxXMLParser(Provider pInputProvider, int pPackSize, boolean pPartialMode, boolean pKeepRootElementPrefix, boolean pIgnoreRootNamespace, final String pWrapValue) throws XMLStreamException, ProviderException
  {
    if (pInputProvider == null)
    {
      throw new ProviderException(BatchErrorDetail.STAXPARSER_NO_INPUTSTREAM);
    }
    _inputProvider = pInputProvider;
    _packSize = pPackSize;
    _partialMode = pPartialMode;
    _keepRootElementPrefix = pKeepRootElementPrefix;
    _ignoreRootNamespace = pIgnoreRootNamespace;
    _firstPackIsDone = false;
    _virtualRoot = pWrapValue;
    init();
  }

  /**
   * constructeur
   * @param pPartialMode
   * @param pKeepRootElementPrefix Indique s'il faut conserver le préfixe de
   *                               l'élément root.
   * @throws XMLStreamException exception
   * @throws ProviderException exception
   */
  public StaxXMLParser(boolean pPartialMode, boolean pKeepRootElementPrefix) throws XMLStreamException, ProviderException
  {
    _packSize = 0;
    _partialMode = pPartialMode;
    _keepRootElementPrefix = pKeepRootElementPrefix;
    _firstPackIsDone = false;
    init();
  }

  /**
   * initialisation des factory de lecture/écriture d'évènement pour le parser
   * @throws XMLStreamException exception
   * @throws ProviderException exception
   */
  private void init() throws XMLStreamException, ProviderException
  {
    // initialisation des factories
    if (_inputProvider != null && _inputProvider.isInputJson())
    {
      _inputFactory = new JsonXMLInputFactory();
      _inputFactory.setProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI, false);
      if (_virtualRoot != null)
      {
        _inputFactory.setProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT , _virtualRoot);
      }
    }
    else
    {
      _inputFactory = XMLInputFactory.newInstance();
    }
    _outputFactory = XMLOutputFactory.newInstance();
  }

  /**
   * Lecture à partir d'un flux récupéré auprès du provider
   * @param pNoProcessor
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return résultat
   * @throws ProviderException exception
   * @throws IOException exception
   * @throws XMLStreamException exception
   */
  public String readXMLString(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException, IOException, XMLStreamException
  {
    // appareillage du flux de lecture
    initBufferedReader(pNoProcessor, pCurrentTaskId);
    checkBufferedReader();
    // tester s'il y a qq chose à lire avant de continuer
    if (isEndOfReading())
    {
      return null;
    }
    if (!Boolean.TRUE.equals(pNoProcessor))
    {
      initXMLreader();
      buildPack();
      if (_packSize > 0 && _counterParsedChildrenElements == 0)
      {
        // optimisation qui évite de renvoyer une balise root sans élément (non pertinent dans un traitement par paquet) et ainsi de déclencher un appel de service inutile
        // on est dans ce cas quand le nombre d'éléments lus est un multiple de la taille du paquet (commitFrequency à 1, unitReject, ...)
        // le rappel à readXMLString permet soit de passer au fichier suivant soit de renvoyer null s'il n'y a plus rien à lire
        return readXMLString(pNoProcessor, pCurrentTaskId);
      }
    }
    else
    {
      _currentBufferedReader = null;
      cleanParam();
    }
    return getPackResult();
  }

  /**
   * Lecture à partir du flux passé en paramètre
   * @param pBufferedReader
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return résultat
   * @throws IOException exception
   * @throws ProviderException exception
   * @throws XMLStreamException exception
   */
  public String readXMLString(BufferedReader pBufferedReader, Integer pCurrentTaskId) throws IOException, ProviderException, XMLStreamException
  {
    _externalBufferedReader = pBufferedReader;
    return readXMLString((Boolean)null, pCurrentTaskId);
  }

  /**
   * récupère le BufferedReader en cours ou un nouvel BufferedReader auprès du provider
   * @param pNoProcessor
   * @param pCurrentTaskId Identifiant de la task en cours
   * @throws ProviderException exception
   */
  private void initBufferedReader(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException, UnsupportedEncodingException, IOException
  {
    if (_currentBufferedReader == null)
    {
      // se positionner sur le BufferedReader externe s'il a été précisé
      if (_externalBufferedReader != null)
      {
        _currentBufferedReader = _externalBufferedReader;
      }
      // récupère un BufferedReader auprès du provider et s'y positionner, si utilisation d'un provider de lecture
      if (_inputProvider != null)
      {
        if (Boolean.TRUE.equals(pNoProcessor))
        {
          _inputProvider.setCheck(false);
        }
        loadNewBufferedReaderFromProvider(pCurrentTaskId);
      }
      // réinitialiser le reader
      _eventReader = null;
    }
    if (_currentBufferedReader == null)
    {
      // pas de lecture possible
      stopReading();
    }
    // sinon, continuer à utiliser le même BufferedReader
  }

  /**
   * controle du flux de lecture
   */
  private void checkBufferedReader() throws ProviderException
  {
    if (_currentBufferedReader == null)
    {
      // rien à lire !!!
      stopReadingCurrentReader();
    }
    else
    {
      _bufferedReaderEos = false;
    }
  }

  /**
   * initialisation du lecteur d'événements XML
   * @throws XMLStreamException exception
   * @throws ProviderException exception
   */
  private void initXMLreader() throws XMLStreamException, ProviderException
  {
    if (_eventReader == null)
    {
      _eventReader = _inputFactory.createXMLEventReader(_currentBufferedReader);
      // réinitialisé le niveau de profondeur de lecture
      _currentReadingDepthLevel = -1;
      identifyRootElementOfCurrentInputStream();
    }
  }

  /**
   * Identify le rootElement du flux courant, et initialise le mainRootElement
   * si pas encore initialisé
   */
  private void identifyRootElementOfCurrentInputStream() throws ProviderException, XMLStreamException
  {
    // recherche de l'élément root contenu dans le currentinputstream
    while (_currentInputRootElement == null && !_bufferedReaderEos)
    {
      readXMLEvent();
    }
    if (_currentInputRootElement == null)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_NO_ROOT);
    }
    // si c'est la première lecture, initialisation du rootElement
    if (isFirstReading())
    {
      _mainRootElement = _currentInputRootElement.getCopy();
    }
    // sinon, vérifier la concordance des rootElement
    else if (!_keepRootElementPrefix)
    {
      //vérifie si l'élément root du flux courant correspond à l'élément root précédement enregistré pour l'ensemble des documents des providers
      if (!_mainRootElement.getElementName().equals(_currentInputRootElement.getElementName()))
      {
        throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_REWRITE_DIFF_TYPE);
      }
    }
  }

  /**
   * contruit un paquet
   * @throws XMLStreamException exception
   * @throws ProviderException exception
   */
  private void buildPack() throws XMLStreamException, ProviderException
  {
    // initialise l'écrivain d'évènements
    initXMLWriter();
    // préparer le paquet réponse
    initResultPack();
    // copie des sous éléments en fonction de la taille du paquet spécifiée
    copySubElementToOutput();
    // complétion du paquet en cours de construction en fonction du mode partiel
    fillResultPack();
    cleanParam();
  }

  /**
   * Ajoute l'entete de document en sortie
   * @throws XMLStreamException exception
   */
  private void appendHeaderToResultPack() throws XMLStreamException
  {
    _eventWriter.add(XMLEventFactory.newInstance().createStartDocument("UTF-8", "1.0"));
    _packResult.write("\n");
  }

  /**
   * Ajoute le tag d'ouverture de l'élément root au document de sortie
   */
  private void appendRootTagToResultPack()
  {
    _packResult.write(_mainRootElement.getStartTag());
  }

  /**
   * charge le prochaine BufferedReader fournit par le inputProvider
   * @param pCurrentTaskId Identifiant de la task en cours
   * @throws ProviderException exception
   */
  public void loadNewBufferedReaderFromProvider(Integer pCurrentTaskId) throws ProviderException, UnsupportedEncodingException, IOException
  {
    if (_inputProvider.hasNextBufferedReader())
    {
      _currentBufferedReader = _inputProvider.nextTransformedBufferedReader(pCurrentTaskId);
    }
    else
    {
      stopReading();
    }
  }

  /**
   * indique si c'est la première lecture dans le provider
   * @return résultat
   */
  private boolean isFirstReading()
  {
    // si le mainRoot n'est pas initialisé, c'est la première lecture
    return _mainRootElement == null;
  }

  /**
   * Initialise l'écrivain pour construire les paquets xml extraits du flux
   * @throws XMLStreamException exception
   * @throws ProviderException exception
   */
  private void initXMLWriter() throws XMLStreamException, ProviderException
  {
    // nouveau stringWriter
    _packResult = new StringWriter();
    // réinitialisation du compteur de paquet
    _counterParsedChildrenElements = 0;
    // positionner le stringwriter dans l'écrivain d'évènement
    _eventWriter = _outputFactory.createXMLEventWriter(_packResult);
    _subElement = false;
  }

  /**
   * prépare le paquet réponse, avec les entêtes si nécessaire
   * @throws XMLStreamException exception
   */
  private void initResultPack() throws XMLStreamException
  {
    // ajout de l'entête et du noeud root
    // - si le parser n'est pas en mode partiel
    // - si le parser est en mode partiel ET que c'est le premier paquet
    if (!_partialMode || (!_firstPackIsDone))
    {
      appendHeaderToResultPack();
      appendRootTagToResultPack();
    }
  }

  /**
   * complète le paquet resultat en fonction du mode d'utilisation
   */
  private void fillResultPack()
  {
    // si on n'est pas en lecture partielle
    if (!_partialMode)
    {
      closePack();
    }
    // si on est en lecture partielle et que c'est la fin de lecture
    if (_partialMode && isEndOfReading())
    {
      closePack();
    }
  }

  /**
   * Retourne le paquet lu sous forme de chaine de caractères
   * @return résultat
   * @throws IOException exception
   */
  private String getPackResult() throws IOException
  {
    // PRECOND : le paquet résultat doit être instancié
    // ATTENTION : une fois qu'un paquet est rendu, le flux utilisé pour créer celui est fermé et ses ressources libérées
    if (_packResult == null)
    {
      return null;
    }
    _packResult.flush();
    _packResult.close();
    String result = _packResult.toString();
    _packResult = null;
    if (!_firstPackIsDone)
    {
      _firstPackIsDone = true;
    }
    return result;
  }

  /**
   * Indique si le paquet est plein
   * @return résultat
   */
  private boolean packIsFull()
  {
    //Teste si le paquet en cours d'assemblage est à son maximum autorisé
    boolean result = false;
    if (isInPackMode() && _counterParsedChildrenElements >= _packSize && _currentReadingDepthLevel == 0)
    {
      // ne pas oublier de décompter les éléments encours
      // le paquet est plein
      result = true;
    }
    else
    {
      // le paquet n'est pas complétement plein, ou pas de gestion par paquet
      result = false;
    }
    return result;
  }

  /**
   * indique si on est en mode par paquet
   * @return résultat
   */
  private boolean isInPackMode()
  {
    boolean test = false;
    if (_packSize > 0)
    {
      test = true;
    }
    return test;
  }

  /**
   * demande l'arrêt de la lecture du provider
   */
  private void stopReading()
  {
    _globalEos = true;
  }

  /**
   * indique si la lecture est terminée
   * @return résultat
   */
  public boolean isEndOfReading()
  {
    // sert lors d'une interruption du flux de lecture
    return _globalEos;
  }

  /**
   * Demande d'arrêt de lecture du flux courant
   */
  private void stopReadingCurrentReader() throws ProviderException
  {
    _bufferedReaderEos = true;
    if (!_partialMode && _inputProvider != null && !_inputProvider.hasNextInputStream())
    {
      stopReading();
    }
  }

  /**
   * Indique si la lecture du flux courant est terminée
   * @return résultat
   */
  private boolean isEndOfCurrentBufferedReader()
  {
    if (_currentBufferedReader == null)
    {
      _bufferedReaderEos = false;
    }
    else if (_bufferedReaderEos)
    {
      _currentBufferedReader = null;
    }
    return _bufferedReaderEos;
  }

  /**
   * Lecteur d'événements de sous éléments
   * @return résultat
   */
  private XMLEvent readSubElementEvent() throws ProviderException, XMLStreamException
  {
    int beforeLevel = _currentReadingDepthLevel;
    XMLEvent event = readXMLEvent();
    int afterLevel = _currentReadingDepthLevel;
    if (beforeLevel == -1 || afterLevel == -1)
    {
      // c'est l'élément root, on l'ignore
      return null;
    }
    else
    {
      // sinon, l'élément est lu et renvoyé pour être copier dans le flux de sortie
      return event;
    }
  }

  /**
   * copie tous les sous éléments du root d'entrée vers le stringwriter
   * @throws XMLStreamException exception
   */
  private void copySubElementToOutput() throws XMLStreamException, ProviderException
  {
    XMLEvent event = null;
    while (!isEndOfCurrentBufferedReader() && !packIsFull())
    {
      event = readSubElementEvent();
      if (event != null)
      {
        _eventWriter.add(event);
        _subElement = true;
      }
    }
  }

  /**
   * Lit les événements du inputStream et compte les niveaux XML
   * @return résultat
   */
  private XMLEvent readXMLEvent() throws ProviderException, XMLStreamException
  {
    XMLEvent event = null;
    if (!_eventReader.hasNext())
    {
      // rien à lire ! on arrête la lecture du flux courant
      stopReadingCurrentReader();
      return null;
    }
    else
    {
      try
      {
        // lecture de l'évènement du flux courant
        event = _eventReader.nextEvent();
        // consommation de l'évènement
        useXMLEvent(event);
        // renvoie de l'évènement
        return event;
      }
      catch (XMLStreamException e)
      {
        LOGGER.warn("error during reading of inputStream : " + e.getMessage());
        // interruption du flux de lecture
        stopReadingCurrentReader();
        throw e;
      }
    }
  }

  /**
   * consomme un XMLevent lu en entrée
   * @param pXmlEvent
   * @return résultat
   */
  private int useXMLEvent(XMLEvent pXmlEvent) throws ProviderException
  {
    int eventType = identifyXMLEvent(pXmlEvent);
    switch (eventType)
    {
      case START_ROOT_ELEMENT:
        _currentReadingDepthLevel++;
        // mémorisation de l'élément root du flux courant de lecture
        _currentInputRootElement = new RootElement(pXmlEvent, _keepRootElementPrefix, _ignoreRootNamespace);
        break;
      case START_ELEMENT:
        _currentReadingDepthLevel++;
        if (_currentReadingDepthLevel == 1)
        {
          // si élément de premier niveau, on compte !
          _counterParsedChildrenElements++;
        }
        break;
      case END_ELEMENT:
        _currentReadingDepthLevel--;
        if (_currentReadingDepthLevel < 0)
        {
          stopReadingCurrentReader();
        }
        break;
      case END_ROOT_ELEMENT:
        // arrivé en fin de flux courant
        _currentReadingDepthLevel--;
        stopReadingCurrentReader();
        break;
      case END_DOCUMENT:
        stopReadingCurrentReader();
        break;
      default:
        break;
    }
    return eventType;
  }

  /**
   * identifie le type d'évènement XML lu par le parser
   * @param pXmlEvent
   * @return int correspondant au type identifié
   */
  private int identifyXMLEvent(XMLEvent pXmlEvent)
  {
    // utilisée par useEvent
    if (pXmlEvent.isStartElement())
    {
      // START_ROOT ou START_ELEM ?
      if (_currentReadingDepthLevel > -1)
      {
        //START_ELEM
        return START_ELEMENT;
      }
      else
      {
        //START_ROOT
        return START_ROOT_ELEMENT;
      }
    }
    if (pXmlEvent.isEndElement())
    {
      //END_ROOT or END_ELEM ?
      if (_currentReadingDepthLevel > 0)
      {
        return END_ELEMENT;
      }
      else
      {
        return END_ROOT_ELEMENT;
      }
    }
    if (pXmlEvent.isEndDocument())
    {
      return END_DOCUMENT;
    }
    return -1;
  }

  /**
   * termine le paquet en ajoutant la fermeture du noeud root
   */
  private void closePack()
  {
    if (_mainRootElement != null && _packResult != null)
    {
      _packResult.write(_mainRootElement.getEndTag());
    }
  }

  /**
   * réinitialise les paramètres pour une prochaine lecture de flux
   */
  private void cleanParam()
  {
    _eventWriter = null;
    _externalBufferedReader = null;
  }

  /**
   * renvoie le tag de fin associé au rootElement
   * @return résultat
   */
  public String getEndTag()
  {
    if (_mainRootElement != null)
    {
      return _mainRootElement.getEndTag();
    }
    return null;
  }

  public boolean isSubElement()
  {
    return _subElement;
  }
}
