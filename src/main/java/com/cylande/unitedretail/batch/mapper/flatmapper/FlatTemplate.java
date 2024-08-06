package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.FlatMapperException;
import com.cylande.unitedretail.framework.cache.URCache;
import com.cylande.unitedretail.framework.cache.URCacheAccess;
import com.cylande.unitedretail.framework.cache.URCacheAccessImpl;
import com.cylande.unitedretail.framework.cache.URCacheImpl;
import com.cylande.unitedretail.framework.cache.exceptions.URCacheException;
import com.cylande.unitedretail.framework.exception.FrameworkErrorDetail;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.framework.tools.FilenameUtil;

/**
 * Template de fichier plat
 */
public final class FlatTemplate
{
  public static final String ROOT_REGION_NAME = "ROOT-REGION";
  private static final String REGION_NAME = "FlatTemplate";
  private static final Logger LOGGER = Logger.getLogger(FlatTemplate.class);
  private String _separator = "|";
  private String _transcoSeparator = ";";
  private String _headerElement = null;
  private String _lineHeader = null;
  private Boolean _header = true;
  private String _lineSeparator = "\n";
  private String _fieldPrefix = null;
  private String _fieldSuffix = null;
  private String _fieldPrefixWithoutData = null;
  private String _fieldSuffixWithoutData = null;
  private Boolean _noSeparator = false;
  private Map<String, AbstractFlatRow> _hDescriptors;

  /**
   * Constructeur du template
   */
  private FlatTemplate()
  {
    _hDescriptors = new HashMap<String, AbstractFlatRow>();
  }

  /**
   * Charge le template en m�moire � partir du fichier
   * @param pRootElement
   * @param pPath
   * @throws IOException exception
   */
  public void loadTemplate(String pRootElement, String pPath) throws IOException
  {
    BufferedReader reader = null;
    try
    {
      String absolutePath = new FilenameUtil().addRelativePath(pPath);
      reader = new BufferedReader(new FileReader(absolutePath));
      String line = null;
      boolean inTemplate = false;
      String separatorEscape = Pattern.quote(_separator);
      while ((line = reader.readLine()) != null)
      {
        if (inTemplate)
        {
          if (line.length() > 0)
          {
            AbstractFlatRow desc = new FlatRowTemplate(line, this);
            // Pour le mode sans en-t�te, on va sauvegarder l'en-t�te de la 2nd ligne (la plupart du temps : "values")
            if (!_header)
            {
              _lineHeader = desc.getRootElement();
            }
            _hDescriptors.put(desc.getDepth() + _separator + desc.getRootElement(), desc);
          }
          else
          {
            break;
          }
        }
        if (line.startsWith("separator:"))
        {
          // On r�cup�re le s�parateur s'il est d�fini, par d�faut ce sera |
          _separator = line.replaceFirst("separator:", "");
          separatorEscape = Pattern.quote(_separator);
        }
        else if (line.startsWith("lineSeparator:"))
        {
          _lineSeparator = getLineSeparator(line.replaceFirst("lineSeparator:", ""));
        }
        else if (line.startsWith("transcoSeparator:"))
        {
          // On r�cup�re le s�parateur des transco
          _transcoSeparator = line.replaceFirst("transcoSeparator:", "");
        }
        else if (line.startsWith("header:"))
        {
          // Est-ce que le fichier de donn�es aura son en-t�te (par d�faut oui)
          _header = Boolean.valueOf(line.replaceFirst("header:", ""));
        }
        else if (line.startsWith("fieldPrefix:"))
        {
          _fieldPrefix = line.replaceFirst("fieldPrefix:", "");
        }
        else if (line.startsWith("fieldSuffix:"))
        {
          _fieldSuffix = line.replaceFirst("fieldSuffix:", "");
        }
        else if (line.startsWith("fieldPrefixWithoutData:"))
        {
          _fieldPrefixWithoutData = line.replaceFirst("fieldPrefixWithoutData:", "");
        }
        else if (line.startsWith("fieldSuffixWithoutData:"))
        {
          _fieldSuffixWithoutData = line.replaceFirst("fieldSuffixWithoutData:", "");
        }
        else if (line.equals("noSeparator"))
        {
          // pas de s�parateur dans le fichier de sortie
          _noSeparator = true;
        }
        // On r�cup�re la premi�re ligne du template si on a pas d'en-t�te (ex : 0;couponType;)
        if (!_header && line.matches("^0" + separatorEscape + ".*$"))
        {
          inTemplate = true;
          AbstractFlatRow desc = new FlatRowTemplate(line, this);
          _hDescriptors.put(desc.getDepth() + _separator + desc.getRootElement(), desc);
          _headerElement = desc.getRootElement();
          continue;
        }
        if (line.matches("^0 *" + separatorEscape + "(ns\\d+:)*" + pRootElement + ".*$"))
        {
          inTemplate = true;
          AbstractFlatRow desc = new FlatRowTemplate(line, this);
          _hDescriptors.put(desc.getDepth() + _separator + desc.getRootElement(), desc);
          continue;
        }
      }
    }
    finally
    {
      if (reader != null)
      {
        reader.close();
      }
    }
  }

  private String getLineSeparator(String pSeparator)
  {
    if (_lineSeparator.equals("\\r\\n"))
    {
      return "\r\n";
    }
    if (_lineSeparator.equals("\\r"))
    {
      return "\r";
    }
    return "\n";
  }

