package com.cylande.unitedretail.batch.tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.cylande.unitedretail.message.batch.BatchGanttRowListType;
import com.cylande.unitedretail.message.batch.BatchGanttRowType;
import com.cylande.unitedretail.message.batch.BatchGanttTaskType;
import com.cylande.unitedretail.message.batch.BatchRunType;
import com.cylande.unitedretail.message.batch.TaskRunType;

public class GanttNode
{

  private SortedMap<String, GanttNode> _children = new TreeMap<String, GanttNode>();

  private List<BatchGanttTaskType> _tasks = new LinkedList<BatchGanttTaskType>();

  private String _nodeName = "";

  private List<Integer> _parentId = new LinkedList<Integer>();

  /**
   * Root node constructor
   */
  public GanttNode()
  {
    setNodeName("root");
  }

  /**
   * Constructeur pour une node à partir d'un batch
   *
   * @param pBatch
   */
  public GanttNode(BatchRunType pBatch)
  {
    String path = pBatch.getPath();
    int dotIndex = path.indexOf('.');
    if (dotIndex == -1)
    {
      setNodeName(path);
      _tasks.add(createTaskFromBatchRunType(pBatch));
    }
    else
    {
      setNodeName(path.substring(0, dotIndex));
      String remain = path.substring(dotIndex + 1);
      pBatch.setPath(remain);
      addChild(pBatch);
    }
  }

  /**
   * Constructeur pour une node à partir d'une tache
   *
   * @param pTask
   */

  public GanttNode(TaskRunType pTask)
  {
    String path = pTask.getPath();
    int dotIndex = path.indexOf('.');
    if (dotIndex == -1)
    {
      setNodeName(path);
      getParentId().add(pTask.getParentId());
      _tasks.add(createTaskFromTaskRunType(pTask));
    }
    else
    {
      setNodeName(path.substring(0, dotIndex));
      String remain = path.substring(dotIndex + 1);
      pTask.setPath(remain);
      addChild(pTask);
    }
  }

  /**
   * Permet de créer une BatchGanttTaskType à partir d'un BatchRunType
   *
   * @param pBatch
   * @return BatchGanttTaskType
   */
  private BatchGanttTaskType createTaskFromBatchRunType(BatchRunType pBatch)
  {
    BatchGanttTaskType newTask = new BatchGanttTaskType();
    newTask.setFrom(pBatch.getStartTime().getTimeInMillis());
    newTask.setTo(pBatch.getEndTime().getTimeInMillis());
    newTask.setName(pBatch.getPath());
    newTask.setIdTask(String.valueOf(pBatch.getId()));
    newTask.setDuration(String.valueOf(pBatch.getEndTime().getTimeInMillis() - pBatch.getStartTime().getTimeInMillis()));
    if (pBatch.getInError() != null && pBatch.getInError().equals(true))
    {
      newTask.setColor("#e74c3c");
    }
    else if (pBatch.getInError() == null || pBatch.getInError().equals(false))
    {
      newTask.setColor("#2ecc71");
    }
    return newTask;
  }

  /**
   * Permet de créer une BatchGanttTaskType à partir d'un TaskRunType
   *
   * @param pTask
   * @return BatchGanttTaskType
   */
  private BatchGanttTaskType createTaskFromTaskRunType(TaskRunType pTask)
  {
    BatchGanttTaskType newTask = new BatchGanttTaskType();
    newTask.setFrom(pTask.getStartTime().getTimeInMillis());
    newTask.setTo(pTask.getEndTime().getTimeInMillis());
    newTask.setName(pTask.getPath());
    newTask.setIdTask(String.valueOf(pTask.getId()));
    newTask.setDuration(String.valueOf(pTask.getEndTime().getTimeInMillis() - pTask.getStartTime().getTimeInMillis()));
    if (pTask.getInError() != null && pTask.getInError().equals(true))
    {
      newTask.setColor("#e74c3c");
    }
    else if (pTask.getInError() == null || pTask.getInError().equals(false))
    {
      newTask.setColor("#2ecc71");
    }
    return newTask;
  }

