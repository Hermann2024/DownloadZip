package com.cylande.unitedretail.batch.exception;

import com.cylande.unitedretail.framework.URException;

public class BatchExceptionManager
{
  private static final String BATCH_PACKAGE_NAME = "com.cylande.unitedretail.batch";
  private static final String PROCESS_PACKAGE_NAME = "com.cylande.unitedretail.process";
  private static final String[] EXCEPTION_FILTER_LIST = { BATCH_PACKAGE_NAME, PROCESS_PACKAGE_NAME };

  public static Throwable getCause(Throwable pException)
  {
    Throwable result = null;
    if (pException != null)
    {
      result = pException.getCause();
      if ((result == null) && (pException instanceof URException))
      {
        Object[] details = ((URException)pException).returnDetails();
        if (details.length > 0)
        {
          result = (Throwable)details[0];
        }
      }
    }
    return result;
  }

  private static boolean isInBatchExceptionList(Throwable pException, String[] pExceptionList)
  {
    boolean result = false;
    if ((pException != null) && (pExceptionList != null))
    {
      String filter;
      for (int i = 0; i < pExceptionList.length; i++)
      {
        filter = pExceptionList[i];
        result = result || pException.getClass().getName().startsWith(filter);
      }
    }
    return result;
  }

  public static boolean isBatchInternalException(Throwable pException)
  {
    boolean result = false;
    result = isInBatchExceptionList(pException, EXCEPTION_FILTER_LIST);
    return result;
  }

  public static Throwable getLastExternalException(Throwable pException)
  {
    Throwable result = pException;
    while ((result != null) && isBatchInternalException(result))
    {
      result = getCause(result);
    }
    return result;
  }

  public static Throwable getFirstInternalException(Throwable pException)
  {
    //pException.printStackTrace(System.out);
    Throwable result = pException;
    while ((result != null) && isBatchInternalException(getCause(result)))
    {
      result = getCause(result);
    }
    return result;
  }
}
