// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoOpAsyncTest {

  private HttpServletRequest servletRequest;
  private ZimbraSoapContext zsc;
  private Element request;
  private Element response;
  private Map<String, Object> context;

  @BeforeEach
  void setUp() throws Exception {
    servletRequest = mock(HttpServletRequest.class);
    zsc = mock(ZimbraSoapContext.class);
    request = mock(Element.class);
    response = mock(Element.class);
    when(request.getAttributeBool(MailConstants.A_WAIT, false)).thenReturn(true);
    when(request.getAttributeBool(MailConstants.A_DELEGATE, true)).thenReturn(true);
    when(request.getAttributeBool(MailConstants.A_LIMIT_TO_ONE_BLOCKED, false)).thenReturn(false);
    when(request.getAttributeLong(anyString(), anyLong())).thenReturn(1000L);
    when(zsc.hasSession()).thenReturn(true);
    when(zsc.createElement(MailConstants.NO_OP_RESPONSE)).thenReturn(response);
    context = new HashMap<>();
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    context.put(SoapServlet.SERVLET_REQUEST, servletRequest);
  }

  @Test
  void nonBlockingOutcomeReturnsResponseWithoutStartingAsync() throws Exception {
    // e.g. DATA_READY / NO_NOTIFY / no SoapSession: registration says "do not block"
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(false);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(servletRequest, never()).startAsync();
    verify(zsc, never()).suspendAndUndispatch(anyLong());
  }

  @Test
  void notificationArrivedBeforeSuspendReturnsResponseWithoutStartingAsync() throws Exception {
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.waitingForNotifications()).thenReturn(false);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(servletRequest, never()).startAsync();
    verify(zsc, never()).suspendAndUndispatch(anyLong());
  }

  @Test
  void blockingOutcomeSuspendsAndReturnsNull() throws Exception {
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.waitingForNotifications()).thenReturn(true);

    Element result = new NoOp().handle(request, context);

    assertNull(result);
    verify(zsc).suspendAndUndispatch(anyLong());
  }

  @Test
  void noWaitNeverTouchesAsync() throws Exception {
    when(request.getAttributeBool(MailConstants.A_WAIT, false)).thenReturn(false);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(servletRequest, never()).startAsync();
  }
}
