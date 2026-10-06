// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.cs.session.IWaitSet;
import com.zimbra.cs.session.WaitSetCallback;
import com.zimbra.cs.session.WaitSetError;
import com.zimbra.cs.session.WaitSetMgr;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.base.WaitSetReq;
import com.zimbra.soap.mail.message.WaitSetResponse;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class WaitSetRequestAsyncTest {

  private HttpServletRequest servletRequest;
  private WaitSetReq req;
  private IWaitSet waitSet;
  private Map<String, Object> context;

  @BeforeEach
  void setUp() throws Exception {
    servletRequest = mock(HttpServletRequest.class);
    ZimbraSoapContext zsc = mock(ZimbraSoapContext.class);
    when(zsc.getRequestedAccountId()).thenReturn("acct");
    req = mock(WaitSetReq.class);
    when(req.getWaitSetId()).thenReturn("ws1");
    when(req.getLastKnownSeqNo()).thenReturn("3");
    waitSet = mock(IWaitSet.class);
    when(waitSet.getOwnerAccountId()).thenReturn("acct");
    when(waitSet.getDefaultInterest()).thenReturn(EnumSet.noneOf(com.zimbra.cs.mailbox.MailItem.Type.class));
    when(waitSet.removeAccounts(any())).thenReturn(Collections.<WaitSetError>emptyList());
    context = new HashMap<>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    context.put(SoapServlet.SERVLET_REQUEST, servletRequest);
  }

  @Test
  void nonBlockingRequestIsAnsweredWithoutStartingAsync() throws Exception {
    when(req.getBlock()).thenReturn(false);
    when(waitSet.doWait(any(), any(), any(), any())).thenReturn(Collections.<WaitSetError>emptyList());

    WaitSetResponse resp = new WaitSetResponse();
    try (MockedStatic<WaitSetMgr> mgr = mockStatic(WaitSetMgr.class)) {
      mgr.when(() -> WaitSetMgr.lookup("ws1")).thenReturn(waitSet);
      mgr.when(() -> WaitSetMgr.checkRightForOwnerAccount(any(), any())).then(i -> null);
      // admin: skips the anti-polling sleep
      WaitSetRequest.staticHandle(req, context, resp, true);
    }

    verify(servletRequest, never()).startAsync();
    assertEquals("ws1", resp.getWaitSetId());
    assertEquals("3", resp.getSeqNo());
  }

  @Test
  void blockingRequestThatCompletesRightAwayIsAnsweredWithoutStartingAsync() throws Exception {
    when(req.getBlock()).thenReturn(true);
    when(waitSet.doWait(any(), any(), any(), any()))
        .thenAnswer(
            inv -> {
              WaitSetCallback cb = inv.getArgument(0);
              cb.completed = true;
              cb.seqNo = "4";
              cb.signalledAccounts = new java.util.HashSet<>();
              return Collections.<WaitSetError>emptyList();
            });

    WaitSetResponse resp = new WaitSetResponse();
    try (MockedStatic<WaitSetMgr> mgr = mockStatic(WaitSetMgr.class)) {
      mgr.when(() -> WaitSetMgr.lookup("ws1")).thenReturn(waitSet);
      mgr.when(() -> WaitSetMgr.checkRightForOwnerAccount(any(), any())).then(i -> null);
      WaitSetRequest.staticHandle(req, context, resp, true);
    }

    verify(servletRequest, never()).startAsync();
    assertEquals("4", resp.getSeqNo());
  }

  @Test
  void blockingRequestStartsAsyncOnlyWhenSuspendingAndDispatchesOnTimeout() throws Exception {
    when(req.getBlock()).thenReturn(true);
    when(waitSet.doWait(any(), any(), any(), any())).thenReturn(Collections.<WaitSetError>emptyList());
    AsyncContext ctx = mock(AsyncContext.class);
    when(servletRequest.startAsync()).thenReturn(ctx);

    WaitSetResponse resp = new WaitSetResponse();
    try (MockedStatic<WaitSetMgr> mgr = mockStatic(WaitSetMgr.class)) {
      mgr.when(() -> WaitSetMgr.lookup("ws1")).thenReturn(waitSet);
      mgr.when(() -> WaitSetMgr.checkRightForOwnerAccount(any(), any())).then(i -> null);
      WaitSetRequest.staticHandle(req, context, resp, true);
    }

    verify(servletRequest).startAsync();
    verify(ctx).setTimeout(anyLong());
    ArgumentCaptor<AsyncListener> listener = ArgumentCaptor.forClass(AsyncListener.class);
    verify(ctx).addListener(listener.capture());
    verify(ctx, never()).dispatch();

    // idle long-poll times out: the request is re-dispatched to produce the regular response
    listener.getValue().onTimeout(new AsyncEvent(ctx));

    verify(ctx).dispatch();
  }
}
