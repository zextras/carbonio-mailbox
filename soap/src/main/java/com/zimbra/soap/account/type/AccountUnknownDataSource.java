// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import com.zimbra.soap.type.DataSource;

@XmlAccessorType(XmlAccessType.NONE)
public class AccountUnknownDataSource extends AccountDataSource {
    public AccountUnknownDataSource(DataSource from) {
        copy(from);
    }

    public AccountUnknownDataSource() {
    }
}
