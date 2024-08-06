package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.cylande.unitedretail.framework.service.TechnicalServiceException;

/**
 * Transformer de flux plat vers XML
 */
public class FlatTransformerIn
{
  /**
   * regexp CDATA
   */
  private static final Pattern CDATA_PATTERN = Pattern.compile("(<|>|&)");
  /**
   * Indice de profonder precedement trait�
   */
  private int _previousDepth = 0;
  /**
   * Redirection de flux : writer qui va �crire du xml dans le flux de lecture du provider
   */
  private XMLRedirectWriter _xmlWriter;
  /**
   * Stax �crit des �l�ments XML
   */
  private XMLStreamWriter _streamWriter;
  /**
   * Objet template
   */
  private FlatTemplate _template;
  /**
   * Path du template
   */
  private String _templatePath;
  private Boolean _writeHeader = true;
  private Map<String, String> _openTagMap = new HashMap();

  /**
   * Constructeur
   * @param pBuffer buffer de lecture du provider
   * @param pTemplatePath
   * @throws XMLStreamException exception
   */
  public FlatTransformerIn(char[] pBuffer, String pTemplatePath) throws XMLStreamException
  {
    XMLOutputFactory output = XMLOutputFactory.newInstance();
    // cr�e une redirection du flux XML stax vers le buffer de lecture du provider
    _xmlWriter = new XMLRedirectWriter(pBuffer);
    _streamWriter = output.createXMLStreamWriter(_xmlWriter);
    _templatePath = pTemplatePath;
  }

  /**
   * Transforme un Objet Row en XML via stax selon le template param�tr�
   * @param pRow
   * @throws XMLStreamException exception
   * @throws TechnicalServiceException exception
   */
  public void transform(FlatRow pRow) throws XMLStreamException, TechnicalServiceException
  {
    if (_template == null) // r�cup�ration du template
    {
      _template = FlatTemplate.getInstance(pRow.getRootElement(), _templatePath);
    }
    // on remonte d'un niveau XML : fermeture des balises
    if (_previousDepth != 0 && _previousDepth >= pRow.getDepth())
    {
      closeStream(pRow.getDepth());
    }
    // on retient la profondeur pour fermer les balises le coup d'apres
    _previousDepth = pRow.getDepth();
    // Si on a pas d'en-t�te, on va l'�crire selon le template (sinon elle ne le sera jamais !)
    if (_writeHeader && !_template.getHeader())
    {
      writeStartElement(_template.getHeaderElement());
      _writeHeader = false;
    }
    // Ecriture de l'�l�ment Root de la ligne
    writeStartElement(pRow.getRootElement());
    // R�cup�re le descripteur de la ligne dans le template pour les r�gles de transformation
    AbstractFlatRow rowDescriptor = _template.getRowDescriptor(pRow);
    if (rowDescriptor == null)
    {
      return;
    }
    // on prend la taille max la plus petite pour �viter le d�passement d'index
    int maxSize = rowDescriptor.getFieldsSize() < pRow.getFieldsSize() ? rowDescriptor.getFieldsSize() : pRow.getFieldsSize();
    // on parcoure chaque champs du fichier de donn�es
    // on recherche le nom du tag XML correspondant � l'indice du champ
    for (int fieldIdx = 0; fieldIdx < maxSize; fieldIdx++)
    {
      // �criture de balise <ouvrante>valeur</fermante>
      writeTagAndValue(rowDescriptor, pRow, fieldIdx);
    }
  }

  /**
   * Fermeture de tous les �l�ments ouverts
   * @throws XMLStreamException exception
   */
  public void closeStream() throws XMLStreamException
  {
    closeStream(0);
  }

  /**
   * Fermeture de tous les �l�ments ouverts jusqu'a la profondeur
   * @param pDepth profondeur
   * @throws XMLStreamException exception
   */
  private void closeStream(int pDepth) throws XMLStreamException
  {
    int elementToClose = _previousDepth - pDepth + 1;
    while (elementToClose-- > 0)
    {
      writeEndElement();
    }
  }

