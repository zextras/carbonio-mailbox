// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * This interface is implemented by different filters to keep malicious content out of the
 * text/human readable code that we distribute down to the browsers.
 *
 * @author jpowers
 */
public interface BrowserDefang {

  // regex for URLs href. TODO: beef this up
  Pattern VALID_URL =
      Pattern.compile(
          "^(https?://[\\w-].*|mailto:.*|cid:.*|notes:.*|smb:.*|ftp:.*|gopher:.*|news:.*|tel:.*|callto:.*|webcal:.*|feed:.*:|file:.*|#.+)",
          Pattern.CASE_INSENSITIVE);
  Pattern VALID_IMG = Pattern.compile("^data:|^cid:|\\.(jpg|jpeg|png|gif)$");

  /**
   * Defangs a text element
   *
   * @param text
   * @param neuterImages
   * @return A safe version of that text to allow a browser to display
   * @throws IOException
   */
  String defang(String text, boolean neuterImages) throws IOException;

  /**
   * Reads an input stream and returns a string representation that's safe for browser consumption
   *
   * @param is
   * @param neuterImages
   * @return
   * @throws IOException
   */
  String defang(InputStream is, boolean neuterImages) throws IOException;

  /**
   * Uses a reader as an input, outputs safe text.
   *
   * @param reader
   * @param neuterImages
   * @return
   * @throws IOException
   */
  String defang(Reader reader, boolean neuterImages) throws IOException;

  /**
   * Defangs an input stream, writes it out to a writer
   *
   * @param is
   * @param neuterImages
   * @param out
   * @throws IOException
   */
  void defang(InputStream is, boolean neuterImages, Writer out) throws IOException;

  /**
   * Defangs a reader, sends it to a writer. A wise person might have just make this the interface
   * and gotten rid of the other methods
   *
   * @param reader
   * @param neuterImages
   * @param out
   * @throws IOException
   */
  void defang(Reader reader, boolean neuterImages, Writer out) throws IOException;
}
