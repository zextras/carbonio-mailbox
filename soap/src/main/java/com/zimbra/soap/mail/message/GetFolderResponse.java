// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.Mountpoint;
import com.zimbra.soap.mail.type.SearchFolder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/*
<GetFolderResponse>
  <folder ...>
    <folder .../>
    <folder ...>
      <folder .../>
    </folder>
    <folder .../>
    [<link .../>]
    [<search .../>]
  </folder>
</GetFolderResponse>
 */
@XmlRootElement(name = MailConstants.E_GET_FOLDER_RESPONSE)
@XmlType(propOrder = {MailConstants.E_FOLDER})
public class GetFolderResponse {

  /**
   * @zm-api-field-description Folder information
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_FOLDER /* folder */, type = Folder.class),
    @XmlElement(name = MailConstants.E_MOUNT /* link */, type = Mountpoint.class),
    @XmlElement(name = MailConstants.E_SEARCH /* search */, type = SearchFolder.class)
  })
  private Folder folder;

  public GetFolderResponse() {}

  public Folder getFolder() {
    return folder;
  }

  public void setFolder(Folder folder) {
    this.folder = folder;
  }
}
