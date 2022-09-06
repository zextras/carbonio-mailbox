// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author pshao
 */
public class CsvWriter {

  private BufferedWriter writer;

  public CsvWriter(Writer writer) throws IOException {
    this.writer = new BufferedWriter(writer);
  }

  public void writeRow(String... values) throws IOException {
    StringBuilder line = new StringBuilder();

    boolean first = true;
    for (String value : values) {
      if (!first) {
        line.append(",");
      } else {
        first = false;
      }
      line.append(value);
    }
    line.append("\n");
    writer.write(line.toString());
  }

  public void close() throws IOException {
    writer.close();
  }
}
