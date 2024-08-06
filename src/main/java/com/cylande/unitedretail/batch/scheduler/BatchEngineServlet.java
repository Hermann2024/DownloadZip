package com.cylande.unitedretail.batch.scheduler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.security.Principal;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.execution.quartz.JobManager;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.dbsynchro.service.DbSynchroEngineServiceDelegate;
import com.cylande.unitedretail.framework.URException;
import com.cylande.unitedretail.framework.business.jbo.BusinessJboException;
import com.cylande.unitedretail.framework.context.URContext;
import com.cylande.unitedretail.message.dbsynchro.DBSynchroScenarioType;
import com.cylande.unitedretail.message.dbsynchro.JarType;
import com.cylande.unitedretail.message.sales.common.VariedMovementType;
import com.cylande.unitedretail.salesnetwork.service.VariedMovementServiceDelegate;

/**
 * Servlet d'activation et d'ex�cution du moteur de batch
 * Attention, c'est une servlet donc une seule instance !
 */
public class BatchEngineServlet extends HttpServlet
{
  /** format de la page */
  protected static final String CONTENT_TYPE = "text/xml; charset=UTF-8";
  /** nom de la propri�t� system pour d�signer le chemin de l'application */
  private static final String PATH_ROOT = "fileRoot";
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(BatchEngineServlet.class);

  /**
   * Servlet initialization.
   * @param pConfig configuration parameters
   * @throws ServletException exception
   */
  public void init(ServletConfig pConfig) throws ServletException
  {
    String result = "OK";
    super.init(pConfig);
    initFileRootSystemProperty();
    try
    {
      LOGGER.info("BatchEngineServlet initialization...");
      result = initBatchEngine();
      LOGGER.info("BatchEngineServlet initialized");
    }
    catch (BusinessJboException e1)
    {
      result = "ERROR";
      LOGGER.error("Un probl�me est survenu pendant l'initialisation de la servlet BatchEngineServlet", e1);
      // Les erreurs d'appel de services ne sont plus bloquantes au deploiement
      // Cas de modification de la table SITE pour que le dbsyncho puisse passer
    }
    catch (Exception e)
    {
      result = "ERROR";
      LOGGER.error("Un probl�me est survenu pendant l'initialisation de la servlet BatchEngineServlet", e);
      throw new ServletException(e);
    }
    finally
    {
      logStarting(result);
    }
  }

  /**
   * M�thode destroy de la servlet
   */
  public void destroy()
  {
    super.destroy();
    LOGGER.info("Stop Scheduled Jobs ....");
    try
    {
      JobManager jobMgr = JobManager.getInstance();
      jobMgr.removeAllScheduledJobs();
      jobMgr.interruptAllJobs();
      jobMgr.stopScheduler();
    }
    catch (Exception e)
    {
      LOGGER.error("An error has occurred during stopping Quartz Scheduler", e);
    }
    finally
    {
      MessageConsumerManager.getInstance().stopAll();
    }
  }

  /**
   * M�thode GET de la servlet, redirig�e vers la m�thode POST
   * @param pRequest HttpServletRequest
   * @param pResponse HttpServletResponse
   * @throws IOException exception
   */
  public void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws IOException
  {
    doPost(pRequest, pResponse);
  }

  /**
   * M�thode POST de la servlet
   * @param pRequest HttpServletRequest
   * @param pResponse HttpServletResponse
   * @throws IOException exception
   */
  public void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws IOException
  {
    // extraction des param�tres
    Map<String, String> params = extractAllParamsFromRequest(pRequest);
    // pr�paration de la r�ponse
    pResponse.setContentType(CONTENT_TYPE);
    PrintWriter output = pResponse.getWriter();
    // r�cup�ration de l'id utilisateur
    params.put(BatchEngineServletDispatch.USER, getUserContext());
    try
    {
      //execution du dispatch;
      BatchEngineServletDispatch dispatch = new BatchEngineServletDispatch(params, pResponse);
      dispatch.service();
    }
    catch (Exception e)
    {
      LOGGER.error("An error has occurred during execution of servlet QuartzHttpServlet", e);
      boolean hasXSL = false;
      if ("true".equals(params.get(BatchEngineServletDispatch.XSL)))
      {
        hasXSL = true;
      }
      output.println(BatchUtil.XML_HEADER);
      if (hasXSL)
      {
        output.println(BatchUtil.XSL_ENGINE_INCLUDE);
      }
      output.println("<error>");
      output.println("<info>An error has occurred</info>");
      if (e instanceof URException)
      {
        URException exception = (URException)e;
        output.println("<code>" + exception.getCanonicalCode() + "</code>");
      }
      output.println("<message>" + e.getLocalizedMessage() + "</message>");
      output.println("</error>");
    }
    finally
    {
      output.flush();
      output.close();
    }
  }

