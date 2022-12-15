// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.soap.type.Id;

@XmlAccessorType(XmlAccessType.NONE)
public class GalDataSourceId extends Id {
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    protected GalDataSourceId() {
        this((String) null);
    }

    GalDataSourceId(String id) {
        super(id);
    }
}
