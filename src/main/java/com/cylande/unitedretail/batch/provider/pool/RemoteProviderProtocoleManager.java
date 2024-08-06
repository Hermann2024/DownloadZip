package com.cylande.unitedretail.batch.provider.pool;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.RemoteProviderProtocolException;
import com.cylande.unitedretail.batch.provider.DataPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

class RemoteProviderProtocoleManager
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(RemoteProviderProtocoleManager.class);
  /** RMI output stream */
  private ObjectInputStream _ois;
  /** RMI input stream */
  private ObjectOutputStream _oos;

  /**
   * Constructor
   * @param pInput
   * @param pOutput
   * @throws RemoteProviderProtocolException exception
   */
  protected RemoteProviderProtocoleManager(InputStream pInput, OutputStream pOutput) throws RemoteProviderProtocolException
  {
    try
    {
      _ois = new ObjectInputStream(pInput);
      _oos = new ObjectOutputStream(pOutput);
      _oos.writeObject(RemoteProviderProtocole.START_OF_STREAM);
    }
    catch (Exception e)
    {
      LOGGER.debug("erreur servlet", e);
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_READ_ERROR);
    }
  }

  public void closeResponse() throws RemoteProviderProtocolException
  {
    RemoteProviderProtocolException flushError = null;
    try
    {
      if (_oos != null)
      {
        _oos.writeObject(RemoteProviderProtocole.END_OF_STREAM);
        _oos.flush();
      }
    }
    catch (IOException e)
    {
      flushError = new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_WRITE_ERROR);
    }
    try
    {
      if (_ois != null)
      {
        _ois.close();
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Erreur provider pool servlet lors de la fermeture de l'inputStream", e);
    }
    try
    {
      if (_oos != null)
      {
        _oos.close();
      }
    }
    catch (IOException e)
    {
      LOGGER.error("Erreur provider pool servlet lors de la fermeture de l'outputStream", e);
    }
    if (flushError != null)
    {
      throw flushError;
    }
  }

  public void notifyException(Exception pException)
  {
    if (_oos != null)
    {
      try
      {
        _oos.writeObject(pException);
        closeResponse();
      }
      catch (Exception e)
      {
        LOGGER.warn("remote provider : unable to transmit exception to client \n ", e);
      }
    }
  }

  public void onFactoryInitialized()
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.error(e);
    }
  }

  public void onReaderInitialized(String pReaderName)
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.error(e);
    }
  }

  public void onWriterInitialized(String pWriterName)
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.error(e);
    }
  }

  public boolean onDataRead(String pReaderName, DataPackage pData)
  {
    boolean dataSend = true;
    try
    {
      _oos.writeObject(pData);
      LOGGER.debug("Remote provider reader (" + pReaderName + "), send data success" + pData);
    }
    catch (IOException e)
    {
      dataSend = false;
      RemoteProviderProtocolException e2 = new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_WRITE_ERROR, e);
      try
      {
        _oos.writeObject(e2);
      }
      catch (IOException f)
      {
        LOGGER.error(e2);
      }
    }
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.warn("Remote provider reader (" + pReaderName + "), error while send data" + pData, e);
    }
    return dataSend;
  }

  public void onDataWrote(String pWriterName)
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.warn(e);
    }
  }

  public void onReaderReleased(String pReaderName, boolean pEnd)
  {
    if (pEnd)
    {
      try
      {
        closeResponse();
      }
      catch (RemoteProviderProtocolException e)
      {
        LOGGER.warn(e);
      }
    }
  }

  public void onWriterReleased(String pWriterName, boolean pEnd)
  {
    if (pEnd)
    {
      try
      {
        closeResponse();
      }
      catch (RemoteProviderProtocolException e)
      {
        LOGGER.warn(e);
      }
    }
  }

  public void onFactoryReleased()
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.warn(e);
    }
  }

  public void onSessionInit(ProviderContainer pContainer)
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.warn(e);
    }
  }

  public void onSessionReleased()
  {
    try
    {
      closeResponse();
    }
    catch (RemoteProviderProtocolException e)
    {
      LOGGER.warn(e);
    }
  }

  public Object readParam() throws RemoteProviderProtocolException
  {
    Object result = null;
    try
    {
      result = _ois.readObject();
    }
    catch (Exception e)
    {
      LOGGER.warn(e);
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_READ_ERROR);
    }
    return result;
  }
}
