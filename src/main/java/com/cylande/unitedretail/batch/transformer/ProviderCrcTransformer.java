package com.cylande.unitedretail.batch.transformer;

import com.cylande.unitedretail.batch.business.query.common.ProviderCrcViewRow;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Booleans;
import com.cylande.unitedretail.framework.business.jbo.domain.common.Timestamp;
import com.cylande.unitedretail.message.batch.ProviderCrcType;

import java.util.Calendar;

public class ProviderCrcTransformer
{

  public static ProviderCrcType toBean(ProviderCrcViewRow pRow)
  {
    ProviderCrcType result = null;
    if (pRow != null)
    {
      result = new ProviderCrcType();
      result.setCrc(pRow.getCrc());
      result.setBatchName(pRow.getBatchName());
      if (pRow.getOverridden() != null)
      {
        result.setOverridden(pRow.getOverridden().booleanValue());
      }
      if (pRow.getModificationTime() != null)
      {
        result.setModificationTime(pRow.getModificationTime().toCalendar());
      }
      result.setModificationUserCode(pRow.getModificationUserCode());
    }
    return result;
  }

  public static void toRow(ProviderCrcType pBean, ProviderCrcViewRow pRow)
  {
    if (pBean != null && pRow != null)
    {
      if (pRow.getCrc() == null)
      {
        pRow.setCrc(pBean.getCrc());
      }
      pRow.setBatchName(pBean.getBatchName());
      pRow.setOverridden(new Booleans(pBean.getOverridden()));
      if (pBean.getModificationTime() != null)
      {
        pRow.setModificationTime(new Timestamp(pBean.getModificationTime()));
      }
      else
      {
        pRow.setModificationTime(new Timestamp(Calendar.getInstance()));
      }
    }
  }
}
