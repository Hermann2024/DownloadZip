package com.cylande.unitedretail.batch.provider.impl;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.WithCrcProvider;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.batch.WITHCRCFILEPROVIDER;
import com.cylande.unitedretail.process.tools.PropertiesManager;

import java.io.IOException;
import java.io.InputStream;

import java.util.zip.CRC32;

/**
 * Provider du type Fichier avec controle de CRC
 */
public class WithCrcFileProvider extends FileProvider implements WithCrcProvider
{

  /** le Crc calculé */
  private String _crc32 = null;

  /** Taille de buffer (en octets) utilisé pour la lecture du fichier sur disque et le calcule du CRC. */
  private int _bufferSize = 16384;

  /** marqueur de vérification*/
  private Boolean _crcCheckIsActive = null;

  /**
   * Constructeur
   * @param pProviderDef : la définition du provider (issue du provider.xml)
   * @param pPropManager : propertiesManager permettant d'accéder au propriété d'environement d'exécution (domaines, etc...)
   * @see Provider
   */
  public WithCrcFileProvider(ProviderType pProviderDef, PropertiesManager pPropManager, String pDomain, String pAlternativeDomain) throws ProviderException
  {
    // TODO editeur fichier remplacer le mot vide
    super(pProviderDef, "", null, pPropManager, pDomain, pAlternativeDomain);
    if (pProviderDef instanceof WITHCRCFILEPROVIDER)
    {
      WITHCRCFILEPROVIDER providerDef = (WITHCRCFILEPROVIDER)pProviderDef;
      if (providerDef.getCrc() != null)
      {
        _bufferSize = providerDef.getCrc().getBufferSize();
      }
    }
  }

  /** {@inheritDoc} */
  public String getCrc32() throws IOException, ProviderException
  {
    if (_crc32 == null)
    {
      //InputStream fis = new FileInputStream(getFile());
      InputStream fis = getFileInputStream();
      CRC32 crc32 = new CRC32();
      crc32.reset();
      byte[] buffer = new byte[_bufferSize];
      int readLength = fis.read(buffer);
      while (readLength > 0)
      {
        crc32.update(buffer, 0, readLength);
        readLength = fis.read(buffer);
      }
      fis.close();
      _crc32 = Long.toHexString(crc32.getValue());
    }
    return _crc32;
  }

  /** {@inheritDoc} */
  public boolean crcCheckisActive()
  {
    if (_crcCheckIsActive == null)
    {
      boolean result;
      try
      {
        String sActive = getFilteredStringByDomain(((WITHCRCFILEPROVIDER)getProviderDef()).getCrc().getActive());
        result = "true".equals(sActive);
      }
      catch (Exception e)
      {
        result = true;
      }
      _crcCheckIsActive = result;
    }
    return _crcCheckIsActive;
  }

  /**
   * Récupère le fileInputStream
   * @return résultat
   * @throws ProviderException exception
   */
  private InputStream getFileInputStream() throws ProviderException
  {
    //TODO à revoir si utilisation multi fichier
    if (_fileProviderManager.getFileQuantity() > 1)
    {
      return null;
    }
    else
    {
      //TODO
      return nextInputStream();
    }
  }
}
