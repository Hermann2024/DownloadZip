package com.cylande.unitedretail.batch.provider.rw.impl;

import java.io.StringWriter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.cylande.unitedretail.message.batch.ProviderWrapperType;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.impl.XMLStringProvider;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.framework.service.JMSConnectionManager;
import com.cylande.unitedretail.framework.service.jms.SOAPObject;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.JMSPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderDestinationEnum;
import com.cylande.unitedretail.message.batch.ProviderFileType;

/**
 * Provider reader concrete implementation
 */
public class JMSProviderReader extends ProviderReader
{

  private static final Logger LOGGER = Logger.getLogger(JMSProviderReader.class);
  /** numéro du paquet lu */
  private int _packageNumber = 0;

  /** Indique s'il faut conserver le préfixe de l'élément root */
  private Boolean _keepRootElementPrefix = null;

  /** Indique s'il faut ignorer l'attribut xmlns de l'élément root */
  private Boolean _ignoreRootNamespace = null;

  private JMSConnectionManager _connManager;

  private javax.jms.MessageConsumer _consumer;

  private Message _currentMessage;
  private long _timeOut = 60000;
  private boolean _unitSplit;
  private ProviderReader _unitSplitReader;
  private String _contextValue;
  private ProviderWrapperType _wrapper;

  /**
   * Constructeur du provider de lecture
   * @param provider le flux à lire
   * @param pSize la taille maximal d'élément fils constituant un lot
   * @param pKeepRootElementPrefix Indique s'il faut conserver le préfixe de l'élément root
   * @throws ProviderException exception
   */
  public JMSProviderReader(Provider provider, final ProviderWrapperType pWrapper, int pSize, Boolean pKeepRootElementPrefix, Boolean pIgnoreRootNamespace, String pBatchName) throws ProviderException
  {
    super(provider);
    _wrapper = pWrapper;
    JMSPROVIDER providerDef = (JMSPROVIDER)provider.getProviderDef();
    _unitSplit = providerDef.getDestination().isUnitSplit() != null ? providerDef.getDestination().isUnitSplit() : false;
    if (providerDef.getDestination().getTimeOut() != null)
    {
      _timeOut = providerDef.getDestination().getTimeOut() * 60000L; // on convertit en millisecondes
    }
    _keepRootElementPrefix = pKeepRootElementPrefix;
    _ignoreRootNamespace = pIgnoreRootNamespace;
    try
    {
      Destination dest = null;
      _connManager = new JMSConnectionManager(providerDef.getDestination().getResourceConnectionFactory());
      ProviderDestinationEnum destType = providerDef.getDestination().getType();
      String clientId = null;
      if (ProviderDestinationEnum.QUEUE.equals(destType))
      {
        dest = _connManager.getTransactedSession().createQueue(getProvider().getFilteredStringByDomain(providerDef.getDestination().getName()));
      }
      else if (ProviderDestinationEnum.TOPIC.equals(destType))
      {
        if (pBatchName != null)
        {
          clientId = pBatchName + "_" + System.getProperty("hostname");
          _connManager.getConnection().setClientID(clientId);
        }
        dest = _connManager.getTransactedSession().createTopic(getProvider().getFilteredStringByDomain(providerDef.getDestination().getName()));
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
        _consumer = _connManager.getTransactedSession().createDurableSubscriber((Topic) dest, clientId, providerDef.getDestination().getSelector(), false);
      }
      else
      {
        _consumer = _connManager.getSession().createConsumer(dest, providerDef.getDestination().getSelector());
      }
      _connManager.startConnection();
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_CONSTRUCTION_ERROR, new Object[] { getProviderName() }, e);
    }
  }

  /** {@inheritDoc} */
  public DataPackage read(Boolean pNoProcessor, Integer pCurrentTaskId) throws ProviderException
  {
    DataPackage result = null;
    try
    {
      if (_unitSplitReader != null)
      {
        // gestion unitSplit pour les messages contenant des list
        _packageNumber++;
        result = _unitSplitReader.read(null, null);
        if (result != null)
        {
          result.setPackageNumber(_packageNumber);
          if (result.isLastPackage())
          {
            _unitSplitReader = null; // reader à null si c'est le dernier paquet de la liste afin de passer au message suivant
          }
        }
        else
        {
          _unitSplitReader = null; // reader à null si c'est le dernier paquet de la liste afin de passer au message suivant
        }
        return result;
      }
      _currentMessage = _consumer.receive(_timeOut);
      if (_currentMessage != null)
      {
        String sValue = ((TextMessage)_currentMessage).getText();
        if (sValue.contains("http://schemas.xmlsoap.org/soap/envelope/"))
        {
          SOAPObject soap = new SOAPObject();
          soap.unmarshallSOAP(sValue);
          if (soap.getBusinessObject() != null)
          {
            JAXBContext context = JAXBManager.getJAXBContext(soap.getBusinessObject().getClass().getPackage().getName());
            Marshaller marshaller = context.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.marshal(soap.getBusinessObject(), sw);
            sValue = sw.toString();
            if (soap.getContext() != null)
            {
              context = JAXBManager.getJAXBContext(soap.getContext().getClass().getPackage().getName());
              sw = new StringWriter();
              marshaller = context.createMarshaller();
              marshaller.marshal(soap.getContext(), sw);
              _contextValue = sw.toString();
            }
          }
          else
          {
            sValue = null;
          }
        }
        if (sValue != null)
        {
          _packageNumber++;
          if (!_unitSplit)
          {
            result = new DataPackage();
            result.setValue(sValue);
            result.setPackageNumber(_packageNumber);
            // pas de gestion du dernier package car cela oblige à lire le message suivant qui peut dès lors être perdu en cas d'interruption
            // inattendue entre le commit JMS et la prochaine lecture
            result.setLastPackage(false);
          }
          else
          {
            // init unitSplit
            Provider xmlStringProvider = new XMLStringProvider(null, getProvider().getPropertiesManager(), getProvider().getCurrentDomain(), getProvider().getAlternativeDomain(), sValue);
            _unitSplitReader = new ProviderReaderImpl(xmlStringProvider, _wrapper, 1, null, null);
            result = _unitSplitReader.read(null, null);
            if (result !=  null)
            {
              result.setPackageNumber(_packageNumber);
              if (result.isLastPackage())
              {
                _unitSplitReader = null; // reader à null si c'est le dernier paquet de la liste afin de passer au message suivant
              }
            }
            else
            {
              _unitSplitReader = null; // reader à null si c'est le dernier paquet de la liste afin de passer au message suivant
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_READ_ERROR, new Object[] { getProviderName() }, e);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void releaseProvider() throws ProviderException
  {
    try
    {
      closeConnection();
    }
    catch (JMSException e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_READ_ERROR, new Object[] { getProviderName() }, e);
    }
    finally
    {
      if (getProvider() != null)
      {
        getProvider().closeInputStream();
      }
    }
  }

  private void closeConnection() throws JMSException
  {
    try
    {
      if (_consumer != null)
      {
        _consumer.close();
      }
    }
    catch (JMSException e)
    {
      LOGGER.error(e, e);
    }
    finally
    {
      if (_connManager != null)
      {
        _connManager.closeConnection();
      }
    }
  }

  private String getProviderName()
  {
    if (getProvider() != null && getProvider().getProviderDef() != null)
    {
      return getProvider().getProviderDef().getName();
    }
    return null;
  }

  /** {@inheritDoc} */
  public String getCurrentFileName()
  {
    return null;
  }

  /** {@inheritDoc} */
  public String getScenarioValue()
  {
    return null;
  }

  /** {@inheritDoc} */
  public ProviderFileType getProviderFileType() throws Exception
  {
    return null;
  }

  /** {@inheritDoc} */
  public void setReject(FileType pFileType)
  {
    getProvider().setReject(pFileType);
  }

  /** {@inheritDoc} */
  public void disableArchive()
  {
    getProvider().disableArchive();
  }

  /**
   * Retourne la valeur de l'attribut keepRootElementPrefix du provider
   * d'entrée
   * @return boolean
   * @throws ProviderException Erreur
   */
  /*private boolean getKeepRootElementPrefix() throws ProviderException
  {
    boolean result = false;
    try
    {
      ProviderFileType providerFileType = getProvider().getCurrentProviderFileType();
      if (providerFileType != null)
      {
        result = Boolean.TRUE.equals(providerFileType.getKeepRootElementPrefix());
      }
      else
      {
        result = Boolean.TRUE.equals(_keepRootElementPrefix);
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_CONSTRUCTION_ERROR, new Object[] { getProviderName() }, exc);
    }
    return result;
  }*/

  /**
   * Retourne la valeur de l'attribut ignoreRootNamespace du provider
   * d'entrée
   * @return boolean
   * @throws ProviderException Erreur
   */
  /*private boolean getIgnoreRootNamespace() throws ProviderException
  {
    boolean result = false;
    try
    {
      ProviderFileType providerFileType = getProvider().getCurrentProviderFileType();
      if (providerFileType != null)
      {
        result = Boolean.TRUE.equals(providerFileType.getIgnoreRootNamespace());
      }
      else
      {
        result = Boolean.TRUE.equals(_ignoreRootNamespace);
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERREADER_CONSTRUCTION_ERROR, new Object[] { getProviderName() }, exc);
    }
    return result;
  }*/

  public JMSConnectionManager getConnection()
  {
    return _connManager;
  }

  @Override
  public String getContextValue()
  {
    return _contextValue;
  }
}
