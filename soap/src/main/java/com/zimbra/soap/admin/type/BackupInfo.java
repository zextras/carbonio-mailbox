// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class BackupInfo {

  /**
   * @zm-api-field-tag full-backup-set-label
   * @zm-api-field-description Full backup set label
   */
  @XmlAttribute(name = BackupConstants.A_LABEL /* label */, required = false)
  private String label;

  /**
   * @zm-api-field-tag incremental-backup-label
   * @zm-api-field-description Incremental backup label
   */
  @XmlAttribute(name = BackupConstants.A_INCR_LABEL /* incr-label */, required = false)
  private String incrementalLabel;

  public BackupInfo() {}

  public void setLabel(String label) {
    this.label = label;
  }

  public void setIncrementalLabel(String incrementalLabel) {
    this.incrementalLabel = incrementalLabel;
  }

  public String getLabel() {
    return label;
  }

  public String getIncrementalLabel() {
    return incrementalLabel;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("label", label).add("incrementalLabel", incrementalLabel);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
