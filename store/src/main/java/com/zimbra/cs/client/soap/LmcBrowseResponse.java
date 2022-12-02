// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcBrowseResponse extends LmcSoapResponse {

    private LmcBrowseData mData[];
    
    public void setData(LmcBrowseData d[]) { mData = d; }
    
    public LmcBrowseData[] getData() { return mData; }
}
