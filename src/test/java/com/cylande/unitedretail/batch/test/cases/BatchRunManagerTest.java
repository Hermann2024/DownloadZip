package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.BatchRunManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchRunManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaType;
import com.cylande.unitedretail.message.batch.BatchRunKeyType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.rmi.RemoteException;

import java.sql.SQLException;

import java.util.Calendar;

import junit.framework.TestCase;

/**
 * Tests sur le service CRUD de gestion des executions de Batch (BATCH_RUN).
 * @author eselosse
 * @version 1.0
 * @see BatchRunManagerService
 * @since 01/04/2008
 */
public class BatchRunManagerTest extends TestCase
{
  BatchRunManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 01/04/2008
   */
  public BatchRunManagerTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * Démarrage du cas de test.
   * Instanciation du service
   * @throws Exception
   * @since 01/04/2008
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    _service = new BatchRunManagerServiceImpl();
  }

  /**
   * Arrêt du cas de test.
   * Libération du service
   * @throws Exception
   * @since 01/0/2008
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
  public void testCreateBatchRunWithEndTimeKO() throws RemoteException, ServiceException
  {
    BatchRunType myBatchRun = new BatchRunType();
    BatchRunScenarioType myScenarioType = new BatchRunScenarioType();
    ContextType pContext = new ContextType();
    try
    {
      myBatchRun.setPath("B0");
      Calendar cal = Calendar.getInstance();
      myBatchRun.setStartTime(cal);
      myBatchRun.setEndTime(cal);
      myBatchRun.setStatus(false);
      myBatchRun = _service.createBatchRun(myBatchRun, myScenarioType, pContext);
    }
    catch (ServiceException e)
    {
      assertTrue(true);
    }
  }

  public void testCreateBatchRun() throws RemoteException, ServiceException
  {
    BatchRunType myBatchRun = new BatchRunType();
    BatchRunScenarioType myScenarioType = new BatchRunScenarioType();
    ContextType pContext = new ContextType();
    myBatchRun.setPath("B0");
    myBatchRun.setStartTime(Calendar.getInstance());
    myBatchRun.setStatus(false);
    myBatchRun = _service.createBatchRun(myBatchRun, myScenarioType, pContext);
  }

  public void testUpdateBatchRun() throws RemoteException, ServiceException
  {
    ContextType myContext = new ContextType();
    BatchRunScenarioType myScenario = new BatchRunScenarioType();
    BatchRunType myBatchRun = new BatchRunType();
    myBatchRun.setId(415);
    myBatchRun.setPath("B0");
    myBatchRun.setStatus(true);
    myBatchRun.setEndTime(Calendar.getInstance());
    myBatchRun = _service.updateBatchRun(myBatchRun, myScenario, myContext);
  }

  public void testCreateBatchRunList() throws RemoteException, ServiceException
  {
    BatchRunListType myList = new BatchRunListType();
    BatchRunType myBatchRun = new BatchRunType();
    myBatchRun.setPath("B1");
    myBatchRun.setStartTime(Calendar.getInstance());
    myBatchRun.setStatus(false);
    myList.getValues().add(myBatchRun);
    BatchRunType myBatchRun2 = new BatchRunType();
    myBatchRun2.setPath("B1");
    myBatchRun2.setStartTime(Calendar.getInstance());
    myBatchRun2.setStatus(false);
    myList.getValues().add(myBatchRun2);
    myList = _service.createBatchRunList(myList, null, new ContextType());
  }

  public void testDeleteBatchRun() throws RemoteException, ServiceException
  {
    BatchRunKeyType myKey = new BatchRunKeyType();
    BatchRunScenarioType myScenarioType = new BatchRunScenarioType();
    ContextType pContext = new ContextType();
    try
    {
      myKey.setPath("B0");
      myKey.setId(415);
      _service.deleteBatchRun(myKey, myScenarioType, pContext);
    }
    catch (ServiceException e)
    {
      assertTrue(true);
    }
  }

  public void testDeleteBatchRunList() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    BatchRunCriteriaListType myCriterias = new BatchRunCriteriaListType();
    BatchRunCriteriaType myCriteria = new BatchRunCriteriaType();
    CriteriaStringType myValue = new CriteriaStringType();
    myValue.setEquals("B2");
    myCriteria.setPath(myValue);
    myCriterias.getList().add(myCriteria);
    _service.deleteBatchRunList(myCriterias, null, myContext);
  }

  public void testGetBatchRun() throws RemoteException, ServiceException, SQLException
  {
    BatchRunKeyType myKey = new BatchRunKeyType();
    BatchRunType myBatchRun = createBatchRun();
    myKey.setId(myBatchRun.getId());
    myKey.setPath(myBatchRun.getPath());
    myKey.setSite(myBatchRun.getSite());
    BatchRunScenarioType myScenario = new BatchRunScenarioType();
    ContextType myContext = new ContextType();
    BatchRunType myBatchRunOut = _service.getBatchRun(myKey, myScenario, myContext);
    assertNotNull(myBatchRunOut);
  }

  public void testFindBatchRun() throws RemoteException, ServiceException, SQLException
  {
    // Créer un BatchRun pour pouvoir le retrouver
    createBatchRun();
    BatchRunScenarioType myScenario = new BatchRunScenarioType();
    BatchRunCriteriaListType myCriteriasList = new BatchRunCriteriaListType();
    BatchRunCriteriaType myCriteria1 = new BatchRunCriteriaType();
    CriteriaStringType myCriteriaString = new CriteriaStringType();
    myCriteriaString.setEquals("B2");
    myCriteria1.setPath(myCriteriaString);
    myCriteriasList.getList().add(myCriteria1);
    ContextType myContext = new ContextType();
    BatchRunListType myBatchRunOut = _service.findBatchRun(myCriteriasList, myScenario, myContext);
    assertNotNull(myBatchRunOut);
    // on doit récupérer un BatchRun
    assertTrue(myBatchRunOut.getValues().size() == 1);
  }

  private BatchRunType createBatchRun() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    BatchRunKeyType myKey = new BatchRunKeyType();
    myKey.setPath("B2");
    myKey.setId(420);
    _service.deleteBatchRun(myKey, null, myContext);
    BatchRunType myBatchRun = new BatchRunType();
    myBatchRun.setPath("B2");
    myBatchRun.setStartTime(Calendar.getInstance());
    myBatchRun.setStatus(false);
    myBatchRun = _service.createBatchRun(myBatchRun, null, myContext);
    assertNotNull(myBatchRun);
    return myBatchRun;
  }
}
