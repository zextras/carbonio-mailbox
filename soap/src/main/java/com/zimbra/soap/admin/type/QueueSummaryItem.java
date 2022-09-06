// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class QueueSummaryItem {

  /**
   * @zm-api-field-tag q-summ-count
   * @zm-api-field-description Count
   */
  @XmlAttribute(name = AdminConstants.A_N, required = true)
  private final int count;

  /**
   * @zm-api-field-tag text-for-item
   * @zm-api-field-description Text for item. e.g. "connect to 10.10.20.40 failed"
   */
  @XmlAttribute(name = AdminConstants.A_T, required = true)
  private final String term;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private QueueSummaryItem() {
    this(-1, (String) null);
  }

  public QueueSummaryItem(int count, String term) {
    this.count = count;
    this.term = term;
  }

  public int getCount() {
    return count;
  }

  public String getTerm() {
    return term;
  }
}
