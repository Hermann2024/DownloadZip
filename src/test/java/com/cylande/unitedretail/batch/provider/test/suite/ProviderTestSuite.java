package com.cylande.unitedretail.batch.provider.test.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.cylande.unitedretail.batch.provider.test.cases.FileProviderTest;
import com.cylande.unitedretail.batch.provider.test.cases.XMLStringProviderTest;

/**
 * suite de test concernant les classes du package batch.provider
 */
public class ProviderTestSuite extends TestSuite
{
  /**
   * suite de test des classes du package batch.provider
   * @return Test : test
   */
  public static Test suite()
  {
    TestSuite suite;
    suite = new TestSuite("ProviderTestSuite");
    suite.addTestSuite(XMLStringProviderTest.class);
    suite.addTestSuite(FileProviderTest.class);
    return suite;
  }
}
