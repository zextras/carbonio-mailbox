// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.Namespace;

public final class SyncAdminConstants {
    public static final String NAMESPACE_STR = AdminConstants.NAMESPACE_STR;
    public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);

    public static final String E_LAST_USED_DATE = "lastUsedDate";

    public static final String A_COUNT = "count";
    public static final String A_DATE = "date";
    public static final String A_LAST_USED_DATE_OLDER_THAN = "lastUsedDateOlderThan";
}
