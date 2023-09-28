// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

/**
 * Factory that creates and instances of {@link Log}.
 *
 * @author bburtin
 * @since 17.2
 *     <!-- Last commit on this file before mine -->
 * @see LogManager to interact and manage {@link Log} classes
 * @see org.apache.logging.log4j.core.config.ConfigurationFactory to see how {@link
 *     org.apache.logging.log4j.Logger} are configured
 */
public class LogFactory {

  private LogFactory() {}

  public static Log getLog(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }

    return getLog(clazz.getName());
  }

  public static Log getLog(String name) {
    return LogManager.getGlobalLogMapper()
        .computeIfAbsent(
            name, n -> new Log(org.apache.logging.log4j.LogManager.getContext().getLogger(n)));
  }
}
