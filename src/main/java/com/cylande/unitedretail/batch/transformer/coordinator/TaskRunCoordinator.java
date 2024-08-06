package com.cylande.unitedretail.batch.transformer.coordinator;

import com.cylande.unitedretail.batch.business.module.common.FileProviderTraceManagerModule;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.batch.AbstractStream;
import com.cylande.unitedretail.message.batch.FileProviderTraceListType;
import com.cylande.unitedretail.message.batch.FileProviderTraceType;
import com.cylande.unitedretail.message.batch.PathFileProviderListType;
import com.cylande.unitedretail.message.batch.PathFileProviderType;
import com.cylande.unitedretail.message.batch.TaskProviderType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.batch.TaskType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

/** Coordinator de TaskRun */
public class TaskRunCoordinator
{
  /**
   * Initialise les providers de pTaskRun à partir de pTaskDef
   * @param pTaskRun
   * @param pTaskDef
   */
  public static void setProvider(TaskRunType pTaskRun, TaskType pTaskDef)
  {
    if (pTaskDef != null)
    {
      pTaskRun.setInputProvider(getProviderInfo(pTaskDef.getInput()));
      pTaskRun.setResponseProvider(getProviderInfo(pTaskDef.getResponse()));
      pTaskRun.setRejectProvider(getProviderInfo(pTaskDef.getReject()));
    }
  }

  /**
   * Génère un ContentInfoType à partir d'un AbstractStream qui définit un provider
   * @param pStream
   * @return résultat
   */
  public static PathFileProviderListType getProviderInfo(AbstractStream pStream)
  {
    if (pStream != null)
    {
      PathFileProviderListType inputInfo = new PathFileProviderListType();
      inputInfo.setName(getProviderName(pStream));
      inputInfo.setDomain(getProviderDomain(pStream));
      return inputInfo;
    }
    return null;
  }

  /**
   * Extrait le domaine positionné sur le provider lors de l'exécution de la tache
   * @param pStream
   * @return résultat
   */
  private static String getProviderDomain(AbstractStream pStream)
  {
    return controlProviderDomain(pStream);
  }

  /**
   * Calcule du domaine pour un provider de la task
   * @param pStream
   * @return résultat
   */
  private static String controlProviderDomain(AbstractStream pStream)
  {
    String activeDomain = null;
    if (pStream != null)
    {
      TaskProviderType provider = pStream.getProvider();
      if (provider != null)
      {
        activeDomain = provider.getActiveDomain();
      }
    }
    if (activeDomain != null && !activeDomain.equals(""))
    {
      return activeDomain;
    }
    return PropertiesManager.DEFAULT_DOMAIN;
  }

  /**
   * Extrait le nom de la référence d'un provider positionné sur un abstractStream
   * @param pStream l'abstractStream sur lequel on veut récupérer la référence de provider
   * @return résultat
   */
  private static String getProviderName(AbstractStream pStream)
  {
    if (pStream.getProvider() != null)
    {
      return pStream.getProvider().getRef();
    }
    return null;
  }

