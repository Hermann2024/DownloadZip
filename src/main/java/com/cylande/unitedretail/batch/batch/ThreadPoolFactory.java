package com.cylande.unitedretail.batch.batch;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.quartz.SchedulerConfigException;
import org.quartz.simpl.SimpleThreadPool;

import com.cylande.unitedretail.batch.execution.quartz.JobManager;
import com.cylande.unitedretail.framework.commonj.CommonjThreadPool;
import com.cylande.unitedretail.framework.commonj.weblogic.WeblogicWorkManagerInfo;

/**
 * Construit une instance d'executor
 */
public final class ThreadPoolFactory
{
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(ThreadPoolFactory.class);
  private static final ThreadPoolFactory THREAD_POOL = new ThreadPoolFactory();
  private Executor _executor;
  private CommonjThreadPool _commonjThreadPool = null;

  private ThreadPoolFactory()
  {
    Class threadPoolClass = JobManager.getInstance().getMetaData().getThreadPoolClass();
    if (SimpleThreadPool.class.getName().equals(threadPoolClass.getName()))
    {
      _executor = Executors.newCachedThreadPool();
    }
    else if (CommonjThreadPool.class.getName().equals(threadPoolClass.getName()))
    {
      // Weblogic ThreadPool
      _commonjThreadPool = new CommonjThreadPool();
      _commonjThreadPool.setWorkManagerName("wm/QUARTZ");
      _commonjThreadPool.setThreadPoolInfoClass(WeblogicWorkManagerInfo.class.getName());
      try
      {
        _commonjThreadPool.initialize();
      }
      catch (SchedulerConfigException e)
      {
        LOGGER.error("[ThreadPoolFactory] error on getInstance", e);
      }
    }
  }

  public static ThreadPoolFactory getInstance()
  {
    return THREAD_POOL;
  }

  public void execute(Runnable pRunnable)
  {
    if (_commonjThreadPool != null)
    {
      _commonjThreadPool.runInThread(pRunnable);
    }
    else
    {
      _executor.execute(pRunnable);
    }
  }
}
