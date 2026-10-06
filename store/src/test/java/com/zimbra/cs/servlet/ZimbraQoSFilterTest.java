// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.util.AuthUtil;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ZimbraQoSFilterTest {

  @Test
  void requestOverTheLimitIsDispatchedOnTimeoutSoItProceeds() throws Exception {
    ZimbraQoSFilter filter = new ZimbraQoSFilter();
    filter.init(
        new FilterConfig() {
          public String getFilterName() {
            return "qos";
          }

          public ServletContext getServletContext() {
            return null;
          }

          public String getInitParameter(String name) {
            return "waitMs".equals(name) ? "1" : null;
          }

          public Enumeration<String> getInitParameterNames() {
            return Collections.emptyEnumeration();
          }
        });

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");
    HttpServletResponse resp = mock(HttpServletResponse.class);
    AsyncContext ctx = mock(AsyncContext.class);
    when(req.startAsync(req, resp)).thenReturn(ctx);

    int max = LC.servlet_max_concurrent_http_requests_per_account.intValue();
    AtomicInteger depth = new AtomicInteger();
    FilterChain[] chain = new FilterChain[1];
    chain[0] =
        (rq, rs) -> {
          // keep holding the permits until the limit is exhausted, then one more request
          if (depth.incrementAndGet() <= max) {
            filter.doFilter(req, resp, chain[0]);
          }
        };

    try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class);
        MockedStatic<AuthProvider> authProvider = mockStatic(AuthProvider.class);
        MockedStatic<ZimbraServlet> servlet = mockStatic(ZimbraServlet.class)) {
      filter.doFilter(req, resp, chain[0]);
    }

    assertEquals(max, depth.get());
    ArgumentCaptor<AsyncListener> captor = ArgumentCaptor.forClass(AsyncListener.class);
    verify(ctx).addListener(captor.capture());
    verify(ctx).setTimeout(anyLong());
    verify(ctx, never()).dispatch();

    captor.getValue().onTimeout(new AsyncEvent(ctx));

    verify(ctx).dispatch();
  }
}
