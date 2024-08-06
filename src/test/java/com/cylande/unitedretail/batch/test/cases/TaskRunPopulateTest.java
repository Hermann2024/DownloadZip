package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.TaskRunManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.TaskRunManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.TaskRunCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskRunListType;
import com.cylande.unitedretail.message.batch.TaskRunScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests sur le service CRUD de gestion des fonctions m�tiers.
 * @author eselosse
 * @version 1.0
 * @see TaskRunManagerService
 * @since 01/04/2008
 */
public class TaskRunPopulateTest extends TestCase
{
  private TaskRunManagerService _service = null;

  /**
   * Constructeur par d�faut.
   * @param pTestName : non du cas de test JUNIT
   * @since 01/04/2008
   */
  public TaskRunPopulateTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * Ordonnancement des tests.
   * @since 01/04/2008
   */
  public static Test suite()
  {
    TestSuite suite;
    suite = new TestSuite("TaskRunPopulateTest");
    suite.addTest(new TaskRunPopulateTest("testPopulateTaskRun"));
    suite.addTest(new TaskRunPopulateTest("testGetTaskRun"));
    return suite;
  }

  /**
   * D�marrage du cas de test.
   * Instanciation du service
   * @throws Exception
   * @since 01/04/2008
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    _service = new TaskRunManagerServiceImpl();
  }

  /**
   * Arr�t du cas de test.
   * Lib�ration du service
   * @throws Exception
   * @since 01/04/2008
   */
  protected void tearDown() throws Exception
  {
    _service = null;
    super.tearDown();
  }

  /**
   * R�cup�ration de la liste de valeur.
   * @throws RemoteException
   * @throws ServiceException
   * @since 01/04/2008
   */
  public void testGetTaskRun() throws RemoteException, ServiceException
  {
    TaskRunCriteriaListType myCriterias = new TaskRunCriteriaListType();
    TaskRunScenarioType myScenario = new TaskRunScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (TaskRunScenarioType)myJaxbManager.read("ScenarioTask.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    TaskRunListType myList = _service.findTaskRun(myCriterias, myScenario, myContext);
    assertEquals(1, myList.getValues().size());
  }

  /**
   * Chargement des donn�es avec l'op�ration <b>post</b>.
   * @throws RemoteException
   * @throws ServiceException
   * @since 01/04/2008
   */
  public void testPopulateTaskRun() throws RemoteException, ServiceException
  {
    TaskRunListType myList = new TaskRunListType();
    TaskRunScenarioType myScenario = new TaskRunScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (TaskRunListType)myJaxbManager.read("PopulateTaskRun.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (TaskRunScenarioType)myJaxbManager.read("ScenarioTask.xml", myScenario);
    myList = _service.postTaskRunList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", 0 < myList.getValues().size());
  }
}
