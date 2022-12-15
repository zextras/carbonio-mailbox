// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import java.util.*;

import com.zimbra.cs.client.*;

public class LmcGetTagResponse extends LmcSoapResponse {

    // for storing the returned tags
    private ArrayList mTags;

    public LmcTag[] getTags() {
        if (mTags == null || mTags.size() == 0)
        	return null;
        LmcTag tags[] = new LmcTag[mTags.size()];
        return (LmcTag []) mTags.toArray(tags);
    }
    
    public void setTags(ArrayList a) { mTags = a; }
}
