package com.cylande.unitedretail.batch.provider.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.rw.impl.StaxXMLControler;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.PathFileProviderType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

/**
 * File Provider Type
 */
public class FileProvider extends Provider
{
  private static final Logger LOGGER = Logger.getLogger(FileProvider.class);
  /** pr�cise la propri�t� syst�me � utiliser pour m�moriser le nom du fichier lu */
  private static final String SYS_INPUTFILENAME = "inputFileName";
  /** pr�cise la propri�t� syst�me � utiliser pour m�moriser le nom du fichier lu sans son extension */
  private static final String SYS_INPUTFILENAMENOEXT = "inputFileNameNoExt";
  /** pr�cise la propri�t� syst�me � utiliser pour m�moriser le num du build */
  private static final String SYS_BUILDNUMBER = "buildNumber";
  /** gestionnaire de fichiers */
  protected FileProviderManager _fileProviderManager;
  /** gestionnaire de fichiers */
  protected BuildNumberManager _buildNumberManager;
  /** d�finition du fichier de provider */
  protected ProviderFileType _fileProviderDef = null;
  /** d�finition du fichier de provider */
  protected ProviderFileType _buildNumberDef = null;
  /** marqueur d'�crasement*/
  private boolean _overwrite = true;
  /** d�finition du fichier d'archive du provider */
  private FileType _archiveDef = null;
  /** d�finition du fichier buildNumber du provider */
  private FileType _buildNumber = null;
  /** inputStream en cours de lecture */
  private InputStream _currentInputStream = null;
  /** nom du fichier en cours d'�criture */
  private String _currentOutputStreamFileName = null;
  /** tache courante */
  private TaskRunType _currentTaskRun = null;
  /** type de provider */
  private String _currentProviderNameType = null;
  private String _scenarioFileName = null;
  private String _scenarioValue = null;
  /** D�finition du fichier de rejet. */

  /**
   * Constructeur classique
   * @param pProviderDef : la d�finition du provider (issue du provider.xml)
   * @param pPropertiesManager : propertiesManager permettant d'acc�der au propri�t� d'environement d'ex�cution (domaines, etc...)
   * @param pDomain : the execution domain
   */
  public FileProvider(ProviderType providerDef, String pProviderName, TaskRunType pTaskRun, PropertiesManager pPropertiesManager, String pDomain, String pAlternativeDomain) throws ProviderException
  {
    super(providerDef, pPropertiesManager, pDomain, pAlternativeDomain);
    _currentTaskRun = pTaskRun;
    _currentProviderNameType = pProviderName;
    initFileProvider();
  }

  public void setOutputJson()
  {
    if (getProviderDef() instanceof FILEPROVIDER)
    {
      _outputJson = _fileProviderDef.getFileName().endsWith(".json") ? true : false;
      _outputJson = getPropertiesManager().getProperty("outputJson", getCurrentDomain(), getAlternativeDomain()).isEmpty() ? _outputJson : true;
    }
  }

  public void setInputJson()
  {
    if (getProviderDef() instanceof FILEPROVIDER)
    {
      _inputJson = _fileProviderDef.getFileName().endsWith(".json") ? true : false;
      _inputJson = getPropertiesManager().getProperty("inputJson", getCurrentDomain(), getAlternativeDomain()).isEmpty() ? _inputJson : true;
    }
  }

  /**
   * initialise le fileProvider
   */
  private void initFileProvider() throws ProviderException
  {
    if (getProviderDef() instanceof FILEPROVIDER)
    {
      _buildNumber = getProviderDef().getBuildnumber();
      if (_buildNumber != null)
      {
        try
        {
          recordBuildNumber();
        }
        catch (ProviderException e)
        {
          throw new ProviderException(BatchErrorDetail.BUILDNUMBER_INPUT_ERROR, e);
        }
      }
      _fileProviderDef = ((FILEPROVIDER)getProviderDef()).getFile();
      _archiveDef = getProviderDef().getArchive();
      if (_fileProviderDef.getOverWrite() != null)
      {
        _overwrite = _fileProviderDef.getOverWrite();
      }
    }
  }

