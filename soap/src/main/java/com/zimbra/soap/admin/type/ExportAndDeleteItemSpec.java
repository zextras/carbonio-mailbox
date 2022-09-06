// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ExportAndDeleteItemSpec {

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID /* id */, required = true)
  private final int id;

  /**
   * @zm-api-field-tag version
   * @zm-api-field-description Version
   */
  @XmlAttribute(name = AdminConstants.A_VERSION_INFO_VERSION /* version */, required = true)
  private final int version;

  /** no-argument constructor wanted by JAXB */
  private ExportAndDeleteItemSpec() {
    this(-1, -1);
  }

  private ExportAndDeleteItemSpec(int id, int version) {
    this.id = id;
    this.version = version;
  }

  public static ExportAndDeleteItemSpec createForIdAndVersion(int id, int version) {
    return new ExportAndDeleteItemSpec(id, version);
  }

  public int getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("version", version);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
