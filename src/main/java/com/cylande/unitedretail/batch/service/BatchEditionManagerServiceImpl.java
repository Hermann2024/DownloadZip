package com.cylande.unitedretail.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.cylande.unitedretail.batch.exception.BatchEditionErrorDetail;
import com.cylande.unitedretail.batch.exception.BatchException;
import com.cylande.unitedretail.batch.service.common.BatchEditionManagerService;
import com.cylande.unitedretail.batch.tools.BatchEditionUtil;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.framework.service.WrapperServiceException;
import com.cylande.unitedretail.framework.tools.JAXBManager;
import com.cylande.unitedretail.message.batch.AbstractBatchType;
import com.cylande.unitedretail.message.batch.BatchChildEnum;
import com.cylande.unitedretail.message.batch.BatchChildType;
import com.cylande.unitedretail.message.batch.BatchChildrenAbstractType;
import com.cylande.unitedretail.message.batch.BatchContentType;
import com.cylande.unitedretail.message.batch.BatchEditionChildAbstractType;
import com.cylande.unitedretail.message.batch.BatchEditionCriteriaType;
import com.cylande.unitedretail.message.batch.BatchEditionScenarioType;
import com.cylande.unitedretail.message.batch.BatchEditionType;
import com.cylande.unitedretail.message.batch.BatchEnum;
import com.cylande.unitedretail.message.batch.BatchesEdition;
import com.cylande.unitedretail.message.batch.CommentChildType;
import com.cylande.unitedretail.message.batch.IncludeXMLType;
import com.cylande.unitedretail.message.batch.InitBatchEditionType;
import com.cylande.unitedretail.message.batch.ProjectDescriptionType;
import com.cylande.unitedretail.message.batch.PropertyEditionScenarioType;
import com.cylande.unitedretail.message.batch.TaskChildType;
import com.cylande.unitedretail.message.common.context.ContextType;

/** {@inheritDoc} */
public class BatchEditionManagerServiceImpl implements BatchEditionManagerService
{
  public static final String MAIN_FILE_NAME = "/Batches.xml";
  private static final PropertyEditionScenarioType DOMAIN_PROPERTY_SCENARIO = new PropertyEditionScenarioType();
  static
  {
    DOMAIN_PROPERTY_SCENARIO.setFromIncludeXML(true);
    DOMAIN_PROPERTY_SCENARIO.setDomainListOnInput(true);
  }
  private JAXBManager _jaxbManager = new JAXBManager();
  private PropertyEditionManagerServiceImpl _propertyServ = new PropertyEditionManagerServiceImpl();
  private ContextType _context;
  private TransformerFactory _transFactory = TransformerFactory.newInstance();
  private DocumentBuilderFactory _docBuilderFactory = DocumentBuilderFactory.newInstance();

  /** {@inheritDoc} */
  public BatchesEdition getMainBatch(ProjectDescriptionType project, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchesEdition result = new BatchesEdition();
    try
    {
      if (project != null && project.getLocation() != null)
      {
        String location = project.getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        File batchFile = new File(location + MAIN_FILE_NAME);
        if (!batchFile.exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
        }
        BatchesEdition edition = readFile(batchFile);
        if (pScenario != null)
        {
          if (!Boolean.TRUE.equals(pScenario.isNotManageDomain()) && edition.getPropertiesEdition() != null)
          {
            edition.getPropertiesEdition().setProjectDescription(project);
            _propertyServ.getDomainList(edition.getPropertiesEdition(), DOMAIN_PROPERTY_SCENARIO, pContext);
            edition.getPropertiesEdition().setProjectDescription(null);
          }
        }
        return edition;
      }
    }
    catch (Exception e)
    {
      throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
    }
    return result;
  }

