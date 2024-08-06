package com.cylande.unitedretail.batch.provider.rw.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLStreamException;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.ProviderFileType;

/**
 * ProviderWriter permet d'écrire dans un flot de sortie XML.
 * Tant que le flot reste ouvert, les données sont concaténées,
 * tout en respectant la structure XML.
 * ProviderWriter est utiliser pour alimenter les flux Response et Reject
 */
public class ProviderWriterImpl extends ProviderWriter
{
  /** encodage par défaut du fichier */
  private static final String DEFAULT_ENCODAGE = "UTF-8";

  /** parser stax utilisé par le writer */
  private StaxXMLParser _currentParser = null;

  /** flux de sortie sur lequel travail le writer */
  private BufferedWriter _currentBufferedWriter = null;

  /** nom du provider sur lequel est attaché le writer */
  private String _providerName = "";

  /** marqueur de cloture du flux d'écriture courant // TK46254 */
  private boolean _alreadyCurrentClosed = false;
  private EUExecutionException _exception;
  private String _exceptionCode;
  private String _taskCode;

  /**
   * Constructeur
   * @param pProvider : provider dans lequel il faut écrire
   * @throws ProviderException : exception
   */
  public ProviderWriterImpl(Provider pProvider) throws ProviderException
  {
    super(pProvider);
    if (pProvider != null)
    {
      pProvider.setOutputJson();
    }
    _providerName = getProvider().getProviderDef().getName();
  }

  /** {@inheritDoc} */
  public void write(String pValue) throws ProviderException
  {
    if (pValue == null)
    {
      //ne rien faire
      return;
    }
    try
    {
      //transformation de la chaîne de caractères en inputStream et utilisation du write
      this.write(new ByteArrayInputStream(pValue.getBytes(DEFAULT_ENCODAGE)));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new ProviderException(BatchErrorDetail.UNSUPPORTED_ENCODING, e);
    }
  }

