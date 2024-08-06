package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.common.BatchApplicationModule;
import com.cylande.unitedretail.batch.service.common.BatchApplicationService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractApplicativeServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.common.CommonScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.processapplication.ProcessIndicatorType;
import com.cylande.unitedretail.message.common.processapplication.ProcessSiteIndicatorListType;

import java.rmi.RemoteException;

/**
 * Service applicatif dédié aux batchs
 */
public class BatchApplicationServiceImpl extends AbstractApplicativeServiceImpl implements BatchApplicationService
{
  /**
   * Constructeur
   */
  public BatchApplicationServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "BatchApplicationModule");
  }

  /**
   * Recherche de l'indicateur de traitements du jour
   * @param pScenario Scenario
   * @param pContext Contexte
   * @return ProcessIndicatorType
   * @throws RemoteException Exception réseau
   * @throws WrapperServiceException Exception service
   */
  public ProcessIndicatorType getBatchIndicator(CommonScenarioType pScenario, ContextType pContext) throws RemoteException, WrapperServiceException
  {
    ProcessIndicatorType result = null;
    getChrono().start();
    ContextType context = pContext;
    getJAXBManager().write(pScenario, context);
    BatchApplicationModule myModule = (BatchApplicationModule)getModule(context);
    try
    {
      getTransaction().init(context);
      context = ContextTransformer.getDefault(context);
      result = myModule.getBatchIndicator(pScenario, context);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(context));
    }
    finally
    {
      getChrono().stop(this);
      release(context);
    }
    return result;
  }

  /**
   * Recherche de la liste d'indicateurs de traitements par site du jour
   * @param pScenario Scenario
   * @param pContext Contexte
   * @return ProcessSiteIndicatorListType
   * @throws RemoteException Exception réseau
   * @throws WrapperServiceException Exception service
   */
  public ProcessSiteIndicatorListType findBatchSiteIndicator(CommonScenarioType pScenario, ContextType pContext) throws RemoteException, WrapperServiceException
  {
    ProcessSiteIndicatorListType result = null;
    getChrono().start();
    ContextType context = pContext;
    getJAXBManager().write(pScenario, context);
    BatchApplicationModule myModule = (BatchApplicationModule)getModule(context);
    try
    {
      getTransaction().init(context);
      context = ContextTransformer.getDefault(context);
      result = myModule.findBatchSiteIndicator(pScenario, context);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(context));
    }
    finally
    {
      getChrono().stop(this);
      release(context);
    }
    return result;
  }
}
