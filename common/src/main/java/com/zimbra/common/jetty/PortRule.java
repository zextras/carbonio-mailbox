// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.jetty;

import java.io.IOException;
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
        _regex = Pattern.compile(regex);
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
    public Handler matchAndApply(Handler input) throws IOException {
        Request request = input;
        int port = Request.getLocalPort(request);
        
        if (port == _port) {
            String path = request.getHttpURI().getPath();
            Matcher matcher = _regex.matcher(path);
            if (!matcher.matches()) {
                return new Handler(input) {
                    @Override
                    public boolean handle(Response response, Callback callback) throws Exception {
                        String reason = null;
                        if (_httpErrorReasonRegexNotMatched != null) {
                            try {
                                L10nUtil.MsgKey reasonKey = L10nUtil.MsgKey.valueOf(_httpErrorReasonRegexNotMatched);
                                reason = L10nUtil.getMessage(reasonKey);
                            } catch (IllegalArgumentException e) {
                                ZimbraLog.misc.debug("invalid msg key: " + _httpErrorReasonRegexNotMatched);
                            }
                        }
                        Response.writeError(this, response, callback, _httpErrorStatusRegexNotmatched, reason);
                        return true;
                    }
                };
            }
        }
        return null;
    }
    
    public String toString()
    {
        return super.toString()+"["+_port+"]"+"["+ ((_regex != null) ? _regex.toString() : "") +"]"+"["+_httpErrorStatusRegexNotmatched+"]";                
    }

}
