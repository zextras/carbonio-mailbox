// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.XMbxSearchConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class SearchID {

  /**
   * @zm-api-field-tag search-id
   * @zm-api-field-description Search ID
   */
  @XmlAttribute(name = XMbxSearchConstants.A_searchID /* searchID */, required = true)
  private final int searchID;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SearchID() {
    this(-1);
  }

  public SearchID(int searchID) {
    this.searchID = searchID;
  }

  public int getSearchID() {
    return searchID;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("searchID", searchID);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
