// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExceptionToStringTest {

  @Test
  void includesMessageAndCause() {
    String trace = ExceptionToString.ToString(new RuntimeException("outer", new IllegalStateException("inner")));
    assertTrue(trace.startsWith("java.lang.RuntimeException: outer"));
    assertTrue(trace.contains("Caused by: java.lang.IllegalStateException: inner"));
  }
}
