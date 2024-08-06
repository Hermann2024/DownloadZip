package com.cylande.unitedretail.batch.batch;

import java.io.File;
import java.util.LinkedList;

import com.cylande.unitedretail.message.batch.QueueEnum;

public class BatchQueue
{
  private String _name;
  private QueueEnum _type;
  private LinkedList<String> _elementList = new LinkedList();
  private boolean _blocked = false;
  private String _currentElement = null;

  public BatchQueue(String pName, QueueEnum pType)
  {
    _name = pName;
    _type = pType == null ? QueueEnum.FIFO : pType;
  }

  public String next()
  {
    if (_currentElement != null)
    {
      // suppression des fichiers de l'ancien élément
      File dir = new File(BatchQueueManager.getWorkDir());
      for (File file: dir.listFiles())
      {
        if (file.getName().startsWith(_currentElement))
        {
          file.delete();
        }
      }
    }
    _currentElement = getElementList().poll();
    return _currentElement;
  }

  public String getName()
  {
    return _name;
  }

  public void setName(String pName)
  {
    _name = pName;
  }

  public boolean isBlocked()
  {
    return _blocked;
  }

  public void setBlocked(boolean pBlocked)
  {
    _blocked = pBlocked;
  }

  public QueueEnum getType()
  {
    return _type;
  }

  public void setType(QueueEnum pType)
  {
    _type = pType;
  }

  public LinkedList<String> getElementList()
  {
    return _elementList;
  }

  public void setElementList(LinkedList<String> pElementList)
  {
    _elementList = pElementList;
  }
}
