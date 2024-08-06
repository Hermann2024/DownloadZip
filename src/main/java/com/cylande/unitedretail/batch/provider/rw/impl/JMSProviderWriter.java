package com.cylande.unitedretail.batch.provider.rw.impl;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.framework.service.JMSConnectionManager;
import com.cylande.unitedretail.message.batch.JMSPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderDestinationEnum;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import org.json.XML;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class JMSProviderWriter extends ProviderWriter
{
  /**
   * encodage par défaut du fichier
   */
  private static final String DEFAULT_ENCODAGE = "UTF-8";
  private JMSPROVIDER _providerDef;
  private JMSConnectionManager _connManager;
  private Destination _dest = null;
  private String _destName = null;
  private Map<String, Destination> _knownDestinations;
  private StaxXMLParser _currentParser;
  private ProviderFileType _providerFile = null;
  private boolean _jsonFormat = false;

  /**
   * constructeur
   *
   * @param provider provider
   */
  public JMSProviderWriter(Provider provider) throws ProviderException
  {
    super(provider);
    _providerDef = (JMSPROVIDER) provider.getProviderDef();
    try
    {
      _connManager = new JMSConnectionManager(_providerDef.getDestination().getResourceConnectionFactory());
      this.setupDestination();
      boolean keepRootElementPrefix = false;
      if (_providerDef.getDestination().getHeader() != null)
      {
        _providerFile = new ProviderFileType();
        _providerFile.setHeader(_providerDef.getDestination().getHeader());
        keepRootElementPrefix = true;
      }
      _jsonFormat = "json".equals(_providerDef.getDestination().getMessageFormat()) ? true : false;
      _currentParser = new StaxXMLParser(false, keepRootElementPrefix);
      _connManager.startConnection();
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_WRITE_ERROR, new Object[]{_providerDef.getName()}, e);
    }
  }

  private void setupDestination() throws JMSException
  {
    if (_knownDestinations == null)
    {
      _knownDestinations = new HashMap<String, Destination>();
    }
    final String newDestName = this.computeDestinationName();
    if (_dest != null && (newDestName == null || newDestName.equals(_destName)))
    {
      return;
    }
    Destination newDestination = null;
    if (newDestName != null)
    {
      newDestination = _knownDestinations.get(newDestName);
    }
    if (newDestination == null)
    {
      ProviderDestinationEnum destType = _providerDef.getDestination().getType();
      if (ProviderDestinationEnum.QUEUE.equals(destType))
      {
        newDestination = _connManager.getSession().createQueue(newDestName);
      }
      else if (ProviderDestinationEnum.TOPIC.equals(destType))
      {
        newDestination = _connManager.getSession().createTopic(newDestName);
      }
      else if (ProviderDestinationEnum.TMPQUEUE.equals(destType))
      {
        newDestination = _connManager.getSession().createTemporaryQueue();
      }
      else if (ProviderDestinationEnum.TMPTOPIC.equals(destType))
      {
        newDestination = _connManager.getSession().createTemporaryTopic();
      }
      if (newDestName != null && newDestination != null)
      {
        _knownDestinations.put(newDestName, newDestination);
      }
    }
    _destName = newDestName;
    _dest = newDestination;
  }

  private String computeDestinationName()
  {
    ProviderDestinationEnum destType = _providerDef.getDestination().getType();
    if (ProviderDestinationEnum.QUEUE.equals(destType))
    {
      return getProvider().getFilteredStringByDomain(_providerDef.getDestination().getName());
    }
    else if (ProviderDestinationEnum.TOPIC.equals(destType))
    {
      return getProvider().getFilteredStringByDomain(_providerDef.getDestination().getName());
    }
    else if (ProviderDestinationEnum.TMPQUEUE.equals(destType))
    {
      return null;
    }
    else if (ProviderDestinationEnum.TMPTOPIC.equals(destType))
    {
      return null;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void write(String pValue) throws ProviderException
  {
    if (pValue == null)
    {
      //ne rien faire
      return;
    }
    try
    {
      //transformation de la chaîne de caractères en inputStream et utilisation du write
      this.write(new ByteArrayInputStream(pValue.getBytes(DEFAULT_ENCODAGE)));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new ProviderException(BatchErrorDetail.UNSUPPORTED_ENCODING, e);
    }
  }

  private void write(InputStream pInputStream) throws ProviderException
  {
    try
    {
      write(new BufferedReader(new InputStreamReader(pInputStream, DEFAULT_ENCODAGE)));
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.MAPPER_UNSUPORTED_ENCODING, e);
    }
  }

  private void write(BufferedReader pBufferedReader) throws ProviderException
  {
    try
    {
      String stringPack = _currentParser.readXMLString(pBufferedReader, null);
      if (stringPack != null && _currentParser.isSubElement())
      {
        stringPack = _jsonFormat ? XML.toJSONObject(stringPack, true).toString() : stringPack;
        TextMessage message = _connManager.getSession().createTextMessage();
        message.setText(stringPack);
        this.setupDestination();
        MessageProducer producer = _connManager.getSession().createProducer(_dest);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        producer.send(message);
        producer.close();
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_WRITE_ERROR, new Object[]{_providerDef.getName()}, e);
    }
  }

  public void releaseProvider() throws ProviderException
  {
    try
    {
      if (_connManager != null)
      {
        _connManager.closeConnection();
      }
    }
    catch (JMSException e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDERWRITER_WRITE_ERROR, new Object[]{_providerDef.getName()}, e);
    }
    finally
    {
      if (getProvider() != null)
      {
        getProvider().closeInputStream();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public ProviderFileType getProviderFileType()
  {
    return _providerFile;
  }
}
