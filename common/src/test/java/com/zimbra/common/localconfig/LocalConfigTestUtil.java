// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.localconfig;

public class LocalConfigTestUtil {
  public static void setLC(KnownKey key, String value) {
    key.setValue(value);
  }

  public static void resetLC(KnownKey key) {
    key.setValue(null);
  }
}
