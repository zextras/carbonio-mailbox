// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.continuation;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

import com.zimbra.common.util.ZimbraLog;

/**
 * AsyncListener implementation to handle internal details of when and when not to attempt resume.
 * Application code which implements timeout + explicit resume should do so via this class.
 */
public class ResumeContinuationListener implements AsyncListener {

    private volatile AsyncContext asyncContext;
    private final AtomicBoolean readyToResume = new AtomicBoolean(false);

    public ResumeContinuationListener(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        asyncContext.addListener(this);
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        ZimbraLog.session.trace("ResumeContinuationListener.onComplete");
        readyToResume.set(false);
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        ZimbraLog.session.trace("ResumeContinuationListener.onTimeout");
        readyToResume.set(false);
        try {
            event.getAsyncContext().dispatch();
        } catch (IllegalStateException e) {
            ZimbraLog.session.debug("ignoring IllegalStateException during timeout dispatch", e);
        }
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        ZimbraLog.session.trace("ResumeContinuationListener.onError");
        readyToResume.set(false);
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        // no-op
    }

    /**
     * Attempt to resume the async context if it is currently suspended.
     */
    public synchronized void resumeIfSuspended() {
        if (readyToResume.compareAndSet(true, false)) {
            try {
                ZimbraLog.session.trace("ResumeContinuationListener.resumeIfSuspended RESUMING");
                AsyncContext ctx = asyncContext;
                if (ctx != null) {
                    // Mark as resumed so SoapEngine can detect re-entry
                    ctx.getRequest().setAttribute("waitset.resumed", Boolean.TRUE);
                    ctx.dispatch();
                }
            } catch (IllegalStateException ise) {
                ZimbraLog.session.debug(
                        "ignoring IllegalStateException during resume; already resumed/expired", ise);
            }
        }
    }

    /**
     * Set the timeout on the async context and mark as ready to resume.
     * @param timeout timeout in milliseconds
     */
    public synchronized void suspendAndUndispatch(long timeout) {
        readyToResume.set(true);
        AsyncContext ctx = asyncContext;
        if (ctx != null) {
            ctx.setTimeout(timeout);
        }
        // The servlet returns after calling this — the container suspends the thread.
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

}
