package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.repository.ProviderRepository;
import com.cylande.unitedretail.batch.service.common.ProviderManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.ProviderCriteriaListType;
import com.cylande.unitedretail.message.batch.ProviderKeyType;
import com.cylande.unitedretail.message.batch.ProviderListType;
import com.cylande.unitedretail.message.batch.ProviderScenarioType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Service qui permet de créer, supprimer et modifier des Providers.
 * @author eselosse.
 * @since 05/03/08.
 */
public class ProviderManagerServiceImpl extends AbstractCRUDServiceImpl implements ProviderManagerService
{

  private ProviderRepository _providerRepository = null;

  /**
   * Constructeur
   */
  public ProviderManagerServiceImpl()
  {
    _providerRepository = ProviderRepository.getInstance();
  }

  /** {@inheritDoc} */
  public ProviderType createProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderType result = null;
    getChrono().start();
    getJAXBManager().write(pProvider, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.createProvider(pProvider, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderListType createProviderList(ProviderListType pList, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.createProviderList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void deleteProvider(ProviderKeyType pKey, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _providerRepository.deleteProvider(pKey, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
  }

  /** {@inheritDoc} */
  public void deleteProviderList(ProviderCriteriaListType pCriterias, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _providerRepository.deleteProviderList(pCriterias, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
  }

  /** {@inheritDoc} */
  public ProviderListType findProvider(ProviderCriteriaListType pCriterias, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.findProvider(pCriterias, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderType getProvider(ProviderKeyType pKey, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.getProvider(pKey, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderType updateProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderType result = null;
    getChrono().start();
    getJAXBManager().write(pProvider, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.updateProvider(pProvider, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderListType updateProviderList(ProviderListType pList, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.updateProviderList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderType postProvider(ProviderType pProvider, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderType result = null;
    getChrono().start();
    getJAXBManager().write(pProvider, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.postProvider(pProvider, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderListType postProviderList(ProviderListType pList, ProviderScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    ProviderListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _providerRepository.postProviderList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }
}