  /**
   * initialisation du moteur de batch
   * @throws Exception exception
   */
  private String initBatchEngine() throws Exception
  {
    String result = "OK";
    BatchEngine batchEngine = new BatchEngine();
    try
    {
      JarType jar = new DbSynchroEngineServiceDelegate().isSynchronizedDatabase(new DBSynchroScenarioType(), ContextTransformer.fromLocale());
      // on ne force l'activation du verrou que dans le cas o� DBSynchro n'est pas OK
      // afin de conserver l'�tat du verrou (true / false) quand il est positionn� manuellement
      if (jar != null)
      {
        result = "LOCKED";
        LOGGER.warn("Jar " + jar.getName() + " non synchronis� : activation du verrouillage des batchs");
        batchEngine.setLockedBatchExecution(true);
      }
    }
    catch (Exception e)
    {
      // une erreur survenue sur le controle / activation du verrouillage ne doit pas emp�cher le blocage de l'appli
      LOGGER.error("Un probl�me est survenu pendant l'initialisation de la servlet BatchEngineServlet", e);
      result = "STARTING WITH ERROR";
    }
    batchEngine.loadRepository();
    batchEngine.launchInitBatch();
    batchEngine.loadQueues();
    batchEngine.scheduleBatchs();
    return result;
  }

  /**
   * R�cup�re tous les param�tres transmis par la requ�te et les stocke dans une Map
   * @param pRequest HttpServletRequest
   * @return la map de tous les param�tres transmis
   */
  private Map<String, String> extractAllParamsFromRequest(HttpServletRequest pRequest)
  {
    LOGGER.info("Extraction des param�tres transmis � la servlet");
    Map<String, String> result = new HashMap<String, String>();
    Enumeration<String> paramNames = pRequest.getParameterNames();
    String paramName;
    while (paramNames.hasMoreElements())
    {
      paramName = paramNames.nextElement();
      result.put(paramName, pRequest.getParameter(paramName));
    }
    return result;
  }

  /**
   * R�cup�re le nom de l'utilisateur authentifi�
   * @return le nom de l'utilisateur qui sollicite le moteur de batch
   */
  private String getUserContext()
  {
    LOGGER.info("R�cup�ration de l'utilisateur");
    String result = null;
    Principal userPrincipal = URContext.getSecurityContext().getUserPrincipal();
    if (userPrincipal != null)
    {
      result = userPrincipal.getName();
      LOGGER.info("User : " + result);
    }
    return result;
  }

  /**
   * R�cup�re le chemin de l'application du disque
   * @return chemin de l'application
   */
  private String getContextURLString()
  {
    String contextURL = getServletConfig().getServletContext().getRealPath("/");
    if (contextURL != null)
    {
      if (contextURL.charAt(0) != File.separatorChar)
      {
        contextURL = File.separator + contextURL;
      }
      if (!contextURL.endsWith(File.separator))
      {
        contextURL = contextURL + File.separator;
      }
    }
    return contextURL;
  }

  /**
   * Positionne la valeur de la propri�t� fileRoot si elle n'existe pas avec la valeur de contextURLString.
   * Attention, travailler avec le registre de variable system n'est pas s�r (acc�s concurrent)
   */
  private void initFileRootSystemProperty()
  {
    LOGGER.debug("R�cup�ration de l'emplacement de l'application");
    String fileRoot = System.getProperty(PATH_ROOT);
    if (fileRoot == null || fileRoot.equals(""))
    {
      System.setProperty(PATH_ROOT, getContextURLString());
    }
  }

  private void logStarting(String pLog)
  {
    try
    {
      VariedMovementType varMvt = new VariedMovementType();
      varMvt.setCode(InetAddress.getLocalHost().getHostName());
      if (System.getProperty("APP_DESCRIPTION") != null)
      {
        varMvt.setCode(varMvt.getCode() + " (" + System.getProperty("APP_DESCRIPTION") + ")");
      }
      varMvt.setMovementCode("startingBatchEngine");
      varMvt.setDateTimeMvt(Calendar.getInstance());
      varMvt.setValue(pLog);
      new VariedMovementServiceDelegate().postVariedMovement(varMvt, null, ContextTransformer.fromLocale());
    }
    catch (Exception e)
    {
      LOGGER.error(e, e);
    }
  }
}
