package com.cylande.unitedretail.batch.task.test.suite;

import com.cylande.unitedretail.batch.task.test.cases.TaskFactoryTest;
import com.cylande.unitedretail.batch.task.test.cases.TaskIntegrationUnitRejectTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * suite de test concernant les classes du package batch.task
 */
public class TaskTestSuite extends TestSuite
{
  /**
   * suite de test des classes du package batch.task
   * @return Test : test
   */
  public static Test suite()
  {
    TestSuite suite;
    suite = new TestSuite("TaskTestSuite");
    suite.addTestSuite(TaskFactoryTest.class);
    suite.addTestSuite(TaskIntegrationUnitRejectTest.class);
    return suite;
  }
}
