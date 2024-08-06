package com.cylande.unitedretail.batch.provider.rw.impl;

import java.util.Iterator;

import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

/**
 * Class for provider's class uses
 */
public class RootElement
{
  /** mémorise le tag d'ouverture du root element */
  private String _startTag = null;

  /** nom du root element */
  private String _elementName = null;

  /** évènement correspondant à l'ouverture du root element */
  private XMLEvent _event = null;

  /** Indique s'il faut conserver le préfixe de l'élément root */
  private final boolean _keepPrefix;

  /** Indique s'il faut ignorer l'attribut xmlns de l'élément root */
  private final boolean _noNamespace;

  /**
   * construit un RootElement à partir dans XMLEvent (seulement pour un start)
   * @param pEvent
   * @param pKeepPrefix Indique s'il faut conserver le préfixe de l'élément root
   */
  protected RootElement(XMLEvent pEvent, boolean pKeepPrefix, boolean pNoNamespace)
  {
    _event = pEvent;
    _keepPrefix = pKeepPrefix;
    _noNamespace = pNoNamespace;
    buildStartTag(pEvent);
    _elementName = getRootElementPrefix(pEvent).concat(pEvent.asStartElement().getName().getLocalPart());
  }

  /**
   * Construit l'element root en prenant en compte les namespaces
   * Ignore ou non le namespace de l'element root (prefix) en fonction de
   * l'indicateur keepPrefix.
   * @param pEvent evenement STAX
   */
  private void buildStartTag(XMLEvent pEvent)
  {
    StringBuilder elementNameBuilder = new StringBuilder("<");
    elementNameBuilder.append(getRootElementPrefix(pEvent));
    elementNameBuilder.append(pEvent.asStartElement().getName().getLocalPart());
    // On concatene les namespaces
    Iterator<Namespace> itNamespace = pEvent.asStartElement().getNamespaces();
    while (itNamespace.hasNext())
    {
      Namespace namespace = itNamespace.next();
      // On ignore les namespace sans prefix (celui de l'element root)
      if (namespace.getPrefix() != null && !"".equals(namespace.getPrefix()))
      {
        elementNameBuilder.append(" ").append(namespace.toString());
      }
      else if (!_noNamespace)
      {
        elementNameBuilder.append(" ").append(namespace.toString().replaceAll(":=", "="));
      }
    }
    elementNameBuilder.append(">");
    _startTag = elementNameBuilder.toString();
  }

  /**
   * Retourne le préfixe au root élément s'il est défini et si l'indicateur
   * keepPrefix est activé.
   * @param pEvent XML
   */
  private String getRootElementPrefix(XMLEvent pEvent)
  {
    String result = "";
    if (_keepPrefix)
    {
      String prefix = pEvent.asStartElement().getName().getPrefix();
      if ((prefix != null) && (!prefix.equalsIgnoreCase("")))
      {
        result = prefix.concat(":");
      }
    }
    return result;
  }

  /**
   * restitue le startTag du RootElement
   * @return résultat
   */
  protected String getStartTag()
  {
    return _startTag;
  }

  /**
   * restitue le nom de l'élément
   * @return résultat
   */
  protected String getElementName()
  {
    return _elementName;
  }

  /**
   * Génère le _endTag associé à l'élément Root représenté et le renvoie sous forme de chaîne de caractères
   * @return résultat
   */
  protected String getEndTag()
  {
    // construction du endTag à partir du nom de l'élément
    return "</" + _elementName + ">";
  }

  /**
   * génère une copie du root element
   * @return résultat
   */
  protected RootElement getCopy()
  {
    return new RootElement(_event, _keepPrefix, _noNamespace);
  }

  /**
   * Restitue l'évènement correspondant à l'ouverture du root element
   * @return résultat
   */
  protected XMLEvent getXMLEvent()
  {
    return _event;
  }
}
