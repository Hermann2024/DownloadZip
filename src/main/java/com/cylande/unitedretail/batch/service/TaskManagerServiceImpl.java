package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.repository.TaskRepository;
import com.cylande.unitedretail.batch.service.common.TaskManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.TaskCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskKeyType;
import com.cylande.unitedretail.message.batch.TaskListType;
import com.cylande.unitedretail.message.batch.TaskScenarioType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Service qui permet de créer, supprimer et modifier des Taches.
 * @author eselosse.
 * @since 05/03/08.
 */
public class TaskManagerServiceImpl extends AbstractCRUDServiceImpl implements TaskManagerService
{

  private TaskRepository _taskRepository = null;

  /**
   * Constructeur
   */
  public TaskManagerServiceImpl()
  {
    _taskRepository = TaskRepository.getInstance();
  }

  /** {@inheritDoc} */
  public TaskType createTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskType result = null;
    getChrono().start();
    getJAXBManager().write(pTask, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.createTask(pTask, pScenario, pContext);
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
  public TaskListType createTaskList(TaskListType pList, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.createTaskList(pList, pScenario, pContext);
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
  public void deleteTask(TaskKeyType pKey, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _taskRepository.deleteTask(pKey, pScenario, pContext);
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
  public void deleteTaskList(TaskCriteriaListType pCriterias, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _taskRepository.deleteTaskList(pCriterias, pScenario, pContext);
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
  public TaskListType findTask(TaskCriteriaListType pCriterias, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.findTask(pCriterias, pScenario, pContext);
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
  public TaskType getTask(TaskKeyType pKey, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.getTask(pKey, pScenario, pContext);
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
  public TaskType updateTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskType result = null;
    getChrono().start();
    getJAXBManager().write(pTask, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.updateTask(pTask, pScenario, pContext);
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
  public TaskListType updateTaskList(TaskListType pList, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.updateTaskList(pList, pScenario, pContext);
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
  public TaskType postTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskType result = null;
    getChrono().start();
    getJAXBManager().write(pTask, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.postTask(pTask, pScenario, pContext);
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
  public TaskListType postTaskList(TaskListType pList, TaskScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    TaskListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _taskRepository.postTaskList(pList, pScenario, pContext);
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
