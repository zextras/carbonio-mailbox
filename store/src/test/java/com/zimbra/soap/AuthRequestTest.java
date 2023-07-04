// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.account.Auth;
import com.zimbra.cs.service.mail.SendMsgTest.DirectInsertionMailboxManager;
import com.zimbra.cs.service.mail.ServiceTestUtil;

import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.account.type.PreAuth;
import com.zimbra.soap.type.AccountSelector;


public class AuthRequestTest {

    private final String username = "someUsername";

    private final String password = "somePass";

    private final long expires = 1600000;

    private final long timestamp = expires + 60000;

    private static final String account = "testAlias@zimbra.com";
    private static final String defaultPwd = "test123";
    private static final String accountAlias = "alias@zimbra.com";
     public String testName;
    

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        Account acct = prov.createAccount(account, defaultPwd, new HashMap<String, Object>());
        prov.addAlias(acct, accountAlias);
        MailboxManager.setInstance(new DirectInsertionMailboxManager());
    }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testBuildAuthRequestWithPassword()
 {
  AuthRequest authRequest = new AuthRequest();
  authRequest.setAccount(AccountSelector.fromName(username));
  authRequest.setPassword(password);

  try {
   Element element = JaxbUtil.jaxbToElement(authRequest);
   String xml = element.toString();
   assertTrue(element.hasChildren());
   Element account = element.getElement("account");
   Element pwdE = element.getElement("password");
   assertEquals(username, account.getText(), "Username embedded in request is incorrect");
   assertEquals(password, pwdE.getText(), "Password embedded in request is incorrect");
  } catch (ServiceException e) {
   fail("Encountered an exception: " + e);
  }
 }

 @Test
 void testBuildAuthRequestWithPreAuth()
 {
  AuthRequest authRequest = new AuthRequest();
  authRequest.setAccount(AccountSelector.fromName(username));
  PreAuth preAuth = new PreAuth()
    .setExpires(expires)
    .setTimestamp(timestamp);
  authRequest.setPreauth(preAuth);

  try {
   Element element = JaxbUtil.jaxbToElement(authRequest);
   String xml = element.toString();

   Element account = element.getElement("account");
   assertEquals(username, account.getText(), "Username embedded in request is incorrect");
   Element preauth = element.getElement("preauth");
   assertEquals(Long.toString(expires), preauth.getAttribute("expires"), "'expires' embedded in preauth is incorrect");
   assertEquals(Long.toString(timestamp), preauth.getAttribute("timestamp"), "'timestamp' embedded in preauth is incorrect");

  } catch (ServiceException e) {
   fail("Encountered a problem: " + e);
  }
 }

 @Test
 void testAccountLoginWithLCEnabled() throws Exception {
  try {
   Element response = getAuthResponse(true, account);
   assertNotNull(response.getElement(AccountConstants.E_AUTH_TOKEN));
  } catch (ServiceException se) {
   fail("Encountered a problem: " + se);
  }
 }

 @Test
 void testAccountLoginWithLCDisabled() throws Exception {
  try {
   Element response = getAuthResponse(false, account);
   assertNotNull(response.getElement(AccountConstants.E_AUTH_TOKEN));
  } catch (ServiceException se) {
   fail("Encountered a problem: " + se);
  }
 }

 @Test
 void testAliasLoginWithLCEnabled() throws Exception {
  try {
   // Login with alias would success as alias login is enabled.
   Element response = getAuthResponse(true, accountAlias);
   assertNotNull(response.getElement(AccountConstants.E_AUTH_TOKEN));
  } catch (ServiceException se) {
   fail("Encountered a problem: " + se);
  }
 }

 @Test
 void testAliasLoginWithLCDisabled() throws Exception {
  assertThrows(AuthFailedServiceException.class, () -> {
   //Expects AuthFailedServiceException as we are trying to login with alias when alias login is disabled.
   getAuthResponse(false, accountAlias);
  });
 }

    private Element getAuthResponse(boolean value, String userName) throws Exception {
        String user = null;
        Element response = null;
        LC.alias_login_enabled.setDefault(value);
        Account acct = Provisioning.getInstance().getAccountByName(account);
        if (userName.equals(account)) {
            user = account;
        } else if (userName.equals(accountAlias)) {
            user = accountAlias;
        }
        Element request = new Element.JSONElement(AccountConstants.E_AUTH_REQUEST);
        request.addUniqueElement(AccountConstants.E_ACCOUNT).addAttribute(AccountConstants.A_BY, AccountConstants.A_NAME).addText(user);
        request.addUniqueElement(AccountConstants.E_PASSWORD).addText(defaultPwd);
        response = new Auth().handle(request, ServiceTestUtil.getRequestContext(acct));
        return response;
    }

 @BeforeEach
 public void setup(TestInfo testInfo) {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
 }
}
