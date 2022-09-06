// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.DomUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcChangePasswordRequest extends LmcSoapRequest {

  private String mOldPassword;
  private String mPassword;
  private String mAccount;

  private static final String BY_NAME = "name";

  public void setOldPassword(String o) {
    mOldPassword = o;
  }

  public void setPassword(String p) {
    mPassword = p;
  }

  public void setAccount(String a) {
    mAccount = a;
  }

  public String getOldPassword() {
    return mOldPassword;
  }

  public String getPassword() {
    return mPassword;
  }

  public String getAccount() {
    return mAccount;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(AccountConstants.CHANGE_PASSWORD_REQUEST);
    // <account>
    Element a = DomUtil.add(request, AccountConstants.E_ACCOUNT, mAccount);
    DomUtil.addAttr(a, AdminConstants.A_BY, BY_NAME);
    // <old password>
    DomUtil.add(request, AccountConstants.E_OLD_PASSWORD, mOldPassword);
    // <password>
    DomUtil.add(request, AccountConstants.E_PASSWORD, mPassword);
    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) throws ServiceException {
    // there is no response to the request, only a fault
    LmcChangePasswordResponse response = new LmcChangePasswordResponse();
    return response;
  }
}
