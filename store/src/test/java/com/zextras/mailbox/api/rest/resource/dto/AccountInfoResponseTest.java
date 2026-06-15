package com.zextras.mailbox.api.rest.resource.dto;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AccountInfoResponseTest extends MailboxTestSuite {

	// Note: add more tests on mapping?

	@Test
	void isExternalVirtualAccount() throws ServiceException {
		final Account account = createAccount().create();
		account.setIsExternalVirtualAccount(false);

		final AccountInfoResponse from = AccountInfoResponse.from(account);
		Assertions.assertFalse(from.isExternalVirtualAccount());
	}

}