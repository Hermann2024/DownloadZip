package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.BatchRunManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchRunManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.BatchRunCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchRunListType;
import com.cylande.unitedretail.message.batch.BatchRunScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests sur le service CRUD de gestion des fonctions métiers.
 * @author eselosse
 * @version 1.0
 * @see BatchRunManagerService
 * @since 01/04/2008
 */
public class BatchRunPopulateTest extends TestCase
{
  public BatchRunManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 01/04/2008
   */
  public BatchRunPopulateTest(String pTestName)
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
    suite = new TestSuite("BatchRunPopulateTest");
    suite.addTest(new BatchRunPopulateTest("testPopulateBatchRun"));
    suite.addTest(new BatchRunPopulateTest("testGetBatchRun"));
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
    _service = new BatchRunManagerServiceImpl();
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
  public void testGetBatchRun() throws RemoteException, ServiceException
  {
    BatchRunCriteriaListType myCriterias = new BatchRunCriteriaListType();
    BatchRunScenarioType myScenario = new BatchRunScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (BatchRunScenarioType)myJaxbManager.read("ScenarioBatch.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    BatchRunListType myList = _service.findBatchRun(myCriterias, myScenario, myContext);
    assertEquals(1, myList.getValues().size());
  }

  /**
   * Chargement des données avec l'opération <b>post</b>.
   * @throws RemoteException
   * @throws ServiceException
   * @since 01/04/2008
   */
  public void testPopulateBatchRun() throws RemoteException, ServiceException
  {
    BatchRunListType myList = new BatchRunListType();
    BatchRunScenarioType myScenario = new BatchRunScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (BatchRunListType)myJaxbManager.read("PopulateBatchRun.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (BatchRunScenarioType)myJaxbManager.read("ScenarioBatch.xml", myScenario);
    myList = _service.postBatchRunList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", 0 < myList.getValues().size());
  }
}
