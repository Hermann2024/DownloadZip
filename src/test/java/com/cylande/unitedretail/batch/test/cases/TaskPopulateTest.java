package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.TaskManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.TaskManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.TaskCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskListType;
import com.cylande.unitedretail.message.batch.TaskScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests sur le service CRUD de gestion des fonctions métiers.
 * @author eselosse
 * @version 1.0
 * @see com.cylande.unitedretail.batch.service.common.TaskManagerService
 * @since 02/04/2008
 */
public class TaskPopulateTest extends TestCase
{
  private TaskManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 02/04/2008
   */
  public TaskPopulateTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * Ordonnancement des tests.
   * @since 02/04/2008
   */
  public static Test suite()
  {
    TestSuite suite;
    suite = new TestSuite("TaskPopulateTest");
    suite.addTest(new TaskPopulateTest("testPopulateTask"));
    suite.addTest(new TaskPopulateTest("testGetTask"));
    return suite;
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
   * @since 02/04/2008
   */
  protected void tearDown() throws Exception
  {
    _service = null;
    super.tearDown();
  }

  /**
   * Récupération de la liste de valeur.
   * @throws RemoteException
   * @throws ServiceException
   * @since 02/04/2008
   */
  public void testGetTask() throws RemoteException, ServiceException
  {
    TaskCriteriaListType myCriterias = new TaskCriteriaListType();
    TaskScenarioType myScenario = new TaskScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (TaskScenarioType)myJaxbManager.read("ScenarioTask.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    TaskListType myList = _service.findTask(myCriterias, myScenario, myContext);
    assertEquals(3, myList.getValues().size());
  }

  /**
   * Chargement des données avec l'opération <b>post</b>.
   * @throws RemoteException
   * @throws ServiceException
   * @since 02/04/2008
   */
  public void testPopulateTask() throws RemoteException, ServiceException
  {
    TaskListType myList = new TaskListType();
    TaskScenarioType myScenario = new TaskScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (TaskListType)myJaxbManager.read("PopulateTask.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (TaskScenarioType)myJaxbManager.read("ScenarioTask.xml", myScenario);
    myList = _service.postTaskList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", 0 < myList.getValues().size());
  }
}
