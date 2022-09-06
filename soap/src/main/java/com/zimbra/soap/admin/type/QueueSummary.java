// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class QueueSummary {

  /**
   * @zm-api-field-tag reason|to|from|todomain|fromdomain|addr|host
   * @zm-api-field-description Queue summary type -
   *     <b>reason|to|from|todomain|fromdomain|addr|host</b>
   */
  @XmlAttribute(name = AdminConstants.A_TYPE, required = true)
  private final String type;

  /**
   * @zm-api-field-description Queue summary items
   */
  @XmlElement(name = AdminConstants.A_QUEUE_SUMMARY_ITEM /* qsi */, required = true)
  private List<QueueSummaryItem> items = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private QueueSummary() {
    this((String) null);
  }

  public QueueSummary(String type) {
    this.type = type;
  }

  public void setItems(Iterable<QueueSummaryItem> items) {
    this.items.clear();
    if (items != null) {
      Iterables.addAll(this.items, items);
    }
  }

  public QueueSummary addItem(QueueSummaryItem item) {
    this.items.add(item);
    return this;
  }

  public String getType() {
    return type;
  }

  public List<QueueSummaryItem> getItems() {
    return Collections.unmodifiableList(items);
  }
}
