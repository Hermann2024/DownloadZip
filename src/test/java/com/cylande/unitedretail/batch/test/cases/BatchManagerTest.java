package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.repositoryloader.BatchRepositoryLoader;
import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.BatchManagerService;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.message.batch.BatchChildType;
import com.cylande.unitedretail.message.batch.BatchContentType;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchCriteriaType;
import com.cylande.unitedretail.message.batch.BatchEnum;
import com.cylande.unitedretail.message.batch.BatchKeyType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchScenarioType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;

import java.rmi.RemoteException;

import java.sql.SQLException;

import junit.framework.TestCase;

/**
 * Tests sur le service CRUD de gestion des Batchs (_repository)
 * @author eselosse
 * @version 1.0
 * @see com.cylande.unitedretail.batch.service.common.BatchManagerService
 * @since 01/04/2008
 */
public class BatchManagerTest extends TestCase
{
  BatchManagerService _service = null;
  BatchRepositoryLoader _repository = null;

  /**
   * Constructeur par défaut.
   * @param pTestName : non du cas de test JUNIT
   * @since 01/04/2008
   */
  public BatchManagerTest(String pTestName)
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
    _service = new BatchManagerServiceImpl();
    _repository = new BatchRepositoryLoader();
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
    _repository = null;
    super.tearDown();
  }

  public void testCreateBatch() throws RemoteException, ServiceException
  {
    BatchType myBatch = new BatchType();
    BatchScenarioType myScenarioType = new BatchScenarioType();
    ContextType pContext = new ContextType();
    //Declaration d'un Batch Parent
    myBatch.setName("B0");
    myBatch.setDescription("Description Traitement Batch");
    BatchContentType batchContentType = new BatchContentType();
    //Referencement d'un 1er Batch
    BatchChildType batchRef1 = new BatchChildType();
    batchRef1.setRef("B1");
    batchContentType.getTaskOrBatchOrComment().add(batchRef1);
    //Referencement d'un 2ème Batch
    BatchChildType batchRef2 = new BatchChildType();
    batchRef2.setRef("B2");
    batchContentType.getTaskOrBatchOrComment().add(batchRef2);
    myBatch.setSequence(batchContentType);
    myBatch.setType(BatchEnum.STATELESS);
    myBatch = _service.createBatch(myBatch, myScenarioType, pContext);
    assertTrue(true);
  }

  public void testUpdateBatch() throws RemoteException, ServiceException
  {
    ContextType myContext = new ContextType();
    BatchScenarioType myScenario = new BatchScenarioType();
    BatchType myBatch = new BatchType();
    myBatch.setName("mixedbatch");
    //Modification de la Description du Batch
    myBatch.setDescription("Description Traitement Batch");
    myBatch = _service.updateBatch(myBatch, myScenario, myContext);
    assertTrue(true);
  }

  public void testCreateBatchList() throws RemoteException, ServiceException
  {
    BatchListType myList = new BatchListType();
    BatchType myBatch = new BatchType();
    myBatch.setName("B0");
    myBatch.setDescription("Description Traitement Batch 1er");
    BatchContentType batchContentType1 = new BatchContentType();
    //Referencement d'un 1er Batch
    BatchChildType batchRef1 = new BatchChildType();
    batchRef1.setRef("B1");
    batchContentType1.getTaskOrBatchOrComment().add(batchRef1);
    //Referencement d'un 2ème Batch
    BatchChildType batchRef2 = new BatchChildType();
    batchRef2.setRef("B2");
    batchContentType1.getTaskOrBatchOrComment().add(batchRef2);
    myBatch.setSequence(batchContentType1);
    myBatch.setType(BatchEnum.STATELESS);
    myList.getValues().add(myBatch);
    BatchType myBatch2 = new BatchType();
    myBatch2.setName("B3");
    myBatch2.setDescription("Description Traitement Batch");
    BatchContentType batchContentType2 = new BatchContentType();
    //Referencement d'un 1er Batch
    BatchChildType batchRef3 = new BatchChildType();
    batchRef3.setRef("B4");
    batchContentType2.getTaskOrBatchOrComment().add(batchRef3);
    //Referencement d'un 2ème Batch
    BatchChildType batchRef4 = new BatchChildType();
    batchRef4.setRef("B5");
    batchContentType2.getTaskOrBatchOrComment().add(batchRef4);
    myBatch2.setSequence(batchContentType2);
    myBatch2.setType(BatchEnum.STATELESS);
    myList.getValues().add(myBatch2);
    myList = _service.createBatchList(myList, null, null);
    assertTrue(true);
  }

  public void testDeleteBatch() throws RemoteException, ServiceException
  {
    BatchKeyType myKey = new BatchKeyType();
    BatchScenarioType myScenarioType = new BatchScenarioType();
    ContextType pContext = new ContextType();
    myKey.setName("mixedbatch");
    _service.deleteBatch(myKey, myScenarioType, pContext);
    assertTrue(true);
  }

  public void testDeleteBatchList() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    BatchCriteriaListType myCriterias = new BatchCriteriaListType();
    BatchCriteriaType myCriteria = new BatchCriteriaType();
    CriteriaStringType myValue = new CriteriaStringType();
    myValue.setEquals(BatchEnum.STATELESS.toString());
    myCriteria.setType(myValue);
    myCriterias.getList().add(myCriteria);
    _service.deleteBatchList(myCriterias, null, myContext);
    assertTrue(true);
  }

  public void testGetBatch() throws RemoteException, ServiceException, SQLException
  {
    BatchKeyType myKey = new BatchKeyType();
    BatchType myBatch = createBatch();
    myKey.setName(myBatch.getName());
    BatchScenarioType myScenario = new BatchScenarioType();
    ContextType myContext = new ContextType();
    BatchType myBatchOut = _service.getBatch(myKey, myScenario, myContext);
    assertNotNull(myBatchOut);
  }

  public void testFindBatch() throws RemoteException, ServiceException, SQLException
  {
    // Créer un Batch pour pouvoir le retrouver
    createBatch();
    BatchScenarioType myScenario = new BatchScenarioType();
    BatchCriteriaListType myCriteriasList = new BatchCriteriaListType();
    BatchCriteriaType myCriteria1 = new BatchCriteriaType();
    CriteriaStringType myCriteriaString = new CriteriaStringType();
    myCriteriaString.setContains("batch");
    myCriteria1.setName(myCriteriaString);
    myCriteriasList.getList().add(myCriteria1);
    ContextType myContext = new ContextType();
    BatchListType myBatchOut = _service.findBatch(myCriteriasList, myScenario, myContext);
    assertNotNull(myBatchOut);
    // on doit récupérer un Batch
    assertTrue(myBatchOut.getValues().size() == 3);
  }

  private BatchType createBatch() throws RemoteException, ServiceException, SQLException
  {
    ContextType myContext = new ContextType();
    BatchKeyType myKey = new BatchKeyType();
    myKey.setName("B01");
    _service.deleteBatch(myKey, null, myContext);
    BatchType myBatch = new BatchType();
    //Declaration d'un Batch Parent
    myBatch.setName("B01");
    myBatch.setDescription("Description Traitement Batch");
    BatchContentType batchContentType = new BatchContentType();
    //Referencement d'un 1er Batch
    BatchChildType batchRef1 = new BatchChildType();
    batchRef1.setRef("B11");
    batchContentType.getTaskOrBatchOrComment().add(batchRef1);
    //Referencement d'un 2ème Batch
    BatchChildType batchRef2 = new BatchChildType();
    batchRef2.setRef("B22");
    batchContentType.getTaskOrBatchOrComment().add(batchRef2);
    myBatch.setSequence(batchContentType);
    myBatch.setType(BatchEnum.STATEFULL);
    myBatch = _service.createBatch(myBatch, null, null);
    assertNotNull(myBatch);
    return myBatch;
  }
}
