package com.cylande.unitedretail.batch.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.TriggerType;

public class MessageConsumerManager
{
  private static final Logger LOGGER = Logger.getLogger(MessageConsumerManager.class);
  private static final MessageConsumerManager INSTANCE;
  private Map<String, MessageConsumer> _messageConsumerMap = new HashMap();
  private Map<String, Integer> _destinationCountMap = new HashMap();
  private List<String> _batchJMSReader = new ArrayList();

  static
  {
    INSTANCE = new MessageConsumerManager();
  }

  public static MessageConsumerManager getInstance()
  {
    return INSTANCE;
  }

  public Map<String, MessageConsumer> getMessageConsumerMap()
  {
    return _messageConsumerMap;
  }

  public void stopAll()
  {
    for (Entry<String, MessageConsumer> entry: _messageConsumerMap.entrySet())
    {
      entry.getValue().closeConnection();
    }
    _messageConsumerMap = new HashMap();
    _destinationCountMap = new HashMap();
  }

  public void addConsumer(TriggerType pTrigger, BatchType pBatch) throws WrapperServiceException
  {
    if (pTrigger.getDestination() != null)
    {
      if (_messageConsumerMap.get(pTrigger.getName()) == null)
      {
        MessageConsumer consumer = new MessageConsumer(pTrigger, pBatch);
        _messageConsumerMap.put(pTrigger.getName(), consumer);
        if (_destinationCountMap.get(pTrigger.getDestination().getName()) == null)
        {
          _destinationCountMap.put(pTrigger.getDestination().getName(), 1);
        }
        else
        {
          _destinationCountMap.put(pTrigger.getDestination().getName(), _destinationCountMap.get(pTrigger.getDestination().getName()) + 1);
        }
        _batchJMSReader.add(pBatch.getName());
      }
    }
  }

  public void startConnection() throws WrapperServiceException
  {
    try
    {
      MessageConsumer consumer;
      for (Entry<String, MessageConsumer> entry: _messageConsumerMap.entrySet())
      {
        consumer = entry.getValue();
        consumer.setMaxDeliveries(_destinationCountMap.get(consumer.getTrigger().getDestination().getName()));
        if (consumer.isInit())
        {
          consumer.startConnection();
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.error(e, e);
      throw new WrapperServiceException(e.getMessage());
    }
  }

  public List<String> getBatchJMSReader()
  {
    return _batchJMSReader;
  }
}
