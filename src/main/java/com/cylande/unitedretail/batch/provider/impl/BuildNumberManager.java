package com.cylande.unitedretail.batch.provider.impl;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.framework.exception.FileManagementException;
import com.cylande.unitedretail.framework.tools.filemanagement.DirectoryFileManager;
import com.cylande.unitedretail.message.batch.FileType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class BuildNumberManager
{
  /** header du fichier buildNumber */
  private static final String HEADER = "#Build Number for Providers. Do not edit!\n";
  /** The name of the property in which the build number is stored. */
  private static final String DEFAULT_PROPERTY_NAME = "build.number";
  /** gestionnaire de fichier */
  protected DirectoryFileManager _directoryFileManager = null;
  /** définition du fichier buildNumber du provider */
  private FileType _buildNumber = null;
  /** répertoire du fichier ciblé (lecture ou écriture) */
  private String _fileDirectory = null;
  /** pattern désignant une liste de fichiers, ou nom du fichier */
  private String _fileNamePattern;
  /** buildNumber courant */
  private int _currentBuildNumber = -1;

  public BuildNumberManager() throws FileManagementException
  {
    init();
  }

  /**
   * initialisation du BuildNumberManager
   * @throws FileManagementException exception
   */
  private void init() throws FileManagementException
  {
    if (_buildNumber != null)
    {
      _fileDirectory = _buildNumber.getDir();
      _fileNamePattern = _buildNumber.getFileName();
      _directoryFileManager = new DirectoryFileManager(_fileDirectory);
    }
  }

  /**
   * vérifie si la chaine passée en paramètre ne contient pas de caractères jokers
   * @param pString
   * @throws ProviderException exception
   */
  private void checkSyntax(String pString) throws ProviderException
  {
    if (pString == null)
    {
      return;
    }
    if (pString.contains("*") || pString.contains("?"))
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDERMANAGER_JOKERS_FORBIDDEN);
    }
  }

  /**
   * récupère la propriété buildNumber
   * @throws ProviderException exception
   */
  public int getBuildNumber() throws ProviderException
  {
    if (_currentBuildNumber != -1)
    {
      return _currentBuildNumber;
    }
    if (_buildNumber != null)
    {
      String targetDir;
      String targetFileName;
      // contrôle si utilisation de jokers
      targetDir = _buildNumber.getDir();
      checkSyntax(targetDir);
      targetFileName = _buildNumber.getFileName();
      checkSyntax(targetFileName);
      if (new File(targetDir + "/" + targetFileName).exists())
      {
        _currentBuildNumber = getBuildNumber(targetDir + "/" + targetFileName);
      }
      else
      {
        // dans le cas contraire, on le créer
        createBuildNumberFile(targetDir + "/" + targetFileName);
      }
    }
    return _currentBuildNumber;
  }

  /**
   * créer le fichier buildNumber s'il n'existe pas
   * @throws ProviderException exception
   */
  private void createBuildNumberFile(String pFile) throws ProviderException
  {
    try
    {
      File file = new File(pFile);
      DirectoryFileManager directory = new DirectoryFileManager(file.getParent());
      OutputStream output = directory.getOutputStream(file.getName(), true);
      output.write(HEADER.getBytes());
      output.write((DEFAULT_PROPERTY_NAME + "=" + 0).getBytes());
      output.flush();
      output.close();
      directory.close();
      _currentBuildNumber = 0;
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.BUILDNUMBER_CREATE_ERROR, e);
    }
  }

  /**
   * récupère buildNumber s'il existe
   * @param pFile : fichier .num
   * @return buildNumber
   * @throws ProviderException exception
   * @throws FileManagementException exception
   */
  private int getBuildNumber(String pFile) throws ProviderException
  {
    int result = -1;
    try
    {
      File file = new File(pFile);
      DirectoryFileManager directory = new DirectoryFileManager(file.getParent());
      InputStream ips = directory.getInputStream(file.getName());
      result = getBuildNumber(ips);
      directory.close();
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.BUILDNUMBER_INPUT_ERROR, e);
    }
    return result;
  }

  private int getBuildNumber(InputStream pInputStream) throws UnsupportedEncodingException, IOException
  {
    BufferedReader br = null;
    try
    {
      int result = -1;
      br = new BufferedReader(new InputStreamReader(pInputStream, "UTF-8"));
      String ligne;
      while ((ligne = br.readLine()) != null)
      {
        if (ligne.contains(DEFAULT_PROPERTY_NAME))
        {
          result = Integer.parseInt(ligne.substring(ligne.indexOf('=') + 1).trim());
        }
      }
      return result;
    }
    finally
    {
      if (br != null)
      {
        br.close();
      }
    }
  }

  /**
   * incrémente le buildNumber s'il existe
   * @return buildNumber
   * @throws ProviderException exception
   * @throws FileManagementException exception
   */
  public void writeBuildNumberToFile() throws ProviderException
  {
    try
    {
      if (_currentBuildNumber == -1)
      {
        getBuildNumber();
      }
      if (_buildNumber != null)
      {
        String targetDir = _buildNumber.getDir();
        checkSyntax(targetDir);
        String targetFileName = _buildNumber.getFileName();
        checkSyntax(targetFileName);
        File entree = new File(targetDir + "/" + targetFileName);
        writeBuildNumberToFile(entree);
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.BUILDNUMBER_WRITE_ERROR, e);
    }
  }

  private void writeBuildNumberToFile(File pEntree) throws IOException
  {
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new FileWriter(pEntree));
      bw.write(HEADER);
      bw.write(DEFAULT_PROPERTY_NAME + "=" + (++_currentBuildNumber));
      bw.flush();
    }
    finally
    {
      if (bw != null)
      {
        bw.close();
      }
    }
  }

  /**
   * @param pBuildNumber
   */
  public void setBuildNumber(FileType pBuildNumber)
  {
    _buildNumber = pBuildNumber;
  }
}
