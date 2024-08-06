package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.BatchInProgressViewRow;
import oracle.jbo.domain.Date;
import com.cylande.unitedretail.message.batch.BatchRunType;

import java.util.Calendar;

public class BatchInProgressTransformer
{
  public static void toBean(BatchRunType pBean, BatchInProgressViewRow pRow)
  {
    if (pRow.getBatchCode() != null)
    {
      pBean.setPath(pRow.getBatchCode());
    }
    if (pRow.getStartTime() != null)
    {
      pBean.setStartTime(toCalendar(pRow.getStartTime()));
    }
    if (pRow.getEndTime() != null)
    {
      pBean.setEndTime(toCalendar(pRow.getEndTime()));
    }
    if (pRow.getState() != null)
    {
      pBean.setStatus(pRow.getState().intValue() != 0);
    }
    if (pRow.getInError() != null)
    {
      pBean.setInError(pRow.getInError().intValue() != 0);
    }
  }

  private static Calendar toCalendar(Date pDate)
  {
    Calendar result = Calendar.getInstance();
    result.setTime(new java.util.Date(pDate.getValue().getTime()));
    return result;
  }
}
