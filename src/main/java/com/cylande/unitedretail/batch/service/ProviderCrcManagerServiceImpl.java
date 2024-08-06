package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.common.ProviderCrcManagerModule;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.service.common.ProviderCrcManagerService;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.BusinessServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.message.batch.ProviderCrcKeyType;
import com.cylande.unitedretail.message.batch.ProviderCrcScenarioType;
import com.cylande.unitedretail.message.batch.ProviderCrcType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

/** {@inheritDoc} */
public class ProviderCrcManagerServiceImpl extends AbstractCRUDServiceImpl implements ProviderCrcManagerService
{

  /**
   * Constructeur
   */
  public ProviderCrcManagerServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "ProviderCrcManagerModule");
  }

  private static String providerCrcType2String(ProviderCrcType pProviderCrcType)
  {
    String result;
    if (pProviderCrcType == null)
    {
      result = "ProviderCrcType null";
    }
    else
    {
      StringBuilder sb = new StringBuilder("ProviderCrcType {");
      sb.append("\n\t Crc        = '" + pProviderCrcType.getCrc() + "'");
      sb.append("\n\t BatchName  = '" + pProviderCrcType.getBatchName() + "'");
      sb.append("\n\t Overridden = '" + pProviderCrcType.getOverridden() + "'");
      sb.append("\n }");
      result = sb.toString();
    }
    return result;
  }

  private static String providerCrcKeyType2String(ProviderCrcKeyType pProviderCrcKeyType)
  {
    String result;
    if (pProviderCrcKeyType == null)
    {
      result = "ProviderCrcKeyType null";
    }
    else
    {
      StringBuilder sb = new StringBuilder("ProviderCrcKeyType {");
      sb.append("\n\t Crc = '" + pProviderCrcKeyType.getCrc() + "'");
      sb.append("\n }");
      result = sb.toString();
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderCrcType createProviderCrc(ProviderCrcType pProviderCrcType, ProviderCrcScenarioType pScenario, ContextType pContext) throws RemoteException, BusinessServiceException, TechnicalServiceException
  {
    ProviderCrcType result = null;
    getChrono().start();
    ProviderCrcManagerModule myModule;
    try
    {
      myModule = (ProviderCrcManagerModule)getModule(pContext);
      getTransaction().init(pContext);
      result = myModule.createProviderCrc(pProviderCrcType, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      String[] params = new String[2];
      params[0] = "createProviderCrc";
      params[1] = providerCrcType2String(pProviderCrcType);
      throw new TechnicalServiceException(BatchErrorDetail.PROVIDER_CRC_WRITE_ERROR, params, e);
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderCrcType updateProviderCrc(ProviderCrcType pProviderCrcType, ProviderCrcScenarioType pScenario, ContextType pContext) throws RemoteException, BusinessServiceException, TechnicalServiceException
  {
    ProviderCrcType result = null;
    getChrono().start();
    ProviderCrcManagerModule myModule;
    try
    {
      myModule = (ProviderCrcManagerModule)getModule(pContext);
      getTransaction().init(pContext);
      result = myModule.updateProviderCrc(pProviderCrcType, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      String[] params = new String[2];
      params[0] = "updateProviderCrc";
      params[1] = providerCrcType2String(pProviderCrcType);
      throw new TechnicalServiceException(BatchErrorDetail.PROVIDER_CRC_WRITE_ERROR, params, e);
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderCrcType postProviderCrc(ProviderCrcType pProviderCrcType, ProviderCrcScenarioType pScenario, ContextType pContext) throws RemoteException, BusinessServiceException, TechnicalServiceException
  {
    ProviderCrcType result = null;
    getChrono().start();
    ProviderCrcManagerModule myModule;
    try
    {
      myModule = (ProviderCrcManagerModule)getModule(pContext);
      getTransaction().init(pContext);
      result = myModule.postProviderCrc(pProviderCrcType, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      String[] params = new String[2];
      params[0] = "postProviderCrc";
      params[1] = providerCrcType2String(pProviderCrcType);
      throw new TechnicalServiceException(BatchErrorDetail.PROVIDER_CRC_WRITE_ERROR, params, e);
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public ProviderCrcType getProviderCrc(ProviderCrcKeyType pProviderCrcKeyType, ProviderCrcScenarioType pScenario, ContextType pContext) throws RemoteException, BusinessServiceException, TechnicalServiceException
  {
    ProviderCrcType result = null;
    getChrono().start();
    ProviderCrcManagerModule myModule;
    try
    {
      myModule = (ProviderCrcManagerModule)getModule(pContext);
      getTransaction().init(pContext);
      result = myModule.getProviderCrc(pProviderCrcKeyType, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      String[] params = new String[2];
      params[0] = "getProviderCrc";
      params[1] = providerCrcKeyType2String(pProviderCrcKeyType);
      throw new TechnicalServiceException(BatchErrorDetail.PROVIDER_CRC_READ_ERROR, params, e);
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void deleteProviderCrc(ProviderCrcKeyType pProviderCrcKeyType, ProviderCrcScenarioType pScenario, ContextType pContext) throws RemoteException, BusinessServiceException, TechnicalServiceException
  {
    getChrono().start();
    ProviderCrcManagerModule myModule;
    try
    {
      myModule = (ProviderCrcManagerModule)getModule(pContext);
      getTransaction().init(pContext);
      myModule.deleteProviderCrc(pProviderCrcKeyType, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      String[] params = new String[2];
      params[0] = "deleteProviderCrc";
      params[1] = providerCrcKeyType2String(pProviderCrcKeyType);
      throw new TechnicalServiceException(BatchErrorDetail.PROVIDER_CRC_WRITE_ERROR, params, e);
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
  }

  /** {@inheritDoc} */
  public ProviderCrcType removeProviderCrc(ProviderCrcKeyType pProviderCrcKeyType, ProviderCrcScenarioType pScenario, ContextType pContext) throws RemoteException, BusinessServiceException, TechnicalServiceException
  {
    ProviderCrcType result = null;
    getChrono().start();
    ProviderCrcManagerModule myModule;
    try
    {
      myModule = (ProviderCrcManagerModule)getModule(pContext);
      getTransaction().init(pContext);
      result = myModule.removeProviderCrc(pProviderCrcKeyType, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      Object[] params = new Object[2];
      params[0] = "removeProviderCrc";
      params[1] = providerCrcKeyType2String(pProviderCrcKeyType);
      throw new TechnicalServiceException(BatchErrorDetail.PROVIDER_CRC_WRITE_ERROR, params, e);
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }
}