  /** {@inheritDoc} */
  public void setMainBatch(BatchesEdition pBatches, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatches != null && pBatches.getProjectDescription() != null)
    {
      try
      {
        String location = pBatches.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        _jaxbManager.write(pBatches, location + MAIN_FILE_NAME);
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /** {@inheritDoc} */
  public BatchesEdition findBatch(BatchEditionCriteriaType pCriteria, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    BatchesEdition result = new BatchesEdition();
    if (pCriteria != null && pCriteria.getProjectDescription() != null)
    {
      try
      {
        BatchesEdition edition;
        File batchFile;
        String location = pCriteria.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        List<IncludeXMLType> includeList;
        if (pCriteria.getFileName() != null)
        {
          IncludeXMLType include = new IncludeXMLType();
          include.setName(pCriteria.getFileName());
          includeList = new ArrayList();
          includeList.add(include);
        }
        else
        {
          batchFile = new File(location + MAIN_FILE_NAME);
          if (!batchFile.exists())
          {
            throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
          }
          edition = readFile(batchFile);
          includeList = edition.getIncludeXML();
        }
        Transformer trans = _transFactory.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        boolean stopFind = false;
        boolean testName, testType, testBatchRef, testTaskRef, testTriggerRef;
        BatchEnum batchType;
        for (IncludeXMLType include: includeList)
        {
          if (include.getName().startsWith("Batchs_"))
          {
            batchFile = new File(location + "/" + include.getName());
            if (!batchFile.exists())
            {
              throw new BatchException(BatchEditionErrorDetail.MISSING_FILE, new Object[] { batchFile.getPath() });
            }
            edition = readFile(new File(location + "/" + include.getName()));
            if (edition.getInitBatch() != null)
            {
              testType = BatchEnum.INITBATCH.equals(pCriteria.getBatchType());
              testBatchRef = pCriteria.getBatchRef() == null || isBatchUseRef(edition.getInitBatch(), pCriteria.getBatchRef());
              testTaskRef = pCriteria.getTaskRef() == null || isTaskUseRef(edition.getInitBatch(), pCriteria.getTaskRef());
              if (testType && testBatchRef && testTaskRef)
              {
                setChildForkOrSequence(edition.getInitBatch());
                result.setInitBatch(edition.getInitBatch());
              }
            }
            for (BatchEditionType element: edition.getBatch())
            {
              testName = BatchEditionUtil.testString(pCriteria.getBatchName(), element.getName());
              // les batchs sont STATELESS par défaut
              batchType = element.getType() == null ? BatchEnum.STATELESS : element.getType();
              testType = pCriteria.getBatchType() == null || pCriteria.getBatchType().equals(batchType);
              testBatchRef = pCriteria.getBatchRef() == null || isBatchUseRef(element, pCriteria.getBatchRef());
              testTaskRef = pCriteria.getTaskRef() == null || isTaskUseRef(element, pCriteria.getTaskRef());
              testTriggerRef = pCriteria.getTriggerRef() == null || element.getTrigger() != null && pCriteria.getTriggerRef().equals(element.getTrigger().getRef());
              if (testName && testType && testBatchRef && testTaskRef && testTriggerRef)
              {
                if (element.getVariables() != null)
                {
                  BatchEditionUtil.valueVarBatchToString(element.getVariables(), trans, element.getName());
                }
                setChildForkOrSequence(element);
                result.getBatch().add(element);
              }
              if (pCriteria.getResultSize() != null && result.getBatch().size() == pCriteria.getResultSize())
              {
                stopFind = true;
                break;
              }
            }
          }
          if (stopFind)
          {
            break;
          }
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
    return result;
  }

  private BatchesEdition readFile(File pFile) throws BatchException
  {
    try
    {
      return (BatchesEdition)_jaxbManager.readAndCloseStream(new FileInputStream(pFile), new BatchesEdition());
    }
    catch (Exception e)
    {
      throw new BatchException(BatchEditionErrorDetail.ERROR_READING_FILE, new Object[] { pFile.getName() }, e);
    }
  }

  private boolean isBatchUseRef(AbstractBatchType pBatch, String pValueRef)
  {
    if (pValueRef != null)
    {
      BatchContentType content = pBatch.getSequence() != null ? pBatch.getSequence() : pBatch.getFork();
      if (content != null)
      {
        for (BatchChildrenAbstractType ref: content.getTaskOrBatchOrComment())
        {
          if (ref instanceof BatchChildType && pValueRef.equals(ref.getRef()))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isTaskUseRef(AbstractBatchType pBatch, String pValueRef)
  {
    if (pValueRef != null)
    {
      BatchContentType content = pBatch.getSequence() != null ? pBatch.getSequence() : pBatch.getFork();
      if (content != null)
      {
        for (BatchChildrenAbstractType ref: content.getTaskOrBatchOrComment())
        {
          if (ref instanceof TaskChildType && pValueRef.equals(ref.getRef()))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Permet de rendre la sequence ou le fork de l'initBatch lisible pour le Flex
   * @param pInitBatch
   */
  private void setChildForkOrSequence(InitBatchEditionType pInitBatch)
  {
    if (pInitBatch.getSequence() != null)
    {
      pInitBatch.getChildSequence().addAll(getChildForkOrSequence(pInitBatch.getSequence().getTaskOrBatchOrComment()));
      pInitBatch.setSequence(null);
    }
    else if (pInitBatch.getFork() != null)
    {
      pInitBatch.getChildFork().addAll(getChildForkOrSequence(pInitBatch.getFork().getTaskOrBatchOrComment()));
      pInitBatch.setFork(null);
    }
  }

  /**
   * Permet de rendre la sequence ou le fork du batch lisible pour le Flex
   * @param pBatch
   */
  private void setChildForkOrSequence(BatchEditionType pBatch)
  {
    if (pBatch.getSequence() != null)
    {
      pBatch.getChildSequence().addAll(getChildForkOrSequence(pBatch.getSequence().getTaskOrBatchOrComment()));
      pBatch.setSequence(null);
    }
    else if (pBatch.getFork() != null)
    {
      pBatch.getChildFork().addAll(getChildForkOrSequence(pBatch.getFork().getTaskOrBatchOrComment()));
      pBatch.setFork(null);
    }
  }

  private List<BatchEditionChildAbstractType> getChildForkOrSequence(List<BatchChildrenAbstractType> pTaskOrBatchList)
  {
    List<BatchEditionChildAbstractType> result = new ArrayList();
    for (BatchChildrenAbstractType ref: pTaskOrBatchList)
    {
      if (ref instanceof BatchChildType)
      {
        result.add(toBatchEditionChild((BatchChildType)ref));
      }
      else if (ref instanceof TaskChildType)
      {
        result.add(toBatchEditionChild((TaskChildType)ref));
      }
      else if (ref instanceof CommentChildType)
      {
        result.add(toBatchEditionChild((CommentChildType)ref));
      }
    }
    return result;
  }

  private BatchEditionChildAbstractType toBatchEditionChild(BatchChildType pBatch)
  {
    BatchEditionChildAbstractType result = new BatchEditionChildAbstractType();
    result.setActiveDomain(pBatch.getActiveDomain());
    result.setDefaultDomain(pBatch.getDefaultDomain());
    result.setFailOnError(pBatch.getFailOnError());
    result.setRef(pBatch.getRef());
    result.setDescription(pBatch.getDescription());
    result.setType(BatchChildEnum.BATCH);
    return result;
  }

  private BatchEditionChildAbstractType toBatchEditionChild(TaskChildType pTask)
  {
    BatchEditionChildAbstractType result = new BatchEditionChildAbstractType();
    result.setActiveDomain(pTask.getActiveDomain());
    result.setDefaultDomain(pTask.getDefaultDomain());
    result.setFailOnError(pTask.getFailOnError());
    result.setRef(pTask.getRef());
    result.setDescription(pTask.getDescription());
    result.setType(BatchChildEnum.TASK);
    return result;
  }

  private BatchEditionChildAbstractType toBatchEditionChild(CommentChildType pComment)
  {
    BatchEditionChildAbstractType result = new BatchEditionChildAbstractType();
    result.setValue(pComment.getValue());
    result.setType(BatchChildEnum.COMMENT);
    return result;
  }

  /** {@inheritDoc} */
  public void deleteBatch(BatchesEdition pBatches, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatches != null && pBatches.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, BatchesEdition> editionMap = new HashMap();
        String location = pBatches.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        BatchesEdition edition;
        BatchesEdition editionRef = null;
        BatchEditionCriteriaType batchCrit = new BatchEditionCriteriaType();
        // parcours des éléments à supprimer
        for (BatchEditionType elementToDelete: pBatches.getBatch())
        {
          // on teste si l'élément à supprimer n'est pas déjà référencé
          batchCrit.setBatchRef(elementToDelete.getName());
          batchCrit.setResultSize(1);
          editionRef = findBatch(batchCrit, null, pContext);
          if (!editionRef.getBatch().isEmpty())
          {
            throw new BatchException(BatchEditionErrorDetail.BATCH_UNDELETABLE_ALREADY_USED, new Object[] { elementToDelete.getName(), editionRef.getBatch().get(0).getName() });
          }
          edition = getEditionFromMap(elementToDelete.getFileName(), editionMap, pBatches.getProjectDescription());
          int i = 0;
          // parcours des éléments du fichier pour trouver l'élément à supprimer
          for (BatchEditionType element: edition.getBatch())
          {
            if (element.getName().equals(elementToDelete.getName()))
            {
              edition.getBatch().remove(i);
              break;
            }
            i++;
          }
        }
        // sauvegarde des modifications
        for (Entry<String, BatchesEdition> entry: editionMap.entrySet())
        {
          _jaxbManager.write(entry.getValue(), location + "/" + entry.getKey());
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  private BatchesEdition getEditionFromMap(String pFileName, Map<String, BatchesEdition> pEditionMap, ProjectDescriptionType project) throws Exception
  {
    BatchesEdition result;
    if (pEditionMap.get(pFileName) != null)
    {
      result = pEditionMap.get(pFileName);
    }
    else
    {
      result = new BatchesEdition();
      File batchFile = new File(project.getLocation() + "/" + pFileName);
      if (!batchFile.exists())
      {
        // création du nouveau fichier
        _jaxbManager.write(result, batchFile.getPath());
        // et mise à jour de la liste des includes sur le fichier principal
        BatchEditionScenarioType scenario = new BatchEditionScenarioType();
        scenario.setNotManageDomain(true);
        BatchesEdition main = getMainBatch(project, scenario, _context);
        IncludeXMLType include = new IncludeXMLType();
        include.setName(pFileName);
        main.getIncludeXML().add(include);
        setMainBatch(main, null, _context);
      }
      result = readFile(batchFile);
      // on stocke le fichier dans la map si pas encore chargé
      pEditionMap.put(pFileName, result);
    }
    return result;
  }

  /** {@inheritDoc} */
  public void postBatch(BatchesEdition pBatches, BatchEditionScenarioType pScenario, ContextType pContext) throws WrapperServiceException
  {
    if (pBatches != null && pBatches.getProjectDescription() != null)
    {
      try
      {
        _context = pContext;
        Map<String, BatchesEdition> editionMap = new HashMap();
        String location = pBatches.getProjectDescription().getLocation();
        if (!new File(location).exists())
        {
          throw new BatchException(BatchEditionErrorDetail.MISSING_DIRECTORY, new Object[] { location });
        }
        BatchesEdition edition;
        // parcours des éléments à modifier
        for (BatchEditionType elementToUpdate: pBatches.getBatch())
        {
          BatchEditionUtil.valueVarToNode(elementToUpdate.getVariables(), _docBuilderFactory);
          setTaskOrBatch(elementToUpdate);
          edition = getEditionFromMap(elementToUpdate.getFileName(), editionMap, pBatches.getProjectDescription());
          int i = 0;
          boolean isCreate = true;
          // parcours des éléments du fichier pour trouver l'élément à modifier
          for (BatchEditionType element: edition.getBatch())
          {
            if (element.getName().equals(elementToUpdate.getName()))
            {
              edition.getBatch().set(i, elementToUpdate);
              isCreate = false;
              break;
            }
            i++;
          }
          if (isCreate)
          {
            edition.getBatch().add(elementToUpdate);
          }
        }
        // sauvegarde des modifications
        for (Entry<String, BatchesEdition> entry: editionMap.entrySet())
        {
          _jaxbManager.write(entry.getValue(), location + "/" + entry.getKey());
        }
      }
      catch (Exception e)
      {
        throw new WrapperServiceException(e, ContextTransformer.toLocale(pContext));
      }
    }
  }

  /**
   * Alimentation des balises standards sequence et fork car childSequence et childFork ont été créés pour la compatibilité avec Flex
   * qui n'est pas compatible avec des beans utilisant un choice dans les schémas XSD
   * @param pBatch
   */
  private void setTaskOrBatch(BatchEditionType pBatch)
  {
    if (!pBatch.getChildSequence().isEmpty())
    {
      pBatch.setSequence(new BatchContentType());
      for (BatchEditionChildAbstractType ref: pBatch.getChildSequence())
      {
        if (BatchChildEnum.BATCH.equals(ref.getType()))
        {
          pBatch.getSequence().getTaskOrBatchOrComment().add(toBatchChild(ref));
        }
        else if (BatchChildEnum.TASK.equals(ref.getType()))
        {
          pBatch.getSequence().getTaskOrBatchOrComment().add(toTaskChild(ref));
        }
        else if (BatchChildEnum.COMMENT.equals(ref.getType()))
        {
          pBatch.getSequence().getTaskOrBatchOrComment().add(toCommentChild(ref));
        }
      }
      pBatch.setChildSequence(null);
    }
    else if (!pBatch.getChildFork().isEmpty())
    {
      pBatch.setFork(new BatchContentType());
      for (BatchEditionChildAbstractType ref: pBatch.getChildFork())
      {
        if (BatchChildEnum.BATCH.equals(ref.getType()))
        {
          pBatch.getFork().getTaskOrBatchOrComment().add(toBatchChild(ref));
        }
        else if (BatchChildEnum.TASK.equals(ref.getType()))
        {
          pBatch.getFork().getTaskOrBatchOrComment().add(toTaskChild(ref));
        }
        else if (BatchChildEnum.COMMENT.equals(ref.getType()))
        {
          pBatch.getFork().getTaskOrBatchOrComment().add(toCommentChild(ref));
        }
      }
      pBatch.setChildFork(null);
    }
  }

  private BatchChildType toBatchChild(BatchEditionChildAbstractType pBatch)
  {
    BatchChildType result = new BatchChildType();
    result.setActiveDomain(pBatch.getActiveDomain());
    result.setDefaultDomain(pBatch.getDefaultDomain());
    result.setFailOnError(pBatch.getFailOnError());
    result.setRef(pBatch.getRef());
    result.setDescription(pBatch.getDescription());
    return result;
  }

  private TaskChildType toTaskChild(BatchEditionChildAbstractType pTask)
  {
    TaskChildType result = new TaskChildType();
    result.setActiveDomain(pTask.getActiveDomain());
    result.setDefaultDomain(pTask.getDefaultDomain());
    result.setFailOnError(pTask.getFailOnError());
    result.setRef(pTask.getRef());
    result.setDescription(pTask.getDescription());
    return result;
  }

  private CommentChildType toCommentChild(BatchEditionChildAbstractType pComment)
  {
    CommentChildType result = new CommentChildType();
    result.setValue(pComment.getValue());
    return result;
  }
}
