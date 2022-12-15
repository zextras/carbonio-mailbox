// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlType;

import com.zimbra.soap.type.RssDataSource;

@XmlType(propOrder= {})
public class AccountRssDataSource
extends AccountDataSource
implements RssDataSource {

    public AccountRssDataSource() {
    }
    
    public AccountRssDataSource(RssDataSource data) {
        super(data);
    }
}
