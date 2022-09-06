// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.NewMountpointSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create mountpoint
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_CREATE_MOUNTPOINT_REQUEST)
public class CreateMountpointRequest {

  /**
   * @zm-api-field-tag link
   * @zm-api-field-description New mountpoint specification
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_MOUNT /* link */, required = true)
  private final NewMountpointSpec folder;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CreateMountpointRequest() {
    this((NewMountpointSpec) null);
  }

  public CreateMountpointRequest(NewMountpointSpec folder) {
    this.folder = folder;
  }

  public NewMountpointSpec getFolder() {
    return folder;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("folder", folder);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
