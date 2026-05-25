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

public class ResumeContinuationListener implements AsyncListener {

    private AsyncContext asyncContext;
    private AtomicBoolean readyToResume;

    public ResumeContinuationListener(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        this.readyToResume = new AtomicBoolean(false);
        asyncContext.addListener(this);
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
        readyToResume.set(false);
    }

    @Override
    public void onError(AsyncEvent event) {
        ZimbraLog.session.trace("ResumeContinuationListener.onError");
        readyToResume.set(false);
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
    }

    /**
     * Attempt to resume the async context if it is currently suspended.
     */
    public synchronized void resumeIfSuspended() {
        if (readyToResume.compareAndSet(true, false)) {
            try {
                ZimbraLog.session.trace("ResumeContinuationListener.resumeIfSuspended RESUMING");
                asyncContext.dispatch();
            } catch (IllegalStateException ise) {
                ZimbraLog.session.debug(
                        "ignoring IllegalStateException during dispatch; context may be completed", ise);
            }
        }
    }

    /**
     * Put the async context into suspended state.
     * @param timeout
     */
    public synchronized void suspendAndUndispatch(long timeout) {
        readyToResume.set(true);
        asyncContext.setTimeout(timeout);
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

}
