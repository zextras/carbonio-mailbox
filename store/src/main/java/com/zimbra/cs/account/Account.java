// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning.GroupMembership;
import com.zimbra.cs.account.Provisioning.SetPasswordResult;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.account.ldap.AccountEntry;
import com.zimbra.soap.admin.type.DataSourceType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author schemers
 */
public class Account extends AccountEntry implements GroupedEntry, AliasedEntry, ZAttrAccount<Provisioning> {

}
