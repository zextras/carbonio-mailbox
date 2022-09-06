// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import com.zimbra.common.service.ServiceException;

/** Common interface for servers based on either {@link TcpServer} or {@link NioServer}. */
public interface Server {
  /**
   * Returns the server name (e.g. ImapServer), which is used for thread name, logging, statistics
   * and etc.
   */
  String getName();

  ServerConfig getConfig();

  void start() throws ServiceException;

  void stop() throws ServiceException;

  void stop(int graceSecs) throws ServiceException;
}
