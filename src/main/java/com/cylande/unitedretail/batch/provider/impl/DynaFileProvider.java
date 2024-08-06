package com.cylande.unitedretail.batch.provider.impl;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.message.batch.DYNAFILEPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

import java.io.File;

/**
 * Provider utilisé par settings management
 */
public class DynaFileProvider extends FileProvider
{

  /** nom du fichier ciblé par le provider */
  private String _fileName = null;

  /** fichier dynamique généré */
  private File _generatedFile = null;

  /**
   * Constructeur
   * @param pFileName
   * @param pProviderDef
   * @param pPropertiesManager
   * @param pDomain
   */
  public DynaFileProvider(String pFileName, ProviderType pProviderDef, PropertiesManager pPropertiesManager, String pDomain, String pAlternativeDomain) throws ProviderException
  {
    // TODO editeur fichier remplacer le mot vide
    super(pProviderDef, "", null, pPropertiesManager, pDomain, pAlternativeDomain);
    _fileName = pFileName;
    initDynaFileProvider();
  }

  private void initDynaFileProvider()
  {
    if ((getProviderDef() != null) && (getProviderDef() instanceof DYNAFILEPROVIDER))
    {
      _fileProviderDef = new ProviderFileType();
      _fileProviderDef.setFileName(_fileName);
      _fileProviderDef.setDir(((DYNAFILEPROVIDER)getProviderDef()).getDir());
      _fileProviderDef.setOverWrite(true);
    }
  }
}
