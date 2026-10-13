// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.cs.service.MockHttpServletResponse;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ContextPathBasedThreadPoolBalancerFilterTest {

  @Test
  void passesThroughWhenNoRulesConfigured() throws Exception {
    ContextPathBasedThreadPoolBalancerFilter filter = initFilter("");
    RecordingChain chain = new RecordingChain();
    filter.doFilter(request(), new MockHttpServletResponse(), chain);
    assertTrue(chain.invoked);
  }

  @Test
  void malformedRulesAreRejectedAtInit() {
    assertThrows(ServletException.class, () -> initFilter("garbage"));
  }

  @Test
  void suspendedRequestIsDispatchedOnTimeoutSoItProceeds() throws Exception {
    ContextPathBasedThreadPoolBalancerFilter filter =
        new ContextPathBasedThreadPoolBalancerFilter() {
          @Override
          protected boolean shouldSuspend(ServletRequest request) {
            return true;
          }
        };
    filter.init(new RulesFilterConfig("/service:max=1"));
    filter.queuedThreadPool = mock(QueuedThreadPool.class);
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    AsyncContext ctx = mock(AsyncContext.class);
    when(req.startAsync(req, resp)).thenReturn(ctx);
    RecordingChain chain = new RecordingChain();

    try (MockedStatic<ZimbraServlet> servlet = mockStatic(ZimbraServlet.class)) {
      filter.doFilter(req, resp, chain);
    }

    assertFalse(chain.invoked);
    ArgumentCaptor<AsyncListener> captor = ArgumentCaptor.forClass(AsyncListener.class);
    verify(ctx).addListener(captor.capture());
    verify(ctx).setTimeout(anyLong());
    verify(ctx, never()).dispatch();

    captor.getValue().onTimeout(new AsyncEvent(ctx));

    verify(ctx).dispatch();
  }

  private static ContextPathBasedThreadPoolBalancerFilter initFilter(String rules)
      throws ServletException {
    ContextPathBasedThreadPoolBalancerFilter filter =
        new ContextPathBasedThreadPoolBalancerFilter();
    filter.init(new RulesFilterConfig(rules));
    return filter;
  }

  private static MockHttpServletRequest request() throws Exception {
    return new MockHttpServletRequest(
        new byte[0], new URL("http://localhost/service/soap/"), "text/xml") {
      @Override
      public String getRequestURI() {
        return "/service/soap/";
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

  private static class RulesFilterConfig implements FilterConfig {
    private final String rules;

    RulesFilterConfig(String rules) {
      this.rules = rules;
    }

    @Override
    public String getFilterName() {
      return "balancer";
    }

    @Override
    public ServletContext getServletContext() {
      return null;
    }

    @Override
    public String getInitParameter(String name) {
      return "Rules".equals(name) ? rules : null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      return Collections.enumeration(Collections.singletonList("Rules"));
    }
  }
}
