package com.cylande.unitedretail.batch.provider.impl;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * provider a usage interne
 * crée un provider a partir d'une string passé au constructeur
 */
public class XMLStringProvider extends Provider
{
  /**
   * encodage utiliser pour convertir la string en byteArray
   */
  private static final String DATA_ENCODING = "UTF-8";

  /**
   * input straem fournisseur de donné
   */
  private InputStream _localInputStream;

  /**
   * les datas de ce provider
   */
  private final String _xmlData;

  /**
   * constructor
   * @param pProviderDef definition du provider (null is permit)
   * @param pPropManager le gestionaire de properties
   * @param pDomain le domain d'execution (null is permit)
   * @param pXmlData source de donnée du provider
   */
  public XMLStringProvider(ProviderType pProviderDef, PropertiesManager pPropManager, String pDomain, String pAlternativeDomain, String pXmlData)
  {
    super(pProviderDef, pPropManager, pDomain, pAlternativeDomain);
    _xmlData = pXmlData;
  }

  /** {@inheritDoc} */
  protected InputStream nextInputStream() throws ProviderException
  {
    if (hasNextInputStream())
    {
      try
      {
        // construction input stream a partir de la chaine xml
        _localInputStream = new ByteArrayInputStream(_xmlData.getBytes(DATA_ENCODING));
      }
      catch (UnsupportedEncodingException uee)
      {
        throw new ProviderException(BatchErrorDetail.UNSUPPORTED_ENCODING, uee);
      }
    }
    else
    {
      //closeInputStream(); on ne ferme pas le stream il peut etre utilisé meme si on a demandé le suivant
      // TODO throw exception Style : out of index ?
      return null;
    }
    return _localInputStream;
  }

  /**
   * {@inheritDoc}
   */
  protected InputStream checkXMLInputStream(InputStream pInputStream) throws ProviderException
  {
    // On ne réalise la vérification que s'il s'agit d'un flux XML provenant
    // d'un fichier.
    return pInputStream;
  }

  /** {@inheritDoc} */
  public boolean hasNextInputStream()
  {
    return _localInputStream == null && _xmlData != null;
  }

  /** {@inheritDoc} */
  public boolean hasNextBufferedReader()
  {
    return hasNextInputStream();
  }

  /** {@inheritDoc} */
  public void closeInputStream() throws ProviderException
  {
    if (_localInputStream != null)
    {
      try
      {
        _localInputStream.close();
      }
      catch (IOException ioe)
      {
        //TODO gestion erreur ??
        throw new ProviderException(BatchErrorDetail.UNSUPPORTED_ENCODING, ioe);
      }
    }
  }

  /** {@inheritDoc} */
  protected OutputStream getOutputStream()
  {
    //TODO ce provider ne fonctionne pas en ecriture
    throw new UnsupportedOperationException("ce provider ne supporte pas l'ecriture");
  }

  /** {@inheritDoc} */
  public void closeOutputStream()
  {
    // TODO log info try close output stream sur provider de lecture uniquement
    //ce provider ne fonctionne pas en ecriture
  }

  /** {@inheritDoc} */
  public boolean providerDefinitionUpdated()
  {
    return false;
  }
}
