package com.cylande.unitedretail.batch.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.jgroups.Channel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.quartz.JobExecutionContext;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EULaunchException;
import com.cylande.unitedretail.batch.execution.EUJobManager;
import com.cylande.unitedretail.batch.execution.EUJobParams;
import com.cylande.unitedretail.batch.execution.ExecutionUnit;
import com.cylande.unitedretail.batch.execution.quartz.TriggerJob;
import com.cylande.unitedretail.framework.cache.URCacheImpl;
import com.cylande.unitedretail.framework.cache.exceptions.URCacheException;
import com.cylande.unitedretail.framework.exception.FileManagementException;
import com.cylande.unitedretail.framework.tools.filemanagement.DirectoryFileManager;
import com.cylande.unitedretail.framework.tools.filemanagement.SortType;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.QueueEnum;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;
import com.cylande.unitedretail.process.tools.PropertiesRepository;
import com.cylande.unitedretail.process.tools.VariablesRepository;

public class BatchQueueManager
{
  private static final Logger LOGGER = Logger.getLogger(BatchQueueManager.class);
  private static final String CLASS_INFO = " [BatchQueueManager] ";
  private static final ConcurrentMap<String, BatchQueue> QUEUE_MAP = new ConcurrentHashMap();
  //private static final AbstractMap<String, BatchQueue> QUEUE_MAP = getQueueMap();
  private static String _workDir;

  public void add(String pQueueName, String pActiveDomain, String pAlternativeDomain, VariablesRepository pVarRepo, PropertiesRepository propRepo, SiteKeyType pSiteKey)
  {
    BatchQueue queue = QUEUE_MAP.get(pQueueName);
    if (queue != null)
    {
      purgeQueueFile(queue, pActiveDomain, pAlternativeDomain);
      String activeDomain = pActiveDomain != null ? pActiveDomain : "";
      String alternativeDomain = pAlternativeDomain != null ? pAlternativeDomain : "";
      String eltId = pQueueName + (queue.getType() == QueueEnum.ONLYLASTDOMAIN ? "" : "_" + activeDomain + "_" + alternativeDomain) + "_" + queue.getType() + "_" + Calendar.getInstance().getTimeInMillis();
      writeObject(_workDir + eltId + "_varrepo", pVarRepo);
      writeObject(_workDir + eltId + "_proprepo", propRepo);
      writeObject(_workDir + eltId + "_site", pSiteKey);
      add(queue, eltId);
    }
  }

  private static AbstractMap<String, BatchQueue> getQueueMap()
  {
    AbstractMap<String, BatchQueue> result = null;
    try
    {
      Channel channel = URCacheImpl.getInstance().getChannel();
      result = channel != null ? new ReplicatedHashMap(channel) : new ConcurrentHashMap();
    }
    catch (URCacheException e)
    {
      LOGGER.error(e, e);
    }
    return result;
  }

  public void add(String pQueueName, ExecutionUnit parent, BatchChildrenAbstractType pBatchChildRef)
  {
    BatchQueue queue = QUEUE_MAP.get(pQueueName);
    if (queue != null)
    {
      String activeDomain = pBatchChildRef.getActiveDomain() != null ? pBatchChildRef.getActiveDomain() : "";
      String defaultDomain = pBatchChildRef.getDefaultDomain() != null ? pBatchChildRef.getDefaultDomain() : "";
      purgeQueueFile(queue, activeDomain, defaultDomain);
      String eltId = pQueueName + (queue.getType() == QueueEnum.ONLYLASTDOMAIN ? "" : "_" + activeDomain + "_" + defaultDomain) + "_" + queue.getType() + "_" + Calendar.getInstance().getTimeInMillis();
      writeObject(_workDir + eltId + "_parent", parent);
      writeObject(_workDir + eltId + "_def", pBatchChildRef);
      add(queue, eltId);
    }
  }

  public void add(String pQueueName, String pActiveDomain, String pAlternativeDomain, JobExecutionContext pJobExecutionContext)
  {
    BatchQueue queue = QUEUE_MAP.get(pQueueName);
    if (queue != null)
    {
      purgeQueueFile(queue, pActiveDomain, pAlternativeDomain);
      String activeDomain = pActiveDomain != null ? pActiveDomain : "";
      String alternativeDomain = pAlternativeDomain != null ? pAlternativeDomain : "";
      String eltId = pQueueName + (queue.getType() == QueueEnum.ONLYLASTDOMAIN ? "" : "_" + activeDomain + "_" + alternativeDomain) + "_" + queue.getType() + "_" + Calendar.getInstance().getTimeInMillis();
      writeObject(_workDir + eltId + "_jobctx", pJobExecutionContext);
      add(queue, eltId);
    }
  }