  /** {@inheritDoc} */
  protected InputStream nextInputStream() throws ProviderException
  {
    if (_currentInputStream != null)
    {
      // fermer le flux de lecture pr�c�dent
      getFileProviderManager().closeCurrentInputStream();
      // nettoyer la memo des noms de fichier et fermer le inputStream courant
      clearSysRepoInputFileName();
    }
    if (_buildNumber != null)
    {
      getBuildNumberManager().setBuildNumber(getCurrentBuildNumber());
      recordBuildNumber();
    }
    // r�cup�rer un inputstream
    _currentInputStream = getFileProviderManager().createInputStream();
    // mise a jour dans le TaskRun du fichier utilis�
    updateTaskRunProvider();
    // enregistr� le nom du fichier utilis� en tant qu'inputstream
    recordInputFileName();
    // pr�parer la conf pour l'archivage
    getFileProviderManager().setArchive(getCurrentArchiveFileType());
    setScenarioValue();
    if (getCurrentProviderFileType().getFlagName() != null && getFileProviderManager().getCurrentFileName() != null)
    {
      File flagFile = new File(getFileProviderManager().getFileDirectory() + "/" + getCurrentProviderFileType().getFlagName());
      if (!flagFile.exists())
      {
        getFileProviderManager().setArchive(null); // le fichier ne doit pas �tre archiv� tant que son t�moin n'est pas pr�sent
        nextInputStream(); // pas de fichier t�moin, on passe au fichier suivant
      }
      else
      {
        flagFile.delete();
      }
    }
    return _currentInputStream;
  }

  /**
   * {@inheritDoc}
   */
  protected InputStream checkXMLInputStream(InputStream pInputStream) throws ProviderException
  {
    if (_check && _currentInputStream != null)
    {
      StaxXMLControler staxXMLControler = new StaxXMLControler();
      try
      {
        staxXMLControler.control(_currentInputStream, _defaultEncodageLimit, _defaultEncodage);
        getFileProviderManager().setReject(null);
      }
      catch (Exception e)
      {
        throw new ProviderException(BatchErrorDetail.STAXPARSER_INVALID_XML_FILE, new Object[] { getFileProviderManager().getCurrentFileName(), e.getLocalizedMessage() }, e);
      }
      _currentInputStream = getFileProviderManager().reloadInputStream();
    }
    return _currentInputStream;
  }

  private void setScenarioValue() throws ProviderException
  {
    ProviderFileType fp = getCurrentProviderFileType();
    // la valeur du sc�nario n'est r�actualis�e que si le nom du fichier a chang�
    if (fp.getScenarioFile() != null && !fp.getScenarioFile().equals(_scenarioFileName))
    {
      _scenarioFileName = fp.getScenarioFile();
      _scenarioValue = readScenario();
    }
  }

  private String readScenario() throws ProviderException
  {
    String result = null;
    File f = new File(getFileProviderManager().getFileDirectory() + "/" + _scenarioFileName);
    if (f.exists() && f.isFile())
    {
      try
      {
        result = readScenario(f);
      }
      catch (Exception e)
      {
        LOGGER.error(e, e);
      }
    }
    return result;
  }

  private String readScenario(File pFile) throws FileNotFoundException, IOException
  {
    StringBuffer result = new StringBuffer();
    BufferedReader in = null;
    try
    {
      in = new BufferedReader(new FileReader(pFile));
      String inputLine;
      while ((inputLine = in.readLine()) != null)
      {
        result.append(inputLine);
      }
      return result.toString();
    }
    finally
    {
      if (in != null)
      {
        in.close();
      }
    }
  }

  /** {@inheritDoc} */
  public boolean hasNextInputStream() throws ProviderException
  {
    return getFileProviderManager().hasNextFile();
  }

  /**
   * M�morise le nom du fichier en cours de lecture dans le sysrepo
   * @throws ProviderException exception
   */
  private void recordInputFileName() throws ProviderException
  {
    String currentFileName = getFileProviderManager().getCurrentFileName();
    LOGGER.debug("Current File : " + currentFileName);
    setCurrentFileName(currentFileName);
    String currentFileNameNoExt = getFileProviderManager().getCurrentFileNameNoExt();
    PropertiesManager pm = getPropertiesManager();
    if (currentFileName != null)
    {
      pm.putSysObject(SYS_INPUTFILENAME, currentFileName);
    }
    if (currentFileNameNoExt != null)
    {
      pm.putSysObject(SYS_INPUTFILENAMENOEXT, currentFileNameNoExt);
    }
  }

  /**
   * M�morise le num du build en cours dans le sysrepo
   * @throws ProviderException exception
   */
  private void recordBuildNumber() throws ProviderException
  {
    int currentBuildNumber = getBuildNumberManager().getBuildNumber();
    PropertiesManager pm = getPropertiesManager();
    if (currentBuildNumber != -1)
    {
      pm.putSysObject(SYS_BUILDNUMBER, currentBuildNumber);
    }
    else
    {
      pm.putSysObject(SYS_BUILDNUMBER, "");
    }
  }

