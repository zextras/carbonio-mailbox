// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

public interface JmxImapDaemonStatsMBean extends JmxStatsMBeanBase {
    long getImapRequests();
    long getImapResponseMs();
}
