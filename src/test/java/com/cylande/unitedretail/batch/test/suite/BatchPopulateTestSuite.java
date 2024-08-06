package com.cylande.unitedretail.batch.test.suite;

import com.cylande.unitedretail.batch.test.cases.BatchPopulateTest;
import com.cylande.unitedretail.batch.test.cases.BatchRunPopulateTest;
import com.cylande.unitedretail.batch.test.cases.ProviderPopulateTest;
import com.cylande.unitedretail.batch.test.cases.TaskAuditPopulateTest;
import com.cylande.unitedretail.batch.test.cases.TaskPopulateTest;
import com.cylande.unitedretail.batch.test.cases.TaskRunPopulateTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite de tests pour le chargement des données du projet Batch.
 * @author eselosse
 * @since 01/04/2008
 */
public class BatchPopulateTestSuite extends TestSuite
{
  /**
   * Ordonnancement des tests.
   * @return une suite de tests JUNIT
   * @since 01/04/2008
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite("BatchPopulateTestSuite");
    addTest(suite);
    return suite;
  }

  /**
   * Lancement des suites de chargement des données.
   * @param pSuite : suite de tests JUNIT à charger
   * @see BatchRunPopulateTest
   * @see TaskRunPopulateTest
   * @see TaskAuditPopulateTest
   * @see BatchPopulateTest
   * @see TaskPopulateTest
   * @since 01/04/2008
   */
  public static void addTest(TestSuite pSuite)
  {
    pSuite.addTest(BatchRunPopulateTest.suite());
    pSuite.addTest(TaskRunPopulateTest.suite());
    pSuite.addTest(TaskAuditPopulateTest.suite());
    pSuite.addTest(BatchPopulateTest.suite());
    pSuite.addTest(TaskPopulateTest.suite());
    pSuite.addTest(ProviderPopulateTest.suite());
  }
}
