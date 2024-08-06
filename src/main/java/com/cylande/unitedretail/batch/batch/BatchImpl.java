package com.cylande.unitedretail.batch.batch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.exception.EUExecutionException;
import com.cylande.unitedretail.batch.execution.EUJob;
import com.cylande.unitedretail.batch.task.TaskLoader;
import com.cylande.unitedretail.framework.AbstractException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.DISPATCHER;
import com.cylande.unitedretail.message.batch.TaskChildType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.VariablesRepository;

/**
 * Implemention of "Batch" concept. <p>Batch : <ul> <li>is defined in a file xml</li> <li>holds an ordered list of tasks and/or batchs</li> </ul> </p>
 */
public class BatchImpl extends AbstractBatch
{
  private static final Logger LOGGER = Logger.getLogger(BatchImpl.class);
  /** flag indicant la demande d'annulation du batch */
  private boolean _cancelled = false;
  private Map<String, Boolean> _threadTaskNoWaiting = new HashMap();

  /**
   * Constructeur
   * @param pParent : le batch parent
   * @param pBatchDef : la d�finition du batch
   * @param pId : l'Id voulu pour cette instance de batch (peut �tre null)
   */
  protected BatchImpl(AbstractBatch pParent, BatchType pBatchDef, Integer pId)
  {
    super(pParent, pBatchDef, pId);
  }

  /**
   * Constructeur
   * @param pParent : le batch parent
   * @param pBatchDef : la d�finition du batch
   */
  protected BatchImpl(AbstractBatch pParent, BatchType pBatchDef)
  {
    super(pParent, pBatchDef);
  }

  /**
   * Batch execution
   * @throws EUExecutionException : if error occurs during batch execution
   */
  public void execute() throws EUExecutionException
  {
    // Get the batch content childs list
    boolean sequential = true;
    List<BatchChildrenAbstractType> batchContentList = null;
    Integer threadCount = null;
    if (_batchDefinition.getSequence() != null)
    {
      batchContentList = _batchDefinition.getSequence().getTaskOrBatchOrComment();
    }
    else if (_batchDefinition.getFork() != null)
    {
      sequential = false;
      batchContentList = _batchDefinition.getFork().getTaskOrBatchOrComment();
      threadCount = _batchDefinition.getFork().getThreadCount();
    }
    if (batchContentList != null)
    {
      try
      {
        // ajout des task multi-thread�es
        batchContentList = plugThreadTasks(batchContentList);
      }
      catch (Exception e)
      {
        logInfo("ERROR", e);
        BatchException batchEx = new BatchException(BatchErrorDetail.BATCH_EXEC_ERR, new Object[] { "", getSysPath(), getSysId() }, e);
        if (getSysId() != null)
        {
          batchEx.setSysId(getSysId());
        }
        throw batchEx;
      }
      // construction des unitExecution de la contentList
      BatchChildrenAbstractType batchChildRef;
      Iterator batchContentListIterator = batchContentList.iterator();
      boolean wait;
      while ((batchContentListIterator.hasNext()) && (!_cancelled) && isNotTimeOut())
      {
        batchChildRef = (BatchChildrenAbstractType)batchContentListIterator.next();
        try
        {
          // en mode s�quentiel, pas de wait sur les threads d'une tache multi-thread sauf sur le dernier pour respecter l'ordre d'ex�cution
          wait = sequential && _threadTaskNoWaiting.get(batchChildRef.getRef()) != null ? false : sequential;
          launchChild(batchChildRef, wait, threadCount);
        }
        catch (AbstractException e)
        {
          logInfo("ERROR", e);
          throw new BatchException(BatchErrorDetail.BATCH_EXEC_ERR, new Object[] { batchChildRef.getRef(), getSysPath(), getSysId() }, e);
        }
      }
    }
  }

