// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcGetConvResponse extends LmcSoapResponse {

    private LmcConversation mConv;

    public LmcConversation getConv() { return mConv; }

    public void setConv(LmcConversation c) { mConv = c; }

}
