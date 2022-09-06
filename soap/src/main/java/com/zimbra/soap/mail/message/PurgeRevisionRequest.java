// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.PurgeRevisionSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Purge revision
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_PURGE_REVISION_REQUEST)
public class PurgeRevisionRequest {

  /**
   * @zm-api-field-description Specification or revision to purge
   */
  @XmlElement(name = MailConstants.E_REVISION /* revision */, required = true)
  private final PurgeRevisionSpec revision;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private PurgeRevisionRequest() {
    this((PurgeRevisionSpec) null);
  }

  public PurgeRevisionRequest(PurgeRevisionSpec revision) {
    this.revision = revision;
  }

  public PurgeRevisionSpec getRevision() {
    return revision;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("revision", revision);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
