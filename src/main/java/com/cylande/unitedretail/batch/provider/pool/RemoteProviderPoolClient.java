package com.cylande.unitedretail.batch.provider.pool;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.RemoteProviderProtocolException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.rw.impl.RemoteProviderReader;
import com.cylande.unitedretail.batch.provider.rw.impl.RemoteProviderWriter;
import com.cylande.unitedretail.batch.task.AbstractTask;
import com.cylande.unitedretail.message.batch.AbstractStream;
import com.cylande.unitedretail.message.batch.TaskRunType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class RemoteProviderPoolClient
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(RemoteProviderPoolClient.class);
  private String _urlStr = null;
  private AbstractTask _parentTask;
  private Integer _preLoadSize = null;
  private ObjectOutputStream _oos = null;
  private ObjectInputStream _ois = null;
  private HttpURLConnection _servletConnection = null;
  private String _sessionCookieValue = null;
  private int _maxRetry = 2;

  /**
   * Constructeur
   * @param pUrlStr : l'url de la servlet de pool
   */
  public RemoteProviderPoolClient(String pUrlStr)
  {
    _urlStr = pUrlStr;
  }

  private void extractSessionCookie()
  {
    _sessionCookieValue = null;
    Map headerMap = _servletConnection.getHeaderFields();
    List<String> values = (List<String>)headerMap.get("Set-Cookie");
    if (LOGGER.isDebugEnabled())
    {
      Iterator itKey = headerMap.keySet().iterator();
      Iterator itValue = headerMap.values().iterator();
      while (itKey.hasNext())
      {
        LOGGER.debug("header key=\'" + itKey.next() + "\' val=\'" + itValue.next() + "\'");
      }
    }
    if (values != null)
    {
      Iterator<String> iter = values.iterator();
      while (iter.hasNext())
      {
        if (_sessionCookieValue == null)
        {
          _sessionCookieValue = iter.next();
        }
        else
        {
          _sessionCookieValue = _sessionCookieValue + ";" + iter.next();
        }
      }
    }
  }

  /**
   * openConection
   * @param pInputParams
   * @throws RemoteProviderProtocolException exception
   */
  private void openConection(Object[] pInputParams) throws RemoteProviderProtocolException
  {
    try
    {
      HttpURLConnection.setFollowRedirects(true);
      _servletConnection = (HttpURLConnection)new URL(_urlStr).openConnection();
      //_servletConnection.setReadTimeout(60000);
      //_servletConnection.setConnectTimeout(60000);
      _servletConnection.setInstanceFollowRedirects(true);
      if (_sessionCookieValue != null)
      {
        _servletConnection.setRequestProperty("Cookie", _sessionCookieValue);
      }
      _servletConnection.setRequestMethod("POST");
      URLConnection.setDefaultAllowUserInteraction(false);
      _servletConnection.setDoInput(true);
      _servletConnection.setDoOutput(true);
      _servletConnection.setUseCaches(false);
      _servletConnection.setDefaultUseCaches(false);
      _servletConnection.setRequestProperty("Content-Type", "application/octet-stream");
      _oos = new ObjectOutputStream(_servletConnection.getOutputStream());
      if (pInputParams != null)
      {
        // Ecriture des objects en entrée;
        for (int i = 0; i < pInputParams.length; i++)
        {
          _oos.writeObject(pInputParams[i]);
        }
        _oos.flush();
      }
      _servletConnection.connect();
      if (_sessionCookieValue == null)
      {
        LOGGER.debug("remote provider - extraction du cookie de session");
        extractSessionCookie();
      }
    }
    catch (Exception e)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_CLIENT_WRITE_ERROR, e);
    }
  }

  /**
   * readResponse
   * @param outputParams
   * @throws ProviderException exception
   */
  private void readResponse(Object[] outputParams) throws ProviderException
  {
    Exception remoteException = null;
    try
    {
      while (_servletConnection.getInputStream().available() == 0)
      {
        try
        {
          Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
          break;
        }
      }
      // lecture de la réponse
      _ois = new ObjectInputStream(_servletConnection.getInputStream());
      int i = 0;
      Object outputParam = null;
      // Lecture du Start
      outputParam = _ois.readObject();
      // Lecture de la donnée suivante
      outputParam = _ois.readObject();
      while (!RemoteProviderProtocole.END_OF_STREAM.equals(outputParam) && (i < outputParams.length))
      {
        if (outputParam instanceof Exception)
        {
          remoteException = (Exception)outputParam;
          break;
        }
        else
        {
          outputParams[i] = outputParam;
          outputParam = _ois.readObject();
          i++;
        }
      }
    }
    catch (Exception e)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_CLIENT_READ_ERROR, e);
    }
    if (remoteException != null)
    {
      if (remoteException instanceof ProviderException)
      {
        throw ((ProviderException)remoteException);
      }
      else
      {
        throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_CLIENT_READ_ERROR, remoteException);
      }
    }
  }

  /**
   * closeConnection
   */
  private void closeConnection()
  {
    // fermeture du flux d'entree de la servlet de pool
    try
    {
      if (_ois != null)
      {
        _ois.close();
        _ois = null;
      }
    }
    catch (Exception e)
    {
      LOGGER.error("erreur ", e);
    }
    // fermeture du flux de sortie vers la Servlet de pool
    try
    {
      if (_oos != null)
      {
        _oos.close();
        _oos = null;
      }
    }
    catch (Exception e)
    {
      LOGGER.error("erreur ", e);
    }
    try
    {
      if (_servletConnection != null)
      {
        _servletConnection.disconnect();
        _servletConnection = null;
      }
    }
    catch (Exception e)
    {
      LOGGER.error("erreur ", e);
    }
  }

  private static String objectArrayToString(Object[] pParams)
  {
    String result;
    if (pParams == null)
    {
      result = "null";
    }
    else
    {
      StringBuilder sbResult = new StringBuilder("[");
      for (int i = 0; i < pParams.length; i++)
      {
        sbResult.append(pParams[i]);
        if (i < (pParams.length - 1))
        {
          sbResult.append(";");
        }
      }
      sbResult.append("]");
      result = sbResult.toString();
    }
    return result;
  }

  /**
   * Effectue l'échange HTTP
   * @param inputParams : les objets passés en paramètre d'entree
   * @param outputParams : les objets retournés par le pool distant
   * @throws ProviderException en cas d'erreur
   */
  private void executeRequest(Object[] inputParams, Object[] outputParams) throws ProviderException
  {
    LOGGER.debug("local provider (" + this + ") connect to remote provider \n\t url=\"" + _urlStr + "\"\n\t Action=\"" + objectArrayToString(inputParams) + "\"\n\t sessionId=\"" + _sessionCookieValue + "\"");
    openConection(inputParams);
    readResponse(outputParams);
    closeConnection();
  }

  /**
   * Initialise une session sur le pool
   * @return the provider pool Session Id
   */
  public String initSession() throws ProviderException
  {
    Object[] inputParams = new Object[2];
    inputParams[0] = RemoteProviderProtocole.INIT_SESSION_ACTION;
    executeRequest(inputParams, null);
    return _sessionCookieValue;
  }

  public void releaseSession()
  {
    Object[] inputParams = new Object[1];
    inputParams[0] = RemoteProviderProtocole.RELEASE_SESSION_ACTION;
    try
    {
      executeRequest(inputParams, null);
    }
    catch (ProviderException e)
    {
      LOGGER.warn(e);
    }
  }

  /**
   * Initialise une factory distante
   * @param pParentTask : La tache parente
   * @param preLoadSize : le nombre de paquets devant être préchargés.
   */
  public void initFactory(AbstractTask pParentTask, Integer preLoadSize) throws ProviderException
  {
    _parentTask = pParentTask;
    _preLoadSize = preLoadSize;
    Object[] inputParams = new Object[3];
    inputParams[0] = RemoteProviderProtocole.INIT_FACTORY_ACTION;
    inputParams[1] = _parentTask;
    inputParams[2] = _preLoadSize;
    executeRequest(inputParams, null);
  }

  /**
   * Initialise un Reader distant
   * @param pReaderName : le nom que l'on souhaite donner au reader
   * @param pAbstractStream : la définition du flux d'entrée
   * @param pTaskRun : task en cours
   * @param pPackageSize : la taille du paquet que l'on veut lire
   * @return the provider reader.
   */
  public RemoteProviderReader initReader(String pReaderName, AbstractStream pAbstractStream, TaskRunType pTaskRun, Integer pPackageSize)
  {
    RemoteProviderReader result = new RemoteProviderReader(this, pReaderName);
    try
    {
      Object[] inputParams = new Object[5];
      inputParams[0] = RemoteProviderProtocole.INIT_READER_ACTION;
      inputParams[1] = pReaderName;
      inputParams[2] = pAbstractStream;
      inputParams[3] = pPackageSize;
      inputParams[4] = (pTaskRun == null ? null : pTaskRun.getId());
      executeRequest(inputParams, null);
    }
    catch (Exception e)
    {
      LOGGER.error("erreur lors de l'init du reader", e);
    }
    return result;
  }

  public RemoteProviderWriter initWriter(String pWriterName, AbstractStream pAbstractStream) throws ProviderException
  {
    RemoteProviderWriter result = new RemoteProviderWriter(this, pWriterName);
    Object[] inputParams = new Object[3];
    inputParams[0] = RemoteProviderProtocole.INIT_WRITER_ACTION;
    inputParams[1] = pWriterName;
    inputParams[2] = pAbstractStream;
    executeRequest(inputParams, null);
    return result;
  }

  /**
   * Lecture d'un paquet
   * @param pReaderName : le nom du reader (exemple : input)
   * @param pCurrentTaskId : Identifiant de task en cours
   * @return le paquet lu.
   */
  public DataPackage read(String pReaderName, Integer pCurrentTaskId) throws ProviderException
  {
    DataPackage result = null;
    Object[] inputParams = new Object[3];
    inputParams[0] = RemoteProviderProtocole.READ_ACTION;
    inputParams[1] = pReaderName;
    inputParams[2] = pCurrentTaskId;
    Object[] outputParams = new Object[1];
    executeRequest(inputParams, outputParams);
    result = (DataPackage)outputParams[0];
    LOGGER.debug("local provider read remote data " + result);
    return result;
  }

  public void write(String pWriterName, String pStringToWrite) throws ProviderException
  {
    Object[] inputParams = new Object[3];
    inputParams[0] = RemoteProviderProtocole.WRITE_ACTION;
    inputParams[1] = pWriterName;
    DataPackage dataToWrite = new DataPackage();
    dataToWrite.setValue(pStringToWrite);
    inputParams[2] = dataToWrite;
    executeRequest(inputParams, null);
  }

  public void releaseWriter(String pWriterName) throws ProviderException
  {
    Object[] inputParams = new Object[2];
    inputParams[0] = RemoteProviderProtocole.RELEASE_WRITER_ACTION;
    inputParams[1] = pWriterName;
    executeRequest(inputParams, null);
  }

  public void releaseReader(String pReaderName) throws ProviderException
  {
    Object[] inputParams = new Object[2];
    inputParams[0] = RemoteProviderProtocole.RELEASE_READER_ACTION;
    inputParams[1] = pReaderName;
    executeRequest(inputParams, null);
  }

  public void setSessionCookieValue(String pSessionCookieValue)
  {
    this._sessionCookieValue = pSessionCookieValue;
  }

  public String getSessionCookieValue()
  {
    return _sessionCookieValue;
  }

  public void setMaxRetry(int pMaxRetry)
  {
    this._maxRetry = pMaxRetry;
  }

  public int getMaxRetry()
  {
    return _maxRetry;
  }
}
