package com.cylande.unitedretail.batch.test.cases;

import com.cylande.unitedretail.batch.service.MapperManagerServiceImpl;
import com.cylande.unitedretail.batch.service.common.MapperManagerService;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.MapperListType;
import com.cylande.unitedretail.message.batch.MapperScenarioType;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.message.common.context.ContextType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MapperPostTest extends TestCase
{
  MapperManagerService _service = null;
  ContextType _context = null;
  MapperScenarioType _scenario = null;
  MapperListType _mapperList = null;
  MapperType _mapper = null;

  public MapperPostTest(String pTestName)
  {
    super(pTestName);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    _service = new MapperManagerServiceImpl();
    _context = new ContextType();
    _context.setUserCode("TEST");
    _context.setUserId(99);
    _context.setLanguage("FR");
    _context.setBusinessUnit(1000);
    _scenario = new MapperScenarioType();
    // Récupère une liste des mappers
    _mapperList = new MapperListType();
    JAXBManager myJaxbManager = new JAXBManager();
    // Le fichier populateMapper.xml vient du projet BATCH
    _mapperList = (MapperListType)myJaxbManager.read("populateMapper.xml", _mapperList);
    for (int i = 0; i < _mapperList.getValues().size(); i++)
    {
      _mapperList.getValues().get(i).setName("test");
    }
    _mapper = _mapperList.getValues().get(0);
    _mapperList.getValues().remove(0);
  }

  protected void tearDown() throws Exception
  {
    _service = null;
    _mapper = null;
    _mapperList = null;
    super.tearDown();
  }

  public static Test suite()
  {
    TestSuite suite;
    suite = new TestSuite("MapperPostTest");
    suite.addTest(new MapperPostTest("testPostMapper"));
    suite.addTest(new MapperPostTest("testPostMapperList"));
    return suite;
  }

  /**
   * Teste la méthode postMapper pour alimenter l'entité Mapper.
   */
  public void testPostMapper()
  {
    try
    {
      MapperType myMapperOut = _service.postMapper(_mapper, _scenario, _context);
      assertNotNull(myMapperOut);
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Teste la méthode postMapperList pour alimenter l'entité Mapper.
   */
  public void testPostMapperList()
  {
    try
    {
      MapperListType myMapperListOut = _service.postMapperList(_mapperList, _scenario, _context);
      assertNotNull(myMapperListOut);
      assertEquals(_mapperList.getValues().size(), myMapperListOut.getValues().size());
    }
    catch (Exception e)
    {
      fail();
    }
  }
}