  /**
   * Enl�ve le nom du fichier encours du sysrepo
   */
  private void clearSysRepoInputFileName()
  {
    PropertiesManager pm = getPropertiesManager();
    pm.putSysObject(SYS_INPUTFILENAME, "");
    pm.putSysObject(SYS_INPUTFILENAMENOEXT, "");
    pm.putSysObject(SYS_BUILDNUMBER, "");
  }

  /** {@inheritDoc} */
  public void closeInputStream() throws ProviderException
  {
    getFileProviderManager().closeInputStream();
    clearSysRepoInputFileName();
  }

  /**
   * Fourni un fichier d'�criture, sp�cifique selon le type de provider
   * @return r�sultat
   * @throws ProviderException exception
   */
  protected OutputStream getOutputStream() throws ProviderException
  {
    if (_buildNumber != null)
    {
      getBuildNumberManager().writeBuildNumberToFile();
      recordBuildNumber();
    }
    OutputStream result = getFileProviderManager().createOutputStream(_overwrite);
    _currentOutputStreamFileName = getFileProviderManager().getCurrentFileName();
    setCurrentFileName(_currentOutputStreamFileName);
    updateTaskRunProvider();
    getFileProviderManager().setArchive(getCurrentArchiveFileType());
    return result;
  }

  /**
   * Ferme le fichier temporaire de travail en �criture, et finalise le outputstream : renommage, archivage, envoi...
   * @return the Provider InputStream
   */
  public void closeOutputStream() throws ProviderException
  {
    getFileProviderManager().closeOutputStream();
    // r�initialisation du fileManager
    _fileProviderManager = null;
  }

  /**
   * r�cup�re le fileProviderManager
   * @return r�sultat
   * @throws ProviderException exception
   */
  public FileProviderManager getFileProviderManager() throws ProviderException
  {
    if (_fileProviderManager == null)
    {
      getNewFileProviderManager();
    }
    return _fileProviderManager;
  }