  /**
   * Retourne une instance du template
   * @param pRootElement
   * @param pPath
   * @return r�sultat
   * @throws TechnicalServiceException exception
   */
  public static FlatTemplate getInstance(String pRootElement, String pPath) throws TechnicalServiceException
  {
    FlatTemplate template = getInCache(pRootElement, pPath);
    if (template == null) // On a pas trouv� le template
    {
      throw new FlatMapperException(BatchErrorDetail.FLATMAPPER_TEMPLATE_NOT_FIND, new Object[] { pRootElement });
    }
    return template;
  }

  /**
   * Purge le cache UR contenant les templates
   * @throws TechnicalServiceException exception
   */
  public static void purgeCache() throws TechnicalServiceException
  {
    URCache cache = null;
    URCacheAccess cacc = null;
    try
    {
      cache = URCacheImpl.getInstance();
      if (cache.isInitialized())
      {
        cacc = new URCacheAccessImpl();
        if (cacc.exist(ROOT_REGION_NAME + "/" + REGION_NAME))
        {
          cacc.destroyObjectsFromRegion(ROOT_REGION_NAME + "/" + REGION_NAME);
        }
      }
    }
    catch (URCacheException e)
    {
      throw new TechnicalServiceException(FrameworkErrorDetail.EXEC_SERVICE_CACHE_PROXY, new Object[] { ROOT_REGION_NAME + "/" + REGION_NAME + " - purgeCache" }, e);
    }
    finally
    {
      if (cacc != null)
      {
        cacc.close();
      }
    }
  }

  /**
   * Retourne une instance du template en cache
   * @param pElement
   * @param pPath
   * @return r�sultat
   * @throws TechnicalServiceException exception
   */
  private static FlatTemplate getInCache(String pElement, String pPath) throws TechnicalServiceException
  {
    URCache cache = null;
    URCacheAccess cacc = null;
    FlatTemplate template = null;
    String cacheKey = null;
    if (pElement == null)
    {
      pElement = "null";
    }
    try
    {
      cacheKey = pElement + pPath;
      cache = URCacheImpl.getInstance();
      if (cache.isInitialized())
      {
        cacc = new URCacheAccessImpl();
        cacc.getAccess(ROOT_REGION_NAME);
        // Si la r�gion du service en question n'est pas d�fini dans le fichier de config du cache, alors on le cr�e dynamiquement, dans ce cas elle
        // h�rite les attributs par d�faut de la r�gion parent
        if (!cacc.exist(ROOT_REGION_NAME + "/" + REGION_NAME))
        {
          LOGGER.warn("Cr�ation dynamique de la r�gion : " + REGION_NAME + "...");
          cacc.createSubRegion(REGION_NAME); // cr�e une sous-region � partir du contexte courant, c-�-d la r�gion racine
          cacc.getAccessSubRegion(REGION_NAME); // on acc�de la r�gion service cr�ee
        }
        cacc.getAccess(ROOT_REGION_NAME);
        cacc.getAccessSubRegion(REGION_NAME);
        if (cacc.isPresent(cacheKey))
        {
          template = (FlatTemplate)cacc.readFromCache(cacheKey);
          return template;
        }
        else
        {
          try
          {
            template = (FlatTemplate)cacc.readFromCache(cacheKey);
          }
          catch (URCacheException pException)
          {
            // Objet non trouv� dans d'autres caches distribu�s...
            LOGGER.info("Objet non trouv� ni dans le cache local ni dans un cache distribu�...");
            template = new FlatTemplate();
            try
            {
              template.loadTemplate(pElement, pPath);
            }
            catch (IOException pIOException)
            {
              LOGGER.error("Impossible de charger le template : " + pPath);
              return null;
            }
            cacc.putInCache(cacheKey, template);
            return template;
          }
          return template;
        }
      }
      else
      {
        LOGGER.error("Cache non initialis�");
        return null;
      }
    }
    catch (URCacheException pException)
    {
      // handle exception
      LOGGER.debug("error : " + pException);
      // lancer une exception
      throw new TechnicalServiceException(FrameworkErrorDetail.EXEC_SERVICE_CACHE_PROXY, new Object[] { "getTemplate" }, pException);
    }
    finally
    {
      if (cacc != null)
      {
        cacc.close();
      }
    }
  }

  /**
   * Retourne le descripteur de la ligne dans le template pour les r�gles de transformation
   * @param pRow
   * @return r�sultat
   */
  public AbstractFlatRow getRowDescriptor(AbstractFlatRow pRow)
  {
    return getRowDescriptor(pRow.getDepth(), pRow.getRootElement());
  }

  /**
   * Retourne le descripteur de la ligne dans le template pour les r�gles de transformation
   * @param pDepth
   * @param pRootElement
   * @return r�sultat
   */
  public AbstractFlatRow getRowDescriptor(int pDepth, String pRootElement)
  {
    return _hDescriptors.get(pDepth + _separator + pRootElement);
  }

  public void setSeparator(String pSeparator)
  {
    _separator = pSeparator;
  }

  public String getSeparator()
  {
    return _separator;
  }

  public String getTranscoSeparator()
  {
    return _transcoSeparator;
  }

  public String getHeaderElement()
  {
    return _headerElement;
  }

  public String getLineHeader()
  {
    return _lineHeader;
  }

  public Boolean getHeader()
  {
    return _header;
  }

  public String getLineSeparator()
  {
    return _lineSeparator;
  }

  public String getFieldPrefix()
  {
    return _fieldPrefix;
  }

  public String getFieldSuffix()
  {
    return _fieldSuffix;
  }

  public String getFieldPrefixWithoutData()
  {
    return _fieldPrefixWithoutData;
  }

  public String getFieldSuffixWithoutData()
  {
    return _fieldSuffixWithoutData;
  }

  public boolean getNoSeparator()
  {
    return _noSeparator;
  }
}
