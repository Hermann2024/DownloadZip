package com.cylande.unitedretail.batch.provider.pool;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.RemoteProviderProtocolException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.task.AbstractTask;
import com.cylande.unitedretail.message.batch.AbstractStream;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Provider reader.
 */
public class RemoteProviderPoolServlet extends HttpServlet
{
  private static ProviderContainer getCurrentContainer(HttpServletRequest pRequest)
  {
    ProviderContainer result = null;
    HttpSession session = pRequest.getSession(false);
    if (session != null)
    {
      result = (ProviderContainer)session.getAttribute("container");
    }
    return result;
  }

  private static void executeAction(RemoteProviderProtocoleManager pProtocoleManager, HttpServletRequest pRequest) throws ProviderException
  {
    Logger logger = Logger.getLogger(RemoteProviderPoolServlet.class);
    RemoteProviderProtocoleTag action = (RemoteProviderProtocoleTag)pProtocoleManager.readParam();
    logger.debug("Remote provider pool action = " + action);
    ProviderContainer providerContainer = getCurrentContainer(pRequest);
    if (RemoteProviderProtocole.INIT_FACTORY_ACTION.equals(action))
    {
      initFactory(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.INIT_READER_ACTION.equals(action))
    {
      initReader(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.INIT_WRITER_ACTION.equals(action))
    {
      initWriter(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.READ_ACTION.equals(action))
    {
      read(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.WRITE_ACTION.equals(action))
    {
      write(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.RELEASE_READER_ACTION.equals(action))
    {
      releaseReader(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.RELEASE_WRITER_ACTION.equals(action))
    {
      releaseWriter(providerContainer, pProtocoleManager);
    }
    else if (RemoteProviderProtocole.RELEASE_SESSION_ACTION.equals(action))
    {
      releaseFactory(providerContainer, pProtocoleManager);
      HttpSession session = pRequest.getSession(false);
      if (session != null)
      {
        session.invalidate();
      }
      pProtocoleManager.onSessionReleased();
    }
    else if (RemoteProviderProtocole.INIT_SESSION_ACTION.equals(action))
    {
      logger.debug("Call Init Session");
      initSession(pRequest, pProtocoleManager);
    }
    else
    {
      logger.error("Provider pool Servlet: action inconnue " + action);
    }
  }

  /**
   * Get
   */
  public void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse)
  {
    doPost(pRequest, pResponse);
  }

  /**
   * Post
   */
  public void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse)
  {
    Logger logger = Logger.getLogger(RemoteProviderPoolServlet.class);
    logger.debug("Remote provider pool called");
    RemoteProviderProtocoleManager protocolManager = null;
    try
    {
      protocolManager = new RemoteProviderProtocoleManager(pRequest.getInputStream(), pResponse.getOutputStream());
      executeAction(protocolManager, pRequest);
    }
    catch (ProviderException e)
    {
      if (protocolManager != null)
      {
        protocolManager.notifyException(e);
      }
    }
    catch (IOException e)
    {
      logger.debug("erreur lors de l'overture servlet " + e.getLocalizedMessage());
    }
  }

  public static void initFactory(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException
  {
    if (pProviderContainer == null)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR);
    }
    AbstractTask parentTask = (AbstractTask)pProtocoleManager.readParam();
    Integer iPreLoadSize = (Integer)pProtocoleManager.readParam();
    pProviderContainer.initFactory(pProtocoleManager, parentTask, iPreLoadSize);
  }

  private static void initReader(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException, ProviderException
  {
    if (pProviderContainer == null)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR);
    }
    String readerName = (String)pProtocoleManager.readParam();
    AbstractStream abstractStream = (AbstractStream)pProtocoleManager.readParam();
    Integer packageSize = (Integer)pProtocoleManager.readParam();
    Integer currentTaskId = (Integer)pProtocoleManager.readParam();
    pProviderContainer.initReader(pProtocoleManager, readerName, abstractStream, packageSize, currentTaskId);
  }

  public static void read(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException
  {
    if (pProviderContainer == null)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR);
    }
    String readerName = (String)pProtocoleManager.readParam();
    Integer currentTaskId = (Integer)pProtocoleManager.readParam();
    pProviderContainer.read(pProtocoleManager, readerName, currentTaskId);
  }

  private static void write(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException, ProviderException
  {
    if (pProviderContainer == null)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR);
    }
    String writerName = (String)pProtocoleManager.readParam();
    DataPackage packageToWrite = (DataPackage)pProtocoleManager.readParam();
    pProviderContainer.write(pProtocoleManager, writerName, packageToWrite);
  }

  private static void initWriter(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException, ProviderException
  {
    if (pProviderContainer == null)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR);
    }
    String writerName = (String)pProtocoleManager.readParam();
    AbstractStream abstractStream = (AbstractStream)pProtocoleManager.readParam();
    pProviderContainer.initWriter(pProtocoleManager, writerName, abstractStream);
  }

  private static void releaseReader(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException, ProviderException
  {
    if (pProviderContainer == null)
    {
      throw new RemoteProviderProtocolException(BatchErrorDetail.REMOTE_PROVIDER_PROTOCOLE_SERVLET_SESSION_ERROR);
    }
    String readerName = (String)pProtocoleManager.readParam();
    pProtocoleManager.onReaderReleased(readerName, true);
  }

  private static void initSession(HttpServletRequest pRequest, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException, ProviderException
  {
    Logger logger = Logger.getLogger(RemoteProviderPoolServlet.class);
    logger.debug("Opening session");
    HttpSession session = pRequest.getSession(true);
    if (session != null)
    {
      logger.debug("Session opened id=\'" + session.getId() + "\'");
      ProviderContainer container = new ProviderContainer();
      session.setAttribute("container", container);
      pProtocoleManager.onSessionInit(container);
    }
  }

  private static void releaseWriter(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager) throws RemoteProviderProtocolException, ProviderException
  {
    String writerName = (String)pProtocoleManager.readParam();
    pProtocoleManager.onWriterReleased(writerName, true);
  }

  private static void releaseFactory(ProviderContainer pProviderContainer, RemoteProviderProtocoleManager pProtocoleManager)
  {
    pProviderContainer.releaseFactory(pProtocoleManager);
  }
}
