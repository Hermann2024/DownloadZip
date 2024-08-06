package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.FlatMapperTechnicalServiceException;
import com.cylande.unitedretail.batch.exception.FlatMapperXmlStreamException;
import com.cylande.unitedretail.batch.exception.StreamMapperException;
import com.cylande.unitedretail.batch.mapper.EngineReader;
import com.cylande.unitedretail.framework.AbstractRuntimeException;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.message.batch.MapperLineCriteriaType;
import com.cylande.unitedretail.message.batch.MapperLineFilterType;
import com.cylande.unitedretail.process.exception.ConfigEnginePropertiesException;
import com.cylande.unitedretail.process.tools.ConfigEngineProperties;

/**
 * Reader utilisé par le provider de lecture en tache d'intégration
 */
public class MapperReaderImpl extends EngineReader
{
  /**
   * Utilisé pour le nom du fichier de debug
   */
  public static final String DATE_FORMAT = "ddMMyyyyHHmm";
  /**
   * Logger
   */
  private static final Logger LOGGER = Logger.getLogger(MapperReaderImpl.class);
  /**
   * Permet de savoir si on est en fin de lecture
   */
  private boolean _lastRead = false;
  /**
   * Handler de fichier de debug XML
   */
  private FileWriter _xmlOut;
  /**
   * Chemin du fichier de template
   */
  private String _templateFile;
  /**
   * Transformer de flux plat vers XML
   */
  private FlatTransformerIn _transformer;
  private String _bufferMemAddr = null;
  private List<MapperLineCriteriaType> _excludeList = new ArrayList();
  private List<MapperLineCriteriaType> _includeList = new ArrayList();
  private boolean _csvFormat = false;
  private Iterator<CSVRecord> _iteratorCSV = null;

  /**
   * Constructeur
   * @param pStream flux d'entrée fichier plat
   * @param pTemplateFile path du template
   * @param pCharset
   * @param pList
   * @throws StreamMapperException exception
   * @throws UnsupportedEncodingException exception
   * @throws IOException exception
   */
  public MapperReaderImpl(InputStream pStream, String pTemplateFile, String pCharset, MapperLineFilterType pFilter) throws StreamMapperException, UnsupportedEncodingException, IOException
  {
    super(pStream, pCharset);
    _templateFile = pTemplateFile;
    if (pFilter != null)
    {
      _csvFormat = Boolean.TRUE.equals(pFilter.isCsvFormat());
      _excludeList.addAll(pFilter.getExcludes());
      _includeList.addAll(pFilter.getIncludes());
    }
    try
    {
      if (LOGGER.isDebugEnabled() && _templateFile != null)
      {
        String scriptFilePath = ConfigEngineProperties.getInstance().getDirectoryEngineProperties("temporaryfile.dir");
        if (scriptFilePath != null)
        {
          _xmlOut = new FileWriter(scriptFilePath + "/flatfile_" + new SimpleDateFormat(DATE_FORMAT).format(new Date()) + ".xml");
        }
      }
    }
    catch (ConfigEnginePropertiesException e)
    {
      LOGGER.warn("impossible de récupérer l'emplacement spécifié pour les fichiers templates");
    }
  }

  /**
   * Constructeur
   * @param pStream flux d'entrée fichier plat
   * @throws StreamMapperException exception
   * @throws UnsupportedEncodingException exception
   * @throws IOException exception
   */
  public MapperReaderImpl(InputStream pStream, String pCharset) throws StreamMapperException, UnsupportedEncodingException, IOException
  {
    this(pStream, null, pCharset, null);
  }

