package com.cylande.unitedretail.batch.test.cases;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.repositoryloader.MapperRepositoryLoader;
import com.cylande.unitedretail.batch.scheduler.BatchEngine;
import com.cylande.unitedretail.batch.service.MapperManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.MapperManagerService;
import com.cylande.unitedretail.framework.service.TechnicalServiceNotDeliveredException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.MapperCriteriaListType;
import com.cylande.unitedretail.message.batch.MapperListType;
import com.cylande.unitedretail.message.batch.MapperScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Tests sur le service CRUD de gestion des fonctions métiers.
 * @see com.cylande.unitedretail.batch.service.common.MapperManagerService
 */
public class MapperPopulateTest extends TestCase
{
  static BatchEngine _batchEngine = null;
  MapperManagerService _service = null;
  MapperRepositoryLoader _repository = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   */
  public MapperPopulateTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * Ordonnancement des tests.
   */
  public static Test suite()
  {
    TestSuite suite;
    try
    {
      _batchEngine = new BatchEngine();
    }
    catch (BatchException e)
    {
      // TODO
    }
    suite = new TestSuite("MapperPopulateTest");
    suite.addTest(new MapperPopulateTest("testPopulateMapper"));
    suite.addTest(new MapperPopulateTest("testGetMapper"));
    return suite;
  }

  /**
   * Démarrage du cas de test.
   * Instanciation du service
   * @throws Exception
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    _service = new MapperManagerServiceImpl();
    _repository = new MapperRepositoryLoader();
  }

  /**
   * Arrêt du cas de test.
   * Libération du service
   * @throws Exception
   */
  protected void tearDown() throws Exception
  {
    _service = null;
    _repository = null;
    super.tearDown();
  }

  /**
   * Récupération de la liste de valeur.
   * @throws RemoteException
   * @throws WrapperServiceException
   */
  public void testGetMapper() throws RemoteException, WrapperServiceException, TechnicalServiceNotDeliveredException
  {
    MapperCriteriaListType myCriterias = new MapperCriteriaListType();
    MapperScenarioType myScenario = new MapperScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (MapperScenarioType)myJaxbManager.read("ScenarioMapper.xml", myScenario);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    MapperListType myList = _service.findMapper(myCriterias, myScenario, myContext);
    assertEquals(2, myList.getValues().size());
  }

  /**
   * Chargement des données avec l'opération <b>post</b>.
   * @throws RemoteException
   * @throws WrapperServiceException
   */
  public void testPopulateMapper() throws RemoteException, WrapperServiceException, TechnicalServiceNotDeliveredException
  {
    MapperListType myList = new MapperListType();
    MapperScenarioType myScenario = new MapperScenarioType();
    ContextType myContext = new ContextType();
    JAXBManager myJaxbManager = new JAXBManager();
    myList = (MapperListType)myJaxbManager.read("PopulateMapper.xml", myList);
    myContext = (ContextType)myJaxbManager.read("ContextDefinitionOne.xml", myContext);
    myScenario = (MapperScenarioType)myJaxbManager.read("ScenarioMapper.xml", myScenario);
    myList = _service.postMapperList(myList, myScenario, myContext);
    assertTrue("Pas d'enregistrements", 0 < myList.getValues().size());
  }
}
