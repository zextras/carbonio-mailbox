// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class IdVersion {

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag version
   * @zm-api-field-description Version
   */
  @XmlAttribute(name = MailConstants.A_VERSION /* ver */, required = false)
  private Integer version;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IdVersion() {
    this((String) null);
  }

  public IdVersion(String id) {
    this.id = id;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public Integer getVersion() {
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
