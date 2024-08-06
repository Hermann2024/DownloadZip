package com.cylande.unitedretail.batch.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.jar.JarFile;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.message.batch.ProviderFileType;

public class BatchUtil
{
  public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  public static final String XSL_ENGINE_INCLUDE = "<?xml-stylesheet type=\"text/xsl\" href=\"BatchEngine.xsl\"?>";
  private static final String CLASS_PATH = BatchUtil.class.getName().replace(".", "/") + ".class";

  public static JarFile getCurrentJarFile() throws Exception
  {
    JarFile result = null;
    URL url = BatchUtil.class.getClassLoader().getResource(CLASS_PATH);
    if (url != null)
    {
      File jarFile = new File(url.getFile().substring(5, url.getFile().indexOf("!")));
      return new JarFile(jarFile);
    }
    return result;
  }

  public static void copyInputStream(InputStream pInputStream, File pFile) throws Exception
  {
    FileOutputStream fo = null;
    try
    {
      fo = new FileOutputStream(pFile);
      while (pInputStream.available() > 0)
      {
        fo.write(pInputStream.read());
      }
    }
    finally
    {
      try
      {
        if (fo != null)
        {
          fo.close();
        }
      }
      finally
      {
        pInputStream.close();
      }
    }
  }

  public static void createXmlFile(String pFileName, String pContent) throws Exception
  {
    if (pFileName != null)
    {
      PrintWriter pw = new PrintWriter(new File(pFileName), "UTF-8");
      pw.println(XML_HEADER);
      pw.println(pContent);
      pw.close();
    }
  }

  /**
   * Retourne l'indicateur KeepRootElementPrefix du provider passé en paramètre
   * @param pProviderReader Provider
   * @return Boolean
   * @throws ProviderException Erreur de provider
   */
  public static Boolean getKeepRootElementPrefix(ProviderReader pProviderReader) throws ProviderException
  {
    Boolean result = null;
    ProviderFileType fileProvider = null;
    try
    {
      if ((pProviderReader != null) && (pProviderReader.getProviderFileType() != null))
      {
        fileProvider = pProviderReader.getProviderFileType();
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.GET_PROVIDER_FILE_TYPE_ERROR, exc);
    }
    if (fileProvider != null)
    {
      result = fileProvider.getKeepRootElementPrefix();
    }
    return result;
  }

  /**
   * Retourne l'indicateur IgnoreRootNamespace du provider passé en paramètre
   * @param pProviderReader Provider
   * @return Boolean
   * @throws ProviderException Erreur de provider
   */
  public static Boolean getIgnoreRootNamespace(ProviderReader pProviderReader) throws ProviderException
  {
    Boolean result = null;
    ProviderFileType fileProvider = null;
    try
    {
      if ((pProviderReader != null) && (pProviderReader.getProviderFileType() != null))
      {
        fileProvider = pProviderReader.getProviderFileType();
      }
    }
    catch (Exception exc)
    {
      throw new ProviderException(BatchErrorDetail.GET_PROVIDER_FILE_TYPE_ERROR, exc);
    }
    if (fileProvider != null)
    {
      result = fileProvider.getIgnoreRootNamespace();
    }
    return result;
  }

  public static String getFileNameNoExt(String pFileName)
  {
    String result = null;
    if (pFileName != null && pFileName.contains("."))
    {
      int pointpos = pFileName.indexOf('.');
      result = pFileName.substring(0, pointpos);
    }
    return result;
  }
}
