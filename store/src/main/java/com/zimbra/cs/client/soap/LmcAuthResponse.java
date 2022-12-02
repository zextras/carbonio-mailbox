// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcAuthResponse extends LmcSoapResponse {

    private LmcSession mSession;

    public void setSession(LmcSession s) { mSession = s; }
    
    public LmcSession getSession() { return mSession; }

}
    
