// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/** */
package com.zimbra.cs.rmgmt;

import java.io.InputStream;

public interface RemoteBackgroundHandler {

  public void read(InputStream stdout, InputStream stderr);

  public void error(Throwable t);
}
