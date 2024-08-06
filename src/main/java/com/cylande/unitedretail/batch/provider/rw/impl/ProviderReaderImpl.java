package com.cylande.unitedretail.batch.provider.rw.impl;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderWrapperType;

import javax.xml.stream.XMLStreamException;

/**
 * Provider reader concrete implementation
 */
public class ProviderReaderImpl extends ProviderReader
{

  /** parser stax utilisé par le reader */
  private StaxXMLParser _parser = null;

  /** numéro du paquet lu */
  private int _packageNumber = 0;

  /** Indique s'il faut conserver le préfixe de l'élément root */
  private Boolean _keepRootElementPrefix = null;

  /** Indique s'il faut ignorer l'attribut xmlns de l'élément root */
  private Boolean _ignoreRootNamespace = null;

  /**
   * Constructeur du provider de lecture
   * @param pProvider le flux à lire
   * @param pSize la taille maximale d'élément fils constituant un lot
   * @param pKeepRootElementPrefix indique s'il faut conserver le préfixe de l'élément root
   * @throws ProviderException exception
   */
  public ProviderReaderImpl(Provider pProvider, int pSize, Boolean pKeepRootElementPrefix, Boolean pIgnoreRootNamespace) throws ProviderException
  {
    this(pProvider, null, pSize, pKeepRootElementPrefix, pIgnoreRootNamespace);
  }

  /**
   * Constructeur du provider de lecture
   * @param pProvider le flux à lire
   * @param pWrapper le wrapper à utiliser
   * @param pSize la taille maximale d'élément fils constituant un lot
   * @param pKeepRootElementPrefix indique s'il faut conserver le préfixe de l'élément root
   * @throws ProviderException exception
   */
  public ProviderReaderImpl(Provider pProvider, final ProviderWrapperType pWrapper, int pSize, Boolean pKeepRootElementPrefix, Boolean pIgnoreRootNamespace) throws ProviderException
  {
    super(pProvider);
    if (pProvider != null)
    {
      pProvider.setInputJson();
    }
    _keepRootElementPrefix = pKeepRootElementPrefix;
    _ignoreRootNamespace = pIgnoreRootNamespace;
    try
    {
      final String wrapValue = (pWrapper != null ? pWrapper.getValue() : null);
      _parser = new StaxXMLParser(getProvider(), pSize, false, getKeepRootElementPrefix(), getIgnoreRootNamespace(), wrapValue);
    }
    catch (XMLStreamException e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_CONSTRUCTION_ERROR, new Object[] { getProviderName() }, e);
    }
  }

  /** {@inheritDoc} */
  public DataPackage read(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException
  {
    DataPackage result = null;
    try
    {
      String sValue = _parser.readXMLString(pNoProcessor, pCurrentTaskId);
      if (Boolean.TRUE.equals(pNoProcessor))
      {
        _packageNumber++;
        result = new DataPackage();
        result.setPackageNumber(_packageNumber);
        result.setLastPackage(_parser.isEndOfReading());
      }
      else if (sValue != null)
      {
        _packageNumber++;
        result = new DataPackage();
        result.setValue(sValue);
        result.setPackageNumber(_packageNumber);
        result.setLastPackage(_parser.isEndOfReading());
      }
    }
    catch (Exception e)
    {
      if (getProvider().getCurrentFileName() != null)
      {
        if (e instanceof ProviderException && ((ProviderException)e).getCanonicalCode().equalsIgnoreCase(BatchErrorDetail.STAXPARSER_INVALID_XML_FILE.getCanonicalCode()))
        {
          throw (ProviderException)e;
        }
        throw new ProviderException(BatchErrorDetail.CAN_NOT_READ_FILE_PROVIDER, new Object[] { getProvider().getCurrentFileName(), getProviderName() }, e);
      }
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_READ_ERROR, new Object[] { getProviderName() }, e);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    if (getProvider() != null)
    {
      getProvider().closeInputStream();
    }
  }

  private String getProviderName()
  {
    if (getProvider() != null && getProvider().getProviderDef() != null)
    {
      return getProvider().getProviderDef().getName();
    }
    return null;
  }

  /** {@inheritDoc} */
  public String getCurrentFileName()
  {
    return getProvider() != null ? getProvider().getCurrentFileName() : null;
  }

  /** {@inheritDoc} */
  public String getScenarioValue()
  {
    return getProvider().getScenarioValue();
  }

  /** {@inheritDoc} */
  public ProviderFileType getProviderFileType() throws Exception
  {
    return getProvider().getCurrentProviderFileType();
  }

  /** {@inheritDoc} */
  public void setReject(FileType pFileType)
  {
    getProvider().setReject(pFileType);
  }

  /** {@inheritDoc} */
  public void disableArchive()
  {
    getProvider().disableArchive();
  }

  /**
   * Retourne la valeur de l'attribut keepRootElementPrefix du provider
   * d'entrée
   * @return boolean
   * @throws ProviderException Erreur
   */
  private boolean getKeepRootElementPrefix() throws ProviderException
  {
    boolean result = false;
    try
    {
      ProviderFileType providerFileType = getProvider().getCurrentProviderFileType();
      if (providerFileType != null)
      {
        result = Boolean.TRUE.equals(providerFileType.getKeepRootElementPrefix());
      }
      else
      {
        result = Boolean.TRUE.equals(_keepRootElementPrefix);
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_CONSTRUCTION_ERROR, new Object[] { getProviderName() }, exc);
    }
    return result;
  }

  /**
   * Retourne la valeur de l'attribut ignoreRootNamespace du provider
   * d'entrée
   * @return boolean
   * @throws ProviderException Erreur
   */
  private boolean getIgnoreRootNamespace() throws ProviderException
  {
    boolean result = false;
    try
    {
      ProviderFileType providerFileType = getProvider().getCurrentProviderFileType();
      if (providerFileType != null)
      {
        result = Boolean.TRUE.equals(providerFileType.getIgnoreRootNamespace());
      }
      else
      {
        result = Boolean.TRUE.equals(_ignoreRootNamespace);
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_CONSTRUCTION_ERROR, new Object[] { getProviderName() }, exc);
    }
    return result;
  }

  @Override
  public String getContextValue()
  {
    return null;
  }
}
