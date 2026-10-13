// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.mail;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;


import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * Do nothing. The main intent of this Soap call is for the client
 * to fetch new notifications (which are automatically added onto the empty
 * response).
 *
 * The caller may set wait=1 for this request in which case the request will
 * block until there are new notifications.
 */
public class NoOp extends MailDocumentHandler  {

    private static final long DEFAULT_TIMEOUT;
    private static final long MIN_TIMEOUT;
    private static final long MAX_TIMEOUT;

    static {
        DEFAULT_TIMEOUT = LC.zimbra_noop_default_timeout.longValue() * 1000;
        MIN_TIMEOUT = LC.zimbra_noop_min_timeout.longValue() * 1000;
        MAX_TIMEOUT = LC.zimbra_noop_max_timeout.longValue() * 1000;
    }

    private static long parseTimeout(Element request) throws ServiceException {
        long timeout = request.getAttributeLong(MailConstants.A_TIMEOUT, DEFAULT_TIMEOUT);
        if (timeout < MIN_TIMEOUT)
            timeout = MIN_TIMEOUT;
        if (timeout > MAX_TIMEOUT)
            timeout = MAX_TIMEOUT;
        return timeout;
    }

    @Override
    public void preProxy(Element request, Map<String, Object> context) throws ServiceException {
        setProxyTimeout(parseTimeout(request) + 10 * Constants.MILLIS_PER_SECOND);
        super.preProxy(request, context);
    }

    ConcurrentHashMap<String /*AccountId*/, ZimbraSoapContext> sBlockedNops =
        new ConcurrentHashMap<>(5000, 0.75f, 50);

    private enum WaitOutcome {
        /** The request is suspended (async started); the response is written on re-dispatch. */
        SUSPENDED,
        /** Blocking was cancelled by the server: tell the client with waitDisallowed. */
        BLOCKING_UNSUPPORTED,
        /** Nothing to wait for (any more): answer right away. */
        DONE
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        boolean wait = request.getAttributeBool(MailConstants.A_WAIT, false);
        boolean includeDelegates = request.getAttributeBool(MailConstants.A_DELEGATE, true);
        HttpServletRequest servletRequest = (HttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
        boolean enforceLimit = request.getAttributeBool(MailConstants.A_LIMIT_TO_ONE_BLOCKED, false);
        boolean blockingUnsupported = false;

        // See bug 16494 - if a session is new, we should return from the NoOp immediately so the client
        // gets the <refresh> block
        if (zsc.hasCreatedSession()) {
            wait = false;
        }

        if (wait) {
            WaitOutcome outcome = waitForNotifications(request, zsc, servletRequest, includeDelegates, enforceLimit);
            if (outcome == WaitOutcome.SUSPENDED) {
                return null;
            }
            blockingUnsupported = outcome == WaitOutcome.BLOCKING_UNSUPPORTED;
        }
        Element toRet = zsc.createElement(MailConstants.NO_OP_RESPONSE);
        if (blockingUnsupported) {
            toRet.addAttribute(MailConstants.A_WAIT_DISALLOWED, true);
        }

        return toRet;
    }

    private WaitOutcome waitForNotifications(Element request, ZimbraSoapContext zsc,
            HttpServletRequest servletRequest, boolean includeDelegates, boolean enforceLimit)
            throws ServiceException {
        if (!zsc.hasSession()) {
            throw ServiceException.INVALID_REQUEST("Cannot execute a NoOpRequest with wait=\"1\" without a session. "
                    + "Set the <session> flag in the <context> of your request", null);
        }
        ZimbraSoapContext origContext = (ZimbraSoapContext) servletRequest.getAttribute("nop_origcontext");
        if (origContext == null) {
            return initialWait(request, zsc, servletRequest, includeDelegates, enforceLimit);
        }
        return resumedWait(origContext, enforceLimit);
    }

    /** First pass: NOT a resumed request -- block if necessary. */
    private WaitOutcome initialWait(Element request, ZimbraSoapContext zsc,
            HttpServletRequest servletRequest, boolean includeDelegates, boolean enforceLimit)
            throws ServiceException {
        servletRequest.setAttribute("nop_origcontext", zsc);
        WaitOutcome outcome = WaitOutcome.DONE;
        // async mode is only entered by suspendAndUndispatch() below, i.e. when we really block
        if (zsc.beginWaitForNotifications(servletRequest, includeDelegates)) {
            if (enforceLimit) {
                ZimbraSoapContext otherContext = sBlockedNops.put(zsc.getAuthtokenAccountId(), zsc);
                if (otherContext != null) {
                    otherContext.signalNotification(true);
                }
            }
            outcome = suspendIfStillWaiting(request, zsc);
            if (outcome == WaitOutcome.SUSPENDED) {
                return outcome;
            }
        }
        if (enforceLimit) {
            // remove this soap context from the blocked-context hash, but only
            // if it hasn't already been removed by someone else...
            sBlockedNops.remove(zsc.getAuthtokenAccountId(), zsc);
        }
        return outcome;
    }

    private WaitOutcome suspendIfStillWaiting(Element request, ZimbraSoapContext zsc)
            throws ServiceException {
        long timeout = parseTimeout(request);
        if (zsc.suspendIfWaitingForNotifications(timeout)) {
            if (ZimbraLog.soap.isTraceEnabled()) {
                ZimbraLog.soap.trace("Suspended <NoOpRequest> for %dms", timeout);
            }
            return WaitOutcome.SUSPENDED;
        }
        return zsc.isCanceledWaitForNotifications()
                ? WaitOutcome.BLOCKING_UNSUPPORTED : WaitOutcome.DONE;
    }

    /** Second pass (ASYNC re-dispatch after resume or timeout): just answer. */
    private WaitOutcome resumedWait(ZimbraSoapContext origContext, boolean enforceLimit) {
        if (enforceLimit) {
            // remove this soap context from the blocked-context hash, but only
            // if it hasn't already been removed by someone else...
            sBlockedNops.remove(origContext.getAuthtokenAccountId(), origContext);
        }
        return origContext.isCanceledWaitForNotifications()
                ? WaitOutcome.BLOCKING_UNSUPPORTED : WaitOutcome.DONE;
    }
}
