// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import com.zimbra.cs.server.Server;

public interface LmtpServer extends Server {
    LmtpConfig getConfig();
}
