// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.continuation;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;

import com.zimbra.common.util.ZimbraLog;

/**
 * Restores the Jetty 9 continuation semantics on top of the Servlet 3 {@link AsyncContext}:
 * a suspended request is re-dispatched exactly once, either because it was resumed
 * ({@link #resumeIfSuspended()}) or because the suspension timed out ({@link #onTimeout}), so the
 * servlet runs again and writes its normal response. Without the dispatch on timeout Jetty answers
 * with HTTP 500 "AsyncContext timeout".
 *
 * <p>The {@link AsyncContext} can be provided up front or, with
 * {@link #ResumeContinuationListener(ServletRequest)}, started lazily in
 * {@link #suspendAndUndispatch(long)} so that requests which do not actually block never enter
 * async mode and are answered synchronously.
 */
public class ResumeContinuationListener implements AsyncListener {

    private volatile AsyncContext asyncContext;
    private final ServletRequest request;
    private final AtomicBoolean readyToResume;
    /** Whoever flips this owns the single dispatch/complete of the async context. */
    private final AtomicBoolean dispatched;
    private volatile boolean expired;

    public ResumeContinuationListener(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        this.request = null;
        this.readyToResume = new AtomicBoolean(false);
        this.dispatched = new AtomicBoolean(false);
        asyncContext.addListener(this);
    }

    /** Lazy variant: async mode is only started by {@link #suspendAndUndispatch(long)}. */
    public ResumeContinuationListener(ServletRequest request) {
        this.asyncContext = null;
        this.request = request;
        this.readyToResume = new AtomicBoolean(false);
        this.dispatched = new AtomicBoolean(false);
    }

    public static ResumeContinuationListener getResumableContinuation(ServletRequest request) {
        return new ResumeContinuationListener(request.startAsync());
    }

    @Override
    public void onComplete(AsyncEvent event) {
        ZimbraLog.session.trace("ResumeContinuationListener.onComplete");
        readyToResume.set(false);
    }

    @Override
    public void onTimeout(AsyncEvent event) {
        ZimbraLog.session.trace("ResumeContinuationListener.onTimeout");
        expired = true;
        readyToResume.set(false);
        // Jetty 9 behaviour: on timeout the request is re-dispatched so the handler produces its
        // regular (empty) response. If nobody dispatches/completes, Jetty sends a 500.
        dispatchOnce();
    }

    @Override
    public void onError(AsyncEvent event) {
        ZimbraLog.session.trace("ResumeContinuationListener.onError");
        readyToResume.set(false);
        if (dispatched.compareAndSet(false, true)) {
            try {
                AsyncContext ctx = event != null && event.getAsyncContext() != null
                        ? event.getAsyncContext() : asyncContext;
                if (ctx != null) {
                    ctx.complete();
                }
            } catch (IllegalStateException ise) {
                ZimbraLog.session.debug(
                        "ignoring IllegalStateException during complete; context may be completed", ise);
            }
        }
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
    }

    public boolean isExpired() {
        return expired;
    }

    private void dispatchOnce() {
        AsyncContext ctx = asyncContext;
        if (ctx != null && dispatched.compareAndSet(false, true)) {
            try {
                ctx.dispatch();
            } catch (IllegalStateException ise) {
                ZimbraLog.session.debug(
                        "ignoring IllegalStateException during dispatch; context may be completed", ise);
            }
        }
    }

    public void resumeIfSuspended() {
        if (readyToResume.compareAndSet(true, false)) {
            ZimbraLog.session.trace("ResumeContinuationListener.resumeIfSuspended RESUMING");
            dispatchOnce();
        }
    }

    public synchronized void suspendAndUndispatch(long timeout) {
        if (asyncContext == null) {
            AsyncContext ctx = request.startAsync();
            ctx.addListener(this);
            asyncContext = ctx;
        }
        readyToResume.set(true);
        asyncContext.setTimeout(timeout);
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }
}
