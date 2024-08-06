package com.cylande.unitedretail.batch.provider.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.batch.tools.FileInUse;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.exception.FileManagementException;
import com.cylande.unitedretail.framework.tools.FilenameUtil;
import com.cylande.unitedretail.framework.tools.filemanagement.DirectoryFileManager;
import com.cylande.unitedretail.framework.tools.filemanagement.SortType;
import com.cylande.unitedretail.infrastructure.service.ArchiveTechnicalServiceDelegate;
import com.cylande.unitedretail.message.batch.CalendarFieldEnum;
import com.cylande.unitedretail.message.batch.DateFilterType;
import com.cylande.unitedretail.message.batch.FileSortEnum;
import com.cylande.unitedretail.message.batch.FileType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.common.archive.ArchiveToCompressType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;

public class FileProviderManager
{
  /** path du répertoire temporaire */
  private static String _temporyDirectoryName = null;
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(FileProviderManager.class);
  /** nom de la propriété renseignant le répertoire temporaire */
  private static final String TEMP_DIR_PROPERTY = "temporaryfile.dir";
  /** flux de sortie ciblé par le provider */
  protected OutputStream _outputStream = null;
  /** gestionnaire de fichier */
  protected DirectoryFileManager _directoryFileManager = null;
  /** définition du fichier provider */
  private ProviderFileType _providerFile = null;
  /** définition du fichier archive du provider */
  private FileType _archive = null;
  /** répertoire du fichier ciblé (lecture ou écriture) */
  private String _fileDirectory = null;
  /** pattern désignant une liste de fichiers, ou nom du fichier */
  private String _fileNamePattern = null;
  /** liste de noms de fichier désignés par le pattern */
  private List<String> _fileNameList = null;
  /** itérator sur la liste de noms de fichier */
  private Iterator<String> _fileNameIterator = null;
  /** nom du fichier couramment utilisé par le provider */
  private String _currentFileName = null;
  private FileType _reject = null;
  private boolean _disableArchive = false;

  /**
   * Construteur
   * @param pProviderFileType la définition du fichier provider
   * @throws FileManagementException exception
   * @throws ParseException exception
   */
  public FileProviderManager(ProviderFileType pProviderFileType) throws FileManagementException, ParseException
  {
    _providerFile = pProviderFileType;
    init();
  }

  public static void setTemporyDirectoryName(String pDir)
  {
    _temporyDirectoryName = pDir;
  }

  /**
   * initialisation du FileProviderManager
   * @throws FileManagementException exception
   * @throws ParseException exception
   */
  private void init() throws FileManagementException, ParseException
  {
    _fileDirectory = _providerFile.getDir();
    _fileDirectory = new FilenameUtil().addRelativePath(_fileDirectory);
    _fileNamePattern = _providerFile.getFileName();
    _directoryFileManager = new DirectoryFileManager(_fileDirectory);
    _directoryFileManager.setCacheDir(recoverCacheDirectory());
    _directoryFileManager.setUseMd5(_providerFile.getMd5());
    _directoryFileManager.setAscSort(Boolean.TRUE.equals(_providerFile.isDescSort()) ? false : true);
    _directoryFileManager.setSortType(getSortType(_providerFile.getSortType()));
    _directoryFileManager.setLastModifiedStartTime(getTime(_providerFile.getStartDate()));
    _directoryFileManager.setLastModifiedEndTime(getTime(_providerFile.getEndDate()));
    initIterator();
  }

  private Long getTime(DateFilterType pFilter) throws ParseException
  {
    Long result = null;
    if (pFilter != null)
    {
      SimpleDateFormat sdf = new SimpleDateFormat(pFilter.getPattern());
      Calendar cal = Calendar.getInstance();
      cal.setTime(sdf.parse(pFilter.getDateTime()));
      if (pFilter.getAdd() != null && pFilter.getUnit() != null)
      {
        cal.add(getCalendarField(pFilter.getUnit()), pFilter.getAdd());
      }
      result = cal.getTimeInMillis();
    }
    return result;
  }

  private int getCalendarField(CalendarFieldEnum pEnum)
  {
    int result = -1;
    switch (pEnum)
    {
      case YEAR:
        result = Calendar.YEAR;
        break;
      case MONTH:
        result = Calendar.MONTH;
        break;
      case DAY:
        result = Calendar.DAY_OF_MONTH;
        break;
      case HOUR:
        result = Calendar.HOUR_OF_DAY;
        break;
      case MINUTE:
        result = Calendar.MINUTE;
        break;
    }
    return result;
  }

