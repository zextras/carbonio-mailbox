// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ForgetPasswordEnums.CodeConstants;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthToken.Usage;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.account.Auth;
import com.zimbra.cs.service.mail.DirectInsertionMailboxManager;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.service.util.JWEUtil;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.account.type.PreAuth;
import com.zimbra.soap.type.AccountSelector;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class AuthRequestTest extends MailboxTestSuite {

	private static String account;
	private static final String defaultPwd = "test123";
	private static String accountAlias;
	private final String username = "someUsername";
	private final String password = "somePass";
	private final long expires = 1600000;
	private final long timestamp = expires + 60000;
	public String testName;


	@BeforeAll
	public static void init() throws Exception {
		final Account acct = createAccount()
				.withPassword(defaultPwd).create();
		account = acct.getName();
		accountAlias = "alias@" + acct.getDomainName();
		acct.addAlias(accountAlias);
		MailboxManager.setInstance(new DirectInsertionMailboxManager());
	}

	@Test
	void testBuildAuthRequestWithPassword() {
		AuthRequest authRequest = new AuthRequest();
		authRequest.setAccount(AccountSelector.fromName(username));
		authRequest.setPassword(password);

		try {
			Element element = JaxbUtil.jaxbToElement(authRequest);
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
	void testBuildAuthRequestWithPreAuth() {
		AuthRequest authRequest = new AuthRequest();
		authRequest.setAccount(AccountSelector.fromName(username));
		PreAuth preAuth = new PreAuth()
				.setExpires(expires)
				.setTimestamp(timestamp);
		authRequest.setPreauth(preAuth);

		try {
			Element element = JaxbUtil.jaxbToElement(authRequest);

			Element account = element.getElement("account");
			assertEquals(username, account.getText(), "Username embedded in request is incorrect");
			Element preauth = element.getElement("preauth");
			assertEquals(Long.toString(expires), preauth.getAttribute("expires"),
					"'expires' embedded in preauth is incorrect");
			assertEquals(Long.toString(timestamp), preauth.getAttribute("timestamp"),
					"'timestamp' embedded in preauth is incorrect");

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

	@Test
	void should_return_auth_token_of_type_auth_when_recovery_code_is_supplied() throws Exception {
		final String testCode = "myCode";
		final String jweCode = generateJweCode(testCode);
		final Account account1 = createAccount().
				withAttribute(ZAttrProvisioning.A_zimbraFeatureResetPasswordStatus, "enabled")
				.withAttribute(ZAttrProvisioning.A_zimbraResetPasswordRecoveryCode, jweCode)
				.withPassword(defaultPwd).
				create();
		account1.setResetPasswordRecoveryCode(jweCode);
		final Element authRequestElement = new Element.JSONElement(AccountConstants.E_AUTH_REQUEST);
		authRequestElement.addUniqueElement(AccountConstants.E_ACCOUNT)
				.addAttribute(AccountConstants.A_BY, AccountConstants.A_NAME).addText(account1.getName());
		authRequestElement.addUniqueElement(AccountConstants.E_RECOVERY_CODE).addText(testCode);

		final Element responseElement = new Auth().handle(authRequestElement,
				ServiceTestUtil.getRequestContext(account1));
		final AuthToken authToken = AuthToken.getAuthToken(
				responseElement.getElement(AccountConstants.E_AUTH_TOKEN).getText());

		assertEquals(Usage.AUTH, authToken.getUsage());
	}

	private static String generateJweCode(String testCode) throws ServiceException {
		return JWEUtil.getJWE(Map.of(
				CodeConstants.CODE.toString(), testCode,
				CodeConstants.EXPIRY_TIME.toString(),
				String.valueOf(System.currentTimeMillis() + 60 * 1000))
		);
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
		request.addUniqueElement(AccountConstants.E_ACCOUNT)
				.addAttribute(AccountConstants.A_BY, AccountConstants.A_NAME).addText(user);
		request.addUniqueElement(AccountConstants.E_PASSWORD).addText(defaultPwd);
		response = new Auth().handle(request, ServiceTestUtil.getRequestContext(acct));
		return response;
	}

}
