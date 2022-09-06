// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.cs.client.*;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcAuthRequest extends LmcSoapRequest {

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
    return mPassword; // high security interface
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(AccountConstants.AUTH_REQUEST);
    Element a = DomUtil.add(request, AccountConstants.E_ACCOUNT, mUsername);
    DomUtil.addAttr(a, AdminConstants.A_BY, AdminConstants.BY_NAME); // XXX should use a constant
    DomUtil.add(request, AccountConstants.E_PASSWORD, mPassword);
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {
    // get the auth token out, no default, must be present or a service exception is thrown
    String authToken = DomUtil.getString(responseXML, AccountConstants.E_AUTH_TOKEN);
    ZAuthToken zat = new ZAuthToken(null, authToken, null);
    // get the session id, if not present, default to null
    String sessionId = DomUtil.getString(responseXML, HeaderConstants.E_SESSION, null);

    LmcAuthResponse responseObj = new LmcAuthResponse();
    LmcSession sess = new LmcSession(zat, sessionId);
    responseObj.setSession(sess);
    return responseObj;
  }
}
