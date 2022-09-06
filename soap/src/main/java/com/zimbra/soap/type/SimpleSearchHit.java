// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class SimpleSearchHit implements SearchHit {

  /**
   * @zm-api-field-tag hit-id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = false)
  private String id;

  /**
   * @zm-api-field-tag hit-sort-field
   * @zm-api-field-description Sort field value
   */
  @XmlAttribute(name = MailConstants.A_SORT_FIELD /* sf */, required = false)
  private String sortField;

  public SimpleSearchHit() {}

  public void setId(String id) {
    this.id = id;
  }

  public void setSortField(String sortField) {
    this.sortField = sortField;
  }

  public String getId() {
    return id;
  }

  public String getSortField() {
    return sortField;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("sortField", sortField);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
