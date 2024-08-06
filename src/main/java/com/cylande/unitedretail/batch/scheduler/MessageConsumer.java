package com.cylande.unitedretail.batch.scheduler;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.service.BatchEngineServiceImpl;
import com.cylande.unitedretail.framework.service.JMSConnectionManager;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.ProviderDestinationEnum;
import com.cylande.unitedretail.message.batch.TriggerType;
import com.cylande.unitedretail.message.batchenginemessages.BatchEngineParamsType;

public class MessageConsumer implements MessageListener
{
  private static final Logger LOGGER = Logger.getLogger(MessageConsumer.class);
  private TriggerType _trigger;
  private BatchType _batch;
  private JMSConnectionManager _connManager;
  private javax.jms.MessageConsumer _consumer;
  private Message _firstMessage = null;
  private int _maxRedeliveries; // correspond au nombre de trigger ayant la même destination
  private boolean _init;

  public MessageConsumer(TriggerType pTrigger, BatchType pBatch)
  {
    _trigger = pTrigger;
    _batch = pBatch;
    _init = true;
  }

  public void startConnection() throws Exception
  {
    _connManager = new JMSConnectionManager(_trigger.getDestination().getResourceConnectionFactory());
    if (_connManager.getConnection() instanceof ActiveMQConnection)
    {
      // gestion du cas où il y a plusieurs batchs sur la même file (relecture multithreadée) : le maximumRedeliveries de la connection doit
      // correspondre au nombre de consommateur sur la file s'il lui est inférieur afin d'éviter le placement direct du message en DLQ car
      // à chaque fois que le message est notifié à un consommateur via le onChange, son maximumRedeliveries est incrémenté de 1
      RedeliveryPolicy policy = ((ActiveMQConnection)_connManager.getConnection()).getRedeliveryPolicy();
      if (policy.getMaximumRedeliveries() < _maxRedeliveries)
      {
        policy.setMaximumRedeliveries(_maxRedeliveries);
      }
    }
    ProviderDestinationEnum destType = _trigger.getDestination().getType();
    Destination dest = null;
    String clientId = null;
    if (ProviderDestinationEnum.QUEUE.equals(destType))
    {
      dest = _connManager.getTransactedSession().createQueue(_trigger.getDestination().getName());
    }
    else if (ProviderDestinationEnum.TOPIC.equals(destType))
    {
      if (_batch != null && _batch.getName() != null)
      {
        clientId = _batch.getName() + "_" + System.getProperty("hostname");
        _connManager.getConnection().setClientID(clientId);
      }
      dest = _connManager.getTransactedSession().createTopic(_trigger.getDestination().getName());
    }
    else if (ProviderDestinationEnum.TMPQUEUE.equals(destType))
    {
      dest = _connManager.getTransactedSession().createTemporaryQueue();
    }
    else if (ProviderDestinationEnum.TMPTOPIC.equals(destType))
    {
      dest = _connManager.getTransactedSession().createTemporaryTopic();
    }
    if (ProviderDestinationEnum.TOPIC.equals(destType) && clientId != null)
    {
      _consumer = _connManager.getTransactedSession().createDurableSubscriber((Topic) dest, clientId, _trigger.getDestination().getSelector(), false);
    }
    else
    {
      _consumer = _connManager.getSession().createConsumer(dest, _trigger.getDestination().getSelector());
    }
    _consumer.setMessageListener(this);
    _connManager.startConnection();
    _init = false;
  }

  public void closeConnection()
  {
    try
    {
      _consumer.close();
    }
    catch (JMSException e)
    {
      LOGGER.error("Close consumer on trigger " + _trigger.getName(), e);
    }
    finally
    {
      try
      {
        _connManager.closeConnection();
      }
      catch (JMSException e)
      {
        LOGGER.error("Close connection on trigger " + _trigger.getName(), e);
      }
    }
  }

  public void onMessage(Message pMsg)
  {
    if (_firstMessage == null)
    {
      _firstMessage = pMsg;
      try
      {
        // fermeture de la connexion sans commit : le 1er message sera relu lors de l'exécution du batch
        // ce système permet de ne pas perdre le 1er message en cas d'interruption inattendue entre la lecture du 1er message sur ce listener et son traitement dans le batch
        closeConnection();
        BatchEngineServiceImpl serv = new BatchEngineServiceImpl();
        BatchEngineParamsType params = new BatchEngineParamsType();
        params.setBatchName(_batch.getName());
        serv.execute(params, null, null);
      }
      catch (Exception e)
      {
        LOGGER.error(e, e);
      }
    }
  }

  public void resetConnection()
  {
    try
    {
      _firstMessage = null;
      startConnection();
    }
    catch (Exception e)
    {
      LOGGER.error("Reset connection on trigger " + _trigger.getName(), e);
    }
  }

  public TriggerType getTrigger()
  {
    return _trigger;
  }

  public void setMaxDeliveries(int pMaxDeliveries)
  {
    _maxRedeliveries = pMaxDeliveries;
  }

  public boolean isInit()
  {
    return _init;
  }
}
