package com.cylande.unitedretail.batch.task.test.cases;

import com.cylande.unitedretail.batch.batch.AbstractBatch;
import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.TaskException;
import com.cylande.unitedretail.batch.task.TaskIntegrationUnitReject;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.TaskProcessorType;
import com.cylande.unitedretail.message.batch.TaskType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * test de la classe taskIntegrationUnitReject
 */
public class TaskIntegrationUnitRejectTest extends TestCase
{
  /**
   * nom du fichier utiliser pour les tests de serialisation
   */
  private static final String SERIALIZATION_FILENAME = "target/testSerilaization.test";

  /**
   * constructor
   * @param pTestName le nom du test case
   */
  public TaskIntegrationUnitRejectTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * test du consturcteur avec une definition de tache null
   */
  public void testConstructorNullTaskDef()
  {
    try
    {
      new TaskIntegrationUnitReject(null, null);
      fail("une exception aurait du etre levée");
    }
    catch (TaskException e)
    {
      assertEquals("le code de l'exception n'est pas celui attendu", BatchErrorDetail.INIT_TASK_PARAM, e.returnErrorDetail());
    }
  }

  /**
   * test du constructeur avec un batch parent null
   * @TODO test a implementer pour l'instant l'implementation ne leve pas d'exception
   * ce qui na pas de sens car une task ne peut pas exister sans batch parent.
   */
  public void testConstructorNullBatchParent()
  {
    try
    {
      new TaskIntegrationUnitReject(null, buildINTEGRATION());
      // TODO l'implementation n'est pas coerente avec la logique du moteur de batch see javadoc
      //fail("une exception aurai dut etre levée");
    }
    catch (TaskException e)
    {
      assertEquals("le code de l'exception n'est pas celui attendu", BatchErrorDetail.INIT_TASK_PARAM, e.returnErrorDetail());
    }
  }

  /**
   * helper pour instancier une definition de tache d'integration
   * @return new INTEGRATION
   */
  private INTEGRATION buildINTEGRATION()
  {
    INTEGRATION result = new INTEGRATION();
    setTaskType(result);
    //result .setCommitFrequency();
    //result .setInput();
    //result .setReject();
    //result .setResponse();
    //result .setThreadCount();
    //result .setUnitReject(Boolean.TRUE);
    return result;
  }

  /**
   * helper pour setter les champs d'un tasktype
   * @param pTaskDef la definition de tache a setter
   */
  private void setTaskType(TaskType pTaskDef)
  {
    pTaskDef.setName("JUnitDomain");
    pTaskDef.setDescription("Task de test");
    TaskProcessorType myTaskProcess = new TaskProcessorType();
    myTaskProcess.setActiveDomain("JUnitDomain");
    myTaskProcess.setRef("Process ref");
    pTaskDef.setProcessor(myTaskProcess);
  }

  /**
   * helper pour la construction d'un batch parent
   * @return an abstractBatch
   */
  private AbstractBatch buildParentBatch()
  {
    // TODO implement this methode
    return null;
  }

  /**
   * test de serialisation pour fonctionnement mode cluster
   * @throws IOException pb sur read/write du fichier de serialization
   * @throws TaskException pb sur construction de la task
   * @throws ClassNotFoundException pb a la lecture de la deserialization
   */
  public void testSerialisation() throws IOException, TaskException, ClassNotFoundException
  {
    // serialize object
    ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(SERIALIZATION_FILENAME));
    TaskIntegrationUnitReject myTask = new TaskIntegrationUnitReject(buildParentBatch(), buildINTEGRATION());
    o.writeObject(myTask);
    o.close();
    myTask = null;
    // read serialized object
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(SERIALIZATION_FILENAME));
    myTask = (TaskIntegrationUnitReject)in.readObject();
    // TODO make assert
  }
}
