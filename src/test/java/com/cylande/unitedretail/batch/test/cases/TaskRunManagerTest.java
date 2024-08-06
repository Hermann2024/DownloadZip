package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.TaskRunManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.TaskRunManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaType;
import com.cylande.unitedretail.message.batch.TaskRunKeyType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunScenarioType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.network.businessunit.SiteKeyType;

import java.rmi.RemoteException;

import java.sql.SQLException;

import java.util.Calendar;

import junit.framework.TestCase;

/**
 * Tests sur le Service CRUD de gestion des executions de Task (TASK_RUN).
 * @author eselosse
 * @version 1.0
 * @see TaskRunManagerService
 * @since 01/04/2008
 */
public class TaskRunManagerTest extends TestCase
{
  TaskRunManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 01/04/2008
   */
  public TaskRunManagerTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * Démarrage du cas de test.
   * Instanciation du _service
   * @throws Exception
   * @since 01/04/2008
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    _service = new TaskRunManagerServiceImpl();
  }

  /**
   * Arrêt du cas de test.
   * Libération du Service
   * @throws Exception
   * @since 01/04/2008
   */
  protected void tearDown() throws Exception
  {
    _service = null;
    super.tearDown();
  }

  /** Règle de Validation sur les Heures non respectée
   * @throws RemoteException exception
   * @throws ServiceException exception
   */
  public void testCreateTaskRunWithEndTimeKO() throws RemoteException, ServiceException
  {
    TaskRunType myTaskRun = new TaskRunType();
    TaskRunScenarioType myScenarioType = new TaskRunScenarioType();
    ContextType pContext = new ContextType();
    try
    {
      myTaskRun.setPath("B0.T1");
      SiteKeyType site = new SiteKeyType();
      site.setCode("Site0");
      myTaskRun.setSite(site);
      Calendar cal = Calendar.getInstance();
      myTaskRun.setStartTime(cal);
      myTaskRun.setEndTime(cal);
      myTaskRun.setStatus(false);
      myTaskRun.setWorkLoad(1000);
      myTaskRun = _service.createTaskRun(myTaskRun, myScenarioType, pContext);
    }
    catch (ServiceException e)
    {
      assertTrue(true);
    }
  }

  public void testCreateTaskRun() throws RemoteException, ServiceException
  {
    TaskRunType myTaskRun = new TaskRunType();
    TaskRunScenarioType myScenarioType = new TaskRunScenarioType();
    ContextType pContext = new ContextType();
    myTaskRun.setPath("B0.T1");
    SiteKeyType site = new SiteKeyType();
    site.setCode("Site0");
    myTaskRun.setSite(site);
    myTaskRun.setStartTime(Calendar.getInstance());
    myTaskRun.setStatus(false);
    myTaskRun.setWorkLoad(1000);
    myTaskRun = _service.createTaskRun(myTaskRun, myScenarioType, pContext);
  }

  public void testUpdateTaskRun() throws RemoteException, ServiceException
  {
    ContextType myContext = new ContextType();
    TaskRunScenarioType myScenario = new TaskRunScenarioType();
    TaskRunType myTaskRun = new TaskRunType();
    myTaskRun.setId(224);
    SiteKeyType site = new SiteKeyType();
    site.setCode("Site0");
    myTaskRun.setSite(site);
    myTaskRun.setStatus(true);
    myTaskRun.setEndTime(Calendar.getInstance());
    myTaskRun.setStep("processor ok");
    myTaskRun = _service.updateTaskRun(myTaskRun, myScenario, myContext);
  }

  public void testCreateTaskRunList() throws RemoteException, ServiceException
  {
    TaskRunListType myList = new TaskRunListType();
    TaskRunType myTaskRun = new TaskRunType();
    myTaskRun.setPath("B1.T1");
    /*SiteKeyType site = new SiteKeyType();
    site.setCode("Site0");
    myTaskRun.setSite(site);*/
    myTaskRun.setStartTime(Calendar.getInstance());
    myTaskRun.setStatus(false);
    myTaskRun.setWorkLoad(1000);
    myList.getValues().add(myTaskRun);
    TaskRunType myTaskRun2 = new TaskRunType();
    myTaskRun2.setPath("B1.T1");
    myTaskRun2.setStartTime(Calendar.getInstance());
    myTaskRun2.setStatus(false);
    myTaskRun2.setWorkLoad(5000);
    myList.getValues().add(myTaskRun2);
    myList = _service.createTaskRunList(myList, null, null);
  }

  public void testDeleteTaskRun() throws RemoteException, ServiceException
  {
    TaskRunKeyType myKey = new TaskRunKeyType();
    TaskRunScenarioType myScenarioType = new TaskRunScenarioType();
    ContextType pContext = new ContextType();
    try
    {
      myKey.setId(224);
      SiteKeyType site = new SiteKeyType();
      site.setCode("Site0");
      myKey.setSite(site);
      _service.deleteTaskRun(myKey, myScenarioType, pContext);
    }
    catch (ServiceException e)
    {
      assertTrue(true);
    }
  }

  public void testDeleteTaskRunList() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    TaskRunCriteriaListType myCriterias = new TaskRunCriteriaListType();
    TaskRunCriteriaType myCriteria = new TaskRunCriteriaType();
    CriteriaStringType myValue = new CriteriaStringType();
    myValue.setEquals("B1.T1");
    myCriteria.setPath(myValue);
    SiteKeyType site = new SiteKeyType();
    site.setCode("Site0");
    myCriteria.setSite(site);
    myCriterias.getList().add(myCriteria);
    _service.deleteTaskRunList(myCriterias, null, myContext);
  }

  public void testGetTaskRun() throws RemoteException, ServiceException, SQLException
  {
    TaskRunKeyType myKey = new TaskRunKeyType();
    TaskRunType myTaskRun = createTaskRun();
    myKey.setId(myTaskRun.getId());
    myKey.setSite(myTaskRun.getSite());
    TaskRunScenarioType myScenario = new TaskRunScenarioType();
    ContextType myContext = new ContextType();
    TaskRunType myTaskRunOut = _service.getTaskRun(myKey, myScenario, myContext);
    assertNotNull(myTaskRunOut);
  }

  public void testFindTaskRun() throws RemoteException, ServiceException, SQLException
  {
    // Créer un TaskRun pour pouvoir le retrouver
    createTaskRun();
    TaskRunScenarioType myScenario = new TaskRunScenarioType();
    TaskRunCriteriaListType myCriteriasList = new TaskRunCriteriaListType();
    TaskRunCriteriaType myCriteria1 = new TaskRunCriteriaType();
    CriteriaStringType myCriteriaString = new CriteriaStringType();
    myCriteriaString.setEquals("B2.T1");
    SiteKeyType site = new SiteKeyType();
    site.setCode("Site0");
    myCriteria1.setSite(site);
    myCriteria1.setPath(myCriteriaString);
    myCriteriasList.getList().add(myCriteria1);
    ContextType myContext = new ContextType();
    TaskRunListType myTaskRunOut = _service.findTaskRun(myCriteriasList, myScenario, myContext);
    assertNotNull(myTaskRunOut);
    // on doit récupérer un TaskRun
    assertTrue(myTaskRunOut.getValues().size() == 1);
  }

  private TaskRunType createTaskRun() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    TaskRunKeyType myKey = new TaskRunKeyType();
    myKey.setPath("B2.T1");
    myKey.setId(230);
    SiteKeyType site = new SiteKeyType();
    site.setCode("Site0");
    myKey.setSite(site);
    _service.deleteTaskRun(myKey, null, myContext);
    TaskRunType myTaskRun = new TaskRunType();
    myTaskRun.setPath("B2.T1");
    myTaskRun.setStartTime(Calendar.getInstance());
    myTaskRun.setStatus(false);
    myTaskRun = _service.createTaskRun(myTaskRun, null, null);
    assertNotNull(myTaskRun);
    return myTaskRun;
  }
}