  private void write(InputStream pInputStream) throws ProviderException
  {
    try
    {
      write(new BufferedReader(new InputStreamReader(pInputStream, DEFAULT_ENCODAGE)));
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.MAPPER_UNSUPORTED_ENCODING, e);
    }
  }

  /**
   * Ecrit un BufferedReader contenant du xml dans le provider
   * @param pBufferedReader
   * @throws ProviderException exception
   */
  private void write(BufferedReader pBufferedReader) throws ProviderException
  {
    initWriting();
    try
    {
      String stringPack = _currentParser.readXMLString(pBufferedReader, null);
      if (stringPack != null)
      {
        // ... et l'écrire dans le outputStream
        _currentBufferedWriter.write(stringPack);
        if (_exception != null && getProvider().getProviderDef().getMapper() == null)
        {
          String msgException = getMessageException(_exception);
          if (getProvider().isOutputJson() && getProvider().getEngineWriter().isListMode())
          {
            // on flush pour être certain que l'appel au write du EngineWriter est fait afin que startElement et endElement sont initialisés
            _currentBufferedWriter.flush();
            // en Json, sur les rejets d'intégration par paquet, nécessité d'encapsuler les balises d'erreurs par la balise utilisée pour le découpage des paquets
            _currentBufferedWriter.write(getProvider().getEngineWriter().getStartElement());
          }
          _currentBufferedWriter.write("<error><![CDATA[" + msgException + "]]></error>");
          _currentBufferedWriter.write("<errorDetail><error><![CDATA[" + msgException + "]]></error>");
          if (_exceptionCode != null)
          {
            _currentBufferedWriter.write("<errorCode>" + _exceptionCode + "</errorCode>");
          }
          _currentBufferedWriter.write("<taskCode>" + _taskCode + "</taskCode>");
          _currentBufferedWriter.write("</errorDetail>");
          if (getProvider().isOutputJson() && getProvider().getEngineWriter().isListMode())
          {
            _currentBufferedWriter.write(getProvider().getEngineWriter().getEndElement());
          }
        }
        _currentBufferedWriter.flush();
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_WRITE_ERROR, new Object[] { _providerName }, e);
    }
  }

  /**
   * Initialisation de l'écriture
   * @throws ProviderException exception
   */
  private void initWriting() throws ProviderException
  {
    if (_currentBufferedWriter == null)
    {
      initNewWriter();
    }
    else
    {
      if (getProvider().providerDefinitionUpdated())
      {
        releaseCurrentOutputStream();
        initNewWriter();
      }
    }
  }

  /**
   * initialisation d'un nouveau writer
   * @throws ProviderException exception
   */
  private void initNewWriter() throws ProviderException
  {
    _currentBufferedWriter = getProvider().getTransformedOutputStream();
    // un nouveau writer est initié, on réinitialise le marqueur de cloture
    // TK46254
    _alreadyCurrentClosed = false;
    try
    {
      _currentParser = new StaxXMLParser(true, getKeepRootElementPrefix());
    }
    catch (XMLStreamException e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_INIT_ERROR, new Object[] { _providerName }, e);
    }
  }

  /**
   * Cloture le flux avec le tag de fin
   * @throws IOException exception
   */
  private void closeXmlStream() throws ProviderException
  {
    String endTag = _currentParser.getEndTag();
    if (endTag != null)
    {
      try
      {
        _currentBufferedWriter.write(endTag);
        _currentBufferedWriter.flush();
      }
      catch (IOException e)
      {
        throw new ProviderException(BatchErrorDetail.UNSUPPORTED_ENCODING, e);
      }
    }
  }

  private String getMessageException(Throwable pException)
  {
    Throwable exception = pException;
    while (exception.getCause() != null)
    {
      exception = exception.getCause();
    }
    String result = exception.getLocalizedMessage();
    if (exception instanceof WrapperServiceException)
    {
      _exceptionCode = ((WrapperServiceException)exception).getCanonicalCode();
      String detail = ((WrapperServiceException)exception).getDetailMessage();
      if ((detail != null) && (detail.trim().length() > 0))
      {
        result += " : " + detail;
      }
    }
    return result;
  }

  /**
   * Complète le outputstream courant et le cloture
   * @throws ProviderException exception
   */
  private void releaseCurrentOutputStream() throws ProviderException
  {
    // le release ne doit se faire qu'une seule fois pour le flux courant (attention au cas du multithread)
    // on vérifie donc le marqueur de cloture
    // TK46254
    if (_currentBufferedWriter != null && !_alreadyCurrentClosed)
    {
      // le writer fonctionne en lecture partielle, il faut donc fermer le document xml
      closeXmlStream();
      getProvider().closeOutputStream(); // ferme le flux d'écriture
      // on positionne le marqueur d'écriture à vrai une fois le flux fermé
      // TK46254
      _alreadyCurrentClosed = true;
    }
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    if (getProvider() != null)
    {
      releaseCurrentOutputStream();
    }
  }

  /** {@inheritDoc} */
  public void setException(EUExecutionException pException)
  {
    _exception = pException;
  }

  /** {@inheritDoc} */
  public String getCurrentFileName()
  {
    return getProvider() != null ? getProvider().getCurrentFileName() : null;
  }

  /** {@inheritDoc} */
  public ProviderFileType getProviderFileType()
  {
    return getProvider().getCurrentProviderFileType();
  }

  /**
   * Retourne la valeur de l'attribut keepRootElementPrefix du provider
   * d'entrée.
   * @return boolean
   * @throws ProviderException Erreur
   */
  private boolean getKeepRootElementPrefix() throws ProviderException
  {
    boolean result = false;
    try
    {
      ProviderFileType providerFileType = getProviderFileType();
      if (providerFileType != null)
      {
        result = Boolean.TRUE.equals(providerFileType.getKeepRootElementPrefix());
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_INIT_ERROR, new Object[] { _providerName }, exc);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void setTaskCode(String pTaskCode)
  {
    _taskCode = pTaskCode;
  }
}
