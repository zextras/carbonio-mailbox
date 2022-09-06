// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.property.CalDavProperty;
import com.zimbra.cs.dav.property.ResourceProperty;
import com.zimbra.cs.mailbox.Mountpoint;
import java.util.Locale;

public class RemoteCalendarCollection extends RemoteCollection {

  public RemoteCalendarCollection(DavContext ctxt, Mountpoint mp)
      throws DavException, ServiceException {
    super(ctxt, mp);
    Account acct = mp.getAccount();

    addResourceType(DavElements.E_CALENDAR);

    Locale lc = acct.getLocale();
    String description =
        L10nUtil.getMessage(
            MsgKey.caldavCalendarDescription,
            lc,
            acct.getAttr(Provisioning.A_displayName),
            mp.getName());
    ResourceProperty desc = new ResourceProperty(DavElements.E_CALENDAR_DESCRIPTION);
    desc.setMessageLocale(lc);
    desc.setStringValue(description);
    desc.setVisible(false);
    addProperty(desc);
    addProperty(CalDavProperty.getSupportedCalendarComponentSet(mp.getDefaultView()));
    addProperty(CalDavProperty.getSupportedCalendarData());
    addProperty(CalDavProperty.getSupportedCollationSet());

    addProperty(getIcalColorProperty());
    setProperty(DavElements.E_ALTERNATE_URI_SET, null, true);
    setProperty(DavElements.E_GROUP_MEMBER_SET, null, true);
    setProperty(DavElements.E_GROUP_MEMBERSHIP, null, true);
  }

  public short getRights() {
    return mRights;
  }
}