  /**
   * g�n�re un nouveau FileProviderManager avec les providerFileType et archiveFileType courant
   * @return r�sultat
   * @throws ProviderException exception
   */
  private FileProviderManager getNewFileProviderManager() throws ProviderException
  {
    try
    {
      ProviderFileType fp = getCurrentProviderFileType();
      _fileProviderManager = new FileProviderManager(fp);
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDER_GETFILEMANAGER_ERROR, e);
    }
    return _fileProviderManager;
  }

  /**
   * r�cup�re le BuildNumberManager
   * @return r�sultat
   * @throws ProviderException exception
   */
  private BuildNumberManager getBuildNumberManager() throws ProviderException
  {
    if (_buildNumberManager == null)
    {
      getNewBuildNumberManager();
    }
    return _buildNumberManager;
  }

  /**
   * g�n�re un nouveau BuildNumberManager
   * @return r�sultat
   * @throws ProviderException exception
   */
  private BuildNumberManager getNewBuildNumberManager() throws ProviderException
  {
    try
    {
      _buildNumberManager = new BuildNumberManager();
      _buildNumberManager.setBuildNumber(filterProperties(_buildNumber));
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDER_GETFILEMANAGER_ERROR, e);
    }
    return _buildNumberManager;
  }

  /** {@inheritDoc} */
  public ProviderFileType getCurrentProviderFileType()
  {
    return filterProperties(_fileProviderDef);
  }

  public void setFileProviderDef(ProviderFileType pFileProviderDef)
  {
    _fileProviderDef = pFileProviderDef;
  }

  /**
   * Donne le archiveFileType courants
   * @return r�sultat
   */
  private FileType getCurrentArchiveFileType()
  {
    return filterProperties(_archiveDef);
  }

  /**
   * Donne le buildNumberFileType courants
   * @return r�sultat
   */
  private FileType getCurrentBuildNumber()
  {
    return filterProperties(filterProperties(_buildNumber));
  }

  /**
   * Filtre les properties d'un providerFileType
   * @param pFileType
   * @return r�sultat
   */
  private ProviderFileType filterProperties(ProviderFileType pFileType)
  {
    if (pFileType == null)
    {
      return null;
    }
    ProviderFileType result = filterProperties((FileType)pFileType);
    if (pFileType.getSortType() != null)
    {
      result.setSortType(pFileType.getSortType());
      result.setDescSort(pFileType.isDescSort());
    }
    if (pFileType.getScenarioFile() != null)
    {
      result.setScenarioFile(getFilteredStringByDomain(pFileType.getScenarioFile()));
    }
    if (pFileType.getStartDate() != null && pFileType.getStartDate().getDateTime() != null)
    {
      result.setStartDate(pFileType.getStartDate());
      result.getStartDate().setDateTime(getFilteredStringByDomain(pFileType.getStartDate().getDateTime()));
    }
    if (pFileType.getEndDate() != null && pFileType.getEndDate().getDateTime() != null)
    {
      result.setEndDate(pFileType.getEndDate());
      result.getEndDate().setDateTime(getFilteredStringByDomain(pFileType.getEndDate().getDateTime()));
    }
    result.setKeepRootElementPrefix(pFileType.getKeepRootElementPrefix());
    result.setIgnoreRootNamespace(pFileType.getIgnoreRootNamespace());
    result.setDefaultContent(pFileType.getDefaultContent());
    result.setHeader(pFileType.getHeader());
    result.setFooter(pFileType.getFooter());
    result.setCompressionFormat(pFileType.getCompressionFormat());
    result.setCompressionExtension(pFileType.getCompressionExtension());
    result.setJson(pFileType.getJson());
    _jsonArray = Boolean.TRUE.equals(pFileType.isJsonArray());
    return result;
  }

  /**
   * Filtre les properties d'un fileType
   * @param pFileType
   * @return r�sultat
   */
  private ProviderFileType filterProperties(FileType pFileType)
  {
    if (pFileType == null)
    {
      return null;
    }
    ProviderFileType result = new ProviderFileType();
    if (pFileType.getDir() != null)
    {
      result.setDir(getFilteredStringByDomain(pFileType.getDir()));
    }
    if (pFileType.getFileName() != null)
    {
      result.setFileName(getFilteredStringByDomain(pFileType.getFileName()));
      if ((_outputJson || _inputJson) && result.getFileName().endsWith(".xml"))
      {
        result.setFileName(result.getFileName().substring(0, result.getFileName().length() - 4) + ".json");
      }
    }
    if (pFileType.getFlagName() != null)
    {
      result.setFlagName(getFilteredStringByDomain(pFileType.getFlagName()));
    }
    result.setMd5(pFileType.getMd5() != null ? pFileType.getMd5() : false);
    result.setOverWrite(pFileType.getOverWrite() != null ? pFileType.getOverWrite() : true);
    result.setFailIfExists(pFileType.getFailIfExists() != null ? pFileType.getFailIfExists() : true);
    return result;
  }

  /** {@inheritDoc} */
  public boolean providerDefinitionUpdated()
  {
    if ("reject".equals(_currentProviderNameType))
    {
      // le test de changement de nom de fichier n'est restreint qu'on provider de type reject afin de pallier au probl�me de fermeture anticip�e
      // et al�atoire sur les providers de type response
      if (_currentOutputStreamFileName == null)
      {
        return true;
      }
      return !getCurrentProviderFileType().getFileName().equals(_currentOutputStreamFileName);
    }
    return false;
  }

  /** {@inheritDoc} */
  public boolean hasNextBufferedReader() throws ProviderException
  {
    return hasNextInputStream();
  }

  /**
   * mise a jour des providers du taskRun pour tra�abilit�
   * des fichiers utilis�s
   * @throws ProviderException exception
   */
  private void updateTaskRunProvider() throws ProviderException
  {
    if (_currentTaskRun != null)
    {
      PathFileProviderType pathFile = new PathFileProviderType();
      pathFile.setFileName(getFileProviderManager().getCurrentFileName());
      pathFile.setDir(getFileProviderManager().getFileDirectory());
      if ("input".equals(_currentProviderNameType))
      {
        _currentTaskRun.getInputProvider().getList().add(pathFile);
      }
      else if ("response".equals(_currentProviderNameType))
      {
        _currentTaskRun.getResponseProvider().getList().add(pathFile);
      }
      else if ("reject".equals(_currentProviderNameType))
      {
        _currentTaskRun.getRejectProvider().getList().add(pathFile);
      }
    }
  }

  /** {@inheritDoc} */
  public String getScenarioValue()
  {
    return _scenarioValue;
  }

  /** {@inheritDoc} */
  public void setReject(FileType pFileType)
  {
    if (_fileProviderManager != null)
    {
      _fileProviderManager.setReject(pFileType);
    }
  }

  /** {@inheritDoc} */
  public void disableArchive()
  {
    if (_fileProviderManager != null)
    {
      _fileProviderManager.disableArchive();
    }
  }

}
