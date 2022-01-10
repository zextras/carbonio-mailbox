// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client;

public class LmcContactAttr {
    
    private String mAttrName;
    private String mID;
    private String mRef;
    private String mAttrData;

    public LmcContactAttr(String attrName,
                          String id,
                          String ref,
                          String attrData)
    {
        mAttrName = attrName;
        mID = id;
        mRef = ref;
        mAttrData = attrData;
    }
    
    public String getAttrName() { return mAttrName; }
    
    public String getID() { return mID; }
    
    public String getRef() { return mRef; }
    
    public String getAttrData() { return mAttrData; }
}