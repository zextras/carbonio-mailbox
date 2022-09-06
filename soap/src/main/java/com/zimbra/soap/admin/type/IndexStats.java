// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class IndexStats {
  /**
   * @zm-api-field-tag max-docs
   * @zm-api-field-description total number of docs in this index
   */
  @XmlAttribute(name = AdminConstants.A_MAX_DOCS /* maxDocs */, required = true)
  private final int maxDocs;

  /**
   * @zm-api-field-tag deleted-docs
   * @zm-api-field-description number of deleted docs for the index
   */
  @XmlAttribute(name = AdminConstants.A_DELETED_DOCS /* totalSize */, required = true)
  private final int numDeletedDocs;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IndexStats() {
    this(0, 0);
  }

  public IndexStats(int maxDocs, int numDeletedDocs) {
    this.maxDocs = maxDocs;
    this.numDeletedDocs = numDeletedDocs;
  }

  public int getMaxDocs() {
    return maxDocs;
  }

  public int getNumDeletedDocs() {
    return numDeletedDocs;
  }
}
