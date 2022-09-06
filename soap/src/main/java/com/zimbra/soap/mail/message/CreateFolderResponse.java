// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.Mountpoint;
import com.zimbra.soap.mail.type.SearchFolder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_CREATE_FOLDER_RESPONSE)
public class CreateFolderResponse {

  /**
   * @zm-api-field-description Information about created folder
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_FOLDER, type = Folder.class),
    @XmlElement(name = MailConstants.E_MOUNT, type = Mountpoint.class),
    @XmlElement(name = MailConstants.E_SEARCH, type = SearchFolder.class)
  })
  private Folder folder;

  public CreateFolderResponse() {}

  public void setFolder(Folder folder) {
    this.folder = folder;
  }

  public Folder getFolder() {
    return folder;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("folder", folder).toString();
  }
}
