// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Rights;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_EFFECTIVE_FOLDER_PERMS_RESPONSE)
public class GetEffectiveFolderPermsResponse {

  /**
   * @zm-api-field-description Folder permissions information
   */
  @XmlElement(name = MailConstants.E_FOLDER /* folder */, required = true)
  private final Rights folder;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetEffectiveFolderPermsResponse() {
    this((Rights) null);
  }

  public GetEffectiveFolderPermsResponse(Rights folder) {
    this.folder = folder;
  }

  public Rights getFolder() {
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
