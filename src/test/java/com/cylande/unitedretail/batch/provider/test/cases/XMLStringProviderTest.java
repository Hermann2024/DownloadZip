package com.cylande.unitedretail.batch.provider.test.cases;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.impl.XMLStringProvider;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderReaderImpl;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderType;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

/**
 * test de la classe XMLStringProvider
 */
public class XMLStringProviderTest extends TestCase
{

  /**
   * constructor
   * @param pTestName le nom du test case
   */
  public XMLStringProviderTest(String pTestName)
  {
    super(pTestName);
  }

  protected void setUp() throws Exception
  {
    // l'implémentation doit être forcée sinon Maven utilise celle du jar wstx-asl qui fait planter le test dans le StaxXMLParser
    System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.stream.ZephyrWriterFactory");
  }

  /**
   * test avec une chaine XML non null
   * @throws ProviderException ?
   * @throws UnsupportedEncodingException ?
   * @throws IOException ?
   */
  public void testNotNullXMLString() throws ProviderException, UnsupportedEncodingException, IOException
  {
    XMLStringProvider result = new XMLStringProvider(null, null, null, null, "test");
    baseAssert(result);
    // test + compliqué
    assertTrue("le provider doit avoir un stream a fournir ", result.hasNextInputStream());
    assertTrue("le provider doit avoir un reader a fournir ", result.hasNextBufferedReader());
    result.closeInputStream();
    Reader myReader = result.nextTransformedBufferedReader(null);
    assertNotNull("le reader ne doit pas etre null", myReader);
    assertEquals("le contenu du reader n'est pas celui fournit en entré", "test", new LineNumberReader(myReader).readLine());
    // comment tester que le stream est bien fermé ??
    result.closeInputStream();
    assertFalse("le provider ne doit pas avoir de stream a fournir ", result.hasNextInputStream());
    assertFalse("le provider ne doit pas avoir de reader a fournir ", result.hasNextBufferedReader());
    /*try
    {
      // TODO il serrai peut etre judicieux que cette methode renvoie une exception + approprié dans ce cas?
      result.nextTransformedBufferedReader(null);
      fail("null pointer must append");
    }
    catch (NullPointerException npe)
    {
      // nothing todo
      //assertTrue(true);
    }*/
  }

  /**
   * test de base qui ne change pas
   * @param pResult XMLStringProvider instance
   * @throws ProviderException ?
   */
  private void baseAssert(XMLStringProvider pResult) throws ProviderException
  {
    pResult.closeOutputStream();
    assertFalse("cette methode renvoie toujour false", pResult.providerDefinitionUpdated());
    try
    {
      pResult.getTransformedOutputStream();
      fail("une exception du type UnsupportedOperationException aurai dut etre levée");
    }
    catch (UnsupportedOperationException uoe)
    {
      assertEquals("le message de l'exception n'est pas celui attendu", "ce provider ne supporte pas l'ecriture", uoe.getMessage());
    }
  }

  /**
   * test avec une chaine xml null
   * @throws ProviderException ?
   * @throws UnsupportedEncodingException ?
   * @throws IOException ?
   */
  public void testNullXmlString() throws ProviderException, UnsupportedEncodingException, IOException
  {
    XMLStringProvider result = new XMLStringProvider(buildProviderDef(), null, null, null, null);
    baseAssert(result);
    assertFalse("le provider ne doit pas avoir de stream a fournir ", result.hasNextInputStream());
    assertFalse("le provider ne doit pas avoir de reader a fournir ", result.hasNextBufferedReader());
    result.closeInputStream();
    /*try
    {
      // TODO il serrai peut etre judicieux que cette methode renvoie une exception + approprié dans ce cas?
      result.nextTransformedBufferedReader(null);
      fail("null pointer must append");
    }
    catch (NullPointerException npe)
    {
      assertTrue(true);
    }*/
  }

  /**
   * helper pour l'instanciation d'une definition de provider
   * @return definition de provider
   */
  private ProviderType buildProviderDef()
  {
    return new FILEPROVIDER();
  }

  public void testProviderReader() throws ProviderException, UnsupportedEncodingException, IOException
  {
    String values = "<values><code>code</code></values>";
    // test de lecture de 3 éléments avec une taille de paquet de 2
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><addressTypeListType>" + values + values + values + "</addressTypeListType>";
    testProviderReaderWithPackSize(xml);
    // test de lecture de 4 éléments avec une taille de paquet de 2
    xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><addressTypeListType>" + values + values + values + values + "</addressTypeListType>";
    testProviderReaderWithPackSize(xml);
  }

  /**
   * Test de lecture par paquet de 2 d'une chaine comportant 3 ou 4 éléments : on vérifie que la lecture ne génère bien que 2 paquets de données
   * @param pXmlString les data
   * @throws ProviderException ?
   * @throws UnsupportedEncodingException ?
   * @throws IOException ?
   */
  private void testProviderReaderWithPackSize(String pXmlString) throws ProviderException, UnsupportedEncodingException, IOException
  {
    Provider xmlStringProvider = new XMLStringProvider(null, null, "test domain", null, pXmlString);
    ProviderReader reader = new ProviderReaderImpl(xmlStringProvider, 2, null, null);
    // lecture paquet 1
    DataPackage data = reader.read(null, null);
    assertFalse("cela ne doit pas etre le dernier paquet", data.isLastPackage());
    assertEquals("l'index du paquet n'est pas correct", 1, data.getPackageNumber());
    // lecture paquet 2
    data = reader.read(null, null);
    // data.isLastPackage() = false sur le dernier paquet quand le nombre d'éléments est un multiple de la taille du paquet
    // ce test n'est donc pas pertinent sur le dernier paquet (d'où sa désactivation)
    // le test qui importe est que dans tous les cas la lecture effectuée après la lecture du dernier parquet doit renvoyer null
    //assertTrue("cela doit etre le dernier paquet", data.isLastPackage());
    assertEquals("l'index du paquet n'est pas correct", 2, data.getPackageNumber());
    // teste qu'il n'y a plus rien à lire après la lecture du dernier paquet attendu
    data = reader.read(null, null);
    assertNull("must be null", data);
  }
}
