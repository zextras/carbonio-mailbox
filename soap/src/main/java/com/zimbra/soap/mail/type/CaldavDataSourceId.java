// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import com.zimbra.soap.type.Id;

@XmlAccessorType(XmlAccessType.NONE)
public class CaldavDataSourceId extends Id {
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    protected CaldavDataSourceId() {
        this(null);
    }

    CaldavDataSourceId(String id) {
        super(id);
    }
}