  /**
   * Ajoute des t�ches multi thread
   * @param pBatchOrTask Liste des t�ches/batch � lancer
   * @return la liste de batch contenant tous les �l�ments pBatchOrTask � laquelle ont �t� ajout�es les tasks multi-threads respectant l'ordre d'apparition des tasks dans pBatchOrTask
   * @throws WrapperServiceException WrapperServiceException
   * @throws EUBuildException EUBuildException
   */
  private List<BatchChildrenAbstractType> plugThreadTasks(List<BatchChildrenAbstractType> pBatchOrTask) throws WrapperServiceException, EUBuildException
  {
    List<BatchChildrenAbstractType> result = new ArrayList();
    // On ne rajoute pas nos task multi thread si elles sont d�j� pr�sentes
    for (BatchChildrenAbstractType batchChildRef: pBatchOrTask)
    {
      if (batchChildRef instanceof TaskChildType && batchChildRef.getRef().contains(EUJob.THREAD_POOLED_NAME))
      {
        return pBatchOrTask;
      }
    }
    // Ajoute une task multi thread par nombre de thread demand�
    for (BatchChildrenAbstractType batchChildRef: pBatchOrTask)
    {
      result.add(batchChildRef);
      if (batchChildRef instanceof TaskChildType)
      {
        TaskType taskDef = TaskLoader.loadTaskDef(batchChildRef.getRef());
        if (taskDef instanceof DISPATCHER)
        {
          _threadTaskNoWaiting.put(batchChildRef.getRef(), false);
          DISPATCHER intTask = (DISPATCHER)taskDef;
          if (intTask.getThreadCount() != null && intTask.getThreadCount().intValue() > 0)
          {
            for (int j = 0; j < intTask.getThreadCount().intValue(); j++)
            {
              TaskChildType taskChildType = new TaskChildType();
              taskChildType.setActiveDomain(batchChildRef.getActiveDomain());
              taskChildType.setFailOnError(batchChildRef.getFailOnError());
              // La r�f�rence doit �tre unique
              taskChildType.setRef(intTask.getName() + EUJob.THREAD_POOLED_NAME + j);
              result.add(taskChildType);
              // en mode s�quentiel, pas de wait sur les threads d'une tache multi-thread sauf sur le dernier pour respecter l'ordre d'ex�cution
              if (j < intTask.getThreadCount().intValue() - 1)
              {
                _threadTaskNoWaiting.put(taskChildType.getRef(), false);
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Enregistre la r�f�rence de la propENGrepo utilis�e par l'instance de batchEngine
   * @param pPropENGrepo les propri�t� Engine du batch
   */
  protected void setEngineProperties(PropertiesRepository pPropENGrepo)
  {
    super.setPropertiesEngRepo(pPropENGrepo);
  }

  /**
   * Enregistre la r�f�rence de la varENGrepo utilis�e par l'instance de batchEngine
   * @param pVarENGrepo les variables Engine du batch
   */
  protected void setEngineVariables(VariablesRepository pVarENGrepo)
  {
    super.setVariablesEngRepo(pVarENGrepo);
  }

  /**
   * Annule l'ex�cution du batch
   * @param pForced true : arr�t immediat du batch false : attente de fin d'ex�cution des op�rations en cours.
   */
  protected void doCancel(boolean pForced)
  {
    // pour un batch, que la demande d'annulation soit immediate ou non,
    // le comportement est le m�me : on arrete de cr�er de nouvelles sous-unit� d'ex�cution.
    _cancelled = true;
  }

  protected boolean isNotTimeOut()
  {
    boolean result = true;
    if (_propManager.getSysObject(SYS_ROOT_TIMEOUT_KEY_NAME, null) != null)
    {
      Integer timeOut = (Integer)_propManager.getSysObject(SYS_ROOT_TIMEOUT_KEY_NAME);
      Calendar startTime = (Calendar)_propManager.getSysObject(SYS_ROOT_START_TIME_KEY_NAME);
      if (Calendar.getInstance().getTimeInMillis() > (startTime.getTimeInMillis() + timeOut * 60000L))
      {
        LOGGER.debug("Batch " + getSysPath() + " is time out");
        result = false;
      }
    }
    return result;
  }
}
