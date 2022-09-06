// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.dav.DavElements;

/**
 * principal resource - a group containing the list of principals for calendar users who can act as
 * a read-write proxy.
 */
public class CalendarProxyWrite extends AbstractCalendarProxy {
  public static final String CALENDAR_PROXY_WRITE = "calendar-proxy-write";

  public CalendarProxyWrite(Account acct, String url) throws ServiceException {
    super(acct, url, DavElements.E_CALENDAR_PROXY_WRITE, false);
  }

  public CalendarProxyWrite(String user, String url) throws ServiceException {
    super(user, url + CALENDAR_PROXY_WRITE, DavElements.E_CALENDAR_PROXY_WRITE, false);
  }
}