  /**
   * Méthode de lecture du provider
   * @param pBuffer buffer XML attendu par le provider
   * @param pOff offset de lecture
   * @param pLen nombre de caracteres à lire
   * @return nombre de caracteres lus
   * @throws IOException exception
   */
  public int read(char[] pBuffer, int pOff, int pLen) throws IOException
  {
    if (_bufferMemAddr == null)
    {
      _bufferMemAddr = pBuffer.toString();
    }
    else
    {
      String newBufferMemAddr = pBuffer.toString();
      if (!newBufferMemAddr.equals(_bufferMemAddr))
      {
        _transformer.setBuffer(pBuffer);
        _bufferMemAddr = newBufferMemAddr;
      }
    }
    int writenChars = -1; // nombre de caractères écrits dna pBuffer
    try
    {
      if (_transformer == null)
      {
        // initialisation du transfomer
        // écrit du XML dans pBuffer
        _transformer = new FlatTransformerIn(pBuffer, _templateFile);
      }
      if (_lastRead) // dernière lecture
      {
        writenChars = _transformer.flushRemainBuffer(); // on vide le dernier buffer
        if (LOGGER.isDebugEnabled())
        {
          generateDebugFile(pBuffer, pOff, writenChars);
        }
        return writenChars; // -1 = fin de lecture du provider
      }
      // récupération du template, car si on a pas d'en-tête dans notre fichier de données il faudra qu'on fournisse certaines info pour notre FlatRow
      FlatTemplate template = FlatTemplate.getInstance(null, _templateFile);
      while (writenChars < pLen) // Tant que l'on n'a pas écrit dans pBuffer autant de caractères demandé (pLen)
      {
        if (!_csvFormat)
        {
          getNextLine(); // lecture de la ligne dans le fichier plat
        }
        else
        {
          getNextRecord(template);
        }
        if (_curLine == null) // dernière ligne du fichier plat
        {
          _transformer.closeStream(); // fermeture de balises ouvertes
          writenChars = _transformer.getWritenChars();
          _lastRead = true;
          if (LOGGER.isDebugEnabled())
          {
            generateDebugFile(pBuffer, pOff, writenChars);
          }
          return writenChars;
        }
        if (isMatchLine(_curLine, template))
        {
          // création du conteneur correspondant à une ligne de fichier plat
          // si csvFormat, on utilise un séparateur improbable pour éviter qu'une donnée ne soit "splitée" à tort si elle contient un ou plusieurs séparateur de champ
          FlatRow row = new FlatRow(_curLine, template, _csvFormat ? "><" : template.getSeparator());
          if (row.isValid()) // controle de la validité de la ligne
          {
            _transformer.transform(row); // transforme la row selon le template
            writenChars = _transformer.getWritenChars(); // retourne le nombre de caractères écrits dans pBuffer
          }
        }
      }
    }
    catch (IOException pException)
    {
      throw pException;
    }
    catch (AbstractRuntimeException pException)
    {
      throw pException;
    }
    catch (XMLStreamException pException)
    {
      throw new FlatMapperXmlStreamException(BatchErrorDetail.FLATMAPPER_XML_EXCEPTION);
    }
    catch (TechnicalServiceException pException)
    {
      throw new FlatMapperTechnicalServiceException(BatchErrorDetail.FLATMAPPER_XML_EXCEPTION);
    }
    if (LOGGER.isDebugEnabled())
    {
      generateDebugFile(pBuffer, pOff, writenChars);
    }
    return writenChars;
  }

  private void getNextRecord(FlatTemplate pTemplate) throws IOException
  {
    if (_iteratorCSV == null)
    {
      CSVFormat csvFileFormat = CSVFormat.DEFAULT;
      csvFileFormat = csvFileFormat.withDelimiter(pTemplate.getSeparator().charAt(0));
      _iteratorCSV = csvFileFormat.parse(in).iterator();
    }
    if (_iteratorCSV.hasNext())
    {
      CSVRecord record = _iteratorCSV.next();
      StringBuilder sb = new StringBuilder();
      for (String field: record)
      {
        sb.append(field + "><");
      }
      _curLine = sb.toString();
    }
    else
    {
      _curLine = null;
    }
  }

  /**
   * Génère le fichier de debug
   * @param pBuffer le buffer
   * @param pOff offset
   * @param pLen longueur
   */
  private void generateDebugFile(char[] pBuffer, int pOff, int pLen) throws IOException
  {
    if (pLen == -1)
    {
      _xmlOut.close();
      return;
    }
    _xmlOut.write(new String(pBuffer, pOff, pLen));
    _xmlOut.flush();
  }

  private boolean isMatchLine(String pLine, FlatTemplate pTemplate)
  {
    String line = _csvFormat ? pLine.replaceAll("><", pTemplate.getSeparator()) : pLine;
    boolean match = _includeList.isEmpty();
    // les critères d'inclusion sont prioritaires sur les critères d'exclusion
    int i = 0;
    for (MapperLineCriteriaType crit: _includeList)
    {
      if (isMatchLine(line, crit))
      {
        if (Boolean.TRUE.equals(crit.isFirst()))
        {
          _includeList.remove(i);
        }
        return true;
      }
      i++;
    }
    if (!match)
    {
      return false; // la ligne ne correspond à aucun critère d'inclusion défini, elle est donc rejetée
    }
    i = 0;
    for (MapperLineCriteriaType crit: _excludeList)
    {
      if (isMatchLine(line, crit))
      {
        if (Boolean.TRUE.equals(crit.isFirst()))
        {
          _excludeList.remove(i);
        }
        return false;
      }
      i++;
    }
    return true;
  }

  private boolean isMatchLine(String pLine, MapperLineCriteriaType pCrit)
  {
    if (Boolean.TRUE.equals(pCrit.isCaseInSensitive()))
    {
      String line = pLine.toUpperCase();
      if (pCrit.getEquals() != null && line.equals(pCrit.getEquals().toUpperCase()))
      {
        return true;
      }
      if (pCrit.getContains() != null && line.contains(pCrit.getContains().toUpperCase()))
      {
        return true;
      }
      if (pCrit.getStartsWith() != null && line.contains(pCrit.getStartsWith().toUpperCase()))
      {
        return true;
      }
      if (pCrit.getEndsWith() != null && line.contains(pCrit.getEndsWith().toUpperCase()))
      {
        return true;
      }
    }
    else
    {
      if (pCrit.getEquals() != null && pLine.equals(pCrit.getEquals()))
      {
        return true;
      }
      if (pCrit.getContains() != null && pLine.contains(pCrit.getContains()))
      {
        return true;
      }
      if (pCrit.getStartsWith() != null && pLine.contains(pCrit.getStartsWith()))
      {
        return true;
      }
      if (pCrit.getEndsWith() != null && pLine.contains(pCrit.getEndsWith()))
      {
        return true;
      }
    }
    return false;
  }
}
