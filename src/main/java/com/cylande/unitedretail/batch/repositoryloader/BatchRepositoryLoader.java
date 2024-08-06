package com.cylande.unitedretail.batch.repositoryloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.repository.BatchPropertiesRepository;
import com.cylande.unitedretail.batch.service.BatchManagerServiceImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.security.Crypto;
import com.cylande.unitedretail.framework.util.StringUtil;
import com.cylande.unitedretail.message.batch.BatchEnum;
import com.cylande.unitedretail.message.batch.BatchType;
import com.cylande.unitedretail.message.batch.Batchs;
import com.cylande.unitedretail.message.batch.InitBatchType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.engineproperties.PropertyListType;
import com.cylande.unitedretail.message.engineproperties.PropertyType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;
import com.cylande.unitedretail.process.tools.Property;

/**
 * Load Batchs and Populate it in BatchRepository
 */
public class BatchRepositoryLoader extends AbstractRepositoryLoader
{
  public static final String INIT_BATCH_NAME = "InitBatch";
  public static final String WAIT_INIT_BATCH_END_PROPERTY_NAME = "WaitInitBatchEnd";
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(BatchRepositoryLoader.class);
  /** nom de la propriété ciblant le fichier de définitions de providers à charger */
  private static final String BATCH_PROPERTY_NAME = "batch.dir";
  private static final Boolean INIT_BATCH_ACTIVE;
  /** l'ensemble des définitions de batchs */
  private Batchs _batchs = null;
  private File _mainBatchFile = null;
  private List<Password> _passwordList = null;
  /** gestionnaire de définitions de batchs */
  private BatchManagerServiceImpl _batchManager = null;
  static
  {
    boolean initBatchActive = true;
    String param;
    try
    {
      param = ConfigEngineProperties.getInstance().getEngineProperty("initBatch.active");
      initBatchActive = param == null || param.equals("") ? true : Boolean.valueOf(param);
    }
    catch (ConfigEnginePropertiesException e)
    {
      LOGGER.error(e, e);
    }
    finally
    {
      INIT_BATCH_ACTIVE = initBatchActive;
    }
  }

  /**
   * Default Constructor : orchestraction of action (load and populate)
   * @throws ConfigEnginePropertiesException exception
   */
  public BatchRepositoryLoader() throws ConfigEnginePropertiesException
  {
    super();
    _batchManager = new BatchManagerServiceImpl();
    _repoPropertyName = BATCH_PROPERTY_NAME;
    loadProperties();
  }