  private void purgeQueueFile(BatchQueue pQueue, String pActiveDomain, String pAlternativeDomain)
  {
    if (pQueue.getType() == QueueEnum.ONLYLAST || pQueue.getType() == QueueEnum.ONLYLASTDOMAIN)
    {
      String prefix = pQueue.getType() == QueueEnum.ONLYLAST ? pQueue.getName() : pQueue.getName() + "_" + pQueue.getType() + "_";
      // on supprime tous les fichiers commençant par prefix
      File dir = new File(_workDir);
      for (File file: dir.listFiles())
      {
        if (file.getName().startsWith(prefix))
        {
          file.delete();
        }
      }
    }
  }

  private void writeObject(String pFileName, Object pObj)
  {
    try
    {
      writeObject(new FileOutputStream(pFileName), pObj);
    }
    catch (Exception e)
    {
      LOGGER.error(CLASS_INFO + "Error during queue object serialization for file " + pFileName, e);
    }
  }

  private void writeObject(FileOutputStream pFile, Object pObj) throws IOException
  {
    ObjectOutputStream oos = null;
    try
    {
      oos = new ObjectOutputStream(pFile);
      oos.writeObject(pObj);
      oos.flush();
    }
    finally
    {
      if (oos != null)
      {
        oos.close();
      }
    }
  }

  /**
   * Ajoute un élément à la file d'attente
   * @param pQueue la file d'attente
   * @param pElementId id de l'élément à ajouter à la file d'attente (équivalent à un numéro de ticket)
   */
  private void add(BatchQueue pQueue, String pElementId)
  {
    LOGGER.info(CLASS_INFO + "Add element " + pElementId + " in queue " + pQueue.getName());
    if (pQueue.getType() == QueueEnum.FIFO)
    {
      pQueue.getElementList().addLast(pElementId);
    }
    else if (pQueue.getType() == QueueEnum.LIFO)
    {
      pQueue.getElementList().addFirst(pElementId);
    }
    else if (pQueue.getType() == QueueEnum.ONLYLAST || pQueue.getType() == QueueEnum.ONLYLASTDOMAIN)
    {
      if (!pQueue.getElementList().isEmpty())
      {
        pQueue.getElementList().removeFirst();
      }
      pQueue.getElementList().addFirst(pElementId);
    }
  }

  public void next(String pQueueName)
  {
    BatchQueue queue = QUEUE_MAP.get(pQueueName);
    if (queue != null)
    {
      synchronized (queue)
      {
        queue.setBlocked(false);
        String eltId = queue.next();
        if (eltId != null)
        {
          ThreadPoolFactory.getInstance().execute(new BatchQueueRunnable(pQueueName, eltId));
        }
      }
    }
  }

  protected void launch(String pQueueName, String pElementId)
  {
    LOGGER.info(CLASS_INFO + "Get element " + pElementId + " in queue " + pQueueName);
    ExecutionUnit parent = (ExecutionUnit)readObject(_workDir + pElementId + "_parent");
    JobExecutionContext jobCtx = (JobExecutionContext)readObject(_workDir + pElementId + "_jobctx");
    try
    {
      if (parent != null)
      {
        new EUJobManager().launchChild(parent, (BatchChildrenAbstractType)readObject(_workDir + pElementId + "_def"));
      }
      else if (jobCtx != null)
      {
        new TriggerJob().execute(jobCtx);
      }
      else
      {
        String[] ids = pElementId.split("_");
        VariablesRepository varRepo = (VariablesRepository)readObject(_workDir + pElementId + "_varrepo");
        PropertiesRepository propRepo = (PropertiesRepository)readObject(_workDir + pElementId + "_proprepo");
        SiteKeyType site = (SiteKeyType)readObject(_workDir + pElementId + "_site");
        new EUJobManager().launchRoot(ids[0], ids[1], ids[2], varRepo, propRepo, site, true);
      }
    }
    catch (Exception e)
    {
      if (e instanceof EULaunchException && ((EULaunchException) e).getCanonicalCode().equals(BatchErrorDetail.LOCKED_BATCH_EXECUTION.getCanonicalCode()))
      {
        // inutile d'afficher la trace complète de l'erreur quand le moteur est verrouillé
        LOGGER.error(CLASS_INFO + "Error on getting element " + pElementId + " in queue " + pQueueName);
        if (parent != null && jobCtx != null)
        {
          // appel au next sur un batch enfant pour purger la file d'attente associée
          // car le next est normalement fait sur le ChildBatchJobListener mais qui n'a pas pu être déclenché dans ce cas
          // car le job n'a pas été créé
          BatchType batchDef = (BatchType)jobCtx.getJobDetail().getJobDataMap().get(EUJobParams.BATCH_DEFINITION_KEY.getName());
          next(batchDef.getName());
        }
      }
      else
      {
        LOGGER.error(CLASS_INFO + "Error on getting element " + pElementId + " in queue " + pQueueName, e);
      }
    }
  }

