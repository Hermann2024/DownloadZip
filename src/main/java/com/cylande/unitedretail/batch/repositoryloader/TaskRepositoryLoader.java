package com.cylande.unitedretail.batch.repositoryloader;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.execution.EUJob;
import com.cylande.unitedretail.batch.repository.TaskPropertiesRepository;
import com.cylande.unitedretail.batch.service.TaskManagerServiceImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.DISPATCHER;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.THREADPOOLED;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.batch.Tasks;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.engineproperties.PropertyListType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;

/**
 * Load Tasks and Populate it in TaskRepository
 */
public class TaskRepositoryLoader extends AbstractRepositoryLoader
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(TaskRepositoryLoader.class);
  /** nom de la propri�t� ciblant le fichier de d�finitions de providers � charger */
  private static final String TASK_PROPERTY_NAME = "task.dir";
  /** l'ensemble des d�finition de tasks */
  private Tasks _tasks = null;
  /** gestionnaire de d�finitions de tasks */
  private TaskManagerServiceImpl _taskManager = null;

  /**
   * Default Constructor : orchestraction of action (load and populate)
   * @throws ConfigEnginePropertiesException exception
   */
  public TaskRepositoryLoader() throws ConfigEnginePropertiesException
  {
    super();
    _taskManager = new TaskManagerServiceImpl();
    _repoPropertyName = TASK_PROPERTY_NAME;
    loadProperties();
  }

  /**
   * Load Tasks File and convert it in Tasks Object
   * @param pFileName : path of the Tasks File
   */
  public void load(String pFileName)
  {
    LOGGER.debug("Lecture des descriptions de tasks");
    if (pFileName != null && !pFileName.equals(""))
    {
      if (fileExists(pFileName))
      {
        _tasks = new Tasks();
        _tasks = (Tasks)_manager.read(pFileName, _tasks);
      }
    }
    if (_tasks == null)
    {
      LOGGER.warn("Aucune task n'a �t� d�finie");
      _tasks = new Tasks();
    }
    LOGGER.info(_tasks.getTask().size() + " tasks ont �t� pr�charg�es");
  }

  /**
   * Populate Tasks Object in TaskRepository thanks to TaskManager (contains Service CRUD of Repository)
   * @throws Exception exception
   */
  public void populate() throws BatchException
  {
    if (_tasks == null)
    {
      throw new BatchException(BatchErrorDetail.TASK_REPO_NOLIST);
    }
    LOGGER.debug("Alimentation de la repository de tasks");
    populateTask();
    populateTaskProperties();
  }

  /**
   * Alimente le r�f�rentiel de Tasks
   * @throws BatchException exception
   */
  private void populateTask() throws BatchException
  {
    try
    {
      ContextType ctx = ContextTransformer.fromLocale();
      for (TaskType bean: _tasks.getTask())
      {
        DISPATCHER taskDispatch = null;
        if (!EUJob.getClusterMode())
        {
          // Cr�� nos task multi thread
          taskDispatch = createMultiThreadTasks(bean);
        }
        if (taskDispatch != null)
        {
          _taskManager.createTask(taskDispatch, null, ctx);
        }
        else
        {
          _taskManager.createTask(bean, null, ctx);
        }
      }
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.TASK_REPO_POPULATE, e);
    }
  }

  /**
   * On va cr�er un nombre de task �gal au nombre de thread demand� + 1, celle en plus sera le dispatcher,
   * la t�che qui contient le provider de lecture
   * @param pTask T�che
   * @return Le dispatcher
   * @throws WrapperServiceException WrapperServiceException
   */
  private DISPATCHER createMultiThreadTasks(TaskType pTask) throws WrapperServiceException
  {
    DISPATCHER result = null;
    if (pTask instanceof INTEGRATION)
    {
      INTEGRATION intTask = (INTEGRATION)pTask;
      if (intTask.getThreadCount() != null && intTask.getThreadCount().intValue() > 1)
      {
        result = new DISPATCHER();
        for (int i = 0; i < intTask.getThreadCount().intValue(); i++)
        {
          THREADPOOLED threadTask = new THREADPOOLED();
          // Le nom de la t�che doit �tre unique
          threadTask.setName(intTask.getName() + EUJob.THREAD_POOLED_NAME + i);
          threadTask.setProcessor(intTask.getProcessor());
          threadTask.setUnitReject(intTask.getUnitReject());
          threadTask.setInput(intTask.getInput());
          threadTask.setReject(intTask.getReject());
          threadTask.setResponse(intTask.getResponse());
          _taskManager.createTask(threadTask, null, ContextTransformer.fromLocale());
        }
        // On recr�� notre dispatcher avec les informations de la t�che en entr�e
        result.setCommitFrequency(((INTEGRATION)pTask).getCommitFrequency());
        result.setDescription(pTask.getDescription());
        result.setInput(pTask.getInput());
        result.setName(pTask.getName());
        result.setProcessor(pTask.getProcessor());
        result.setReject(pTask.getReject());
        result.setResponse(pTask.getResponse());
        result.setThreadCount(((INTEGRATION)pTask).getThreadCount());
        result.setUnitReject(((INTEGRATION)pTask).getUnitReject());
        result.setXpath(((INTEGRATION)pTask).getXpath());
      }
    }
    return result;
  }

  /**
   * Alimente le r�f�rentiel des propri�t�s de task
   */
  private void populateTaskProperties()
  {
    PropertyListType propertyList = _tasks.getProperties();
    if (propertyList == null)
    {
      LOGGER.debug("Pas de propri�t� de task � enregistrer");
      return;
    }
    TaskPropertiesRepository.getInstance().putPropertyList(propertyList);
    LOGGER.debug("TaskPropertiesRepository poss�de " + TaskPropertiesRepository.getInstance().getSize() + " propri�t�s");
  }
}
