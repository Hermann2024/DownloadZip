package com.cylande.unitedretail.batch.business.module;

import com.cylande.unitedretail.batch.business.module.common.FileProviderTraceManagerModule;
import com.cylande.unitedretail.batch.business.query.FileProviderTraceViewImpl;
import com.cylande.unitedretail.batch.business.query.common.FileProviderTraceViewRow;
import com.cylande.unitedretail.batch.transformer.FileProviderTraceTransformer;
import com.cylande.unitedretail.framework.business.jbo.server.ApplicationModuleImpl;
import com.cylande.unitedretail.message.batch.FileProviderTraceCriteriaListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceKeyType;
import com.cylande.unitedretail.message.batch.FileProviderTraceListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceScenarioType;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.common.context.ContextType;

import oracle.jbo.AttributeList;
import oracle.jbo.NameValuePairs;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class FileProviderTraceManagerModuleImpl extends ApplicationModuleImpl implements FileProviderTraceManagerModule
{
  /**This is the default constructor (do not remove)
   */
  public FileProviderTraceManagerModuleImpl()
  {
  }

  /**Sample main for debugging Business Components code using the tester.
   */
  public static void main(String[] args)
  { /* package name */
    /* Configuration Name */launchTester("com.cylande.unitedretail.batch.business.module", "FileProviderTraceManagerModuleLocal");
  }

  /**Container's getter for FileProviderTraceView
   */
  public FileProviderTraceViewImpl getFileProviderTraceView()
  {
    return (FileProviderTraceViewImpl)findViewObject("FileProviderTraceView");
  }

  /**
   * cr�ation d'un FileProviderTraceType
   * @param pFileProviderTrace
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceType createFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    FileProviderTraceType result = null;
    if (pFileProviderTrace != null && pFileProviderTrace.getTaskId() != null && pFileProviderTrace.getTaskCode() != null)
    {
      AttributeList attList = new NameValuePairs();
      attList.setAttribute("TaskId", pFileProviderTrace.getTaskId());
      attList.setAttribute("TaskCode", pFileProviderTrace.getTaskCode());
      if (pFileProviderTrace.getSite() != null)
      {
        attList.setAttribute("SiteCode", pFileProviderTrace.getSite().getCode());
      }
      FileProviderTraceViewRow row = (FileProviderTraceViewRow)getFileProviderTraceView().createAndInitRow(attList);
      setRow(pFileProviderTrace, row, true, pContext);
      result = getBean(row, pContext);
    }
    return result;
  }

  /**
   * createFileProviderTraceList
   * @param pList
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceListType createFileProviderTraceList(FileProviderTraceListType pList, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    FileProviderTraceListType result = null;
    if (pList != null)
    {
      result = new FileProviderTraceListType();
      for (FileProviderTraceType bean: pList.getList())
      {
        bean = createFileProviderTrace(bean, pScenario, pContext);
        if (bean != null)
        {
          result.getList().add(bean);
        }
      }
    }
    return result;
  }

  /**
   * FileProviderTraceKeyType
   * @param pKey
   * @param pScenario
   * @param pContext
   */
  public void deleteFileProviderTrace(FileProviderTraceKeyType pKey, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    if (pKey != null)
    {
      FileProviderTraceViewRow row = getFileProviderTraceView().getRow(pKey);
      if (row != null)
      {
        row.remove();
      }
    }
  }

  /**
   * FileProviderTraceCriteriaListType
   * @param pCriterias
   * @param pScenario
   * @param pContext
   */
  public void deleteFileProviderTraceList(FileProviderTraceCriteriaListType pCriterias, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    if (pCriterias != null)
    {
      FileProviderTraceViewImpl view = getFileProviderTraceView();
      view.findByCriterias(pCriterias);
      while (view.hasNext())
      {
        view.next().remove();
      }
      view.clearViewCriterias();
    }
  }

  /**
   * findFileProviderTrace
   * @param pCriterias
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceListType findFileProviderTrace(FileProviderTraceCriteriaListType pCriterias, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    FileProviderTraceViewImpl view = getFileProviderTraceView();
    try
    {
      view.initForwardOnly();
      FileProviderTraceListType result = null;
      if (pCriterias != null)
      {
        view.findByCriterias(pCriterias);
        if (view.hasNext())
        {
          result = new FileProviderTraceListType();
          while (view.hasNext())
          {
            result.getList().add(getBean((FileProviderTraceViewRow)view.next(), pContext));
          }
        }
        view.clearViewCriterias();
      }
      return result;
    }
    finally
    {
      view.releaseForwardOnly();
    }
  }

  /**
   * getFileProviderTrace
   * @param pKey
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceType getFileProviderTrace(FileProviderTraceKeyType pKey, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    FileProviderTraceType result = null;
    if (pKey != null)
    {
      result = getBean(getFileProviderTraceView().getRow(pKey), pContext);
    }
    return result;
  }

  /**
   * postFileProviderTrace
   * @param pFileProviderTrace
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceType postFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    return updateFileProviderTrace(pFileProviderTrace, pScenario, pContext, true);
  }

  /**
   * postFileProviderTraceList
   * @param pList
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceListType postFileProviderTraceList(FileProviderTraceListType pList, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    FileProviderTraceListType result = null;
    if (pList != null)
    {
      result = new FileProviderTraceListType();
      for (FileProviderTraceType bean: pList.getList())
      {
        bean = postFileProviderTrace(bean, pScenario, pContext);
        if (bean != null)
        {
          result.getList().add(bean);
        }
      }
    }
    return result;
  }

  /**
   * updateFileProviderTrace
   * @param pFileProviderTrace
   * @param pScenario
   * @param pContext
   * @return r�sultat
   */
  public FileProviderTraceType updateFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    return updateFileProviderTrace(pFileProviderTrace, pScenario, pContext, false);
  }

  public FileProviderTraceListType updateFileProviderTraceList(FileProviderTraceListType pList, FileProviderTraceScenarioType pScenario, ContextType pContext)
  {
    FileProviderTraceListType result = null;
    if (pList != null)
    {
      result = new FileProviderTraceListType();
      for (FileProviderTraceType bean: pList.getList())
      {
        bean = updateFileProviderTrace(bean, pScenario, pContext);
        if (bean != null)
        {
          result.getList().add(bean);
        }
      }
    }
    return result;
  }

  /**
   * updateFileProviderTrace
   * @param pFileProviderTrace
   * @param pScenario
   * @param pContext
   * @param pCreate
   * @return r�sultat
   */
  private FileProviderTraceType updateFileProviderTrace(FileProviderTraceType pFileProviderTrace, FileProviderTraceScenarioType pScenario, ContextType pContext, boolean pCreate)
  {
    FileProviderTraceType result = null;
    if (pFileProviderTrace != null)
    {
      FileProviderTraceViewRow row = getFileProviderTraceView().getRow(pFileProviderTrace);
      if (row != null)
      {
        setRow(pFileProviderTrace, row, false, pContext);
        result = getBean(row, pContext);
      }
      else if (pCreate)
      {
        result = createFileProviderTrace(pFileProviderTrace, pScenario, pContext);
      }
    }
    return result;
  }

  /**
   * getBean
   * @param pRow
   * @param pContext
   * @return r�sultat
   */
  private FileProviderTraceType getBean(FileProviderTraceViewRow pRow, ContextType pContext)
  {
    FileProviderTraceType result = FileProviderTraceTransformer.toBean(pRow, pContext);
    return result;
  }

  /**
   * setRow
   * @param pFileProviderTrace
   * @param pRow
   * @param pCreate
   * @param pContext
   */
  private void setRow(FileProviderTraceType pFileProviderTrace, FileProviderTraceViewRow pRow, boolean pCreate, ContextType pContext)
  {
    FileProviderTraceTransformer.toRow(pFileProviderTrace, pRow);
    if (pCreate)
    {
      getFileProviderTraceView().insertRow(pRow);
    }
    getRootApplicationModule().getTransaction().postChanges();
  }
}