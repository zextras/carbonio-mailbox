// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

public interface ZimbraTag {

    /**
     * Return the ID of the tag
     */
    public int getTagId();

    /**
     * Return the name of the tag;
     */
    public String getTagName();

}
