/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.service.MockHttpServletResponse;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SizeTrackingHttpServletResponseTest {

  private SizeTrackingHttpServletResponse response;

  @BeforeEach
  void setUp() {
    response = new SizeTrackingHttpServletResponse(new MockHttpServletResponse());
  }

  @Test
  void shouldCountBytesWrittenToOutputStream() throws Exception {
    final ServletOutputStream out = response.getOutputStream();

    out.write('a');
    out.write("hello".getBytes());
    out.write("full content".getBytes(), 5, 7);
    out.flush();
    out.close();

    assertEquals(13, response.getSize());
  }

  @Test
  void shouldDelegateOutputStreamReadiness() throws Exception {
    final ServletOutputStream out = response.getOutputStream();

    assertTrue(out.isReady());
    out.setWriteListener(null);
    assertEquals(0, response.getSize());
  }

  @Test
  void shouldCountCharactersWrittenThroughWriter() throws Exception {
    final PrintWriter writer = response.getWriter();

    writer.write('a');
    writer.print("hello");
    writer.write(new char[] {'x', 'y', 'z'}, 0, 3);
    writer.flush();

    assertEquals(9, response.getSize());
  }

  @Test
  void shouldAccumulateSizeAcrossStreamAndWriter() throws Exception {
    response.getOutputStream().write("bytes".getBytes());
    final PrintWriter writer = response.getWriter();
    writer.print("chars");
    writer.flush();

    assertEquals(10, response.getSize());
  }
}
