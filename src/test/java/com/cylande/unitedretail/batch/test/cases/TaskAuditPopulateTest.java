package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.TaskAuditManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.TaskAuditManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.TaskAuditCriteriaListType;
import com.cylande.unitedretail.message.batch.TaskAuditListType;
import com.cylande.unitedretail.message.batch.TaskAuditScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests sur le service CRUD de gestion des fonctions métiers.
 * @author eselosse
 * @version 1.0
 * @see TaskAuditManagerService
 * @since 01/04/2008
 */
public class TaskAuditPopulateTest extends TestCase
{
  private TaskAuditManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 01/04/2008
   */
  public TaskAuditPopulateTest(String pTestName)
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
    suite = new TestSuite("TaskAuditPopulateTest");
    suite.addTest(new TaskAuditPopulateTest("testPopulateTaskAudit"));
    suite.addTest(new TaskAuditPopulateTest("testGetTaskAudit"));
    return suite;
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
    _service = new TaskAuditManagerServiceImpl();
  }

  /**
   * Arrêt du cas de test.
   * Libération du service
   * @throws Exception
   * @since 01/04/2008
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
   * @since 01/04/2008
   */
  public void testGetTaskAudit() throws RemoteException, ServiceException
  {
    TaskAuditCriteriaListType myCriterias = new TaskAuditCriteriaListType();
    TaskAuditScenarioType myScenario = new TaskAuditScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (TaskAuditScenarioType)myJaxbManager.read("ScenarioTask.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    TaskAuditListType myList = _service.findTaskAudit(myCriterias, myScenario, myContext);
    assertEquals(1, myList.getValues().size());
  }

  /**
   * Chargement des données avec l'opération <b>post</b>.
   * @throws RemoteException
   * @throws ServiceException
   * @since 01/04/2008
   */
  public void testPopulateTaskAudit() throws RemoteException, ServiceException
  {
    TaskAuditListType myList = new TaskAuditListType();
    TaskAuditScenarioType myScenario = new TaskAuditScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (TaskAuditListType)myJaxbManager.read("PopulateTaskAudit.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (TaskAuditScenarioType)myJaxbManager.read("ScenarioTask.xml", myScenario);
    myList = _service.postTaskAuditList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", !myList.getValues().isEmpty());
  }
}
