package com.cylande.unitedretail.batch.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

/**
 * Permet de lancer la servlet du moteur de batch sur un site d'exploitation précis.
 */
public class ServletLauncher
{
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(ServletLauncher.class);

  /**
   * Constructor
   */
  public ServletLauncher()
  {
  }

  /**
   * @param pURL : URL de connection
   * @param pRessource Contient l'action a exécuter
   *                   Exemple :
   *                   batchEngine?action=execute&batchName=MyBatch&domain=myDomain
   * @param pData Contient le user et mot de passe de la servlet.
   *              Elle doit contenir un j_username et un j_password
   *              Exemple :
   *                NameValuePair[] data = new NameValuePair[2];
   *                data[0] = new NameValuePair("j_username", "myUserName");
   *                data[1] = new NameValuePair("j_password", "myPassword");
   * @return id du batch
   * @throws Exception : exception
   */
  public String launchBatch(String pURL, String pRessource, NameValuePair[] pData) throws Exception
  {
    if (pData == null)
    {
      return null;
    }
    //Set Cookie Policy to be generically compatible.
    String url = pURL + "/" + getVersion();
    HttpClient client = new HttpClient();
    HostConfiguration hostconfig = new HostConfiguration();
    hostconfig.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
    //------------------------------------------------------------------
    // Get Method: Request secure page and get redirected to login page
    // ----------------------------------------------------------------
    GetMethod authget = new GetMethod(url);
    try
    {
      client.executeMethod(authget);
    }
    catch (HttpException httpe)
    {
      LOGGER.error("HttpException occured : " + httpe.getMessage());
    }
    catch (IOException ioe)
    {
      LOGGER.error("IOException occured : " + ioe.getMessage());
    }
    //String responseBody = authget.getResponseBodyAsString();
    String responseBody;
    // Post Method: logs into url
    PostMethod authpost = new PostMethod(pURL + "/" + "j_security_check");
    authpost.setRequestBody(pData);
    if (authget.getRequestHeader("Cookie") != null)
    {
      authpost.setRequestHeader(authget.getRequestHeader("Cookie"));
    }
    authpost.setRequestHeader(authget.getRequestHeader("Host"));
    authpost.setRequestHeader(authget.getRequestHeader("User-Agent"));
    //Release Get Connection
    authget.releaseConnection();
    try
    {
      client.executeMethod(authpost);
    }
    catch (HttpException httpe)
    {
      LOGGER.error("HttpException occured : " + httpe.getMessage());
    }
    catch (IOException ioe)
    {
      LOGGER.error("IOException occured : " + ioe.getMessage());
    }
    if (authget.getRequestHeader("Cookie") != null)
    {
      authget.setRequestHeader(authpost.getRequestHeader("Cookie"));
    }
    authget.setRequestHeader(authpost.getRequestHeader("Host"));
    authget.setRequestHeader(authpost.getRequestHeader("User-Agent"));
    authpost.releaseConnection();
    // ----------------------------------------------------------------
    url = pURL + "/" + pRessource;
    authget = new GetMethod(url);
    try
    {
      client.executeMethod(authget);
    }
    catch (HttpException httpe)
    {
      LOGGER.error("HttpException occured : " + httpe.getMessage());
    }
    catch (IOException ioe)
    {
      LOGGER.error("IOException occured : " + ioe.getMessage());
    }
    // Version évoluée adaptée à une volumétrie plus importante
    InputStream responseIS = authget.getResponseBodyAsStream();
    InputStreamReader streamReader = new InputStreamReader(responseIS);
    BufferedReader buffer = null;
    try
    {
      buffer = new BufferedReader(streamReader);
      String line = "";
      StringWriter writer = new StringWriter();
      while (null != (line = buffer.readLine()))
      {
        writer.write(line);
      }
      // Sortie finale dans le String
      responseBody = writer.toString();
      authget.releaseConnection();
      return responseBody;
    }
    finally
    {
      if (buffer != null)
      {
        buffer.close();
      }
    }
  }

  /**
   * Construit la ressource pour l'appel a la servlet.
   * @param pBatchName Nom du batch a lancer
   * @param pDomain Domaine associé au batch
   * @return String
   */
  public static String getRessource(String pBatchName, String pDomain, String pAction)
  {
    return "batchEngine?action=" + pAction + "&batchName=" + pBatchName + "&domain=" + pDomain;
  }

  /**
   * getVersion
   * @return résultat
   */
  public static String getVersion()
  {
    return "batchEngine?action=about";
  }
}
