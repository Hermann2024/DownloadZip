package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.TaskManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.TaskManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.EXTRACTION;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.TaskCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskCriteriaType;
import com.cylande.unitedretail.message.batch.TaskInputType;
import com.cylande.unitedretail.message.batch.TaskKeyType;
import com.cylande.unitedretail.message.batch.TaskListType;
import com.cylande.unitedretail.message.batch.TaskProviderType;
import com.cylande.unitedretail.message.batch.TaskScenarioType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.math.BigInteger;

import java.rmi.RemoteException;

import java.sql.SQLException;

import junit.framework.TestCase;

/**
 * Tests sur le service CRUD de gestion des Tasks (Repository)
 * @author eselosse
 * @version 1.0
 * @see com.cylande.unitedretail.batch.service.common.TaskManagerService
 * @since 02/04/2008
 */
public class TaskManagerTest extends TestCase
{
  private TaskManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 02/04/2008
   */
  public TaskManagerTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * Démarrage du cas de test.
   * Instanciation du service
   * @throws Exception
   * @since 02/04/2008
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    _service = new TaskManagerServiceImpl();
  }

  /**
   * Arrêt du cas de test.
   * Libération du service
   * @throws Exception
   * @since 02/0/2008
   */
  protected void tearDown() throws Exception
  {
    _service = null;
    super.tearDown();
  }

  public void testCreateTask() throws RemoteException, ServiceException
  {
    TaskType myTask = new INTEGRATION();
    TaskScenarioType myScenarioType = new TaskScenarioType();
    ContextType pContext = new ContextType();
    //Declaration d'un Task Parent
    myTask.setName("B0.T1");
    myTask.setDescription("Description Traitement Task");
    ((INTEGRATION)myTask).setCommitFrequency(new BigInteger("10000"));
    TaskInputType input = new TaskInputType();
    //TaskType.Input.Provider providerInput = new TaskType.Input.Provider();
    TaskProviderType providerInput = new TaskProviderType();
    providerInput.setRef("providerInput");
    input.setProvider(providerInput);
    myTask.setInput(input);
    myTask = _service.createTask(myTask, myScenarioType, pContext);
    assertTrue(true);
  }

  public void testUpdateTask() throws RemoteException, ServiceException
  {
    ContextType myContext = new ContextType();
    TaskScenarioType myScenario = new TaskScenarioType();
    TaskType myTask = new EXTRACTION();
    myTask.setName("extractionSupplierTask");
    //Modification de la Description du Task
    myTask.setDescription("Description Traitement Task");
    myTask = _service.updateTask(myTask, myScenario, myContext);
    assertTrue(true);
  }

  public void testCreateTaskList() throws RemoteException, ServiceException
  {
    TaskListType myList = new TaskListType();
    TaskType myTask = new INTEGRATION();
    myTask.setName("B1.T1");
    myTask.setDescription("Description Traitement Task 1er");
    ((INTEGRATION)myTask).setCommitFrequency(new BigInteger("10000"));
    TaskInputType input1 = new TaskInputType();
    //TaskType.Input.Provider providerInput1 = new TaskType.Input.Provider();
    TaskProviderType providerInput1 = new TaskProviderType();
    providerInput1.setRef("providerInput");
    input1.setProvider(providerInput1);
    myTask.setInput(input1);
    myList.getValues().add(myTask);
    TaskType myTask2 = new EXTRACTION();
    myTask2.setName("B2.T1");
    myTask2.setDescription("Description Traitement Task 2eme");
    ((EXTRACTION)myTask2).setMaxFetchSize(new BigInteger("10000"));
    TaskInputType input2 = new TaskInputType();
    //TaskType.Input.Provider providerInput2 = new TaskType.Input.Provider();
    TaskProviderType providerInput2 = new TaskProviderType();
    providerInput2.setRef("providerInput");
    input2.setProvider(providerInput2);
    myTask2.setInput(input2);
    myList.getValues().add(myTask2);
    myList = _service.createTaskList(myList, null, null);
    assertTrue(true);
  }

  public void testDeleteTask() throws RemoteException, ServiceException
  {
    TaskKeyType myKey = new TaskKeyType();
    TaskScenarioType myScenarioType = new TaskScenarioType();
    ContextType pContext = new ContextType();
    myKey.setName("extractionSupplierTask");
    _service.deleteTask(myKey, myScenarioType, pContext);
    assertTrue(true);
  }

  public void testDeleteTaskList() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    TaskCriteriaListType myCriterias = new TaskCriteriaListType();
    TaskCriteriaType myCriteria = new TaskCriteriaType();
    CriteriaStringType myValue = new CriteriaStringType();
    myValue.setContains("Task");
    myCriteria.setName(myValue);
    myCriterias.getList().add(myCriteria);
    _service.deleteTaskList(myCriterias, null, myContext);
    assertTrue(true);
  }

  public void testGetTask() throws RemoteException, ServiceException, SQLException
  {
    TaskKeyType myKey = new TaskKeyType();
    TaskType myTask = createTask();
    myKey.setName(myTask.getName());
    TaskScenarioType myScenario = new TaskScenarioType();
    ContextType myContext = new ContextType();
    TaskType myTaskOut = _service.getTask(myKey, myScenario, myContext);
    assertNotNull(myTaskOut);
  }

  public void testFindTask() throws RemoteException, ServiceException, SQLException
  {
    // Créer un Task pour pouvoir le retrouver
    createTask();
    TaskScenarioType myScenario = new TaskScenarioType();
    TaskCriteriaListType myCriteriasList = new TaskCriteriaListType();
    TaskCriteriaType myCriteria1 = new TaskCriteriaType();
    CriteriaStringType myCriteriaString = new CriteriaStringType();
    myCriteriaString.setContains("Task");
    myCriteria1.setName(myCriteriaString);
    myCriteriasList.getList().add(myCriteria1);
    ContextType myContext = new ContextType();
    TaskListType myTaskOut = _service.findTask(myCriteriasList, myScenario, myContext);
    assertNotNull(myTaskOut);
    // on doit récupérer un Task
    assertTrue(myTaskOut.getValues().size() == 2);
  }

  private TaskType createTask() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    TaskKeyType myKey = new TaskKeyType();
    myKey.setName("B01.T1");
    _service.deleteTask(myKey, null, myContext);
    TaskType myTask = new INTEGRATION();
    //Declaration d'un Task Parent
    myTask.setName("B01.T1");
    myTask.setDescription("Description Traitement Task");
    myTask = _service.createTask(myTask, null, null);
    assertNotNull(myTask);
    return myTask;
  }
}
