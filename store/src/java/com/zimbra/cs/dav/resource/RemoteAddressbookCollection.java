// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.mailbox.Mountpoint;

public class RemoteAddressbookCollection extends RemoteCollection {

    public RemoteAddressbookCollection(DavContext ctxt, Mountpoint mp)
            throws DavException, ServiceException {
        super(ctxt, mp);
        AddressbookCollection.setupAddressbookCollection(this, ctxt, mp);
    }

    public short getRights() {
        return mRights;
    }
}
