package com.cylande.unitedretail.batch.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.cylande.unitedretail.batch.service.BatchEditionEngineServiceImpl;
import com.cylande.unitedretail.batch.tools.BatchUtil;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;

public class BatchEntityResolver extends DefaultHandler2
{
  public static final String XML_HEADER_REGEX = "<\\?xml version=[\"|'].*[\"|'] encoding=[\"|'].*[\"|']\\?>";
  private static final Map<String, String> ROOT_TAGNAME_OF_ELEMENT_MAP = new HashMap<String, String>();
  private ProjectDescriptionType _project;
  private Exception _exception = null;
  static
  {
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("property", "propertiesEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("tstmp", "propertiesEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("batch", "batchesEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("initBatch", "batchesEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("task", "tasksEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("provider", "providersEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("processor", "processorsEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("trigger", "triggersEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("mapper", "mappersEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("stylesheet", "stylesheetsEdition");
    ROOT_TAGNAME_OF_ELEMENT_MAP.put("variables", "processorsEdition");
  }
  private Map<String, String> _entityFileMap = new HashMap<String, String>();

  public BatchEntityResolver(ProjectDescriptionType project)
  {
    _project = project;
  }

  public void externalEntityDecl(String pName, String publicId, String pSystemId) throws SAXException
  {
    try
    {
      _entityFileMap.put(pName, pSystemId);
      importIncludeFile(pSystemId);
    }
    catch (Exception e)
    {
      _exception = e;
    }
  }

  public void importIncludeFile(String pName) throws IOException, Exception, FileNotFoundException
  {
    File destFile = new File(_project.getLocation() + "/" + pName);
    File entityFile = new File(_project.getFromDir() + "/" + pName);
    Entry<String, String> entry = getRootTagNameEntry(entityFile);
    String stringFile = FileUtils.readFileToString(entityFile);
    String rootName;
    PrintWriter writer = null;
    try
    {
      if (entry == null)
      {
        // si le fichier est vide et correspond à un type connu, le fichier est importé en générant la balise root vide
        rootName = getRootTagName(entityFile.getName());
        if (rootName != null)
        {
          writer = new PrintWriter(new FileWriter(destFile, false));
          writer.println(BatchUtil.XML_HEADER);
          writer.println("<" + rootName + "/>");
        }
      }
      else
      {
        rootName = entry.getValue();
        writer = new PrintWriter(new FileWriter(destFile, false));
        writer.println(BatchUtil.XML_HEADER);
        writer.println("<" + rootName + ">");
        stringFile = FileUtils.readFileToString(entityFile);
        stringFile = stringFile.replaceFirst(XML_HEADER_REGEX, "");
        // ajout de l'attribut fileName
        stringFile = stringFile.replaceAll("<" + entry.getKey() + " ", "<" + entry.getKey() + " fileName=\"" + pName + "\" ");
        if (entry.getKey().equals("property"))
        {
          stringFile = stringFile.replaceAll("<tstmp", "<tstmp fileName=\"" + pName + "\"");
        }
        else if (entry.getKey().equals("tstmp"))
        {
          stringFile = stringFile.replaceAll("<property", "<property fileName=\"" + pName + "\"");
        }
        stringFile = stringFile.replaceAll("<!-- mandatory -->", "<mandatory>true</mandatory>");
        stringFile = stringFile.replaceAll("<!--", "<comment><![CDATA[");
        stringFile = stringFile.replaceAll("-->", "]]></comment>");
        // modification des attributs xsi:type="" en type=""
        stringFile = stringFile.replaceAll("xsi:type=\"(.*)\"", "type=\"$1\"");
        writer.print(stringFile);
        writer.println(rootName.equals("variables") ? "" : "</" + rootName + ">");
      }
    }
    finally
    {
      if (writer != null)
      {
        writer.close();
      }
    }
  }

  private String getRootTagName(String pNameFile) throws Exception
  {
    String result = null;
    for (Entry<String, String> entry: BatchEditionEngineServiceImpl.TAGNAME_CONVERSION_MAP.entrySet())
    {
      if (pNameFile.toLowerCase().startsWith(entry.getKey()))
      {
        return entry.getValue();
      }
    }
    return result;
  }

  private Entry<String, String> getRootTagNameEntry(File pFile) throws Exception
  {
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new FileReader(pFile));
      String line;
      while ((line = reader.readLine()) != null)
      {
        line = line.trim().replaceAll("<!--", "");
        for (Entry<String, String> entry: ROOT_TAGNAME_OF_ELEMENT_MAP.entrySet())
        {
          if (line.startsWith("<" + entry.getKey() + " ") || line.equals("<" + entry.getKey() + ">"))
          {
            return entry;
          }
        }
      }
      return null;
    }
    finally
    {
      if (reader != null)
      {
        reader.close();
      }
    }
  }

  public Exception getException()
  {
    return _exception;
  }

  public Map getEntityFileMap()
  {
    return _entityFileMap;
  }
}
