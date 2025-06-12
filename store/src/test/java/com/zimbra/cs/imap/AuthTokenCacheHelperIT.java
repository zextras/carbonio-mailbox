/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.imap;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AuthTokenCacheHelperIT extends MailboxTestSuite {

	private static Provisioning provisioning;

	@BeforeAll
	static void setUpClass() {
		provisioning = Provisioning.getInstance();
	}

	@Test
	void returnsSameCachedToken_whenTokenIsValid() throws Exception {
		final AuthTokenCacheHelper cacheHelper = new AuthTokenCacheHelper(provisioning);
		final Account account = new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get()
				.create();
		final AuthToken tokenValue1 = cacheHelper.getValidAuthToken(account);

		final AuthToken tokenValue2 = cacheHelper.getValidAuthToken(account);

		Assertions.assertEquals(tokenValue1, tokenValue2);
	}

	@Test
	void generatesANewToken_WhenTokenHasBeenDeregistered() throws Exception {
		final AuthTokenCacheHelper cacheHelper = new AuthTokenCacheHelper(provisioning);
		final Account account = new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get()
				.create();
		final AuthToken tokenValue1 = cacheHelper.getValidAuthToken(account);
		tokenValue1.deRegister();

		final AuthToken tokenValue2 = cacheHelper.getValidAuthToken(account);

		Assertions.assertNotEquals(tokenValue1.getEncoded(), tokenValue2.getEncoded());
	}

	@Test
	void generatesNewToken_WhenTokenHasExpired() throws Exception {
		final TestCacheHelper cacheHelper = new TestCacheHelper(provisioning);
		final Account account = new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get()
				.create();
		final ZimbraAuthToken expiredToken = new ZimbraAuthToken(account, System.currentTimeMillis());
		cacheHelper.populateCache(Map.of(account.getId(), expiredToken));

		final AuthToken newToken = cacheHelper.getValidAuthToken(account);

		Assertions.assertNotEquals(expiredToken.getEncoded(), newToken.getEncoded());
	}

	@Test
	void shouldRemoveExpiredTokensFromCache_WhenRequestingANewOne() throws Exception {
		final TestCacheHelper cacheHelper = new TestCacheHelper(provisioning);
		final Account account = new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get()
				.create();
		final ZimbraAuthToken expiredToken = new ZimbraAuthToken(account, System.currentTimeMillis());
		cacheHelper.populateCache(Map.of(account.getId(), expiredToken));
		final AuthToken newToken = cacheHelper.getValidAuthToken(account);

		final AuthToken tokenInCache = AuthTokenCacheHelper.CACHE.get(account.getId());

		Assertions.assertEquals(tokenInCache.getEncoded(), newToken.getEncoded());
		Assertions.assertEquals(1, AuthTokenCacheHelper.CACHE.size());
	}

	private static class TestCacheHelper extends AuthTokenCacheHelper {

		public TestCacheHelper(Provisioning provisioning) {
			super(provisioning);
		}
		public void populateCache(Map<String, AuthToken> customCache) {
			CACHE.putAll(customCache);
		}
	}

}


