// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class WikiIdsAttr extends IdsAttr {
    public WikiIdsAttr(String ids) {
        super(ids);
    }

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private WikiIdsAttr() {
        this((String) null);
    }
}
