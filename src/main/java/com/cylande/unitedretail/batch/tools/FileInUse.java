package com.cylande.unitedretail.batch.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * Locker de fichier en cours d'utilisation
 */
public final class FileInUse
{
  /**
   * Logger
   */
  private static final Logger LOGGER = Logger.getLogger(FileInUse.class);

  /**
   * Map des fichiers en cours d'utilisation
   */
  private static final Map<String, FileInUse> TASK_FILE_IN_USE = new ConcurrentHashMap<String, FileInUse>();

  /**
   * Nombre de tasks utilisant le fichiers
   */
  private AtomicInteger _tasksCount = new AtomicInteger(0);

  /**
   * Lock permettant d'attendre la liberation des _tasksCount (=0)
   */
  private Object _mutex = new Object();

  /**
   * Constructeur
   */
  private FileInUse()
  {
  }

  /**
   * Permet d'ajouter et/ou d'obtenir un pointeur sur le locker de fichier
   * @param pFileName nom du fichier
   * @return résultat
   */
  public static synchronized FileInUse getFileInUse(String pFileName)
  {
    return TASK_FILE_IN_USE.get(pFileName);
  }

  /**
   * Permet d'ajouter et/ou d'obtenir un pointeur sur le locker de fichier
   * @param pFileName nom du fichier
   * @return résultat
   */
  public static synchronized void registerFileInUse(String pFileName)
  {
    LOGGER.debug("registerFileInUse FILE  " + pFileName);
    TASK_FILE_IN_USE.put(pFileName, new FileInUse());
  }

  /**
   * Supprime l'utilisation d'un fichiers
   * @param pFileName nom du fichier
   */
  public static synchronized void removeFileInUse(String pFileName)
  {
    LOGGER.debug("REMOVE FILE  " + pFileName);
    TASK_FILE_IN_USE.remove(pFileName);
  }

  /**
   * incremente le pointeur d'utisation d'un fichier
   */
  public void increment()
  {
    synchronized (_tasksCount)
    {
      int val = _tasksCount.incrementAndGet();
      LOGGER.debug("increment AFTER = " + val);
    }
  }

  /**
   * decremente le pointeur d'utisation d'un fichier
   * notifie le wait si il n'y a plus de pointeurs
   */
  public void decrement()
  {
    synchronized (_tasksCount)
    {
      int val = _tasksCount.get();
      LOGGER.debug("DECREMENT BEFORE = " + val);
      if (val == 0)
      {
        return;
      }
      val = _tasksCount.decrementAndGet();
      if (val == 0)
      {
        synchronized (_mutex)
        {
          LOGGER.debug("NOTIFY");
          _mutex.notifyAll();
          LOGGER.debug("END NOTIFY");
        }
      }
    }
  }

  /**
   * Attend la notification de la liberation des tasks utilisant le fichier
   * @throws InterruptedException exception
   */
  public void waitForFree() throws InterruptedException
  {
    int waiting = 0;
    synchronized (_tasksCount)
    {
      waiting = _tasksCount.get();
      LOGGER.debug("TRY WAIT FOR FREE val=" + waiting);
    }
    if (waiting != 0)
    {
      synchronized (_mutex)
      {
        waiting = _tasksCount.get();
        if (waiting != 0)
        {
          LOGGER.debug("WAIT FOR FREE");
          _mutex.wait();
        }
        LOGGER.debug("END WAIT FOR FREE");
      }
    }
  }
}
