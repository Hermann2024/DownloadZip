package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.common.FileProviderTraceManagerModule;
import com.cylande.unitedretail.batch.service.common.FileProviderTraceManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.FileProviderTraceCriteriaListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceKeyType;
import com.cylande.unitedretail.message.batch.FileProviderTraceListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceScenarioType;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class FileProviderTraceManagerServiceImpl extends AbstractCRUDServiceImpl implements FileProviderTraceManagerService
{

  /**
   * constructr
   */
  public FileProviderTraceManagerServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "FileProviderTraceManagerModule");
  }

  /** {@inheritDoc} */
  public FileProviderTraceType createFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceType result = null;
    getChrono().start();
    getJAXBManager().write(pFileProviderTrace, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createFileProviderTrace(pFileProviderTrace, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public FileProviderTraceListType createFileProviderTraceList(FileProviderTraceListType pList, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createFileProviderTraceList(pList, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void deleteFileProviderTrace(FileProviderTraceKeyType pKey, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteFileProviderTrace(pKey, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
  }

  /** {@inheritDoc} */
  public void deleteFileProviderTraceList(FileProviderTraceCriteriaListType pCriterias, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteFileProviderTraceList(pCriterias, pScenario, pContext);
      getTransaction().commit(pContext);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
  }

  /** {@inheritDoc} */
  public FileProviderTraceListType findFileProviderTrace(FileProviderTraceCriteriaListType pCriterias, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceListType result = null;
    getChrono().start();
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      getJAXBManager().write(pCriterias, pScenario, pContext);
      FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
      result = myModule.findFileProviderTrace(pCriterias, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public FileProviderTraceType getFileProviderTrace(FileProviderTraceKeyType pKey, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getFileProviderTrace(pKey, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public FileProviderTraceType postFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceType result = null;
    getChrono().start();
    getJAXBManager().write(pFileProviderTrace, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postFileProviderTrace(pFileProviderTrace, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public FileProviderTraceListType postFileProviderTraceList(FileProviderTraceListType pList, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postFileProviderTraceList(pList, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public FileProviderTraceType updateFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceType result = null;
    getChrono().start();
    getJAXBManager().write(pFileProviderTrace, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateFileProviderTrace(pFileProviderTrace, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }

  /** {@inheritDoc} */
  public FileProviderTraceListType updateFileProviderTraceList(FileProviderTraceListType pList, FileProviderTraceScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    FileProviderTraceListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    FileProviderTraceManagerModule myModule = (FileProviderTraceManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateFileProviderTraceList(pList, pScenario, pContext);
      getTransaction().commit(pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      getTransaction().rollback(pContext);
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
      release(pContext);
    }
    return result;
  }
}
