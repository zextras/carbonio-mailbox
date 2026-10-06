// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.continuation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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

  @Test
  void factoryStartsAsyncAndRegistersListener() {
    ServletRequest request = mock(ServletRequest.class);
    AsyncContext ctx = mock(AsyncContext.class);
    when(request.startAsync()).thenReturn(ctx);

    ResumeContinuationListener listener = ResumeContinuationListener.getResumableContinuation(request);

    assertSame(ctx, listener.getAsyncContext());
    verify(ctx).addListener(listener);
  }

  @Test
  void onStartAsyncIsANoOp() {
    AsyncContext ctx = mock(AsyncContext.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    assertDoesNotThrow(() -> listener.onStartAsync(new AsyncEvent(ctx)));
    assertFalse(listener.isExpired());
  }

  @Test
  void onErrorFallsBackToStoredContextWhenEventHasNone() {
    AsyncContext ctx = mock(AsyncContext.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);

    listener.onError(null);

    verify(ctx).complete();
  }

  @Test
  void onErrorWithoutAnyContextDoesNothing() {
    ServletRequest request = mock(ServletRequest.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(request);
    assertDoesNotThrow(() -> listener.onError(null));
  }

  @Test
  void onErrorWithoutContextDoesNotBlockLaterDispatch() {
    ServletRequest request = mock(ServletRequest.class);
    AsyncContext ctx = mock(AsyncContext.class);
    when(request.startAsync()).thenReturn(ctx);
    ResumeContinuationListener listener = new ResumeContinuationListener(request);

    listener.onError(null);
    listener.suspendAndUndispatch(1000);
    listener.onTimeout(new AsyncEvent(ctx));

    verify(ctx).dispatch();
    verify(ctx, never()).complete();
  }

  @Test
  void onTimeoutWithoutContextDoesNotThrow() {
    ServletRequest request = mock(ServletRequest.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(request);
    assertDoesNotThrow(() -> listener.onTimeout(null));
    assertTrue(listener.isExpired());
  }

  @Test
  void onCompleteDisarmsResume() {
    AsyncContext ctx = mock(AsyncContext.class);
    ResumeContinuationListener listener = new ResumeContinuationListener(ctx);
    listener.suspendAndUndispatch(1000);
    listener.onComplete(new AsyncEvent(ctx));
    listener.resumeIfSuspended();
    verify(ctx, never()).dispatch();
  }

  @Test
  void dispatchOnTimeoutListenerOnErrorCompletesOnce() throws Exception {
    AsyncContext ctx = mock(AsyncContext.class);
    DispatchOnTimeoutListener l = new DispatchOnTimeoutListener();
    l.onError(new AsyncEvent(ctx));
    l.onError(new AsyncEvent(ctx));
    l.onTimeout(new AsyncEvent(ctx));
    verify(ctx, times(1)).complete();
    verify(ctx, never()).dispatch();
  }

  @Test
  void dispatchOnTimeoutListenerOnErrorSwallowsIllegalState() throws Exception {
    AsyncContext ctx = mock(AsyncContext.class);
    doThrow(new IllegalStateException()).when(ctx).complete();
    DispatchOnTimeoutListener l = new DispatchOnTimeoutListener();
    assertDoesNotThrow(() -> l.onError(new AsyncEvent(ctx)));
  }

  @Test
  void dispatchOnTimeoutListenerIgnoresCompleteAndStartAsync() throws Exception {
    AsyncContext ctx = mock(AsyncContext.class);
    DispatchOnTimeoutListener l = new DispatchOnTimeoutListener();
    l.onComplete(new AsyncEvent(ctx));
    l.onStartAsync(new AsyncEvent(ctx));
    verify(ctx, never()).dispatch();
    verify(ctx, never()).complete();
  }
}
