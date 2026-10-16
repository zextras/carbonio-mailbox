// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.httpclient;

import java.io.IOException;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

public class InputStreamRequestHttpRetryHandler extends DefaultHttpRequestRetryStrategy {

    public InputStreamRequestHttpRetryHandler() {
        super(3, TimeValue.ZERO_MILLISECONDS);
    }

    @Override
    /**
     * Same as default, but returns false if method is an unbuffered input stream request 
     * This avoids HttpMethodDirector masking real IO exception with bogus 'Unbuffered content cannot be retried' exception
     */
    public boolean retryRequest(HttpRequest request, IOException exception, int executionCount, HttpContext context) {
        boolean canRetry = super.retryRequest(request, exception, executionCount, context);
        if (canRetry && request instanceof HttpEntityContainer) {
            HttpEntity reqEntity = ((HttpEntityContainer) request).getEntity();
            if (reqEntity != null && !reqEntity.isRepeatable()) {
                return false;
            }
        }
        return canRetry;
    }

    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        return false;
    }
}
