package com.cylande.unitedretail.batch.provider.rw.build;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.rw.ProviderRW;
import com.cylande.unitedretail.batch.repository.ProviderPropertiesRepository;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.process.tools.EngineSysObjectRepository;
import com.cylande.unitedretail.process.tools.PropertiesManager;
import com.cylande.unitedretail.process.tools.PropertiesRepository;

public abstract class ProviderRWBuilder
{
  private PropertiesManager _propertiesManager = null;
  private String _currentDomain = PropertiesManager.DEFAULT_DOMAIN;
  private String _alternativeDomain = null;
  private ProviderType _providerDef = null;
  private String _name = null;
  private EngineSysObjectRepository _systemRepository = null;
  private PropertiesRepository _propertiesRepository = null;

  public abstract ProviderRW buildProvider(String pProviderName, TaskRunType pTaskRun) throws ProviderException;

  public void setProviderInformations(ProviderType pProviderBean)
  {
    _providerDef = pProviderBean;
  }

  /**
   * Mutateur du domaine d'exécution
   * @param pDomain
   */
  public void setCurrentDomain(String pDomain)
  {
    if ((pDomain != null) && (pDomain.trim().length() > 0))
    {
      _currentDomain = pDomain;
    }
  }

  public void setAlternativeDomain(String pAlternativeDomain)
  {
    if ((pAlternativeDomain != null) && (pAlternativeDomain.trim().length() > 0))
    {
      _alternativeDomain = pAlternativeDomain;
    }
  }

  protected void init()
  {
    // préparation du propertiesManager
    _propertiesManager = new PropertiesManager();
    _propertiesManager.setSYSrepo(_systemRepository);
    _propertiesManager.setPropENGrepo(_propertiesRepository);
    _propertiesManager.setPropGLOrepo(ProviderPropertiesRepository.getInstance());
  }

  protected ProviderType getProviderDef()
  {
    return _providerDef;
  }

  public PropertiesManager getPropertiesManager()
  {
    return _propertiesManager;
  }

  protected String getCurrentDomain()
  {
    return _currentDomain;
  }

  /**
   * Accesseur alternativeDomain
   * @return alternative Domain
   */
  protected String getAlternativeDomain()
  {
    return _alternativeDomain;
  }

  protected String getName()
  {
    return _name;
  }

  protected void setName(String pName)
  {
    _name = pName;
  }

  public void setSystemRepository(EngineSysObjectRepository systemRepository)
  {
    _systemRepository = systemRepository;
  }

  public EngineSysObjectRepository getSystemRepository()
  {
    return _systemRepository;
  }

  public void setPropertiesRepository(PropertiesRepository propertiesRepository)
  {
    _propertiesRepository = propertiesRepository;
  }

  public PropertiesRepository getPropertiesRepository()
  {
    return _propertiesRepository;
  }
}
