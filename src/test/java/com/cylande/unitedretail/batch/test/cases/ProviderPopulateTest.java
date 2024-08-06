package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.ProviderManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.ProviderManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.ProviderCriteriaListType;
import com.cylande.unitedretail.message.batch.ProviderListType;
import com.cylande.unitedretail.message.batch.ProviderScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests sur le service CRUD de gestion des fonctions métiers.
 * @author eselosse
 * @version 1.0
 * @see com.cylande.unitedretail.batch.service.common.ProviderManagerService
 * @since 02/04/2008
 */
public class ProviderPopulateTest extends TestCase
{
  private ProviderManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 02/04/2008
   */
  public ProviderPopulateTest(String pTestName)
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
    suite = new TestSuite("ProviderPopulateTest");
    suite.addTest(new ProviderPopulateTest("testPopulateProvider"));
    suite.addTest(new ProviderPopulateTest("testGetProvider"));
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
    _service = new ProviderManagerServiceImpl();
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
  public void testGetProvider() throws RemoteException, ServiceException
  {
    ProviderCriteriaListType myCriterias = new ProviderCriteriaListType();
    ProviderScenarioType myScenario = new ProviderScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (ProviderScenarioType)myJaxbManager.read("ScenarioProvider.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    ProviderListType myList = _service.findProvider(myCriterias, myScenario, myContext);
    assertEquals(6, myList.getValues().size());
  }

  /**
   * Chargement des données avec l'opération <b>post</b>.
   * @throws RemoteException
   * @throws ServiceException
   * @since 02/04/2008
   */
  public void testPopulateProvider() throws RemoteException, ServiceException
  {
    ProviderListType myList = new ProviderListType();
    ProviderScenarioType myScenario = new ProviderScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (ProviderListType)myJaxbManager.read("PopulateProvider.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (ProviderScenarioType)myJaxbManager.read("ScenarioProvider.xml", myScenario);
    myList = _service.postProviderList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", 0 < myList.getValues().size());
  }
}
