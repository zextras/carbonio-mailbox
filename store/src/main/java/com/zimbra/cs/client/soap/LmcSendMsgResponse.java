// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

public class LmcSendMsgResponse extends LmcSoapResponse {

    private String mID;

    /**
     * Get the ID of the created message.
     */
    public String getID() { return mID; }

    public void setID(String id) { mID = id; }

}
