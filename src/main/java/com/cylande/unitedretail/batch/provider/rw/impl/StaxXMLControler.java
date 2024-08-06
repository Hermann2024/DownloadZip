package com.cylande.unitedretail.batch.provider.rw.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.tools.XMLEncodingDetector;

/**
 * Classe utilitaire permettant de contr�ler la validit� d'un flux XML (balise
 * manquante, caract�re invalide...).
 */
public class StaxXMLControler
{
  /** Logger. */
  private static final Logger LOGGER = Logger.getLogger(StaxXMLControler.class);

  /** Ev�nement d�but de root element. */
  private static final int START_ROOT_ELEMENT = 0;

  /** Ev�nement fin de root element. */
  private static final int END_ROOT_ELEMENT = 1;

  /** Ev�nement d�but element. */
  private static final int START_ELEMENT = 2;

  /** Ev�nement fin element. */
  private static final int END_ELEMENT = 3;

  /** Ev�nement fin document. */
  private static final int END_DOCUMENT = 5;

  /** Fabrique de lecteur d'�v�nements XML. */
  private final XMLInputFactory _inputFactory;

  /** Lecteur d'�v�nements XML. */
  private XMLEventReader _eventReader;

  /** Niveau de profondeur de la lecture. */
  private int _currentReadingDepthLevel = -1;

  /** RootElementCourant, doit �tre identique au mainroot. */
  private RootElement _currentInputRootElement;

  /** Marqueur de fin de l'inputStream courant. */
  private boolean _bufferedReaderEos = false;

  /** Compteur d'�l�ment pour la constitution du paquet. */
  private int _counterParsedChildrenElements = 0;

  /** Flux XML en cours de v�rification. */
  private InputStream _inputStream = null;

  /**
   * Constructeur.
   */
  public StaxXMLControler()
  {
    // initialisation des factories
    _inputFactory = XMLInputFactory.newInstance();
  }

  /**
   * Contr�le la validit� du flux XML pass� en param�tre.
   * G�n�re une exception si le flux est invalide.
   * @param pInputStream Flux XML � contr�ler
   * @param pDefaultEncodingLimit Limite de taille de fichier utilis�e pour la
   *                              v�rification de l'encodage
   * @param pDefaultEncoding Encodage par d�faut
   * @throws XMLStreamException Erreur de lecture du flux XML
   * @throws ProviderException Erreur de traitement du provider
   * @throws IOException Erreur de lecture du flux d'entr�e
   */
  public void control(InputStream pInputStream, int pDefaultEncodingLimit, String pDefaultEncoding) throws XMLStreamException, ProviderException, IOException
  {
    _inputStream = pInputStream;
    _eventReader = null;
    _bufferedReaderEos = false;
    if (_inputStream != null)
    {
      initXMLreader(pDefaultEncodingLimit, pDefaultEncoding);
      // r�initialisation du compteur de paquet
      _counterParsedChildrenElements = 0;
      while (!_bufferedReaderEos)
      {
        readXMLEvent();
      }
    }
  }

  /**
   * Initialisation du lecteur d'�v�nements XML.
   * @param pDefaultEncodingLimit Limite de taille de fichier utilis�e pour la
   *                              v�rification de l'encodage
   * @param pDefaultEncoding Encodage par d�faut
   * @throws XMLStreamException Erreur de lecture du flux XML
   * @throws ProviderException Erreur de traitement du provider
   * @throws IOException Erreur de lecture du flux d'entr�e
   */
  private void initXMLreader(int pDefaultEncodingLimit, String pDefaultEncoding) throws XMLStreamException, ProviderException, IOException
  {
    String encoding = XMLEncodingDetector.detect(_inputStream, pDefaultEncodingLimit, pDefaultEncoding);
    _eventReader = _inputFactory.createXMLEventReader(new InputStreamReader(_inputStream, encoding));
    // r�initialis� le niveau de profondeur de lecture
    _currentReadingDepthLevel = -1;
    identifyRootElementOfCurrentInputStream();
  }

  /**
   * Identifie le rootElement du flux courant, et initialise le mainRootElement
   * si pas encore initialis�.
   * @throws XMLStreamException Erreur de lecture du flux XML
   * @throws ProviderException Erreur de traitement du provider
   */
  private void identifyRootElementOfCurrentInputStream() throws XMLStreamException, ProviderException
  {
    // recherche de l'�l�ment root contenu dans le currentinputstream
    while (_currentInputRootElement == null && !_bufferedReaderEos)
    {
      readXMLEvent();
    }
    if (_currentInputRootElement == null)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_NO_ROOT);
    }
  }

  /**
   * Lit les �v�nements du inputStream et compte les niveaux XML.
   * @throws XMLStreamException Erreur de lecture du flux XML
   */
  private void readXMLEvent() throws XMLStreamException
  {
    XMLEvent event = null;
    if (!_eventReader.hasNext())
    {
      // rien � lire ! on arr�te la lecture du flux courant
      stopReadingCurrentReader();
    }
    else
    {
      try
      {
        // lecture de l'�v�nement du flux courant
        event = _eventReader.nextEvent();
        // consommation de l'�v�nement
        useXMLEvent(event);
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
   * Consomme un XMLevent lu en entr�e.
   * @param pXmlEvent Ev�nement XML
   * @return int
   */
  private int useXMLEvent(XMLEvent pXmlEvent)
  {
    int eventType = identifyXMLEvent(pXmlEvent);
    switch (eventType)
    {
      case START_ROOT_ELEMENT:
        _currentReadingDepthLevel++;
        // m�morisation de l'�l�ment root du flux courant de lecture
        _currentInputRootElement = new RootElement(pXmlEvent, false, true);
        break;
      case START_ELEMENT:
        _currentReadingDepthLevel++;
        if (_currentReadingDepthLevel == 1)
        {
          // si �l�ment de premier niveau, on compte !
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
        // arriv� en fin de flux courant
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
   * Identifie le type d'�v�nement XML lu par le parser.
   * @param pXmlEvent Ev�nement XML
   * @return int correspondant au type identifi�
   */
  private int identifyXMLEvent(XMLEvent pXmlEvent)
  {
    // utilis�e par useEvent
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
   * Demande d'arr�t de lecture du flux courant
   */
  private void stopReadingCurrentReader()
  {
    _bufferedReaderEos = true;
  }

  /**
   * Retourne le nombre d'�l�ments enfants pars�s.
   * Si le fichier XML est valide, cela correspond au nombre d'�l�ments
   * rattach�s au noeud root.
   * @return int
   */
  public int getParsedChildrenElementsCount()
  {
    return _counterParsedChildrenElements;
  }
}