  /**
   * Retourne la liste des chaines de caractère existant dans le flux situées entre les deux tags indiqués
   * Rque : les éventuels retours chariots sont supprimés.
   * @param pReader le lecteur du flux source
   * @param pStartTag le tag de début
   * @param pEndTag le tag de fin
   * @return la liste des chaines.
   */
  private List<String> extractStrings(BufferedReader pReader, String pStartTag, String pEndTag)
  {
    List<String> result = new ArrayList();
    try
    {
      String line = pReader.readLine();
      int startTagLength = pStartTag.length();
      int endTagLength = pEndTag.length();
      int startPos;
      int endPos;
      StringBuilder sb = null;
      while (line != null)
      {
        startPos = line.indexOf(pStartTag);
        if (startPos >= 0)
        {
          sb = new StringBuilder();
          endPos = line.indexOf(pEndTag, startPos + startTagLength);
          if (endPos >= 0)
          {
            sb.append(line.substring(startPos + startTagLength, endPos));
            result.add(sb.toString().trim());
            line = line.substring(endPos + endTagLength);
            sb = null;
          }
          else
          {
            sb.append(line.substring(startPos + startTagLength, line.length()));
            line = pReader.readLine();
          }
        }
        else
        {
          if (sb != null)
          {
            endPos = line.indexOf(pEndTag);
            if (endPos >= 0)
            {
              sb.append(line.substring(0, endPos));
              result.add(sb.toString().trim());
              sb = null;
              line = line.substring(endPos + endTagLength);
            }
            else
            {
              sb.append(line);
              line = pReader.readLine();
            }
          }
          else
          {
            line = pReader.readLine();
          }
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
    return result;
  }

  /**
   * Retourne la liste des fichiers de définition de batch
   * @param pMainBatchFile : le fichier de définition de batch principal.
   * @return la liste des fichiers de définition.
   */
  private List<File> getBatchDefFiles(File pMainBatchFile)
  {
    List<File> result = new ArrayList();
    result.add(pMainBatchFile);
    //Extraire les sous-fichiers
    BufferedReader bfReader;
    File tmpFile;
    try
    {
      bfReader = new BufferedReader(new FileReader(pMainBatchFile));
      List<String> entityStrings = extractStrings(bfReader, "<!ENTITY", ">");
      Iterator<String> it = entityStrings.iterator();
      String entityStr;
      int pos;
      while (it.hasNext())
      {
        entityStr = it.next();
        pos = entityStr.indexOf("SYSTEM");
        if (pos >= 0)
        {
          entityStr = entityStr.substring(pos + 6, entityStr.length());
        }
        pos = entityStr.indexOf("NDATA");
        if (pos >= 0)
        {
          entityStr = entityStr.substring(0, pos);
        }
        pos = entityStr.indexOf('\"');
        if (pos >= 0)
        {
          entityStr = entityStr.substring(pos + 1, entityStr.length());
          pos = entityStr.indexOf('\"');
          if (pos >= 0)
          {
            entityStr = entityStr.substring(0, pos);
          }
        }
        tmpFile = new File(pMainBatchFile.getParent(), entityStr);
        if (tmpFile.exists())
        {
          result.add(tmpFile);
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
    return result;
  }

  /**
   * Load Batchs File and convert it in Batchs Object
   * @param pFileName : path of the Batchs File
   */
  public void load(String pFileName)
  {
    LOGGER.debug("Lecture des descriptions de batchs");
    if (pFileName != null && !pFileName.equals(""))
    {
      if (fileExists(pFileName))
      {
        _batchs = new Batchs();
        _batchs = (Batchs)_manager.read(pFileName, _batchs);
        _mainBatchFile = new File(pFileName);
        _passwordList = new ArrayList();
      }
    }
    if (_batchs == null)
    {
      LOGGER.warn("Aucun batch n'a été défini");
      _batchs = new Batchs();
    }
    LOGGER.info(_batchs.getBatch().size() + " batchs ont été préchargés");
  }

  /**
   * Populate Batchs Object in BatchRepository thanks to BatchManager (contains Service CRUD of Repository)
   * @throws Exception exception
   */
  public void populate() throws BatchException
  {
    if (_batchs == null)
    {
      throw new BatchException(BatchErrorDetail.BATCH_REPO_NOLIST);
    }
    LOGGER.debug("Alimentation de la repository de batchs");
    populateBatch();
    populateBatchProperties();
    updatePasswordsInFiles();
  }

  /**
   * indicate if text value is a property
   * @param value the text
   * @return true or false
   */
  private boolean isProperty(String pValue)
  {
    return ((pValue != null) && pValue.trim().startsWith("${") && pValue.trim().endsWith("}"));
  }

  /**
   * String replacement method
   * @param pSource the source string in wich to replace
   * @param pSearchedSubString the serched subString
   * @param pReplacedSubString the sting to replace the substring occurences
   * @return résultat
   */
  private static String replaceInString(String pSource, String pSearchedSubString, String pReplacedSubString)
  {
    StringBuilder result = new StringBuilder();
    int pos = pSource.indexOf(pSearchedSubString);
    int searchedLength = pSearchedSubString.length();
    int copyPos = 0;
    while (pos >= 0)
    {
      result.append(pSource.substring(copyPos, pos));
      result.append(pReplacedSubString);
      copyPos = pos + searchedLength;
      pos = pSource.indexOf(pSearchedSubString, copyPos);
    }
    result.append(pSource.substring(copyPos, pSource.length()));
    return result.toString();
  }

  /**
   * Cryptes le mot de passe et met a jour la source
   * @param pwd : le mot de passe en clair.
   * @return le mot de passe crypté.
   */
  private String cryptPass(String pWd)
  {
    String sCrypted = "";
    try
    {
      sCrypted = Crypto.cryptInfo(StringUtil.extractClearPassword(pWd));
      _passwordList.add(new Password(pWd, sCrypted));
    }
    catch (Exception e)
    {
      LOGGER.debug(e, e);
    }
    return sCrypted;
  }

  /**
   * Met a jour les fichiers avec les mots de passe cryptés.
   */
  private void updatePasswordsInFiles()
  {
    if (_passwordList != null && !_passwordList.isEmpty())
    {
      List<File> batchFiles = getBatchDefFiles(_mainBatchFile);
      if (!batchFiles.isEmpty())
      {
        Iterator<File> batchFilesIterator = batchFiles.iterator();
        File definitionFile;
        try
        {
          while (batchFilesIterator.hasNext())
          {
            definitionFile = batchFilesIterator.next();
            File tmpFile = new File(definitionFile.getAbsolutePath() + ".tmp");
            updatePasswordsInFile(definitionFile, tmpFile);
            if (definitionFile.delete())
            {
              tmpFile.renameTo(definitionFile);
            }
            else
            {
              tmpFile.delete();
            }
          }
        }
        catch (Throwable t)
        {
          LOGGER.debug(t, t);
        }
      }
    }
  }

  private void updatePasswordsInFile(File pDefinitionFile, File pTmpFile) throws FileNotFoundException, IOException
  {
    Iterator<Password> pwdIterator;
    Password pwd;
    BufferedReader bfReader = null;
    BufferedWriter bfWriter = null;
    try
    {
      bfReader = new BufferedReader(new FileReader(pDefinitionFile));
      pTmpFile.delete();
      bfWriter = new BufferedWriter(new FileWriter(pTmpFile));
      String line = bfReader.readLine();
      while (line != null)
      {
        pwdIterator = _passwordList.iterator();
        while (pwdIterator.hasNext())
        {
          pwd = pwdIterator.next();
          line = replaceInString(line, pwd._clear, pwd._crypted);
        }
        bfWriter.write(line);
        line = bfReader.readLine();
        if (line != null)
        {
          bfWriter.newLine();
        }
      }
      bfWriter.flush();
    }
    finally
    {
      try
      {
        if (bfReader != null)
        {
          bfReader.close();
        }
      }
      finally
      {
        if (bfWriter != null)
        {
          bfWriter.close();
        }
      }
    }
  }

  private void treatUser(BatchType pBeanBatch, PropertyListType pPropertyList)
  {
    if (pBeanBatch.getUser() != null)
    {
      String password = pBeanBatch.getUser().getPassword();
      if (isProperty(password))
      {
        String sPropName = password.substring(2, password.length() - 1);
        if ((pPropertyList != null) && (pPropertyList.getProperty() != null))
        {
          // on itères sur toutes les propriétés pour traiter tous les domaines
          Iterator<PropertyType> propIterator = pPropertyList.getProperty().iterator();
          PropertyType prop;
          while (propIterator.hasNext())
          {
            prop = propIterator.next();
            if (prop.getName().equals(sPropName))
            {
              password = prop.getValue();
              if (StringUtil.isClearPassword(password))
              {
                // le mot de passe est en clair : il faut le crypter.
                prop.setValue(cryptPass(password));
              }
            }
          }
        }
      }
      else if (StringUtil.isClearPassword(password))
      {
        // le mot de passe est en clair : il faut le crypter.
        pBeanBatch.getUser().setPassword(cryptPass(password));
      }
    }
  }

  /**
   * Peuple le référentiel des définitions de batch.
   * effectue le cryptage automatique des mots de passes.
   * @throws BatchException exception
   */
  private void populateBatch() throws BatchException
  {
    try
    {
      ContextType context = ContextTransformer.fromLocale();
      PropertyListType propertyList = _batchs.getProperties();
      // chargement de la définition du batch d'initialisation
      InitBatchType initBatchDef = _batchs.getInitBatch();
      if (initBatchDef != null)
      {
        boolean active = initBatchDef.getActive() != null ? initBatchDef.getActive() : INIT_BATCH_ACTIVE;
        if (active)
        {
          // on force le nom du batch d'init.
          BatchType initBatch = new BatchType();
          initBatch.setType(BatchEnum.STATEFULL);
          initBatch.setName(BatchRepositoryLoader.INIT_BATCH_NAME);
          initBatch.setDescription(initBatchDef.getDescription());
          initBatch.setUser(initBatchDef.getUser());
          initBatch.setFork(initBatchDef.getFork());
          initBatch.setSequence(initBatchDef.getSequence());
          treatUser(initBatch, propertyList);
          _batchManager.createBatch(initBatch, null, context);
        }
      }
      // chargement de la définition des batchs
      for (BatchType bean: _batchs.getBatch())
      {
        treatUser(bean, propertyList);
        _batchManager.createBatch(bean, null, context);
      }
    }
    catch (Exception e)
    {
      throw new BatchException(BatchErrorDetail.BATCH_REPO_POPULATE, e);
    }
  }

  /**
   * Peuple le référentiel des propriétés de batch
   */
  private void populateBatchProperties()
  {
    PropertyListType propertyList = _batchs.getProperties();
    if (propertyList == null)
    {
      LOGGER.debug("Pas de propriété de batch à enregistrer");
    }
    else
    {
      BatchPropertiesRepository.getInstance().putPropertyList(propertyList);
      LOGGER.debug("BatchPropertiesRepository possède " + BatchPropertiesRepository.getInstance().getSize() + " propriétés");
    }
    InitBatchType initBatchDef = _batchs.getInitBatch();
    // la valeur par défaut des attributs 'Active' et 'WaitEnd' est 'true'
    // on testes !Boolean.FALSE.equals(la valeur) pour traiter le cas non défini (null).
    if ((initBatchDef != null) && (!Boolean.FALSE.equals(initBatchDef.getActive())))
    {
      com.cylande.unitedretail.message.engineproperties.PropertyType waitEndProperty = new PropertyType();
      if (!Boolean.FALSE.equals(_batchs.getInitBatch().getWaitEnd()))
      {
        waitEndProperty.setValue("true");
      }
      else
      {
        waitEndProperty.setValue("false");
      }
      BatchPropertiesRepository.getInstance().putProperty(WAIT_INIT_BATCH_END_PROPERTY_NAME, new Property(waitEndProperty));
    }
  }

  /**
   * Password class for internal use.
   */
  private static class Password
  {
    private String _clear;
    private String _crypted;

    /**
     * Contstructor
     * @param pClear the uncrypted password
     * @param pCrypted the crypted password
     */
    private Password(String pClear, String pCrypted)
    {
      _clear = pClear;
      _crypted = pCrypted;
    }
  }
}
