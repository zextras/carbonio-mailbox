// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ZItem;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;

public class ZCreateContactEvent extends ZContactEvent implements ZCreateItemEvent {

  public ZCreateContactEvent(Element e) throws ServiceException {
    super(e);
  }

  public ZItem getItem() throws ServiceException {
    return null;
  }
}
