// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.znative;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Util {

  private Util() {}

  public static void halt(String message) {
    try {
      System.err.println("Fatal error: terminating: " + message);
    } finally {
      Runtime.getRuntime().halt(1);
    }
  }

  public static void halt(String message, Throwable t) {
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println(message);
      t.printStackTrace(pw);
      System.err.println("Fatal error: terminating: " + sw);
    } finally {
      Runtime.getRuntime().halt(1);
    }
  }
}
