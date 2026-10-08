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

    // limit of one concurrent request per account: while the first request holds its permit
    // (we are inside its chain) a second one cannot get it and must be delayed
    final String original = LC.servlet_max_concurrent_http_requests_per_account.value();
    LC.servlet_max_concurrent_http_requests_per_account.setDefault(1);
    AtomicInteger admitted = new AtomicInteger();
    FilterChain admittedChain = (rq, rs) -> admitted.incrementAndGet();
    FilterChain firstRequest =
        (rq, rs) -> {
          admitted.incrementAndGet();
          try {
            filter.doFilter(req, resp, admittedChain);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        };

    try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class);
        MockedStatic<AuthProvider> authProvider = mockStatic(AuthProvider.class);
        MockedStatic<ZimbraServlet> servlet = mockStatic(ZimbraServlet.class)) {
      filter.doFilter(req, resp, firstRequest);
    } finally {
      LC.servlet_max_concurrent_http_requests_per_account.setDefault(original);
    }

    // only the first request was admitted, the second one was suspended
    assertEquals(1, admitted.get());
    ArgumentCaptor<AsyncListener> captor = ArgumentCaptor.forClass(AsyncListener.class);
    verify(ctx).addListener(captor.capture());
    verify(ctx).setTimeout(anyLong());
    verify(ctx, never()).dispatch();

    captor.getValue().onTimeout(new AsyncEvent(ctx));

    verify(ctx).dispatch();
  }
}