  /**
   * Permet de créer l'objet XSD correspondant
   *
   * @param pBatchList BatchGanttRowListType
   * @return BatchGanttRowType
   */
  public BatchGanttRowListType getGanttRow(BatchGanttRowListType pBatchList)
  {
    BatchGanttRowType row = null;
    if (!_nodeName.equals("root"))
    {
      row = new BatchGanttRowType();
      row.setName(getNodeName());
      row.setId(hashCode());

      for (BatchGanttTaskType task: _tasks)
      {
        row.getTasks().add(task);
      }
    }
    for (Map.Entry<String, GanttNode> entry: _children.entrySet())
    {
      GanttNode child = entry.getValue();
      if (!_nodeName.equals("root"))
      {
        row.getChildren().add(String.valueOf(child.hashCode()));
      }
      child.getGanttRow(pBatchList);
    }
    if (row != null)
    {
      pBatchList.getBatchGanttRows().add(row);
    }
    return pBatchList;
  }

  /**
   * Ajoute un enfant à la node
   *
   * @param BatchRunType
   */
  public void addChild(BatchRunType pBatch)
  {
    String path = pBatch.getPath();
    GanttNode child = null;
    int dotIndex = path.indexOf('.');
    if (dotIndex != -1)
    {
      String childDisplayName = path.substring(0, dotIndex);
      child = _children.get(childDisplayName);
    }
    else
    {
      child = _children.get(path);
    }
    if (child == null)
    {
      GanttNode newNode = new GanttNode(pBatch);
      _children.put(newNode.getNodeName(), newNode);
    }
    else
    {
      if (dotIndex != -1)
      {
        String remain = path.substring(dotIndex + 1);
        pBatch.setPath(remain);
        child.addChild(pBatch);
      }
      else
      {
        child.addTasks(pBatch);
      }
    }
  }

  /**
   * Ajoute un enfant à la node
   *
   * @param TaskRunType
   */
  public void addChild(TaskRunType pTask)
  {
    String path = pTask.getPath();
    GanttNode child = null;
    int dotIndex = path.indexOf('.');
    if (dotIndex != -1)
    {
      String childDisplayName = path.substring(0, dotIndex);
      child = _children.get(childDisplayName);
    }
    else
    {
      child = _children.get(path);
    }
    if (child == null)
    {
      GanttNode newNode = new GanttNode(pTask);
      _children.put(newNode.getNodeName(), newNode);
    }
    else
    {
      if (dotIndex != -1)
      {
        String remain = path.substring(dotIndex + 1);
        pTask.setPath(remain);
        child.addChild(pTask);
      }
      else
      {
        Integer count = 0;
        Integer pTaskParentId = pTask.getParentId();
        for (GanttNode childNode: _children.values())
        {
          List<Integer> childParentId = childNode.getParentId();
          if (childParentId.contains(pTaskParentId))
          {
            count++;
          }
        }
        if (count.equals(0))
        {
          if (_children.get(pTask.getPath()) == null)
          {
            GanttNode newNode = new GanttNode(pTask);
            _children.put(newNode.getNodeName(), newNode);
          }
          else
          {
            _children.get(pTask.getPath()).getParentId().add(pTask.getParentId());
            _children.get(pTask.getPath()).addTasks(pTask);
          }
        }
        else
        {
          GanttNode multiChild = _children.get(pTask.getPath() + "-t" + count);
          if (multiChild != null)
          {
            multiChild.getParentId().add(pTask.getParentId());
            multiChild.addTasks(pTask);
          }
          else
          {
            pTask.setPath(pTask.getPath() + "-t" + count);
            GanttNode newNode = new GanttNode(pTask);
            _children.put(newNode.getNodeName(), newNode);
          }
        }
      }
    }

  }

  private void addTasks(BatchRunType pBatch)
  {
    _tasks.add(createTaskFromBatchRunType(pBatch));
  }

  private void addTasks(TaskRunType pBatch)
  {
    _tasks.add(createTaskFromTaskRunType(pBatch));
  }

  public List<BatchGanttTaskType> getTasks()
  {
    return _tasks;
  }

  public void setTasks(List<BatchGanttTaskType> pTasks)
  {
    _tasks = pTasks;
  }

  public SortedMap<String, GanttNode> getChildren()
  {
    return _children;
  }

  public void setChildren(SortedMap<String, GanttNode> pChildren)
  {
    _children = pChildren;
  }

  public String getNodeName()
  {
    return _nodeName;
  }

  public void setNodeName(String pNodeName)
  {
    _nodeName = pNodeName;
  }

  public List<Integer> getParentId()
  {
    return _parentId;
  }

  public void setParentId(List<Integer> parentId)
  {
    _parentId = parentId;
  }
}
