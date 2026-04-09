// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.znative;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class formerly used to load the JNI native library. The native library has been replaced
 * by Java FFM and stdlib equivalents in {@link IO}, so this class now always reports native code as
 * available (the FFM implementation works on any Linux without a {@code .so}).
 */
public final class Util {

  private Util() {}

  /** Always returns {@code true} — FFM-based IO works without a native {@code .so}. */
  public static boolean haveNativeCode() {
    return true;
  }

  public static boolean loadLibrary() {
    return true;
  }

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