  private SortType getSortType(FileSortEnum pEnum)
  {
    SortType result = null;
    if (pEnum != null)
    {
      switch (pEnum)
      {
        case NAME:
          result = SortType.NAME;
          break;
        case NAMENOSENS:
          result = SortType.NAMENOSENS;
          break;
        case MODIFTIME:
          result = SortType.MODIFTIME;
          break;
      }
    }
    return result;
  }

  /**
   * renvoie le path du fichier temporaire
   * @return path du fichier temporaire
   */
  private String recoverCacheDirectory()
  {
    String result;
    if (_temporyDirectoryName == null)
    {
      try
      {
        result = ConfigEngineProperties.getInstance().getDirectoryEngineProperties(TEMP_DIR_PROPERTY);
        if (result == null)
        {
          LOGGER.warn("impossible de récupérer l'emplacement spécifié pour les fichiers temporaires");
          result = new FilenameUtil().addRelativePath("/");
        }
      }
      catch (ConfigEnginePropertiesException e)
      {
        LOGGER.warn("impossible de récupérer l'emplacement spécifié pour les fichiers temporaires");
        result = new FilenameUtil().addRelativePath("/");
      }
    }
    else
    {
      result = _temporyDirectoryName;
    }
    return result;
  }

  /**
   * Initialisation de l'iterateur de fichier
   */
  private void initIterator()
  {
    _fileNameList = _directoryFileManager.listFiles(_fileNamePattern);
    if (_fileNameList != null && getFileQuantity() > 0)
    {
      _fileNameIterator = _fileNameList.iterator();
    }
  }

  /**
   * indique combien de fichiers trouvés
   * @return résultat
   */
  public int getFileQuantity()
  {
    if (_fileNameList != null)
    {
      return _fileNameList.size();
    }
    return 0;
  }

