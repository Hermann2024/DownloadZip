package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests sur le service CRUD de gestion des fonctions métiers.
 * @author eselosse
 * @version 1.0
 * @see com.cylande.unitedretail.batch.service.common.BatchManagerService
 * @since 02/04/2008
 */
public class BatchPopulateTest extends TestCase
{
  private BatchManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 02/04/2008
   */
  public BatchPopulateTest(String pTestName)
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
    suite = new TestSuite("BatchPopulateTest");
    suite.addTest(new BatchPopulateTest("testPopulateBatch"));
    suite.addTest(new BatchPopulateTest("testGetBatch"));
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
    _service = new BatchManagerServiceImpl();
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
  public void testGetBatch() throws RemoteException, ServiceException
  {
    BatchCriteriaListType myCriterias = new BatchCriteriaListType();
    BatchScenarioType myScenario = new BatchScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (BatchScenarioType)myJaxbManager.read("ScenarioBatch.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    BatchListType myList = _service.findBatch(myCriterias, myScenario, myContext);
    assertEquals(4, myList.getValues().size());
  }

  /**
   * Chargement des données avec l'opération <b>post</b>.
   * @throws RemoteException
   * @throws ServiceException
   * @since 02/04/2008
   */
  public void testPopulateBatch() throws RemoteException, ServiceException
  {
    BatchListType myList = new BatchListType();
    BatchScenarioType myScenario = new BatchScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (BatchListType)myJaxbManager.read("PopulateBatch.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (BatchScenarioType)myJaxbManager.read("ScenarioBatch.xml", myScenario);
    myList = _service.postBatchList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", 0 < myList.getValues().size());
  }
}
