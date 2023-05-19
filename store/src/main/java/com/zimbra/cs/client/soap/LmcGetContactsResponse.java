// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcGetContactsResponse extends LmcSoapResponse {

    private LmcContact[] mContacts;

    public LmcContact[] getContacts() { return mContacts; }

    public void setContacts(LmcContact[] s) { mContacts = s; }
}
