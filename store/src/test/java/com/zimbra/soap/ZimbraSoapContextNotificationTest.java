// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.session.Session;
import com.zimbra.cs.session.SessionCache;
import com.zimbra.cs.session.SoapSession;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ZimbraSoapContextNotificationTest {

  private ZimbraSoapContext zsc;
  private HttpServletRequest servletRequest;
  private AsyncContext asyncContext;

  @BeforeEach
  void setUp() throws Exception {
    AuthToken token = mock(AuthToken.class);
    when(token.getAccountId()).thenReturn("acct");
    zsc = new ZimbraSoapContext(token, "acct", SoapProtocol.Soap12, SoapProtocol.Soap12);
    var field = ZimbraSoapContext.class.getDeclaredField("mSessionInfo");
    field.setAccessible(true);
    field.set(zsc, zsc.new SessionInfo("sid", 0, false));
    servletRequest = mock(HttpServletRequest.class);
    asyncContext = mock(AsyncContext.class);
    when(servletRequest.startAsync()).thenReturn(asyncContext);
  }

  private SoapSession sessionReturning(SoapSession.RegisterNotificationResult result)
      throws Exception {
    SoapSession session = mock(SoapSession.class);
    when(session.registerNotificationConnection(any())).thenReturn(result);
    return session;
  }

  @Test
  void blockingRegistrationDoesNotStartAsyncUntilSuspended() throws Exception {
    SoapSession session = sessionReturning(SoapSession.RegisterNotificationResult.BLOCKING);
    try (MockedStatic<SessionCache> cache = mockStatic(SessionCache.class)) {
      cache.when(() -> SessionCache.lookup("sid", "acct")).thenReturn(session);

      assertTrue(zsc.beginWaitForNotifications(servletRequest, true));
    }

    assertTrue(zsc.waitingForNotifications());
    verify(servletRequest, never()).startAsync();

    zsc.suspendAndUndispatch(2000);

    verify(servletRequest).startAsync();
    verify(asyncContext).setTimeout(2000);

    zsc.signalNotification(false);

    assertFalse(zsc.waitingForNotifications());
    assertFalse(zsc.isCanceledWaitForNotifications());
    verify(asyncContext).dispatch();
  }

  @Test
  void signalBeforeSuspendDoesNotTouchAsync() throws Exception {
    SoapSession session = sessionReturning(SoapSession.RegisterNotificationResult.BLOCKING);
    try (MockedStatic<SessionCache> cache = mockStatic(SessionCache.class)) {
      cache.when(() -> SessionCache.lookup("sid", "acct")).thenReturn(session);
      assertTrue(zsc.beginWaitForNotifications(servletRequest, false));
    }

    zsc.signalNotification(true);

    assertTrue(zsc.isCanceledWaitForNotifications());
    assertFalse(zsc.waitingForNotifications());
    verify(servletRequest, never()).startAsync();
  }

  @Test
  void nonBlockingResultsAreReportedAsNotBlocking() throws Exception {
    for (SoapSession.RegisterNotificationResult result :
        new SoapSession.RegisterNotificationResult[] {
          SoapSession.RegisterNotificationResult.DATA_READY,
          SoapSession.RegisterNotificationResult.NO_NOTIFY
        }) {
      SoapSession session = sessionReturning(result);
      try (MockedStatic<SessionCache> cache = mockStatic(SessionCache.class)) {
        cache.when(() -> SessionCache.lookup("sid", "acct")).thenReturn(session);
        assertFalse(zsc.beginWaitForNotifications(servletRequest, true));
      }
    }
    verify(servletRequest, never()).startAsync();
  }

  @Test
  void missingOrForeignSessionIsNotBlocking() throws Exception {
    try (MockedStatic<SessionCache> cache = mockStatic(SessionCache.class)) {
      cache.when(() -> SessionCache.lookup("sid", "acct")).thenReturn(null);
      assertFalse(zsc.beginWaitForNotifications(servletRequest, true));
      cache.when(() -> SessionCache.lookup("sid", "acct")).thenReturn(mock(Session.class));
      assertFalse(zsc.beginWaitForNotifications(servletRequest, true));
    }
    verify(servletRequest, never()).startAsync();
  }

  @Test
  void asyncContextOverloadRegistersListenerAndDispatchesOnSignal() throws Exception {
    SoapSession session = sessionReturning(SoapSession.RegisterNotificationResult.BLOCKING);
    try (MockedStatic<SessionCache> cache = mockStatic(SessionCache.class)) {
      cache.when(() -> SessionCache.lookup("sid", "acct")).thenReturn(session);
      assertTrue(zsc.beginWaitForNotifications(asyncContext, true));
    }
    verify(asyncContext).addListener(any());

    zsc.suspendAndUndispatch(1000);
    zsc.signalNotification(false);

    verify(asyncContext).dispatch();
  }
}
