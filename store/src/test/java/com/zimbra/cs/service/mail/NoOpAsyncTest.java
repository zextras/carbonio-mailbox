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
    verify(zsc, never()).suspendIfWaitingForNotifications(anyLong());
  }

  @Test
  void notificationArrivedBeforeSuspendReturnsResponseWithoutStartingAsync() throws Exception {
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.suspendIfWaitingForNotifications(anyLong())).thenReturn(false);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(servletRequest, never()).startAsync();
    verify(zsc, never()).suspendAndUndispatch(anyLong());
  }

  @Test
  void blockingOutcomeSuspendsAndReturnsNull() throws Exception {
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.suspendIfWaitingForNotifications(anyLong())).thenReturn(true);

    Element result = new NoOp().handle(request, context);

    assertNull(result);
    verify(zsc).suspendIfWaitingForNotifications(anyLong());
  }

  @Test
  void noWaitNeverTouchesAsync() throws Exception {
    when(request.getAttributeBool(MailConstants.A_WAIT, false)).thenReturn(false);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(servletRequest, never()).startAsync();
  }

  @Test
  void waitWithoutSessionIsRejected() {
    when(zsc.hasSession()).thenReturn(false);
    org.junit.jupiter.api.Assertions.assertThrows(
        com.zimbra.common.service.ServiceException.class,
        () -> new NoOp().handle(request, context));
  }

  @Test
  void newSessionReturnsImmediatelyEvenWithWait() throws Exception {
    when(zsc.hasCreatedSession()).thenReturn(true);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(zsc, never()).beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean());
  }

  @Test
  void canceledWaitAfterRegistrationAnswersWaitDisallowed() throws Exception {
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.suspendIfWaitingForNotifications(anyLong())).thenReturn(false);
    when(zsc.isCanceledWaitForNotifications()).thenReturn(true);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(response).addAttribute(MailConstants.A_WAIT_DISALLOWED, true);
  }

  @Test
  void resumedPassAnswersWithoutStartingAsync() throws Exception {
    ZimbraSoapContext orig = mock(ZimbraSoapContext.class);
    when(servletRequest.getAttribute("nop_origcontext")).thenReturn(orig);

    Element result = new NoOp().handle(request, context);

    assertSame(response, result);
    verify(servletRequest, never()).startAsync();
    verify(zsc, never()).beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean());
    verify(response, never()).addAttribute(MailConstants.A_WAIT_DISALLOWED, true);
  }

  @Test
  void resumedPassAfterCanceledWaitAnswersWaitDisallowedAndReleasesBlockedSlot() throws Exception {
    when(request.getAttributeBool(MailConstants.A_LIMIT_TO_ONE_BLOCKED, false)).thenReturn(true);
    ZimbraSoapContext orig = mock(ZimbraSoapContext.class);
    when(orig.getAuthtokenAccountId()).thenReturn("acct");
    when(orig.isCanceledWaitForNotifications()).thenReturn(true);
    when(servletRequest.getAttribute("nop_origcontext")).thenReturn(orig);
    NoOp noOp = new NoOp();
    noOp.sBlockedNops.put("acct", orig);

    Element result = noOp.handle(request, context);

    assertSame(response, result);
    verify(response).addAttribute(MailConstants.A_WAIT_DISALLOWED, true);
    org.junit.jupiter.api.Assertions.assertTrue(noOp.sBlockedNops.isEmpty());
  }

  @Test
  void limitToOneBlockedSignalsPreviousNoOpAndKeepsSlotWhileSuspended() throws Exception {
    when(request.getAttributeBool(MailConstants.A_LIMIT_TO_ONE_BLOCKED, false)).thenReturn(true);
    when(zsc.getAuthtokenAccountId()).thenReturn("acct");
    ZimbraSoapContext previous = mock(ZimbraSoapContext.class);
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.suspendIfWaitingForNotifications(anyLong())).thenReturn(true);
    NoOp noOp = new NoOp();
    noOp.sBlockedNops.put("acct", previous);

    Element result = noOp.handle(request, context);

    assertNull(result);
    verify(previous).signalNotification(true);
    assertSame(zsc, noOp.sBlockedNops.get("acct"));
  }

  @Test
  void limitToOneBlockedSlotIsReleasedWhenNotSuspended() throws Exception {
    when(request.getAttributeBool(MailConstants.A_LIMIT_TO_ONE_BLOCKED, false)).thenReturn(true);
    when(zsc.getAuthtokenAccountId()).thenReturn("acct");
    when(zsc.beginWaitForNotifications(any(HttpServletRequest.class), anyBoolean()))
        .thenReturn(true);
    when(zsc.suspendIfWaitingForNotifications(anyLong())).thenReturn(false);
    NoOp noOp = new NoOp();

    Element result = noOp.handle(request, context);

    assertSame(response, result);
    org.junit.jupiter.api.Assertions.assertTrue(noOp.sBlockedNops.isEmpty());
  }
}
