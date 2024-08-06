package com.cylande.unitedretail.batch.service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.AbstractFunctionnalServiceImpl;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchCriteriaListType;
import com.cylande.unitedretail.message.batch.BatchDomainListType;
import com.cylande.unitedretail.message.batch.BatchDomainType;
import com.cylande.unitedretail.message.batch.BatchExecutionDomainListType;
import com.cylande.unitedretail.message.batch.BatchExecutionModuleListType;
import com.cylande.unitedretail.message.batch.BatchExecutionModuleType;
import com.cylande.unitedretail.message.batch.BatchExecutionProcessListType;
import com.cylande.unitedretail.message.batch.BatchExecutionScenarioType;
import com.cylande.unitedretail.message.batch.BatchExecutionSelectionDataType;
import com.cylande.unitedretail.message.batch.BatchListType;
import com.cylande.unitedretail.message.batch.BatchScenarioType;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.DomainEngineParam;
import com.cylande.unitedretail.message.batch.DomainEngineScenarioType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.process.ProcessorCriteriaListType;
import com.cylande.unitedretail.message.process.ProcessorListType;
import com.cylande.unitedretail.message.process.ProcessorScenarioType;
import com.cylande.unitedretail.message.process.ProcessorType;
import com.cylande.unitedretail.process.service.ProcessorManagerServiceDelegate;

public class BatchExecutionEngineServiceImpl extends AbstractFunctionnalServiceImpl implements com.cylande.unitedretail.batch.service.common.BatchExecutionEngineService
{
  /**
   * Permet de récupérer la liste des batchs triées par modules, ainsi que les domaines disponibles
   */
  public BatchExecutionSelectionDataType getModuleListData(BatchExecutionScenarioType pBatchExecutionScenarioType, ContextType pContextType) throws WrapperServiceException, RemoteException
  {
    BatchManagerServiceDelegate batchManagerService = new BatchManagerServiceDelegate();
    try
    {
      HashMap<String, LinkedList<String>> listBatchByModule = new HashMap<String, LinkedList<String>>();
      BatchListType listBatch = batchManagerService.findBatch(new BatchCriteriaListType(), new BatchScenarioType(), pContextType);
      for (BatchType batch: listBatch.getValues())
      {
        String batchName = batch.getName();
        String batchModule = batch.getModule();
        if (batchModule == null)
        {
          batchModule = "Sans module";
        }
        if (listBatchByModule.containsKey(batchModule))
        {
          listBatchByModule.get(batchModule).add(batchName);
        }
        else
        {
          LinkedList<String> listBatchName = new LinkedList<String>();
          listBatchName.add(batchName);
          listBatchByModule.put(batchModule, listBatchName);
        }
      }

      BatchExecutionModuleListType batchExecutionModuleListType = new BatchExecutionModuleListType();
      for (Map.Entry<String, LinkedList<String>> entry: listBatchByModule.entrySet())
      {
        String key = entry.getKey();
        LinkedList<String> value = entry.getValue();
        BatchExecutionModuleType moduleType = new BatchExecutionModuleType();
        moduleType.setModuleName(key);
        moduleType.setBatchName(value);
        batchExecutionModuleListType.getBatchExecutionModuleList().add(moduleType);
      }

      DomainEngineServiceDelegate domainEngineServiceDelegate = new DomainEngineServiceDelegate();
      BatchDomainListType domainList = domainEngineServiceDelegate.getDomain(new DomainEngineParam(), new DomainEngineScenarioType(), pContextType);
      HashSet<String> domains = new HashSet<String>();
      for (BatchDomainType domain: domainList.getList())
      {
        domains.add(domain.getBatchDomain());
      }

      BatchExecutionDomainListType batchExecutionDomainListType = new BatchExecutionDomainListType();
      for (String domain: domains)
      {
        batchExecutionDomainListType.getDomainName().add(domain);
      }

      ProcessorManagerServiceDelegate processorManagerService = new ProcessorManagerServiceDelegate();
      ProcessorListType processorListType = processorManagerService.findProcessor(new ProcessorCriteriaListType(), new ProcessorScenarioType(), pContextType);
      BatchExecutionProcessListType batchExecutionProcessListType = new BatchExecutionProcessListType();
      for (ProcessorType processor: processorListType.getValues())
      {
        batchExecutionProcessListType.getProcessName().add(processor.getName());
      }
      BatchExecutionSelectionDataType batchExecutionSelectionDataType = new BatchExecutionSelectionDataType();
      batchExecutionSelectionDataType.setDomains(batchExecutionDomainListType);
      batchExecutionSelectionDataType.setModules(batchExecutionModuleListType);
      batchExecutionSelectionDataType.setProcess(batchExecutionProcessListType);
      return batchExecutionSelectionDataType;
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContextType));
    }
  }
}
