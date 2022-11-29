// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 2, 2004
 *
 */
package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.mail.Action;

public class ActionTag implements Action {

    private String mTagName;
    
    public ActionTag(String tagName) {
        mTagName = tagName;
    }

    public String getTagName() {
        return mTagName;
    }

    public void setTagName(String tagName) {
        this.mTagName = tagName;
    }
    
    public String toString() {
        return "ActionTag, tag=" + mTagName;
    }
}
