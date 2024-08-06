package com.cylande.unitedretail.batch.logging;

import com.cylande.unitedretail.framework.logging.MDCEntries;

/** {@inheritDoc} */
public enum BatchEditionMDCEntries implements MDCEntries
{
//CHECKSTYLE:OFF
;
//CHECKSTYLE:ON
  private String _label;

  BatchEditionMDCEntries(String pLabel)
  {
    _label = pLabel;
  }

  /** {@inheritDoc} */
  public String getLabel()
  {
    return _label;
  }
}
