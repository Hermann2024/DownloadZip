package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.common.TaskAuditManagerModule;
import com.cylande.unitedretail.batch.service.common.TaskAuditManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskAuditKeyType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskAuditScenarioType;
import com.cylande.unitedretail.message.batch.TaskAuditType;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class TaskAuditManagerServiceImpl extends AbstractCRUDServiceImpl implements TaskAuditManagerService
{

  /**
   * Constructeur
   */
  public TaskAuditManagerServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "TaskAuditManagerModule");
  }

  /** {@inheritDoc} */
  public TaskAuditType createTaskAudit(TaskAuditType pTaskAudit, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskAudit, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createTaskAudit(pTaskAudit, pScenario, pContext);
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
  public TaskAuditListType createTaskAuditList(TaskAuditListType pList, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createTaskAuditList(pList, pScenario, pContext);
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
  public void deleteTaskAudit(TaskAuditKeyType pKey, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteTaskAudit(pKey, pScenario, pContext);
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
  public void deleteTaskAuditList(TaskAuditCriteriaListType pCriterias, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteTaskAuditList(pCriterias, pScenario, pContext);
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
  public TaskAuditListType findTaskAudit(TaskAuditCriteriaListType pCriterias, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.findTaskAudit(pCriterias, pScenario, pContext);
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
  public TaskAuditType getTaskAudit(TaskAuditKeyType pKey, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getTaskAudit(pKey, pScenario, pContext);
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
  public TaskAuditType postTaskAudit(TaskAuditType pTaskAudit, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskAudit, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postTaskAudit(pTaskAudit, pScenario, pContext);
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
  public TaskAuditListType postTaskAuditList(TaskAuditListType pList, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postTaskAuditList(pList, pScenario, pContext);
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
  public TaskAuditType updateTaskAudit(TaskAuditType pTaskAudit, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskAudit, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateTaskAudit(pTaskAudit, pScenario, pContext);
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
  public TaskAuditListType updateTaskAuditList(TaskAuditListType pList, TaskAuditScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    TaskAuditManagerModule myModule = (TaskAuditManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateTaskAuditList(pList, pScenario, pContext);
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
