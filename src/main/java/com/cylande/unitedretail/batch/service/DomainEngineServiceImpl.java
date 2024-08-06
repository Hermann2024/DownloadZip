package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.service.common.DomainEngineService;
import com.cylande.unitedretail.message.batch.BatchDomainListType;
import com.cylande.unitedretail.message.batch.BatchDomainType;
import com.cylande.unitedretail.message.batch.DomainEngineParam;
import com.cylande.unitedretail.message.batch.DomainEngineScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.process.repository.ProcessorPropertiesRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * classe java qui permet de récupérer l'ensemble des domaines des Batchs.
 */
public class DomainEngineServiceImpl implements DomainEngineService
{
  /**
   * Variables
   */
  private ProcessorPropertiesRepository _propertiesRepository = null;

  /**
   * Constructeur.
   */
  public DomainEngineServiceImpl()
  {
    _propertiesRepository = ProcessorPropertiesRepository.getInstance();
  }

  /**
   * Retourne la liste des domaines utilisé pour les fichiers batch.
   * @param pDomainengineParam : paramètre
   * @param pScenario : scenario
   * @param pContext : context
   * @return BatchDomainListType contenant la liste des domaines.
   */
  public BatchDomainListType getDomain(DomainEngineParam pDomainengineParam, DomainEngineScenarioType pScenario, ContextType pContext)
  {
    BatchDomainListType result = new BatchDomainListType();
    List<BatchDomainType> list = new ArrayList<BatchDomainType>();
    Set<String> hashSet = new HashSet<String>();
    BatchDomainType item;
    Set<String> keys = _propertiesRepository.getKeySet();
    // on extrait les domaines tout en enlevant les doublons
    for (String s: keys)
    {
      hashSet.add(s.substring(0, s.indexOf("//")));
    }
    // on créé les type BatchDomainType
    for (String s: hashSet)
    {
      item = new BatchDomainType();
      item.setBatchDomain(s);
      list.add(item);
    }
    result.setList(list);
    return result;
  }
}
