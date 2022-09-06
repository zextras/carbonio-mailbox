// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.MailCustomMetadata;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_MAILBOX_METADATA_RESPONSE)
public class GetMailboxMetadataResponse {

  /**
   * @zm-api-field-description Metadata information
   */
  @XmlElement(name = MailConstants.E_METADATA /* meta */, required = false)
  private MailCustomMetadata metadata;

  public GetMailboxMetadataResponse() {}

  public void setMetadata(MailCustomMetadata metadata) {
    this.metadata = metadata;
  }

  public MailCustomMetadata getMetadata() {
    return metadata;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("metadata", metadata);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
