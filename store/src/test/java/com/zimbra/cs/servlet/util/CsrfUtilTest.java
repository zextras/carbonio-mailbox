// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.util;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.net.HttpHeaders;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.ZimbraAuthToken;
import java.net.MalformedURLException;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * @author zimbra
 */
public class CsrfUtilTest extends MailboxTestSuite {

	private static final long AUTH_TOKEN_EXPR = System.currentTimeMillis() + 60 * 1000 * 60;
	private static final int CSRFTOKEN_SALT = 5;
	private static Account acct;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		byte[] nonce = new byte[16];
		Random nonceGen = new Random();
		nonceGen.nextBytes(nonce);
		acct = createAccount().create();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	final void testIsCsrfRequest() throws MalformedURLException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getMethod()).thenReturn("POST");
		Mockito.when(request.getRequestURI()).thenReturn(
				"service/soap/AuthRequest");

		boolean csrfReq = CsrfUtil.doCsrfCheck(request, null);
		assertFalse(csrfReq);
	}

	@Test
	final void testIsCsrfRequestForGet() throws MalformedURLException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getMethod()).thenReturn("GET");
		Mockito.when(request.getRequestURI()).thenReturn(
				"service/soap/AuthRequest");

		boolean csrfReq = CsrfUtil.doCsrfCheck(request, null);
		assertFalse(csrfReq);
	}


	@Test
	final void testDecodeValidCsrfToken() throws ServiceException, AuthTokenException {
			AuthToken authToken = new ZimbraAuthToken(acct);

			String csrfToken = CsrfUtil.generateCsrfToken(acct.getId(),
					AUTH_TOKEN_EXPR, CSRFTOKEN_SALT, authToken);
			Pair<String, String> tokenParts = CsrfUtil.parseCsrfToken(csrfToken);
			assertNotNull(tokenParts.getFirst());
			assertNotNull(tokenParts.getSecond());
			assertEquals("0", tokenParts.getSecond());
	}

	@Test
	final void testIsValidCsrfTokenForAccountWithMultipleTokens() throws ServiceException {
			AuthToken authToken = new ZimbraAuthToken(acct);

			String csrfToken1 = CsrfUtil.generateCsrfToken(acct.getId(),
					AUTH_TOKEN_EXPR, CSRFTOKEN_SALT, authToken);
			boolean validToken = CsrfUtil.isValidCsrfToken(csrfToken1, authToken);
			assertTrue(validToken);
	}

	@Test
	final void testIsValidCsrfTokenForAccountWithNullAuthToken() throws Exception {
		AuthToken authToken = new ZimbraAuthToken(acct);

		String csrfToken1 = CsrfUtil.generateCsrfToken(acct.getId(),
				AUTH_TOKEN_EXPR, CSRFTOKEN_SALT, authToken);
		boolean validToken = CsrfUtil.isValidCsrfToken(csrfToken1, null);
		assertFalse(validToken);
	}


	@Test
	final void testIsCsrfRequestWhenCsrfCheckIsTurnedOn() throws MalformedURLException {

		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito
				.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn(
				"example.com");
		Mockito.when(request.getServerName()).thenReturn("example.com");
		Mockito.when(request.getServerName()).thenReturn("example.com");
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getMethod()).thenReturn("POST");
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(null);

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertFalse(csrfReq);
	}

	private static String[] getAllowedRefHost() {
		return new String[]{"test"};
	}

	@Test
	final void testIsCsrfRequestForSameReferer() throws MalformedURLException {
		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn("www.example.com");
		Mockito.when(request.getServerName()).thenReturn("www.example.com");
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"http://www.example.com/zimbra/#15");
		Mockito.when(request.getMethod()).thenReturn("POST");

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertFalse(csrfReq);
	}

	@Test
	final void testIsCsrfRequestForRefererInMatchHost() throws MalformedURLException {
		final String[] allowedRefHost = generateThreeRefHosts();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn("example.com");
		Mockito.when(request.getServerName()).thenReturn("example.com");
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"http://www.newexample.com");
		Mockito.when(request.getMethod()).thenReturn("POST");

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertFalse(csrfReq);
	}

	private static String[] generateThreeRefHosts() {
		String[] allowedRefHost = new String[3];
		allowedRefHost[0] = "www.newexample.com";
		allowedRefHost[1] = "www.zextras.com:8080";
		allowedRefHost[2] = "www.abc.com";
		return allowedRefHost;
	}

	@Test
	final void testIsCsrfRequestForAllowedRefHostListEmptyAndNonMatchingHost() throws MalformedURLException {
		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn("example.com");
		Mockito.when(request.getServerName()).thenReturn("example.com");
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"http://www.newexample.com");
		Mockito.when(request.getMethod()).thenReturn("POST");

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertTrue(csrfReq);
	}

	@Test
	final void testIsCsrfRequestForRefererNotInMatchHost() throws MalformedURLException {
		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito
				.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn(
				"example.com");
		Mockito.when(request.getServerName()).thenReturn("example.com");
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"http://www.newexample.com");
		Mockito.when(request.getMethod()).thenReturn("POST");

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertTrue(csrfReq);
	}

	@Test
	final void testIsCsrfRequestForSameRefererWithUrlHavingPort() throws MalformedURLException {

		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito
				.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn(
				"www.example.com:7070");
		Mockito.when(request.getServerName()).thenReturn(
				"www.example.com:7070");
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"http://www.example.com:7070");
		Mockito.when(request.getMethod()).thenReturn("POST");
		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertFalse(csrfReq);
	}

	@Test
	final void testIsCsrfRequestForSameRefererWithHttpsUrl() throws MalformedURLException {
		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader(HttpHeaders.HOST)).thenReturn("mail.zimbra.com");
		Mockito.when(request.getServerName()).thenReturn("mail.zimbra.com");
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"https://mail.zimbra.com/zimbra/");
		Mockito.when(request.getMethod()).thenReturn("POST");

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertFalse(csrfReq);
	}

	@Test
	final void testIsCsrfRequestForSameRefererWithXFowardedHostHdr() throws MalformedURLException {
		String[] allowedRefHost = getAllowedRefHost();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Forwarded-Host")).thenReturn("mail.zimbra.com");
		Mockito.when(request.getHeader(HttpHeaders.REFERER)).thenReturn(
				"https://mail.zimbra.com/zimbra/");
		Mockito.when(request.getMethod()).thenReturn("POST");

		boolean csrfReq = CsrfUtil.isCsrfRequestBasedOnReferrer(request, allowedRefHost);
		assertFalse(csrfReq);
	}

}