  /**
   * fournit un inputstream sur le fichier courant visé par l'itérateur de fichier
   * @return résultat
   * @throws ProviderException exception
   */
  public InputStream createInputStream() throws ProviderException
  {
    InputStream inputStream = null;
    try
    {
      if (hasNextFile())
      {
        _currentFileName = _fileNameIterator.next();
        // Flag le fichier comme en cours d'utilisation
        // Sequential activé par défaut pour corriger un problème d'écriture des
        // paquets en rejet dans un mauvais fichier.
        if (_providerFile.getSequential() == null || _providerFile.isSequential())
        {
          FileInUse.registerFileInUse(_currentFileName);
        }
        if (_providerFile.getJson() != null)
        {
          File currentFile = new File(_fileDirectory + "/" + _currentFileName);
          File tmp = new File(_fileDirectory + "/" + _currentFileName + ".tmp");
          FileUtils.writeStringToFile(tmp, "{\"" + _providerFile.getJson().getRootElement() + "\":{\"" + _providerFile.getJson().getNodeName() + "\":", "UTF-8");
          FileUtils.write(tmp, FileUtils.readFileToString(currentFile, "UTF-8"), "UTF-8", true);
          FileUtils.write(tmp, "}}", "UTF-8", true);
          currentFile.delete();
          tmp.renameTo(currentFile);
        }
        inputStream = _directoryFileManager.getInputStream(_currentFileName);
      }
      return inputStream;
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDERMANAGER_GETINPUTSTREAM_ERROR, new Object[] { _currentFileName, _fileDirectory }, e);
    }
  }

  /**
   * Réinitialise et retourne le flux entrant courant.
   * @return InputStream
   * @throws ProviderException Erreur de provider
   */
  public InputStream reloadInputStream() throws ProviderException
  {
    InputStream inputStream = null;
    try
    {
      if (_currentFileName != null)
      {
        // Fermeture du flux courant.
        _directoryFileManager.closeInputStream(_currentFileName);
        // Rechargement du flux courant.
        inputStream = _directoryFileManager.getInputStream(_currentFileName);
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDERMANAGER_GETINPUTSTREAM_ERROR, new Object[] { _currentFileName, _fileDirectory }, e);
    }
    return inputStream;
  }

  /**
   * indique s'il y a (encore) des fichiers dans le fileProviderManager
   * @return résultat
   */
  public boolean hasNextFile()
  {
    if (_fileNameIterator == null)
    {
      return false;
    }
    return _fileNameIterator.hasNext();
  }

  /**
   * ferme le inputstream du fichier courant
   * @throws ProviderException exception
   */
  public void closeCurrentInputStream() throws ProviderException
  {
    try
    {
      if (_currentFileName != null)
      {
        _directoryFileManager.closeInputStream(_currentFileName);
        // Sequential activé par défaut pour corriger un problème d'écriture des
        // paquets en rejet dans un mauvais fichier.
        if (_providerFile.getSequential() == null || _providerFile.isSequential())
        {
          // Attente de liberation des tasks utilisant le fichier
          FileInUse fileInUse = FileInUse.getFileInUse(_currentFileName);
          fileInUse.waitForFree();
          FileInUse.removeFileInUse(_currentFileName);
        }
        if (_reject != null)
        {
          moveFile(_reject);
        }
        else if (!_disableArchive)
        {
          moveFile(_archive);
        }
        _reject = null;
        _currentFileName = null;
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDERMANAGER_CLOSECURRENTINPUTSTREAM_ERROR, new Object[] { _currentFileName, _fileDirectory }, e);
    }
  }

  /**
   * ferme le inputstream courant et réinitialise l'itérateur de fichier
   * @throws ProviderException exception
   */
  public void closeInputStream() throws ProviderException
  {
    if (_currentFileName != null)
    {
      closeCurrentInputStream();
      initIterator();
    }
  }

  /**
   * ouvre un outputstream sur le premier fichier visé par l'iterateur de fichier
   * @param pOverride
   * @return résultat
   * @throws ProviderException exception
   */
  public OutputStream createOutputStream(boolean pOverride) throws ProviderException
  {
    try
    {
      _currentFileName = _fileNamePattern;
      checkSyntax(_currentFileName);
      return _directoryFileManager.getOutputStream(_currentFileName, pOverride);
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDERMANAGER_GETOUTPUTSTREAM_ERROR, new Object[] { _currentFileName, _fileDirectory }, e);
    }
  }

  /**
   * ferme le outputstream ouvert sur le fichier courant
   * @throws ProviderException exception
   */
  public void closeOutputStream() throws ProviderException
  {
    try
    {
      if (_currentFileName != null)
      {
        _directoryFileManager.closeOutputStream(_currentFileName);
        if (_providerFile.getCompressionFormat() != null)
        {
          ArchiveTechnicalServiceDelegate serv = new ArchiveTechnicalServiceDelegate();
          ArchiveToCompressType compress = new ArchiveToCompressType();
          compress.setDir(_fileDirectory + "/" + _currentFileName);
          compress.setFormat(_providerFile.getCompressionFormat());
          compress.setExtension(_providerFile.getCompressionExtension());
          serv.compress(compress, null, ContextTransformer.fromLocale());
        }
        moveFile(_archive);
        initIterator();
      }
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDERMANAGER_CLOSEOUTPUTSTREAM_ERROR, new Object[] { _currentFileName, _fileDirectory }, e);
    }
  }

  /**
   * Déplace un fichier
   * @throws ProviderException exception
   */
  private void moveFile(FileType pFileType) throws ProviderException, FileManagementException
  {
    String targetDir;
    String targetFileName;
    if (pFileType != null)
    {
      // contrôle si utilisation de jokers
      targetDir = new FilenameUtil().addRelativePath(pFileType.getDir());
      checkSyntax(targetDir);
      targetFileName = pFileType.getFileName();
      checkSyntax(targetFileName);
      LOGGER.debug("moveFile " + _currentFileName + " -> " + targetDir + "/" + targetFileName);
      _directoryFileManager.moveFile(_currentFileName, targetDir, targetFileName, pFileType.getOverWrite(), pFileType.getFailIfExists());
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
   * retourne le nom du fichier courant
   * @return résultat
   */
  public String getCurrentFileName()
  {
    return _currentFileName;
  }

  /**
   * retourne le nom du fichier courant sans l'extension de fichier
   * @return résultat
   */
  public String getCurrentFileNameNoExt()
  {
    return BatchUtil.getFileNameNoExt(_currentFileName);
  }

  /**
   * mutator sur la définition de l'archive
   * @param pArchive
   */
  public void setArchive(FileType pArchive)
  {
    _archive = pArchive;
  }

  public List<String> getFileNameList()
  {
    return _fileNameList;
  }

  public String getFileDirectory()
  {
    return _fileDirectory;
  }

  public FileType getReject()
  {
    return _reject;
  }

  public void setReject(FileType pFileType)
  {
    _reject = pFileType;
  }

  public void disableArchive()
  {
    _disableArchive = true;
  }
}
