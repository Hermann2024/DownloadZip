package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.repository.MapperRepository;
import com.cylande.unitedretail.batch.service.common.MapperManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.MapperCriteriaListType;
import com.cylande.unitedretail.message.batch.MapperKeyType;
import com.cylande.unitedretail.message.batch.MapperListType;
import com.cylande.unitedretail.message.batch.MapperScenarioType;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.message.common.context.ContextType;

import org.apache.log4j.Logger;

/**
 * class MapperManagerServiceImpl implements MapperManagerService.
 */
public class MapperManagerServiceImpl extends AbstractCRUDServiceImpl implements MapperManagerService
{

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(MapperManagerServiceImpl.class);
  private MapperRepository _mapperRepository = null;

  /**
   * Constructor
   */
  public MapperManagerServiceImpl()
  {
    _mapperRepository = MapperRepository.getInstance();
  }

  /**
   * put pMapper in the mapperRepositoryMap.
   * @param pMapper mapperType
   * @param pScenario Scenario
   * @param pContext Context
   * @return MapperType : mapperType
   * @throws WrapperServiceException : exception
   */
  public MapperType createMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperType result = null;
    getChrono().start();
    getJAXBManager().write(pMapper, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.createMapper(pMapper, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      LOGGER.error(e.getMessage());
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * put List of mapper in the mapperRepositoryMap.
   * @param pList mapperType
   * @param pScenario Scenario
   * @param pContext Context
   * @return MapperType : mapperType
   * @throws WrapperServiceException : exception
   */
  public MapperListType createMapperList(MapperListType pList, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.createMapperList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * Remove mapper of mapperRepositoryMap.
   * @param pKey : MapperKey
   * @param pScenario : Scenario
   * @param pContext : Context
   * @throws WrapperServiceException : Exception
   */
  public void deleteMapper(MapperKeyType pKey, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _mapperRepository.deleteMapper(pKey, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
  }

  /**
   * Remove List mapper of mapperRepositoryMap.
   * @param pCriterias : MapperCriteriaList
   * @param pScenario : Scenario
   * @param pContext : Context
   * @throws WrapperServiceException : Exeption
   */
  public void deleteMapperList(MapperCriteriaListType pCriterias, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      _mapperRepository.deleteMapperList(pCriterias, pScenario, pContext);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
  }

  /**
   * Find mapper with criteria
   * @param pCriterias : MapperCriteriaList
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return mapperList : List of mapper found
   * @throws WrapperServiceException : Exception
   */
  public MapperListType findMapper(MapperCriteriaListType pCriterias, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperListType result = null;
    getChrono().start();
    getJAXBManager().write(pCriterias, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.findMapper(pCriterias, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * get mapper.
   * @param pKey : MapperKey
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return Mapper : mapper
   * @throws WrapperServiceException : Exception
   */
  public MapperType getMapper(MapperKeyType pKey, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperType result = null;
    getChrono().start();
    getJAXBManager().write(pKey, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.getMapper(pKey, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * update mapper in mapperRepositoryMap
   * @param pMapper : MapperType
   * @param pScenario : Scenarion
   * @param pContext : Context
   * @return MapperType : mapper updated
   * @throws WrapperServiceException : Exception
   */
  public MapperType updateMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperType result = null;
    getChrono().start();
    getJAXBManager().write(pMapper, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.updateMapper(pMapper, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * update list of mapper in mapperRepositoryMap
   * @param pList : MapperList
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return : list of mapper updated
   * @throws WrapperServiceException : Exception
   */
  public MapperListType updateMapperList(MapperListType pList, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.updateMapperList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * update mapper in mapperRepositoryMap
   * @param pMapper : MapperType
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return MapperType.
   * @throws WrapperServiceException : Exception
   */
  public MapperType postMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperType result = null;
    getChrono().start();
    getJAXBManager().write(pMapper, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.postMapper(pMapper, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }

  /**
   * update list of mapper in mapperRepositoryMap
   * @param pList : MapperList
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return : list of Mapper updated
   * @throws WrapperServiceException : Exception
   */
  public MapperListType postMapperList(MapperListType pList, MapperScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    MapperListType result = null;
    getChrono().start();
    getJAXBManager().write(pList, pScenario, pContext);
    try
    {
      pContext = ContextTransformer.getDefault(pContext);
      result = _mapperRepository.postMapperList(pList, pScenario, pContext);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    finally
    {
      getChrono().stop(this);
    }
    return result;
  }
}
