// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Does nothing interesting...
 *
 * @author jpowers
 */
public class NoopDefang implements BrowserDefang {

  @Override
  public String defang(String text, boolean neuterImages) throws IOException {
    return text;
  }

  @Override
  public String defang(InputStream is, boolean neuterImages) throws IOException {
    InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
    return defang(reader, neuterImages);
  }

  @Override
  public String defang(Reader reader, boolean neuterImages) throws IOException {
    StringWriter writer = new StringWriter();
    char[] buffer = new char[256];
    int read;
    while ((read = reader.read(buffer)) > 0) {
      writer.write(buffer, 0, read);
    }

    return writer.toString();
  }

  @Override
  public void defang(InputStream is, boolean neuterImages, Writer out) throws IOException {
    InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
    defang(reader, neuterImages, out);
  }

  @Override
  public void defang(Reader reader, boolean neuterImages, Writer out) throws IOException {
    char[] buffer = new char[256];
    int read;
    while ((read = reader.read(buffer)) > 0) {
      out.write(buffer, 0, read);
    }
  }
}
