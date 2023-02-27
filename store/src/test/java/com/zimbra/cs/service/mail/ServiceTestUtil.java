// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zextras.carbonio.files.FilesClient;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.cs.service.MockHttpServletResponse;
import com.zimbra.soap.DocumentService;
import com.zimbra.soap.MockSoapEngine;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ServiceTestUtil {

  public static Map<String, Object> getRequestContext(Account acct) throws Exception {
    return getRequestContext(acct, acct);
  }

  public static Map<String, Object> getExternalRequestContext(
      String externalEmail, Account targetAcct) throws Exception {
    return getRequestContext(new GuestAccount(externalEmail, "password"), targetAcct);
  }

  public static Map<String, Object> getRequestContext(Account authAcct, Account targetAcct)
      throws Exception {
    return getRequestContext(
        authAcct,
        targetAcct,
        new MailService(
            new MailboxAttachmentService(), FilesClient.atURL("http://127.78.0.7:20002")));
  }

  public static Map<String, Object> getRequestContext(
      Account authAcct, Account targetAcct, DocumentService service) throws Exception {
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(authAcct),
            targetAcct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12));
    context.put(
        SoapServlet.SERVLET_REQUEST,
        new MockHttpServletRequest(
            "test".getBytes("UTF-8"), new URL("http://localhost:7070/service/FooRequest"), ""));
    context.put(SoapEngine.ZIMBRA_ENGINE, new MockSoapEngine(service));
    context.put(SoapServlet.SERVLET_RESPONSE, new MockHttpServletResponse());
    return context;
  }
}
