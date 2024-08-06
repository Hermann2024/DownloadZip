package com.cylande.unitedretail.batch.task.test.cases;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.EUBuildException;
import com.cylande.unitedretail.batch.task.AbstractTask;
import com.cylande.unitedretail.batch.task.TaskExtractionImpl;
import com.cylande.unitedretail.batch.task.TaskFactory;
import com.cylande.unitedretail.batch.task.TaskIntegrationImpl;
import com.cylande.unitedretail.batch.task.TaskIntegrationUnitReject;
import com.cylande.unitedretail.message.batch.EXTRACTION;
import com.cylande.unitedretail.message.batch.INTEGRATION;
import com.cylande.unitedretail.message.batch.TaskProcessorType;
import com.cylande.unitedretail.message.batch.TaskType;

import junit.framework.TestCase;

/**
 * test unitaire de la factory de task
 */
public class TaskFactoryTest extends TestCase
{
  /**
   * message si le type n'est pas celui attendu
   */
  private static final String NOT_EXPECTED_TYPE_MSG = "ce n'est pas le type d'instance attendue(";

  /**
   * domain pour les tests unitaires
   */
  private static final String TEST_DOMAIN = "TEST-JUNIT";

  /**
   * constructor
   * @param pTestName le nom du test case
   */
  public TaskFactoryTest(String pTestName)
  {
    super(pTestName);
  }

  /**
   * test avec une definition de tache null
   * les assertions sont faites dans la sous methode
   */
  @SuppressWarnings("PMD")
  public void testNullTaskType()
  {
    testInvalidTaskType(null);
  }

  /**
   * test avec une definition de tache non supportée
   * les assertions sont faites dans la sous methode
   */
  @SuppressWarnings("PMD")
  public void testUnsuportedTaskType()
  {
    testInvalidTaskType(new UnsuportedTaskType());
  }

  /**
   * test pour les invocations avec des parametres invalides
   * @param pTaskdef la definition de la tache a utiliser
   */
  private void testInvalidTaskType(TaskType pTaskdef)
  {
    try
    {
      TaskFactory.create(null, pTaskdef, "test", null, Boolean.TRUE);
      fail("Une EUBuildException aurai du être levée");
    }
    catch (EUBuildException e)
    {
      assertEquals("le code l'exception n'est pas celui attendu", BatchErrorDetail.LOAD_TASK_ERR, e.returnErrorDetail());
      assertEquals("le type de la cause n'est pas celui attendu", e.getCause().getClass(), e.getClass());
      assertEquals("le code de la cause n'est pas celui attendu", BatchErrorDetail.UNSUPORTED_TASKTYPE, ((EUBuildException)e.getCause()).returnErrorDetail());
    }
  }

  /**
   * test creation tache d'integration
   * @throws EUBuildException ne doit pas se produire => failure
   */
  public void testCreateINTEGRATIONTask() throws EUBuildException
  {
    INTEGRATION myIntegrationDef = buildINTEGRATION();
    AbstractTask result = TaskFactory.create(null, myIntegrationDef, "JUnitDomain", null, Boolean.FALSE);
    assertTrue(NOT_EXPECTED_TYPE_MSG + result.getClass().getName() + ")", result instanceof TaskIntegrationImpl);
    assertFalse(NOT_EXPECTED_TYPE_MSG + result.getClass().getName() + ")", result instanceof TaskIntegrationUnitReject);
  }

  /**
   * test creation tache d'integration avec retry unitaire sur les paquets rejetés
   * @throws EUBuildException ne doit pas se produire => failure
   */
  public void testCreateINTEGRATIONTaskWhithRetry() throws EUBuildException
  {
    INTEGRATION myIntegrationDef = buildINTEGRATION();
    //set specifique a ce test
    myIntegrationDef.setUnitReject(Boolean.TRUE);
    AbstractTask result = TaskFactory.create(null, myIntegrationDef, "JUnitDomain", null, Boolean.FALSE);
    assertTrue(NOT_EXPECTED_TYPE_MSG + result.getClass().getName() + ")", result instanceof TaskIntegrationUnitReject);
  }

  /**
   * test creation tache d'extraction
   * @throws EUBuildException ne doit pas se produire => failure
   */
  public void testCreateEXTRACTIONTask() throws EUBuildException
  {
    EXTRACTION myIntegrationDef = buildEXTRACTION();
    AbstractTask result = TaskFactory.create(null, myIntegrationDef, TEST_DOMAIN, null, Boolean.FALSE);
    assertTrue(NOT_EXPECTED_TYPE_MSG + result.getClass().getName() + ")", result instanceof TaskExtractionImpl);
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
   * helper pour instancier une definition de tache d'extraction
   * @return new EXTRACTION
   */
  private EXTRACTION buildEXTRACTION()
  {
    EXTRACTION result = new EXTRACTION();
    setTaskType(result);
    return result;
  }

  /**
   * helper pour setter les champs d'un tasktype
   * @param pTaskDef la definition de tache a setter
   */
  private void setTaskType(TaskType pTaskDef)
  {
    pTaskDef.setName(TEST_DOMAIN);
    pTaskDef.setDescription("Task de test");
    TaskProcessorType myTaskProcess = new TaskProcessorType();
    myTaskProcess.setActiveDomain("JUnitDomain");
    myTaskProcess.setRef("Process ref");
    pTaskDef.setProcessor(myTaskProcess);
  }
}
