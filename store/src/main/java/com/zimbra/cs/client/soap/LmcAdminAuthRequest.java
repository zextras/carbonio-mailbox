// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.cs.client.*;

public class LmcAdminAuthRequest extends LmcSoapRequest {

    private String mUsername;

    private String mPassword;

    public void setUsername(String u) {
        mUsername = u;
    }

    public void setPassword(String p) {
        mPassword = p;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;   // high security interface
    } 

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(AdminConstants.AUTH_REQUEST);
        DomUtil.addAttr(request, AdminConstants.A_NAME, mUsername);
        DomUtil.addAttr(request, AdminConstants.E_PASSWORD, mPassword);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
            throws ServiceException 
    {
        // get the auth token out, no default, must be present or a service exception is thrown
        String authToken = DomUtil.getString(responseXML, AdminConstants.E_AUTH_TOKEN);
        ZAuthToken zat = new ZAuthToken(null, authToken, null);
        // get the session id, if not present, default to null
        String sessionId = DomUtil.getString(responseXML, HeaderConstants.E_SESSION, null);

        LmcAdminAuthResponse responseObj = new LmcAdminAuthResponse();
        LmcSession sess = new LmcSession(zat, sessionId);
        responseObj.setSession(sess);
        return responseObj;
    }
}
