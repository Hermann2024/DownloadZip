package com.cylande.unitedretail.process.service;

import com.cylande.unitedretail.batch.service.FileProviderTraceManagerServiceDelegate;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.message.batch.*;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.criteria.CriteriaIntegerType;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/downloadZip")
public class DownloadZip extends HttpServlet
{

  @Override
  public void init(ServletConfig pConfig) throws ServletException
  {
    super.init(pConfig);
  }

  @Override
  protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws IOException
  {

    String taskId = pRequest.getParameter("taskId");

    if (taskId == null || taskId.isEmpty())
    {
      pResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "taskId manquant ou vide");
      return;
    }

    FileProviderTraceManagerServiceDelegate fileProvider = new FileProviderTraceManagerServiceDelegate();
    FileProviderTraceCriteriaListType criteriaList = new FileProviderTraceCriteriaListType();
    FileProviderTraceCriteriaType criteria = new FileProviderTraceCriteriaType();
    CriteriaIntegerType taskIdCriteria = new CriteriaIntegerType();
    taskIdCriteria.setEquals(Integer.parseInt(taskId));
    criteria.setTaskId(taskIdCriteria);
    criteriaList.getList().add(criteria);

    String fileName;
    try
    {
      FileProviderTraceListType result = fileProvider.findFileProviderTrace(criteriaList, new FileProviderTraceScenarioType(), new ContextType());
      fileName = result.getList().get(0).getFilePath();

    }
    catch (TechnicalServiceException e)
    {
      pResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur du service technique");
      return;
    }
    catch (com.cylande.unitedretail.framework.service.ServiceException e)
    {
      pResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur de service");
      return;
    }

    File tempFile = new File(fileName);

    prepareZipResponse(pResponse, taskId);
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(pResponse.getOutputStream()))
    {
      addToZipFile(tempFile, zipOutputStream);
    }
    catch (IOException e)
    {
      pResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la génération du fichier zip");

    }
    finally
    {
      if (tempFile.exists())
      {
        tempFile.delete();
      }
    }
  }


  private void prepareZipResponse(HttpServletResponse pResponse, String pBatchId) throws UnsupportedEncodingException
  {
    pResponse.setContentType("application/zip");
    pResponse.setHeader("Content-Disposition", "attachment;filename=batch_" + URLEncoder.encode(pBatchId, StandardCharsets.UTF_8.toString()) + ".zip");
  }

  private void addToZipFile(File pFile, ZipOutputStream zipOutputStream) throws IOException
  {
    try (FileInputStream fis = new FileInputStream(pFile);
         BufferedInputStream bis = new BufferedInputStream(fis))
    {
      zipOutputStream.putNextEntry(new ZipEntry("batch_" + pFile.getName()));
      byte[] buffer = new byte[2048];
      int bytesRead;
      while ((bytesRead = bis.read(buffer)) != -1)
      {
        zipOutputStream.write(buffer, 0, bytesRead);
      }
      zipOutputStream.closeEntry();
    }
  }

  @Override
  protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws  IOException
  {
    doPost(pRequest, pResponse);
  }
}