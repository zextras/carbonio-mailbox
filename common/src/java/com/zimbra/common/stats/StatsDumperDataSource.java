// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import java.util.Collection;

public interface StatsDumperDataSource {

  /**
   * Returns the name of the file that stats are written to. This is a simple filename with no
   * directory path. All stats files are currently written to /opt/zextras/zmstat.
   */
  String getFilename();

  /** Returns the first line logged in a new stats file, or <tt>null</tt> if there is no header. */
  String getHeader();

  /** Returns the latest set of data lines and resets counters. */
  Collection<String> getDataLines();

  /**
   * Specifies whether a <tt>timestamp</tt> column is prepended to the data returned by
   * <tt>getHeader()</tt> and <tt>getDataLines()</tt>.
   */
  boolean hasTimestampColumn();
}
