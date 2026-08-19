/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.audit;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper that tracks how much content is sent to the client, counting bytes written to
 * the output stream and characters written through the writer.
 */
public class SizeTrackingHttpServletResponse extends HttpServletResponseWrapper {

  private long size = 0;

  public SizeTrackingHttpServletResponse(HttpServletResponse response) {
    super(response);
  }

  public long getSize() {
    return size;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    ServletOutputStream out = super.getOutputStream();
    return new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return out.isReady();
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
        out.setWriteListener(writeListener);
      }

      @Override
      public void write(int b) throws IOException {
        out.write(b);
        size++;
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        size += len;
      }

      @Override
      public void flush() throws IOException {
        out.flush();
      }

      @Override
      public void close() throws IOException {
        out.close();
      }
    };
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    PrintWriter writer = super.getWriter();
    return new PrintWriter(writer) {
      @Override
      public void write(int c) {
        super.write(c);
        size++;
      }

      @Override
      public void write(char[] buf, int off, int len) {
        super.write(buf, off, len);
        size += len;
      }

      @Override
      public void write(String s, int off, int len) {
        super.write(s, off, len);
        size += len;
      }
    };
  }
}
