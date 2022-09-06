// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.UUID;

// just a wrapper to call UUID.randomUUID()
// so we can use another UUID generator if we want to (highly unlikely though)
public class UUIDUtil {

  /**
   * Returns a new UUID.
   *
   * @return
   */
  public static String generateUUID() {
    return UUID.randomUUID().toString();
  }
}
