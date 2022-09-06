// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;

public class ZModifyContactEvent extends ZContactEvent
    implements ZModifyItemEvent, ZModifyItemFolderEvent {

  public ZModifyContactEvent(Element e) throws ServiceException {
    super(e);
  }
}