  private Object readObject(String pFileName)
  {
    try
    {
      File file = new File(pFileName);
      if (file.exists())
      {
        ObjectInputStream ois = null;
        try
        {
          ois = new ObjectInputStream(new FileInputStream(file));
          return ois.readObject();
        }
        finally
        {
          if (ois != null)
          {
            ois.close();
          }
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.error(CLASS_INFO + "Error during queue object reading for file " + pFileName, e);
    }
    return null;
  }

  /**
   * Bloque la file d'attente si celle-ci est libre
   * @param pQueueName le nom de la file d'attente
   * @param pQueueType le type de la file d'attente utilisé pour son éventuel initialisation
   * @return true si la file d'attente était libre avant son blocage (équivalent à un droit de passage)
   */
  public boolean blockIfFree(String pQueueName, QueueEnum pQueueType)
  {
    QUEUE_MAP.putIfAbsent(pQueueName, new BatchQueue(pQueueName, pQueueType));
    BatchQueue queue = QUEUE_MAP.get(pQueueName);
    if (queue != null)
    {
      synchronized (queue)
      {
        if (!queue.isBlocked())
        {
          queue.setBlocked(true);
          return true;
        }
      }
    }
    return false;
  }

  public String getQueueName(String pBatchName, QueueEnum pQueueType, String pActiveDomain, String pAlternativeDomain)
  {
    if (pQueueType == QueueEnum.ONLYLASTDOMAIN)
    {
      return pBatchName +  "_" + (pActiveDomain != null ? pActiveDomain : "") + "_" + (pAlternativeDomain != null ? pAlternativeDomain : "");
    }
    return pBatchName;
  }

  public static String getWorkDir()
  {
    return _workDir;
  }

  /**
   * Remplie et relance les files d'attente avec les éléments présent dans le dossier de travail
   * @throws FileManagementException exception
   * @throws ConfigEnginePropertiesException exception
   */
  public void load() throws FileManagementException, ConfigEnginePropertiesException
  {
    setWorkDir(ConfigEngineProperties.getInstance().getDirectoryEngineProperties("temporaryfile.dir") + "/");
    DirectoryFileManager fileManager = new DirectoryFileManager(_workDir);
    fileManager.setSortType(SortType.MODIFTIME);
    List<String> fileList = fileManager.listFiles("*");
    for (String fileName: fileList)
    {
      String[] ids = fileName.split("_");
      if (ids.length == 6)
      {
        String batchName = ids[0];
        QueueEnum queueType = getQueueEnum(ids[3]);
        if (queueType != null)
        {
          String queueName = getQueueName(batchName, queueType, ids[1], ids[2]);
          BatchQueue newQueue = new BatchQueue(queueName, queueType);
          BatchQueue queue = QUEUE_MAP.putIfAbsent(queueName, newQueue);
          if (queue == null)
          {
            queue = newQueue;
          }
          String elementId = fileName.substring(0, fileName.lastIndexOf("_"));
          if (!queue.getElementList().contains(elementId))
          {
            add(queue, elementId);
          }
        }
      }
    }
    // relance de toutes les files d'attente dans un thread afin de les rendre indépendantes les unes des autres et
    // de ne pas bloquer la phase d'init au rédémarrage
    for (String queueName: QUEUE_MAP.keySet())
    {
      next(queueName);
    }
  }

  private QueueEnum getQueueEnum(String pEnum)
  {
    QueueEnum result = null;
    try
    {
      result = QueueEnum.valueOf(pEnum);
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
    return result;
  }

  private static void setWorkDir(String pWorkDir)
  {
    _workDir = pWorkDir;
  }
}
