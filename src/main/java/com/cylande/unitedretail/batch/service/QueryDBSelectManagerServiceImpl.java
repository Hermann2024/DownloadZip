package com.cylande.unitedretail.batch.service;

import com.cylande.unitedretail.batch.business.module.QueryDBSelectManagerModuleImpl;
import com.cylande.unitedretail.batch.service.common.QueryDBSelectManagerService;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractCRUDServiceImpl;
import com.cylande.unitedretail.framework.service.PagingMode;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceNotDeliveredException;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryResultType;
import com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryScenarioType;
import com.cylande.unitedretail.message.common.queryselect.AnalyzeCostQueryType;
import com.cylande.unitedretail.message.common.queryselect.QueryResultType;
import com.cylande.unitedretail.message.common.queryselect.QuerySelectScenarioType;
import com.cylande.unitedretail.message.common.queryselect.QuerySelectType;

import java.rmi.RemoteException;

/**
 * Service d'extraction de données directement depuis la BDD à partir d'une requête SQL
 */
public class QueryDBSelectManagerServiceImpl extends AbstractCRUDServiceImpl implements QueryDBSelectManagerService
{
  /**
   * Constructeur
   */
  public QueryDBSelectManagerServiceImpl()
  {
    super();
    setModuleConfiguration("com.cylande.unitedretail.batch.business.module", "QueryDBSelectManagerModule");
  }

  /**
   * Execution de la requête SQL pour récupération des données
   * @param pQuerySelect la requête à exécuter sur la base
   * @param pScenario    le scenario
   * @param pContext     le context
   * @return QueryResultType le resultat
   * @throws RemoteException                       Erreur Technique
   * @throws TechnicalServiceNotDeliveredException Erreur de service non livré
   * @throws ServiceException                      Erreur de service
   */
  @PagingMode
  public QueryResultType querySelect(QuerySelectType pQuerySelect, QuerySelectScenarioType pScenario, ContextType pContext) throws RemoteException, TechnicalServiceNotDeliveredException, ServiceException
  {
    QueryResultType result = null;
    getChrono().start();
    getJAXBManager().write(pQuerySelect, pScenario, pContext);
    QueryDBSelectManagerModuleImpl module = (QueryDBSelectManagerModuleImpl) getModule(pContext);
    ContextType context = pContext;
    try
    {
      getTransaction().init(context);
      context = ContextTransformer.getDefault(pContext);
      result = module.querySelect(pQuerySelect, pScenario, context);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(context));
    }
    finally
    {
      getChrono().stop(this);
      release(context);
    }
    return result;
  }

  /**
   * Analyse du coup d'execution d'une requête sur la BDD
   * @param pAnalyseCostQuery la requête à analyser sur la base
   * @param pScenario         le scenario
   * @param pContext          le context
   * @return AnalyzeCostQueryResultType le resultat
   * @throws RemoteException                       Erreur Technique
   * @throws TechnicalServiceNotDeliveredException Erreur de service non livré
   * @throws ServiceException                      Erreur de service
   */
  public AnalyzeCostQueryResultType analyzeCostQuery(AnalyzeCostQueryType pAnalyseCostQuery, AnalyzeCostQueryScenarioType pScenario, ContextType pContext) throws RemoteException, TechnicalServiceNotDeliveredException, ServiceException
  {
    AnalyzeCostQueryResultType result = null;
    getChrono().start();
    getJAXBManager().write(pAnalyseCostQuery, pScenario, pContext);
    QueryDBSelectManagerModuleImpl module = (QueryDBSelectManagerModuleImpl) getModule(pContext);
    ContextType context = pContext;
    try
    {
      getTransaction().init(context);
      context = ContextTransformer.getDefault(pContext);
      result = module.analyzeCostQuery(pAnalyseCostQuery, pScenario, context);
      getJAXBManager().write(result);
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(context));
    }
    finally
    {
      getChrono().stop(this);
      release(context);
    }
    return result;
  }
}