  /**
   * Ecriture des caract�res avec ou sans CDATA
   * @param pContent
   * @throws XMLStreamException exception
   */
  private void writeCharacters(String pContent) throws XMLStreamException
  {
    if (CDATA_PATTERN.matcher(pContent).find())
    {
      _streamWriter.writeCData(pContent);
    }
    else
    {
      _streamWriter.writeCharacters(pContent);
    }
  }

  /**
   * Ecriture de balise <ouvrante>valeur</fermante>
   * @param pTag
   * @param pValue
   * @throws XMLStreamException exception
   */
  private void writeTagAndValue(AbstractFlatRow pRowDesc, FlatRow pRow, int pIndex) throws XMLStreamException
  {
    String value = pRow.getField(pIndex);
    // on n'�crit pas les balises si il n'y a pas de valeurs
    if (value == null || value.trim().equals(""))
    {
      return;
    }
    // On est sur une donn�e avec plusieurs niveaux de pronfondeur
    if (pRowDesc._levelMap.get(pIndex) != null)
    {
      // On s�pare chaque niveau
      String[] templateLineSplit = pRowDesc._levelMap.get(pIndex).split("\\.");
      // parcoure des diff�rents niveaux pour ouverture des balises si pas d�j� ouvertes
      for (int i = 0; i < templateLineSplit.length; i++)
      {
        String tag = templateLineSplit[i];
        if (_openTagMap.get(tag + '-' + i) == null)
        {
          writeStartElement(tag);
          _openTagMap.put(tag + '-' + i,  tag);
        }
      }
      writeCharacters(value);
      // recherche du templateLine sur la colonne suivante et qui a un d�but de path similaire
      String[] templateLineSplit2 = null;
      for (Entry<Integer, String> entry: pRowDesc._levelMap.entrySet())
      {
        if (entry.getKey() == pIndex + 1 && pRow.getFieldsSize() > entry.getKey() && pRow.getField(entry.getKey()) != null && !pRow.getField(entry.getKey()).trim().equals(""))
        {
          // le template n'est pas retenu si aucune donn�e n'est pr�sente sur la colonne associ�e
          String[] templateLineSplitTmp = entry.getValue().split("\\.");
          if (templateLineSplit[0].equals(templateLineSplitTmp[0]))
          {
            templateLineSplit2 = templateLineSplitTmp;
            break;
          }
        }
      }
      // parcoure des diff�rents niveaux pour fermeture des balises si pas utilis�es sur l'�ventuel prochain template trouv� similaire (templateLineSplit2)
      for (int i = templateLineSplit.length - 1; i > -1; i--)
      {
        String tag = templateLineSplit[i];
        if (templateLineSplit2 == null || i > templateLineSplit2.length - 2 || !templateLineSplit[i].equals(templateLineSplit2[i]))
        {
          writeEndElement();
          _openTagMap.remove(tag + '-' + i);
        }
        else
        {
          break;
        }
      }
    }
    else
    // On est sur un niveau simple
    {
      // As-t'on une transco ?
      Map<String, String> transcoMap = pRowDesc._transcoMap.get(pIndex);
      if (transcoMap != null && transcoMap.get(value) != null)
      {
        value = transcoMap.get(value);
      }
      writeStartElement(pRowDesc.getField(pIndex));
      writeCharacters(value);
      writeEndElement();
    }
  }

  /**
   * Ecriture de la balise ouvrante
   * @param pStart
   * @throws XMLStreamException exception
   */
  private void writeStartElement(String pStart) throws XMLStreamException
  {
    _streamWriter.writeStartElement(pStart);
  }

  /**
   * Ecriture le la balise fermante
   * @throws XMLStreamException exception
   */
  private void writeEndElement() throws XMLStreamException
  {
    _streamWriter.writeEndElement();
  }

  /**
   * Nombres de carat�res �crits dans le buffer de lecture du provider
   * @return r�sultat
   */
  public int getWritenChars()
  {
    return _xmlWriter.getBufferIndex();
  }

  /**
   * Vide le dernier buffer
   * @return le nombre de carateres ecrits
   */
  public int flushRemainBuffer()
  {
    return _xmlWriter.flushRemainBuffer();
  }

  public void setBuffer(char[] pBuffer)
  {
    _xmlWriter.setBuffer(pBuffer);
  }
}
