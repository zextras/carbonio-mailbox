// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SET_METADATA_RESPONSE)
public class SetCustomMetadataResponse {

  /**
   * @zm-api-field-tag item-id
   * @zm-api-field-description Item ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SetCustomMetadataResponse() {
    this((String) null);
  }

  public SetCustomMetadataResponse(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
