// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.util;

/**
 * Renders a size in bytes using the largest 1024-based unit that keeps the value above one.
 */
public final class ByteSizeFormatter {

  private static final long KILOBYTES = 1024L;
  private static final long MEGABYTES = KILOBYTES * 1024L;
  private static final long GIGABYTES = MEGABYTES * 1024L;

  public static String format(long bytes) {
    if (bytes >= GIGABYTES) {
      return String.format("%.2f GB", ((double) bytes) / GIGABYTES);
    } else if (bytes >= MEGABYTES) {
      return String.format("%.2f MB", ((double) bytes) / MEGABYTES);
    } else if (bytes >= KILOBYTES) {
      return String.format("%.2f KB", ((double) bytes) / KILOBYTES);
    } else {
      return String.format("%d B", bytes);
    }
  }

  private ByteSizeFormatter() {
    throw new UnsupportedOperationException("Utility class and cannot be instantiated");
  }
}
