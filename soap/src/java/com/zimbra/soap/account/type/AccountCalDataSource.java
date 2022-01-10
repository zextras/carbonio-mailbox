// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlType;

import com.zimbra.soap.type.CalDataSource;

@XmlType(propOrder = {})
public class AccountCalDataSource
extends AccountDataSource
implements CalDataSource {

    public AccountCalDataSource() {
    }
    
    public AccountCalDataSource(CalDataSource data) {
        super(data);
    }
}
