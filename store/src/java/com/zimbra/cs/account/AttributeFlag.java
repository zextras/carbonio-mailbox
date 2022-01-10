// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

public enum AttributeFlag {
    accountInfo,
    accountInherited,
    accountCosDomainInherited,
    domainAdminModifiable,
    domainInfo,
    domainInherited,
    serverInherited,
    idn,
    serverPreferAlwaysOn,
    ephemeral,
    dynamic,
    expirable,
    octopus  // for tracking octopus specific attributes

}