// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

public class LmcMsgActionResponse extends LmcSoapResponse {

    private String mIDList;
    private String mOp;

    public String getMsgList() { return mIDList; }
    public String getOp() { return mOp; }
    
    public void setMsgList(String idList) { mIDList = idList; }
    public void setOp(String op) { mOp = op; }
}
