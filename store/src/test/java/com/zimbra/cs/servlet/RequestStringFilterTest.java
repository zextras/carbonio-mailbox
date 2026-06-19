// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.cs.service.MockHttpServletResponse;
import java.net.URL;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class RequestStringFilterTest {

  private static final String NUL = String.valueOf((char) 0);

  private final RequestStringFilter filter = new RequestStringFilter();

  @Test
  void cleanRequestPassesThrough() throws Exception {
    assertTrue(passesThrough(request("q=clean", "/service/soap/")));
  }

  @Test
  void nullQueryAndUriPassThrough() throws Exception {
    assertTrue(passesThrough(request(null, null)));
  }

  @Test
  void encodedNullInQueryStringIsRejected() throws Exception {
    assertRejected(request("q=%00", "/service/soap/"));
  }

  @Test
  void rawNullInQueryStringIsRejected() throws Exception {
    assertRejected(request("q=" + NUL, "/service/soap/"));
  }

  @Test
  void encodedNullInUriIsRejected() throws Exception {
    assertRejected(request(null, "/service/%00/soap"));
  }

  @Test
  void rawNullInUriIsRejected() throws Exception {
    assertRejected(request(null, "/service/" + NUL + "/soap"));
  }

  private boolean passesThrough(MockHttpServletRequest request) throws Exception {
    RecordingChain chain = new RecordingChain();
    filter.doFilter(request, new RecordingResponse(), chain);
    return chain.invoked;
  }

  private void assertRejected(MockHttpServletRequest request) throws Exception {
    RecordingChain chain = new RecordingChain();
    RecordingResponse response = new RecordingResponse();
    filter.doFilter(request, response, chain);
    assertFalse(chain.invoked);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.error);
  }

  private static MockHttpServletRequest request(String query, String uri) throws Exception {
    return new MockHttpServletRequest(new byte[0], new URL("http://localhost/"), "text/xml") {
      @Override
      public String getQueryString() {
        return query;
      }

      @Override
      public String getRequestURI() {
        return uri;
      }
    };
  }

  private static class RecordingChain implements FilterChain {
    boolean invoked = false;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) {
      invoked = true;
    }
  }

  private static class RecordingResponse extends MockHttpServletResponse {
    int error = -1;

    @Override
    public void sendError(int sc) {
      this.error = sc;
    }
  }
}