  /**
   * Création des fichiers utilisés par pTaskRun sur l'objet list des providers. Les fichiers déjà créés sont ignorés.
   * Les fichiers nouvellement créés sont stockés sur l'objet fileList des providers de pTaskRun.
   * @param pTaskRun
   * @param pFileProviderTraceManagerModule
   */
  public static void postAssociatedRow(TaskRunType pTaskRun, FileProviderTraceManagerModule pFileProviderTraceManagerModule)
  {
    FileProviderTraceListType fileList;
    // optimisation : on ne sollicite pas le service de création si il n'y a pas de nouveau fichier
    if (pTaskRun.getInputProvider() != null && pTaskRun.getInputProvider().getList().size() > pTaskRun.getInputProvider().getFileList().size())
    {
      fileList = createFileProviderTrace(pTaskRun, getNewFileProviderTrace(pTaskRun.getInputProvider()), pFileProviderTraceManagerModule);
      if (fileList != null)
      {
        pTaskRun.getInputProvider().getFileList().addAll(fileList.getList());
        // Propagation des fichiers créés avec les id correspondant permettant de les enregistrer dans TASK_AUDIT lors de la survenance d'une erreur
      }
    }
    if (pTaskRun.getResponseProvider() != null && pTaskRun.getResponseProvider().getList().size() > pTaskRun.getResponseProvider().getFileList().size())
    {
      fileList = createFileProviderTrace(pTaskRun, getNewFileProviderTrace(pTaskRun.getResponseProvider()), pFileProviderTraceManagerModule);
      if (fileList != null)
      {
        pTaskRun.getResponseProvider().getFileList().addAll(fileList.getList());
        // Propagation des fichiers créés avec les id correspondant permettant de les enregistrer dans TASK_AUDIT lors de la survenance d'une erreur
      }
    }
    if (pTaskRun.getRejectProvider() != null && pTaskRun.getRejectProvider().getList().size() > pTaskRun.getRejectProvider().getFileList().size())
    {
      fileList = createFileProviderTrace(pTaskRun, getNewFileProviderTrace(pTaskRun.getRejectProvider()), pFileProviderTraceManagerModule);
      if (fileList != null)
      {
        pTaskRun.getRejectProvider().getFileList().addAll(fileList.getList());
        // Propagation des fichiers créés avec les id correspondant permettant de les enregistrer dans TASK_AUDIT lors de la survenance d'une erreur
      }
    }
  }

  private static PathFileProviderListType getNewFileProviderTrace(PathFileProviderListType pathFileProviderList)
  {
    PathFileProviderListType result = new PathFileProviderListType();
    result.getList().addAll(pathFileProviderList.getList().subList(pathFileProviderList.getFileList().size(), pathFileProviderList.getList().size()));
    result.setDomain(pathFileProviderList.getDomain());
    result.setName(pathFileProviderList.getName());
    return result;
  }
  /**
   * @param pTaskRun
   * @param pathFileProviderList
   * @param pFileProviderTraceManagerModule
   * @return la liste des fichiers créés
   */
  private static FileProviderTraceListType createFileProviderTrace(TaskRunType pTaskRun, PathFileProviderListType pathFileProviderList, FileProviderTraceManagerModule pFileProviderTraceManagerModule)
  {
    FileProviderTraceListType result = null;
    if (pFileProviderTraceManagerModule != null)
    {
      FileProviderTraceListType fileProviderTraceList = new FileProviderTraceListType();
      for (PathFileProviderType pathFileProvider: pathFileProviderList.getList())
      {
        FileProviderTraceType fileProvider = new FileProviderTraceType();
        fileProvider.setTaskId(pTaskRun.getId());
        fileProvider.setTaskCode(pTaskRun.getPath());
        fileProvider.setDomain(pathFileProviderList.getDomain());
        fileProvider.setSite(pTaskRun.getSite());
        fileProvider.setProviderName(pathFileProviderList.getName());
        fileProvider.setFilePath(pathFileProvider.getDir() + "/" + pathFileProvider.getFileName());
        fileProvider.setFileName(pathFileProvider.getFileName());
        fileProviderTraceList.getList().add(fileProvider);
      }
      result = pFileProviderTraceManagerModule.createFileProviderTraceList(fileProviderTraceList, null, ContextTransformer.fromLocale());
    }
    return result;
  }

  public static void setFileTraceList(TaskRunType pTaskRun, FileProviderTraceListType pFileTraceList)
  {
    if (pFileTraceList != null)
    {
      setFileTraceList(pTaskRun.getInputProvider(), pFileTraceList);
      setFileTraceList(pTaskRun.getResponseProvider(), pFileTraceList);
      setFileTraceList(pTaskRun.getRejectProvider(), pFileTraceList);
    }
  }

  private static void setFileTraceList(PathFileProviderListType pathFileList, FileProviderTraceListType pFileTraceList)
  {
    if (pathFileList != null)
    {
      for (FileProviderTraceType fileTrace: pFileTraceList.getList())
      {
        if (pathFileList.getName().equals(fileTrace.getProviderName()))
        {
          PathFileProviderType pathFile = new PathFileProviderType();
          pathFile.setFileName(fileTrace.getFilePath());
          pathFileList.getList().add(pathFile);
        }
      }
    }
  }
}
