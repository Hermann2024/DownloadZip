package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.message.batch.TaskCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskCriteriaType;
import com.cylande.unitedretail.message.batch.TaskKeyType;
import com.cylande.unitedretail.message.batch.TaskListType;
import com.cylande.unitedretail.message.batch.TaskScenarioType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.repository.CriteriaTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Référentiel des définitions de tasks
 */
public final class TaskRepository
{
  /** singleton */
  private static final TaskRepository INSTANCE;
  static {
    INSTANCE = new TaskRepository();
  }

  /** repository of definitions of tasks */
  private ConcurrentHashMap<String, TaskType> _taskRepositoryMap = null;

  /**
   * Constructor
   */
  private TaskRepository()
  {
    _taskRepositoryMap = new ConcurrentHashMap<String, TaskType>();
  }

  /**
   * Get Unique Instance of Repository
   * @return instance of Repository
   */
  public static TaskRepository getInstance()
  {
    return INSTANCE;
  }

  public TaskType createTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext)
  {
    if (pTask != null)
    {
      _taskRepositoryMap.put(pTask.getName(), pTask);
    }
    return pTask;
  }

  public TaskListType createTaskList(TaskListType pList, TaskScenarioType pScenario, ContextType pContext)
  {
    TaskListType result = null;
    if (pList != null)
    {
      result = new TaskListType();
      for (TaskType task: pList.getValues())
      {
        result.getValues().add(createTask(task, pScenario, pContext));
      }
    }
    return result;
  }

  public TaskType getTask(TaskKeyType pKey, TaskScenarioType pScenario, ContextType pContext)
  {
    TaskType result = null;
    if (pKey != null)
    {
      result = _taskRepositoryMap.get(pKey.getName());
    }
    return result;
  }

  public TaskListType findTask(TaskCriteriaListType pCriterias, TaskScenarioType pScenario, ContextType pContext)
  {
    TaskListType result = null;
    if (pCriterias != null)
    {
      if (!pCriterias.getList().isEmpty())
      {
        result = findCriterias(pCriterias, pScenario, pContext);
      }
      else
      {
        result = new TaskListType();
        result.getValues().addAll(_taskRepositoryMap.values());
      }
    }
    return result;
  }

  public void deleteTask(TaskKeyType pKey, TaskScenarioType pScenario, ContextType pContext)
  {
    if (pKey != null)
    {
      if (_taskRepositoryMap.containsKey(pKey.getName()))
      {
        _taskRepositoryMap.remove(pKey.getName());
      }
    }
  }

  public void deleteTaskList(TaskCriteriaListType pCriterias, TaskScenarioType pScenario, ContextType pContext)
  {
    if (pCriterias != null)
    {
      TaskListType pList = findTask(pCriterias, pScenario, pContext);
      for (TaskType task: pList.getValues())
      {
        _taskRepositoryMap.remove(task.getName());
      }
    }
  }

  public TaskType updateTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext)
  {
    return updateTask(pTask, pScenario, pContext, false);
  }

  private TaskType updateTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext, boolean pCreate)
  {
    TaskType result = null;
    if (pTask != null)
    {
      TaskKeyType pKey = new TaskKeyType();
      pKey.setName(pTask.getName());
      TaskType beanTask = getTask(pKey, pScenario, pContext);
      if (beanTask != null)
      {
        result = _taskRepositoryMap.put(pTask.getName(), pTask);
      }
      else if (pCreate)
      {
        result = createTask(pTask, pScenario, pContext);
      }
    }
    return result;
  }

  public TaskListType updateTaskList(TaskListType pList, TaskScenarioType pScenario, ContextType pContext)
  {
    TaskListType result = null;
    if (pList != null)
    {
      result = new TaskListType();
      for (TaskType task: pList.getValues())
      {
        result.getValues().add(updateTask(task, pScenario, pContext));
      }
    }
    return result;
  }

  public TaskType postTask(TaskType pTask, TaskScenarioType pScenario, ContextType pContext)
  {
    return updateTask(pTask, pScenario, pContext, true);
  }

  public TaskListType postTaskList(TaskListType pList, TaskScenarioType pScenario, ContextType pContext)
  {
    TaskListType result = null;
    if (pList != null)
    {
      result = new TaskListType();
      for (TaskType task: pList.getValues())
      {
        result.getValues().add(postTask(task, pScenario, pContext));
      }
    }
    return result;
  }

  public TaskListType findCriterias(TaskCriteriaListType pList, TaskScenarioType pScenario, ContextType pContext)
  {
    TaskListType result = new TaskListType();
    if (pList != null)
    {
      List<TaskType> taskList = new ArrayList<TaskType>();
      for (TaskCriteriaType criteria: pList.getList())
      {
        taskList.addAll(matchTaskCriteria(criteria));
      }
      result.setValues(taskList);
    }
    return result;
  }

  private Set<TaskType> matchTaskCriteria(TaskCriteriaType pCriteria)
  {
    Set<TaskType> resultTaskSet = new HashSet<TaskType>();
    Collection<TaskType> values = _taskRepositoryMap.values();
    for (TaskType task: values)
    {
      if (pCriteria.getName() != null && CriteriaTools.matchCriteriaString(pCriteria.getName(), task.getName()))
      {
        resultTaskSet.add(task);
      }
    }
    return resultTaskSet;
  }
}
