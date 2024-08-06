package com.cylande.unitedretail.batch.test.suite;

import com.cylande.unitedretail.batch.test.cases.BatchManagerTest;
import com.cylande.unitedretail.batch.test.cases.BatchRunManagerTest;
import com.cylande.unitedretail.batch.test.cases.ProviderManagerTest;
import com.cylande.unitedretail.batch.test.cases.TaskManagerTest;
import com.cylande.unitedretail.batch.test.cases.TaskRunManagerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BatchTestSuite extends TestSuite
{
  public static Test suite()
  {
    TestSuite suite;
    suite = new TestSuite("BatchTestSuite");
    suite.addTestSuite(BatchRunManagerTest.class);
    suite.addTestSuite(TaskRunManagerTest.class);
    suite.addTestSuite(BatchManagerTest.class);
    suite.addTestSuite(TaskManagerTest.class);
    suite.addTestSuite(ProviderManagerTest.class);
    return suite;
  }
}
