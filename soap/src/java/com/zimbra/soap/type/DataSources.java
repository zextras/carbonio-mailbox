// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.soap.account.type.AccountCalDataSource;
import com.zimbra.soap.account.type.AccountDataSource;
import com.zimbra.soap.account.type.AccountImapDataSource;
import com.zimbra.soap.account.type.AccountPop3DataSource;
import com.zimbra.soap.account.type.AccountRssDataSource;
import com.zimbra.soap.account.type.AccountUnknownDataSource;

public class DataSources {

    public static AccountDataSource newDataSource(DataSource data) {
        return new AccountDataSource(data);
    }

    public static AccountDataSource newDataSource() {
        return new AccountUnknownDataSource();
    }

    public static Pop3DataSource newPop3DataSource() {
        return new AccountPop3DataSource();
    }

    public static Pop3DataSource newPop3DataSource(Pop3DataSource data) {
        return new AccountPop3DataSource(data);
    }

    public static ImapDataSource newImapDataSource() {
        return new AccountImapDataSource();
    }

    public static ImapDataSource newImapDataSource(ImapDataSource data) {
        return new AccountImapDataSource(data);
    }

    public static RssDataSource newRssDataSource() {
        return new AccountRssDataSource();
    }

    public static RssDataSource newRssDataSource(RssDataSource data) {
        return new AccountRssDataSource(data);
    }

    public static CalDataSource newCalDataSource() {
        return new AccountCalDataSource();
    }

    public static CalDataSource newCalDataSource(CalDataSource data) {
        return new AccountCalDataSource(data);
    }
}
