package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.ProviderManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.ProviderManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderCriteriaListType;
import com.cylande.unitedretail.message.batch.ProviderCriteriaType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderKeyType;
import com.cylande.unitedretail.message.batch.ProviderListType;
import com.cylande.unitedretail.message.batch.ProviderScenarioType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.rmi.RemoteException;

import java.sql.SQLException;

import junit.framework.TestCase;

/**
 * Tests sur le service CRUD de gestion des Providers (Repository)
 * @author eselosse
 * @version 1.0
 * @see com.cylande.unitedretail.batch.service.common.ProviderManagerService
 * @since 02/04/2008
 */
public class ProviderManagerTest extends TestCase
{
  private ProviderManagerService _service = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 02/04/2008
   */
  public ProviderManagerTest(String pTestName)
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

  public void testCreateProvider() throws RemoteException, ServiceException
  {
    ProviderType myProvider = new FILEPROVIDER();
    ProviderScenarioType myScenarioType = new ProviderScenarioType();
    ContextType pContext = new ContextType();
    //Declaration d'un Provider Parent
    myProvider.setName("ProviderInput");
    myProvider.setDescription("Description Provider");
    ProviderFileType providerFile = new ProviderFileType();
    providerFile.setDir("C:\\");
    providerFile.setFileName("myFile.xml");
    ((FILEPROVIDER)myProvider).setFile(providerFile);
    myProvider = _service.createProvider(myProvider, myScenarioType, pContext);
    assertTrue(true);
  }

  public void testUpdateProvider() throws RemoteException, ServiceException
  {
    ContextType myContext = new ContextType();
    ProviderScenarioType myScenario = new ProviderScenarioType();
    ProviderType myProvider = new FILEPROVIDER();
    myProvider.setName("supplierCriteriaList");
    //Modification de la Description du Provider
    myProvider.setDescription("Description Traitement Provider");
    myProvider = _service.updateProvider(myProvider, myScenario, myContext);
    assertTrue(true);
  }

  public void testCreateProviderList() throws RemoteException, ServiceException
  {
    ProviderListType myList = new ProviderListType();
    ProviderType myProvider = new FILEPROVIDER();
    myProvider.setName("P1");
    myProvider.setDescription("Description P1");
    ProviderFileType providerFile1 = new ProviderFileType();
    providerFile1.setDir("C:\\");
    providerFile1.setFileName("myFile1.xml");
    ((FILEPROVIDER)myProvider).setFile(providerFile1);
    myList.getValues().add(myProvider);
    ProviderType myProvider2 = new FILEPROVIDER();
    myProvider2.setName("P2");
    myProvider2.setDescription("Description P2");
    ProviderFileType providerFile2 = new ProviderFileType();
    providerFile2.setDir("D:\\");
    providerFile2.setFileName("myFile2.xml");
    ((FILEPROVIDER)myProvider2).setFile(providerFile2);
    myList.getValues().add(myProvider2);
    myList = _service.createProviderList(myList, null, null);
    assertTrue(true);
  }

  public void testDeleteProvider() throws RemoteException, ServiceException
  {
    ProviderKeyType myKey = new ProviderKeyType();
    ProviderScenarioType myScenarioType = new ProviderScenarioType();
    ContextType pContext = new ContextType();
    myKey.setName("supplierCriteriaList");
    _service.deleteProvider(myKey, myScenarioType, pContext);
    assertTrue(true);
  }

  public void testDeleteProviderList() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    ProviderCriteriaListType myCriterias = new ProviderCriteriaListType();
    ProviderCriteriaType myCriteria = new ProviderCriteriaType();
    CriteriaStringType myValue = new CriteriaStringType();
    myValue.setContains("supplier");
    myCriteria.setName(myValue);
    myCriterias.getList().add(myCriteria);
    _service.deleteProviderList(myCriterias, null, myContext);
    assertTrue(true);
  }

  public void testGetProvider() throws RemoteException, ServiceException, SQLException
  {
    ProviderKeyType myKey = new ProviderKeyType();
    ProviderType myProvider = createProvider();
    myKey.setName(myProvider.getName());
    ProviderScenarioType myScenario = new ProviderScenarioType();
    ContextType myContext = new ContextType();
    ProviderType myProviderOut = _service.getProvider(myKey, myScenario, myContext);
    assertNotNull(myProviderOut);
  }

  public void testFindProvider() throws RemoteException, ServiceException, SQLException
  {
    // Créer un Provider pour pouvoir le retrouver
    createProvider();
    ProviderScenarioType myScenario = new ProviderScenarioType();
    ProviderCriteriaListType myCriteriasList = new ProviderCriteriaListType();
    ProviderCriteriaType myCriteria1 = new ProviderCriteriaType();
    CriteriaStringType myCriteriaString = new CriteriaStringType();
    myCriteriaString.setContains("supplier");
    myCriteria1.setName(myCriteriaString);
    myCriteriasList.getList().add(myCriteria1);
    ContextType myContext = new ContextType();
    ProviderListType myProviderOut = _service.findProvider(myCriteriasList, myScenario, myContext);
    assertNotNull(myProviderOut);
    // on doit récupérer un Provider
    assertTrue(myProviderOut.getValues().size() == 2);
  }

  private ProviderType createProvider() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    ProviderKeyType myKey = new ProviderKeyType();
    myKey.setName("P1");
    _service.deleteProvider(myKey, null, myContext);
    ProviderType myProvider = new FILEPROVIDER();
    //Declaration d'un Provider Parent
    myProvider.setName("P1");
    myProvider.setDescription("Description P1");
    ProviderFileType providerFile = new ProviderFileType();
    providerFile.setDir("C:\\");
    providerFile.setFileName("myFile.xml");
    ((FILEPROVIDER)myProvider).setFile(providerFile);
    myProvider = _service.createProvider(myProvider, null, null);
    assertNotNull(myProvider);
    return myProvider;
  }
}
