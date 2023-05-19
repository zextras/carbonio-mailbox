// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.common.mailbox;

public interface ZimbraTag {

    /**
     * Return the ID of the tag
     */
    int getTagId();

    /**
     * Return the name of the tag;
     */
    String getTagName();

}
