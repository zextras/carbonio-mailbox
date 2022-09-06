// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ZFolder;
import com.zimbra.client.ZItem;
import com.zimbra.common.service.ServiceException;

public class ZCreateFolderEvent implements ZCreateItemEvent {

  protected ZFolder mFolder;

  public ZCreateFolderEvent(ZFolder folder) throws ServiceException {
    mFolder = folder;
  }

  /**
   * @return id of created folder
   * @throws com.zimbra.common.service.ServiceException
   */
  public String getId() throws ServiceException {
    return mFolder.getId();
  }

  public ZItem getItem() throws ServiceException {
    return mFolder;
  }

  public ZFolder getFolder() {
    return mFolder;
  }

  public String toString() {
    return mFolder.toString();
  }
}
