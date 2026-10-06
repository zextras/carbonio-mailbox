// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.continuation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ResumeContinuationListenerDispatchTest {

  @Test
  void onTimeoutDispatchesSoTheHandlerRunsAgain() {
    AsyncContext ctx = mock(AsyncContext.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    listener.suspendAndUndispatch(5000);

    listener.onTimeout(new AsyncEvent(ctx));

    verify(ctx, times(1)).dispatch();
    assertTrue(listener.isExpired());
  }

  @Test
  void onTimeoutAfterResumeDoesNotDispatchTwice() {
    AsyncContext ctx = mock(AsyncContext.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    listener.suspendAndUndispatch(5000);

    listener.resumeIfSuspended();
    listener.onTimeout(new AsyncEvent(ctx));

    verify(ctx, times(1)).dispatch();
  }

  @Test
  void resumeAfterTimeoutDoesNotDispatchTwice() {
    AsyncContext ctx = mock(AsyncContext.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    listener.suspendAndUndispatch(5000);

    listener.onTimeout(new AsyncEvent(ctx));
    listener.resumeIfSuspended();

    verify(ctx, times(1)).dispatch();
  }

  @Test
  void dispatchIllegalStateExceptionIsSwallowed() {
    AsyncContext ctx = mock(AsyncContext.class);
    doThrow(new IllegalStateException("completed")).when(ctx).dispatch();
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    listener.suspendAndUndispatch(5000);

    assertDoesNotThrow(() -> listener.onTimeout(new AsyncEvent(ctx)));
    assertDoesNotThrow(listener::resumeIfSuspended);
  }

  @Test
  void onErrorCompletesSafely() {
    AsyncContext ctx = mock(AsyncContext.class);
    doThrow(new IllegalStateException("completed")).when(ctx).complete();
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    listener.suspendAndUndispatch(5000);

    assertDoesNotThrow(() -> listener.onError(new AsyncEvent(ctx)));
    verify(ctx).complete();
    listener.resumeIfSuspended();
    verify(ctx, never()).dispatch();
  }

  @Test
  void lazyListenerDoesNotStartAsyncUntilSuspended() {
    ServletRequest request = mock(ServletRequest.class);
    AsyncContext ctx = mock(AsyncContext.class);
    when(request.startAsync()).thenReturn(ctx);

    ResumeContinuationListener listener = new ResumeContinuationListener(request);
    listener.resumeIfSuspended();
    verify(request, never()).startAsync();

    listener.suspendAndUndispatch(7000);
    verify(request, times(1)).startAsync();
    verify(ctx).addListener(listener);
    verify(ctx).setTimeout(7000);
    listener.resumeIfSuspended();
    verify(ctx).dispatch();
  }

  @Test
  void dispatchOnTimeoutListenerDispatchesOnce() throws Exception {
    AsyncContext ctx = mock(AsyncContext.class);
    DispatchOnTimeoutListener.delay(ctx, 1234);
    verify(ctx).setTimeout(1234);
    ArgumentCaptor<AsyncListener> captor = ArgumentCaptor.forClass(AsyncListener.class);
    verify(ctx).addListener(captor.capture());

    AsyncListener l = captor.getValue();
    l.onTimeout(new AsyncEvent(ctx));
    l.onTimeout(new AsyncEvent(ctx));
    verify(ctx, times(1)).dispatch();
  }

  @Test
  void dispatchOnTimeoutListenerSwallowsIllegalState() {
    AsyncContext ctx = mock(AsyncContext.class);
    doThrow(new IllegalStateException()).when(ctx).dispatch();
    DispatchOnTimeoutListener l = new DispatchOnTimeoutListener();
    assertDoesNotThrow(() -> l.onTimeout(new AsyncEvent(ctx)));
    verify(ctx, never()).complete();
  }
}
