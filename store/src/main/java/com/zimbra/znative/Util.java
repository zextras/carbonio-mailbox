// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.znative;

import com.zimbra.common.util.ZimbraLog;

public final class Util {

  private Util() {}

  public static void halt(String message) {
    try {
      ZimbraLog.system.fatal("terminating: " + message);
    } finally {
      Runtime.getRuntime().halt(1);
    }
  }

  public static void halt(String message, Throwable t) {
    try {
      ZimbraLog.system.fatal("terminating: " + message, t);
    } finally {
      Runtime.getRuntime().halt(1);
    }
  }
}
