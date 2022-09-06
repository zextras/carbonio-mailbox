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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Set Mailbox Metadata
 *     <ul>
 *       <li>Setting a mailbox metadata section but providing no key/value pairs will remove the
 *           section from mailbox metadata
 *       <li>Empty value not allowed
 *       <li><b>{metadata-section-key}</b> must be no more than 36 characters long and must be in
 *           the format of <b>{namespace}:{section-name}</b>. currently the only valid namespace is
 *           <b>"zwc"</b>.
 *     </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SET_MAILBOX_METADATA_REQUEST)
public class SetMailboxMetadataRequest {

  /**
   * @zm-api-field-description New metadata information
   */
  @XmlElement(name = MailConstants.E_METADATA /* meta */, required = false)
  private MailCustomMetadata metadata;

  public SetMailboxMetadataRequest() {}

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
