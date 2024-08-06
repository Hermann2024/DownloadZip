package com.cylande.unitedretail.batch.provider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.StreamMapperException;
import com.cylande.unitedretail.batch.mapper.EngineWriter;
import com.cylande.unitedretail.batch.mapper.MapperFactory;
import com.cylande.unitedretail.batch.mapper.StreamMapper;
import com.cylande.unitedretail.batch.provider.rw.impl.JsonWriterImpl;
import com.cylande.unitedretail.batch.service.MapperManagerServiceDelegate;
import com.cylande.unitedretail.batch.tools.XMLEncodingDetector;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceNotDeliveredException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.MapperKeyType;
import com.cylande.unitedretail.message.batch.MapperScenarioType;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderMapperType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.tools.PropertiesManager;
import com.cylande.unitedretail.process.tools.PropertiesTools;

/**
 * Provider
 * Implements providers defined in Providers.xml Engine file.
 */
public abstract class Provider
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(Provider.class);
  /** définition du fichier de provider */
  protected ProviderMapperType _mapperProviderDef = null;
  /** encodage par défaut du fichier */
  protected String _defaultEncodage = "UTF-8";
  /**
   * limite pour détecter l'encodage
   * La taille définie est la taille par défaut dans monq.
   */
  protected int _defaultEncodageLimit = 1000;
  protected boolean _check = true;
  protected boolean _outputJson = false;
  protected boolean _inputJson = false;
  protected boolean _jsonArray = false;
  /** définition du provider */
  private ProviderType _providerDef;
  /** gestionnaire de définitions de providers */
  private PropertiesManager _propertiesManager;
  /** domaine courant */
  private String _currentDomain = null;
  /** domaine alternatif */
  private String _alternativeDomain = null;
  /** fichier courant */
  private String _currentFileName = null;
  /** charset d'encodage du fichier */
  private String _charset = null;
  private EngineWriter _engineWriter = null;

  /**
   * Constructeur
   * @param pProviderDef : la définition du provider (issue du provider.xml)
   * @param pPropManager : propertiesManager permettant d'accéder au propriété d'environement d'exécution (domaines, etc...)
   */
  public Provider(ProviderType pProviderDef, PropertiesManager pPropManager, String pDomain, String pAlternativeDomain)
  {
    _providerDef = pProviderDef;
    if (pProviderDef != null)
    {
      _charset = pProviderDef.getCharset();
    }
    _mapperProviderDef = pProviderDef == null ? null : getProviderDef().getMapper();
    _propertiesManager = pPropManager;
    _currentDomain = pDomain;
    _alternativeDomain = pAlternativeDomain;
  }

  /**
   * Modifie le domaine courant
   * @param pDomain
   */
  protected void setCurrentDomain(String pDomain)
  {
    _currentDomain = pDomain;
  }

  /**
   * Mutateur du domaine alternatif
   * @param pAlternativeDomain
   */
  protected void setAlternativeDomain(String pAlternativeDomain)
  {
    _alternativeDomain = pAlternativeDomain;
  }

  protected void setCurrentFileName(String pName)
  {
    _currentFileName = pName;
  }

  /**
   * renvoie le domaine courant
   * @return String
   */
  public String getCurrentDomain()
  {
    return _currentDomain;
  }

  /**
   * Accesseur du domaine alternatif
   * @return String
   */
  public String getAlternativeDomain()
  {
    return _alternativeDomain;
  }

  /**
   * Nom du fichier en cours de traitement
   * @return résultat
   */
  public String getCurrentFileName()
  {
    return _currentFileName;
  }

  /**
   * Lit une chaîne de caractères en filtrant les properties selon le domaine
   * @param pString
   * @return String
   */
  public String getFilteredStringByDomain(String pString)
  {
    if (_propertiesManager == null || pString == null)
    {
      return null;
    }
    return PropertiesTools.replaceProperties(pString, _propertiesManager, _currentDomain, _alternativeDomain);
  }

  /**
   * Renvoie le providerType associé à la définition du provider
   * @return ProviderType
   */
  public ProviderType getProviderDef()
  {
    return _providerDef;
  }

  public void setProviderDef(ProviderType providerDef)
  {
    _providerDef = providerDef;
  }

  /**
   * Renvoie le propertie manager associé
   * @return PropertiesManager
   */
  public PropertiesManager getPropertiesManager()
  {
    return _propertiesManager;
  }

  /**
   * Fourni des flux de lecture sur le provider
   * @return résultat
   * @throws ProviderException exception
   */
  protected abstract InputStream nextInputStream() throws ProviderException;

  /**
   * Vérifie le flux XML entrant et retourne le flux réinitialisé si nécessaire.
   * @param pInputStream Flux XML entrant
   * @return InputStream
   * @throws ProviderException Erreur du provider
   */
  protected abstract InputStream checkXMLInputStream(InputStream pInputStream) throws ProviderException;

  /**
   * Fourni des flux de lecture sur le provider
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return résultat
   * @throws ProviderException exception
   */
  protected Reader nextBufferedReaderProvider(Integer pCurrentTaskId) throws ProviderException, UnsupportedEncodingException, IOException
  {
    InputStream nextInputStream = nextInputStream();
    if (_mapperProviderDef == null)
    {
      if (!_inputJson)
      {
        boolean isInputStreamInvalid = false;
        do
        {
          isInputStreamInvalid = false;
          nextInputStream = checkXMLInputStream(nextInputStream);
        }
        while (isInputStreamInvalid);
      }
      // Lecture du flux (InputStream)
      try
      {
        if (nextInputStream != null)
        {
          return new BufferedReader(new InputStreamReader(nextInputStream, _check ? XMLEncodingDetector.detect(nextInputStream, _defaultEncodageLimit, _defaultEncodage) : _defaultEncodage));
        }
      }
      catch (UnsupportedEncodingException e)
      {
        throw new ProviderException(BatchErrorDetail.MAPPER_UNSUPORTED_ENCODING, e);
      }
      catch (IOException e)
      {
        LOGGER.error("IOException occured : " + e.getMessage());
      }
    }
    else
    {
      try
      {
        StreamMapper myMapper = new MapperFactory().createMapper(getMapper(_mapperProviderDef.getRef()), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
        if (_charset != null)
        {
          return myMapper.transformInputStream(nextInputStream, _charset);
        }
        else
        {
          return myMapper.transformInputStream(nextInputStream, _defaultEncodage);
        }
      }
      catch (StreamMapperException e)
      {
        LOGGER.error("StreamMapperException occured : " + e.getMessage());
      }
    }
    return null;
  }

  /**
   * Fourni des flux (transformés) de lecture sur le provider
   * @param pCurrentTaskId Identifiant de la task en cours
   * @return BufferedReader transformé
   * @throws ProviderException exception
   */
  public Reader nextTransformedBufferedReader(Integer pCurrentTaskId) throws ProviderException, UnsupportedEncodingException, IOException
  {
    Reader transformedBufferedReader;
    transformedBufferedReader = nextBufferedReaderProvider(pCurrentTaskId);
    return transformedBufferedReader;
  }

  /**
   * Indique s'il existe (encore) un flux de lecture sur le provider
   * @return boolean
   * @throws ProviderException exception
   */
  public abstract boolean hasNextInputStream() throws ProviderException;

  /**
   * Indique s'il existe (encore) un flux de lecture sur le provider
   * @return boolean
   * @throws ProviderException exception
   */
  public abstract boolean hasNextBufferedReader() throws ProviderException;

  /**
   * Ferme le flux de lecture en cours sur le provider
   * @throws ProviderException exception
   */
  public abstract void closeInputStream() throws ProviderException;

  /**
   * Fourni un flux d'écriture sur le provider
   * @return OutputStream
   * @throws ProviderException exception
   */
  protected abstract OutputStream getOutputStream() throws ProviderException;

  /**
   * Fourni un flux (transformé) d'écriture sur le provider
   * @return OutputStream transformé
   * @throws ProviderException exception
   */
  public BufferedWriter getTransformedOutputStream() throws ProviderException
  {
    if (_outputJson)
    {
      try
      {
        _engineWriter = new JsonWriterImpl(getOutputStream(), _jsonArray);
        return new BufferedWriter(_engineWriter);
      }
      catch (IOException e)
      {
        LOGGER.error("IOException occured : " + e.getMessage());
      }
    }
    if (_mapperProviderDef == null)
    {
      try
      {
        return new BufferedWriter(new OutputStreamWriter(getOutputStream(), _defaultEncodage));
      }
      catch (UnsupportedEncodingException e)
      {
        throw new ProviderException(BatchErrorDetail.MAPPER_UNSUPORTED_ENCODING, e);
      }
    }
    try
    {
      StreamMapper myMapper = new MapperFactory().createMapper(getMapper(_mapperProviderDef.getRef()), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
      return myMapper.transformOutputStream(getOutputStream());
    }
    catch (StreamMapperException e)
    {
      LOGGER.error("StreamMapperException occured : " + e.getMessage());
    }
    catch (IOException e)
    {
      LOGGER.error("IOException occured : " + e.getMessage());
    }
    return null;
  }

  /**
   * Ferme et finalise le flux d'écriture sur le provider
   * @return the Provider InputStream
   */
  public abstract void closeOutputStream() throws ProviderException;

  /**
   * indique s'il y a eu une modification de la définition du provider (renommage dynamique)
   * @return boolean
   */
  public abstract boolean providerDefinitionUpdated() throws ProviderException;

  /**
   * récupère le mapper associé au provider
   * @param pKey : clé du mapper
   * @return MapperType : mapper
   * @throws ProviderException exception
   */
  protected MapperType getMapper(String pKey) throws ProviderException
  {
    MapperType myMapper = null;
    MapperManagerServiceDelegate mapperManagerServiceDelegate = new MapperManagerServiceDelegate();
    MapperKeyType mapperKeyType = new MapperKeyType();
    mapperKeyType.setName(pKey);
    try
    {
      myMapper = mapperManagerServiceDelegate.getMapper(mapperKeyType, new MapperScenarioType(), new ContextType());
    }
    catch (TechnicalServiceNotDeliveredException e)
    {
      LOGGER.error("TechnicalServiceNotDeliveredException occured : " + e.getMessage());
    }
    catch (WrapperServiceException e)
    {
      LOGGER.error("WrapperServiceException occured : " + e.getMessage());
    }
    catch (TechnicalServiceException e)
    {
      LOGGER.error("TechnicalServiceException occured : " + e.getMessage());
    }
    if (myMapper == null)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDER_NO_MAPPER_FOUND_ERROR);
    }
    return myMapper;
  }

  public String getScenarioValue()
  {
    return null;
  }

  /**
   * Donne le providerFileType courant
   * @return résultat
   */
  public ProviderFileType getCurrentProviderFileType()
  {
    return (ProviderFileType)null;
  }

  public void setReject(FileType pFileType)
  {
  }

  public void disableArchive()
  {
  }

  public void setCheck(boolean pCheck)
  {
    _check = pCheck;
  }

  public void setOutputJson()
  {
    _outputJson = false;
  }

  public void setInputJson()
  {
    _inputJson = false;
  }

  public boolean isInputJson()
  {
    return _inputJson;
  }

  public boolean isOutputJson()
  {
    return _outputJson;
  }

  public void setOutputJson(boolean pJson)
  {
    _outputJson = pJson;
  }

  public EngineWriter getEngineWriter()
  {
    return _engineWriter;
  }
}
