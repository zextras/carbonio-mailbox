// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class QA {

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Bug {
    int[] bug();
  }
}
