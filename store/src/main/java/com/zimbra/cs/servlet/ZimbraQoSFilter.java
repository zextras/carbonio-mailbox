// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.util.AuthUtil;
import com.zimbra.soap.SoapServlet;

/**
 * This Servlet {@link Filter} limits the number of concurrent HTTP requests per
 * account.
 * <p>
 * It temporarily suspends the requests if exceeding the limit. The limit is configurable
 * by {@link LC#servlet_max_concurrent_http_requests_per_account}. To disable, set
 * the LC key to 0 or remove this servlet filter from web.xml.
 *
 */


public class ZimbraQoSFilter implements Filter {

    static final int DEFAULT_WAIT_MS=50;
    static final long DEFAULT_SUSPEND_MS = 1000;
    
    static final String MAX_WAIT_INIT_PARAM="waitMs";
    static final String SUSPEND_INIT_PARAM="suspendMs";

    private long waitMs;
    private long suspendMs;

    private ConcurrentLinkedHashMap<String, Semaphore> passes = new ConcurrentLinkedHashMap.Builder<String, Semaphore>()
                                                                .maximumWeightedCapacity(2000).build();

    public static String extractUserId(ServletRequest request) {
        try {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) request;
                boolean isAdminRequest = AuthUtil.isAdminRequest(req);
                AuthToken at = AuthProvider.getAuthToken(req, isAdminRequest);
                if (at == null) {
                    Map <Object, Object> engineCtxt = new HashMap<>();
                    engineCtxt.put(SoapServlet.SERVLET_REQUEST, req);
                    at = AuthProvider.getJWToken(null, engineCtxt);
                }
                if (at != null) {
                    return at.getAccountId();
                }
                // Check if this is Http Basic Authentication, if so return authorization string.
                String auth = req.getHeader("Authorization");
                if (auth != null) {
                    return auth;
                }
            } 
        } catch (Exception e) {
            // ignore
            ZimbraLog.misc.debug("error while extracting authtoken" , e);
        }
        return null;
    }
    
    public void init(FilterConfig filterConfig) {
        waitMs = DEFAULT_WAIT_MS;
        if (filterConfig.getInitParameter(MAX_WAIT_INIT_PARAM)!=null) {
            waitMs=Integer.parseInt(filterConfig.getInitParameter(MAX_WAIT_INIT_PARAM));
        }

        suspendMs = DEFAULT_SUSPEND_MS;
        if (filterConfig.getInitParameter(SUSPEND_INIT_PARAM)!=null) {
            suspendMs=Integer.parseInt(filterConfig.getInitParameter(SUSPEND_INIT_PARAM));
        }
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    throws IOException, ServletException {
        try {
            String user = extractUserId(request);
            int max = LC.servlet_max_concurrent_http_requests_per_account.intValue();
            if (user == null || max <= 0) {
                chain.doFilter(request,response);
                return;
            }
            Semaphore pass = passes.putIfAbsent(user, new Semaphore(max, true));
            if (pass == null) {
                pass = passes.get(user);
            }
            if (pass.tryAcquire(waitMs, TimeUnit.MILLISECONDS)) {
                try {
                    chain.doFilter(request, response);
                } finally {
                    pass.release();
                }
            } else {
                Continuation continuation = ContinuationSupport.getContinuation(request);
                HttpServletRequest hreq = (HttpServletRequest) request;
                ZimbraServlet.addRemoteIpToLoggingContext(hreq);
                ZimbraServlet.addUAToLoggingContext(hreq);
                ZimbraLog.misc.warn("Exceeded the max requests limit. Suspending " + continuation);
                ZimbraLog.clearContext();
                continuation.setTimeout(suspendMs);
                continuation.suspend();
            }
        } catch(InterruptedException e) {
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public void destroy() {
    }

}
