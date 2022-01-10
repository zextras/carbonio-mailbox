// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.common.soap.Element;

public final class WildcardExpansionQueryInfo implements QueryInfo {

    private String mStr;
    private int mNumExpanded;
    private boolean mExpandedAll;

    public WildcardExpansionQueryInfo(String baseStr, int numExpanded, boolean expandedAll) {
        mStr = baseStr;
        mNumExpanded = numExpanded;
        mExpandedAll = expandedAll;
    }

    @Override
    public Element toXml(Element parent) {
        Element qinfo = parent.addElement("wildcard");
        qinfo.addAttribute("str", mStr);
        qinfo.addAttribute("expanded", mExpandedAll);
        qinfo.addAttribute("numExpanded", mNumExpanded);
        return qinfo;
    }

    @Override
    public String toString() {
        return "WILDCARD(" + mStr + "," + mNumExpanded + "," +
            (mExpandedAll ? "ALL" : "PARTIAL") + ")";
    }
}
