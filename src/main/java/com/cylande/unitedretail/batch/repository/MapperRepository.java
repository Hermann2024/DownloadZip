package com.cylande.unitedretail.batch.repository;

import com.cylande.unitedretail.message.batch.MapperCriteriaListType;
import com.cylande.unitedretail.message.batch.MapperCriteriaType;
import com.cylande.unitedretail.message.batch.MapperKeyType;
import com.cylande.unitedretail.message.batch.MapperListType;
import com.cylande.unitedretail.message.batch.MapperScenarioType;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.repository.CriteriaTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class MapperRepository
 * Instance of MapperRepository
 */
public final class MapperRepository
{
  /**
   * Instance of MapperRepository
   */
  private static final MapperRepository INSTANCE;
  static {
    INSTANCE = new MapperRepository();
  }

  /**
   * mapperRepositoryMap
   */
  private ConcurrentHashMap<String, MapperType> _mapperRepositoryMap = null;

  /**
   * Constructor
   */
  private MapperRepository()
  {
    _mapperRepositoryMap = new ConcurrentHashMap<String, MapperType>();
  }

  /**
   * Get Unique Instance of MapperRepository
   * @return instance of MapperRepository
   */
  public static MapperRepository getInstance()
  {
    return INSTANCE;
  }

  /**
   * put pMapper in the mapperRepositoryMap.
   * @param pMapper : Mapper.
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return MapperType : pMapper
   */
  public MapperType createMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext)
  {
    if (pMapper != null)
    {
      _mapperRepositoryMap.put(pMapper.getName(), pMapper);
    }
    return pMapper;
  }

  /**
   * put pList in the mapperRepositoryMap.
   * @param pList : list of mapper.
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return MapperListType : new MapperList
   */
  public MapperListType createMapperList(MapperListType pList, MapperScenarioType pScenario, ContextType pContext)
  {
    MapperListType result = null;
    if (pList != null)
    {
      result = new MapperListType();
      for (MapperType mapper: pList.getValues())
      {
        result.getValues().add(createMapper(mapper, pScenario, pContext));
      }
    }
    return result;
  }

  /**
   * get mapper.
   * @param pKey : mapperKey
   * @param pScenario : Scenario
   * @param pContext Context
   * @return mapper
   */
  public MapperType getMapper(MapperKeyType pKey, MapperScenarioType pScenario, ContextType pContext)
  {
    MapperType result = null;
    if (pKey != null)
    {
      result = _mapperRepositoryMap.get(pKey.getName());
    }
    return result;
  }

  /**
   * Find mapper with criteria
   * @param pCriterias : MapperCriteria
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return MapperListType : List of mapper found with criteria
   */
  public MapperListType findMapper(MapperCriteriaListType pCriterias, MapperScenarioType pScenario, ContextType pContext)
  {
    MapperListType result = null;
    if (pCriterias != null)
    {
      if (!pCriterias.getList().isEmpty())
      {
        result = findCriterias(pCriterias, pScenario, pContext);
      }
      else
      {
        result = new MapperListType();
        Collection<MapperType> values = _mapperRepositoryMap.values();
        result.getValues().addAll(values);
      }
    }
    return result;
  }

  /**
   * Remove mapper of mapperRepositoryMap.
   * @param pKey : mapperKey
   * @param pScenario Scenario
   * @param pContext Context
   */
  public void deleteMapper(MapperKeyType pKey, MapperScenarioType pScenario, ContextType pContext)
  {
    if (pKey != null)
    {
      if (_mapperRepositoryMap.containsKey(pKey.getName()))
      {
        _mapperRepositoryMap.remove(pKey.getName());
      }
    }
  }

  /**
   * Remove List mapper of mapperRepositoryMap.
   * @param pCriterias : MapperCriteria
   * @param pScenario : Scenario
   * @param pContext : Context
   */
  public void deleteMapperList(MapperCriteriaListType pCriterias, MapperScenarioType pScenario, ContextType pContext)
  {
    if (pCriterias != null)
    {
      MapperListType pList = findMapper(pCriterias, pScenario, pContext);
      for (MapperType mapper: pList.getValues())
      {
        _mapperRepositoryMap.remove(mapper.getName());
      }
    }
  }

  /**
   * update mapper in mapperRepositoryMap
   * @param pMapper : MapperType
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return : Mapper updated
   */
  public MapperType updateMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext)
  {
    return updateMapper(pMapper, pScenario, pContext, false);
  }

  /**
   * update mapper in mapperRepositoryMap
   * @param pMapper : MapperType
   * @param pScenario : Scenario
   * @param pContext : Context
   * @param pCreate : create?
   * @return MapperType : mapper updated
   */
  private MapperType updateMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext, boolean pCreate)
  {
    MapperType result = null;
    if (pMapper != null)
    {
      MapperKeyType pKey = new MapperKeyType();
      pKey.setName(pMapper.getName());
      MapperType beanMapper = getMapper(pKey, pScenario, pContext);
      if (beanMapper != null)
      {
        result = _mapperRepositoryMap.put(pMapper.getName(), pMapper);
      }
      else if (pCreate)
      {
        result = createMapper(pMapper, pScenario, pContext);
      }
    }
    return result;
  }

  /**
   * update list of mapper in mapperRepositoryMap
   * @param pList : MapperList
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return : list of Mapper updated
   */
  public MapperListType updateMapperList(MapperListType pList, MapperScenarioType pScenario, ContextType pContext)
  {
    MapperListType result = null;
    if (pList != null)
    {
      result = new MapperListType();
      for (MapperType mapper: pList.getValues())
      {
        result.getValues().add(updateMapper(mapper, pScenario, pContext));
      }
    }
    return result;
  }

  /**
   * update mapper in mapperRepositoryMap
   * @param pMapper : MapperType
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return MapperType : mapper updated
   */
  public MapperType postMapper(MapperType pMapper, MapperScenarioType pScenario, ContextType pContext)
  {
    return updateMapper(pMapper, pScenario, pContext, true);
  }

  /**
   * update list of mapper in mapperRepositoryMap
   * @param pList : MapperList
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return : list of Mapper updated
   */
  public MapperListType postMapperList(MapperListType pList, MapperScenarioType pScenario, ContextType pContext)
  {
    MapperListType result = null;
    if (pList != null)
    {
      result = new MapperListType();
      for (MapperType mapper: pList.getValues())
      {
        result.getValues().add(postMapper(mapper, pScenario, pContext));
      }
    }
    return result;
  }

  /**
   * Find a mapper with criterias
   * @param pList : list of mapperCriteria
   * @param pScenario : Scenario
   * @param pContext : Context
   * @return MapperListType : list of mapper found
   */
  public MapperListType findCriterias(MapperCriteriaListType pList, MapperScenarioType pScenario, ContextType pContext)
  {
    MapperListType result = new MapperListType();
    if (pList == null)
    {
      return result;
    }
    List<MapperType> mapperList = new ArrayList<MapperType>();
    for (MapperCriteriaType criteria: pList.getList())
    {
      mapperList.addAll(matchMapperCriteria(criteria));
    }
    result.setValues(mapperList);
    return result;
  }

  /**
   * find mapper matching with criteria
   * @param pCriteria : MapperCriteria
   * @return Set : Set of mapper matched with criteria
   */
  private Set<MapperType> matchMapperCriteria(MapperCriteriaType pCriteria)
  {
    Set<MapperType> resultMapperSet = new HashSet<MapperType>();
    Collection<MapperType> values = _mapperRepositoryMap.values();
    for (MapperType mapper: values)
    {
      if (pCriteria.getName() != null && CriteriaTools.matchCriteriaString(pCriteria.getName(), mapper.getName()))
      {
        resultMapperSet.add(mapper);
      }
    }
    return resultMapperSet;
  }
}
