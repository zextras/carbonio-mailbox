// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;

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
import com.zimbra.cs.util.ZTestWatchman;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.account.type.PreAuth;
import com.zimbra.soap.type.AccountSelector;

import junit.framework.Assert;


public class AuthRequestTest {

    private final String username = "someUsername";

    private final String password = "somePass";

    private final long expires = 1600000;

    private final long timestamp = expires + 60000;

    private static final String account = "testAlias@zimbra.com";
    private static final String defaultPwd = "test123";
    private static final String accountAlias = "alias@zimbra.com";
    @Rule public TestName testName = new TestName();
    @Rule public MethodRule watchman = new ZTestWatchman();

    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        Account acct = prov.createAccount(account, defaultPwd, new HashMap<String, Object>());
        prov.addAlias(acct, accountAlias);
        MailboxManager.setInstance(new DirectInsertionMailboxManager());
    }

    @After
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void testBuildAuthRequestWithPassword()
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
            assertEquals("Username embedded in request is incorrect", username, account.getText());
            assertEquals("Password embedded in request is incorrect", password, pwdE.getText());
        } catch (ServiceException e) {
            fail("Encountered an exception: " + e);
        }
    }

    @Test
    public void testBuildAuthRequestWithPreAuth()
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
            assertEquals("Username embedded in request is incorrect", username, account.getText());
            Element preauth = element.getElement("preauth");
            assertEquals("'expires' embedded in preauth is incorrect", Long.toString(expires), preauth.getAttribute("expires"));
            assertEquals("'timestamp' embedded in preauth is incorrect", Long.toString(timestamp), preauth.getAttribute("timestamp"));

        } catch (ServiceException e) {
            fail("Encountered a problem: " + e);
        }
    }

    @Test
    public void testAccountLoginWithLCEnabled() throws Exception {
        try {
            Element response = getAuthResponse(true, account);
            Assert.assertNotNull(response.getElement(AccountConstants.E_AUTH_TOKEN));
        } catch (ServiceException se) {
            fail("Encountered a problem: " + se);
        }
    }

    @Test
    public void testAccountLoginWithLCDisabled() throws Exception {
        try {
            Element response = getAuthResponse(false, account);
            Assert.assertNotNull(response.getElement(AccountConstants.E_AUTH_TOKEN));
        } catch (ServiceException se) {
            fail("Encountered a problem: " + se);
        }
    }

    @Test
    public void testAliasLoginWithLCEnabled() throws Exception {
        try {
            // Login with alias would success as alias login is enabled.
            Element response = getAuthResponse(true, accountAlias);
            Assert.assertNotNull(response.getElement(AccountConstants.E_AUTH_TOKEN));
        } catch (ServiceException se) {
            fail("Encountered a problem: " + se);
        }
    }

    @Test(expected = AuthFailedServiceException.class)
    public void testAliasLoginWithLCDisabled() throws Exception {
      //Expects AuthFailedServiceException as we are trying to login with alias when alias login is disabled.
        getAuthResponse(false, accountAlias);
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
}
