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
public class DiffDocumentVersionSpec {

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = false)
  private String id;

  /**
   * @zm-api-field-tag revision-1
   * @zm-api-field-description Revision 1
   */
  @XmlAttribute(name = MailConstants.A_V1 /* v1 */, required = false)
  private Integer version1;

  /**
   * @zm-api-field-tag revision-2
   * @zm-api-field-description Revision 2
   */
  @XmlAttribute(name = MailConstants.A_V2 /* v2 */, required = false)
  private Integer version2;

  public DiffDocumentVersionSpec() {}

  public void setId(String id) {
    this.id = id;
  }

  public void setVersion1(Integer version1) {
    this.version1 = version1;
  }

  public void setVersion2(Integer version2) {
    this.version2 = version2;
  }

  public String getId() {
    return id;
  }

  public Integer getVersion1() {
    return version1;
  }

  public Integer getVersion2() {
    return version2;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("version1", version1).add("version2", version2);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
