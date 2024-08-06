package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.common.TaskRunManagerModule;
import com.cylande.unitedretail.batch.service.common.TaskRunManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskBooleanResponseType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskRunKeyType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunScenarioType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class TaskRunManagerServiceImpl extends AbstractCRUDServiceImpl implements TaskRunManagerService
{

  public TaskRunManagerServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "TaskRunManagerModule");
  }

  /** {@inheritDoc} */
  public TaskRunType createTaskRun(TaskRunType pTaskRun, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskRun, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createTaskRun(pTaskRun, pScenario, pContext);
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
  public TaskRunListType createTaskRunList(TaskRunListType pList, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.createTaskRunList(pList, pScenario, pContext);
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
  public void deleteTaskRun(TaskRunKeyType pKey, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteTaskRun(pKey, pScenario, pContext);
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
  public void deleteTaskRunList(TaskRunCriteriaListType pCriterias, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      myModule.deleteTaskRunList(pCriterias, pScenario, pContext);
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
  public TaskRunListType findTaskRun(TaskRunCriteriaListType pCriterias, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.findTaskRun(pCriterias, pScenario, pContext);
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
  public TaskRunType getTaskRun(TaskRunKeyType pKey, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.getTaskRun(pKey, pScenario, pContext);
      if (pScenario != null && Boolean.TRUE.equals(pScenario.isManageTaskAudit()))
      {
        result.setErrorList(getTaskAuditOfTaskRun(pKey, pScenario, pContext));
      }
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
  public TaskRunType postTaskRun(TaskRunType pTaskRun, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskRun, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postTaskRun(pTaskRun, pScenario, pContext);
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
  public TaskRunListType postTaskRunList(TaskRunListType pList, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.postTaskRunList(pList, pScenario, pContext);
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
  public TaskRunType updateTaskRun(TaskRunType pTaskRun, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskRun, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateTaskRun(pTaskRun, pScenario, pContext);
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
  public TaskRunListType updateTaskRunList(TaskRunListType pList, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskRunListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.updateTaskRunList(pList, pScenario, pContext);
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
  public TaskBooleanResponseType atLeastTaskError(TaskRunKeyType pTaskRunKey, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskBooleanResponseType result = null;
    getChrono().start();
    getJAXBManager().write(pTaskRunKey, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      getTransaction().init(pContext);
      pContext = ContextTransformer.getDefault(pContext);
      result = myModule.atLeastTaskError(pTaskRunKey, pScenario, pContext);
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

  public TaskAuditListType getTaskAuditOfTaskRun(TaskRunKeyType pKey, TaskRunScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskAuditListType response = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    TaskRunManagerModule myModule = (TaskRunManagerModule)getModule(pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      response = myModule.getTaskAuditOfTaskRun(pKey, pScenario, pContext);
      getJAXBManager().write(response);
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
    return response;
  }
}
