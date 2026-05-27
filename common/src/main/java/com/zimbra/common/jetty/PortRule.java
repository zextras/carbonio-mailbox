// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.jetty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.ZimbraLog;

public class PortRule extends Rule {

    private int _port;
    protected Pattern _regex;

    private Integer _httpErrorStatusRegexNotmatched;
    private String _httpErrorReasonRegexNotMatched;

    public PortRule()
    {
        setTerminating(true);
    }

    public void setPort(int port)
    {
        _port = port;
    }

    public void setRegex(String regex)
    {
        _regex=Pattern.compile(regex);
    }

    public void setHttpErrorStatusRegexNotMatched(int status)
    {
        _httpErrorStatusRegexNotmatched = status;
    }

    public void setHttpErrorReasonRegexNotMatched(String reason)
    {
        _httpErrorReasonRegexNotMatched = reason;
    }

    @Override
    public Handler matchAndApply(Handler handler) throws IOException {
        if (_regex == null) {
            return null;
        }

        final Request request = handler.getWrapped();
        final int port = getLocalPort(request);

        if (port == _port) {
            final String target = request.getHttpURI().getPath();
            final Matcher matcher = _regex.matcher(target);
            if (!matcher.matches()) {
                return createErrorHandler(handler, _httpErrorStatusRegexNotmatched, _httpErrorReasonRegexNotMatched);
            }
        }
        return null;
    }

    private static int getLocalPort(Request request) {
        final var addr = request.getConnectionMetaData().getLocalSocketAddress();
        if (addr instanceof InetSocketAddress socketAddr) {
            return socketAddr.getPort();
        }
        return -1;
    }

    private static Handler createErrorHandler(Handler handler, int status, String reasonKey) {
        final String reason = resolveReason(reasonKey);
        return new Handler(handler) {
            @Override
            protected boolean handle(Response response, Callback callback) throws Exception {
                if (status > 0) {
                    Response.writeError(getWrapped(), response, callback, status, reason);
                } else {
                    callback.succeeded();
                }
                return true;
            }
        };
    }

    private static String resolveReason(String reasonKey) {
        if (reasonKey == null) {
            return null;
        }
        try {
            final L10nUtil.MsgKey key = L10nUtil.MsgKey.valueOf(reasonKey);
            return L10nUtil.getMessage(key);
        } catch (IllegalArgumentException e) {
            ZimbraLog.misc.debug("invalid msg key: " + reasonKey);
            return null;
        }
    }

    public String toString()
    {
        return super.toString()+"["+_port+"]"+"["+ ((_regex != null) ? _regex.toString() : "") +"]"+"["+_httpErrorStatusRegexNotmatched+"]";
    }

}
