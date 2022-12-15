// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcGetMsgPartResponse extends LmcSoapResponse {

    private LmcMessage mMsg;

    /**
     * Get the message that includes the MIME part that was requested.
     */
    public LmcMessage getMessage() { return mMsg; }

    public void setMessage(LmcMessage m) { mMsg = m; }
}
