// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.httpclient;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

public class InputStreamRequestHttpRetryHandler extends DefaultHttpRequestRetryHandler {

    @Override
    /**
     * Same as default, but returns false if method is an unbuffered input stream request 
     * This avoids HttpMethodDirector masking real IO exception with bogus 'Unbuffered content cannot be retried' exception
     */
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

        boolean canRetry = super.retryRequest(exception, executionCount, context);

        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (canRetry && idempotent) {
            HttpEntity reqEntity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (reqEntity.isRepeatable()) {
                canRetry = true;
            }
        }
        return canRetry;

    }
}
