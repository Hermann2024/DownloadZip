package com.cylande.unitedretail.batch.tools;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.cylande.unitedretail.batch.logging.BatchEditionTraceDetail;
import com.cylande.unitedretail.framework.logging.CylandeLogger;
import com.cylande.unitedretail.message.common.criteria.CriteriaStringType;
import com.cylande.unitedretail.message.enginevariables.VariableListType;
import com.cylande.unitedretail.message.enginevariables.VariableType;

public class BatchEditionUtil
{
  private static final CylandeLogger LOGGER = new CylandeLogger(BatchEditionTraceDetail.DEFAULT);

  public static boolean testString(CriteriaStringType pCrit, String pString)
  {
    if (pCrit != null)
    {
      if (pString != null)
      {
        if (pCrit.getEquals() != null)
        {
          return pString.equalsIgnoreCase(pCrit.getEquals());
        }
        else if (pCrit.getContains() != null)
        {
          return pString.toUpperCase().contains(pCrit.getContains().toUpperCase());
        }
      }
      return false;
    }
    return true;
  }

  public static void valueVarProcessToString(VariableListType pVarList, Transformer pTrans, String processorName)
  {
    for (VariableType var: pVarList.getVariable())
    {
      try
      {
        var.setValue(valueVarToString(var, pTrans, processorName));
      }
      catch (Exception e)
      {
        LOGGER.technicalWarn(BatchEditionTraceDetail.PROCESS_VAR_VALUE_CONVERSION_ERROR, new Object[] { var.getName(), processorName });
      }
    }
  }

  public static void valueVarBatchToString(VariableListType pVarList, Transformer pTrans, String processorName)
  {
    for (VariableType var: pVarList.getVariable())
    {
      try
      {
        var.setValue(valueVarToString(var, pTrans, processorName));
      }
      catch (TransformerException e)
      {
        LOGGER.technicalWarn(BatchEditionTraceDetail.PROCESS_VAR_VALUE_CONVERSION_ERROR, new Object[] { var.getName(), processorName });
      }
    }
  }

  /**
   * Transforme les values des variables en chaîne de caractère car les values sont de type anyType qui n'est pas interprétable en Flex
   * @param pVar
   * @param pTrans
   * @param processorName
   * @return résultat
   * @throws TransformerException
   */
  private static String valueVarToString(VariableType pVar, Transformer pTrans, String processorName) throws TransformerException
  {
    StringWriter sw = new StringWriter();
    if (pVar.getValue() != null)
    {
      Node node = ((Node)pVar.getValue()).getFirstChild();
      if (node != null)
      {
        pTrans.transform(new DOMSource(node), new StreamResult(sw));
      }
    }
    return sw.toString().trim();
  }

  public static void valueVarToNode(VariableListType pVarList, DocumentBuilderFactory pDocBuilderFactory) throws Exception
  {
    if (pVarList != null)
    {
      DocumentBuilder docBuilder = pDocBuilderFactory.newDocumentBuilder();
      for (VariableType var: pVarList.getVariable())
      {
        if (var.getValue() instanceof String)
        {
          Node node = docBuilder.parse(new ByteArrayInputStream(("<value>" + var.getValue() + "</value>").getBytes("UTF-8"))).getFirstChild();
          var.setValue(node);
        }
      }
    }
  }
}
