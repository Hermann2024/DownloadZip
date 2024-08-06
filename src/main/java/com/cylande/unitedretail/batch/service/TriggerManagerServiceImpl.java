package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.repository.BatchPropertiesRepository;
import com.cylande.unitedretail.batch.repository.TriggerRepository;
import com.cylande.unitedretail.batch.service.common.TriggerManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.TriggerCriteriaListType;
import com.cylande.unitedretail.message.batch.TriggerKeyType;
import com.cylande.unitedretail.message.batch.TriggerListType;
import com.cylande.unitedretail.message.batch.TriggerScenarioType;
import com.cylande.unitedretail.message.batch.TriggerType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.tools.EngineProperty;

/** {@inheritDoc} */
public class TriggerManagerServiceImpl extends AbstractCRUDServiceImpl implements TriggerManagerService
{
  private TriggerRepository _triggerRepository = null;

  /**
   * Constructeur
   */
  public TriggerManagerServiceImpl()
  {
    _triggerRepository = TriggerRepository.getInstance();
  }

  /** {@inheritDoc} */
  public TriggerType createTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerType result = null;
    getChrono().start();
    getJAXBManager().write(pTrigger, pScenario, pContext);
    try
    {
      if (pTrigger != null)
      {
        pContext = ContextTransformer.getDefault(pContext);
        EngineProperty activeTrigger = BatchPropertiesRepository.getInstance().getProperty("ACTIVETRIGGER_" + pTrigger.getName(), "default", null);
        if (activeTrigger != null)
        {
          pTrigger.setActive(Boolean.valueOf(activeTrigger.getValue()));
        }
        result = _triggerRepository.createTrigger(pTrigger, pScenario, pContext);
        getJAXBManager().write(result);
      }
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
  public TriggerListType createTriggerList(TriggerListType pList, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.createTriggerList(pList, pScenario, pContext);
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
  public void deleteTrigger(TriggerKeyType pKey, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _triggerRepository.deleteTrigger(pKey, pScenario, pContext);
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
  public void deleteTriggerList(TriggerCriteriaListType pCriterias, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _triggerRepository.deleteTriggerList(pCriterias, pScenario, pContext);
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
  public TriggerListType findTrigger(TriggerCriteriaListType pCriterias, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.findTrigger(pCriterias, pScenario, pContext);
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
  public TriggerType getTrigger(TriggerKeyType pKey, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.getTrigger(pKey, pScenario, pContext);
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
  public TriggerType updateTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerType result = null;
    getChrono().start();
    getJAXBManager().write(pTrigger, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.updateTrigger(pTrigger, pScenario, pContext);
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
  public TriggerListType updateTriggerList(TriggerListType pList, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.updateTriggerList(pList, pScenario, pContext);
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
  public TriggerType postTrigger(TriggerType pTrigger, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerType result = null;
    getChrono().start();
    getJAXBManager().write(pTrigger, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.postTrigger(pTrigger, pScenario, pContext);
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
  public TriggerListType postTriggerList(TriggerListType pList, TriggerScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TriggerListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _triggerRepository.postTriggerList(pList, pScenario, pContext);
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
