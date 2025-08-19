package com.zimbra.cs.service.admin;

import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.AccountCreator;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.type.AccountSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class DeleteAccountApiTest  extends SoapTestSuite {

	private static AccountCreator.Factory accountCreatorFactory;
	private static AccountAction.Factory accountActionFactory;


	@BeforeAll
	static void beforeAll() throws Exception {
		Provisioning provisioning = Provisioning.getInstance();
		final MailboxManager mailboxManager = MailboxManager.getInstance();
		accountCreatorFactory = getCreateAccountFactory();
		accountActionFactory = new AccountAction.Factory(mailboxManager,
				RightManager.getInstance());
	}

	@Test
	void shouldDeleteAccountWithPublicSharedFolder() throws Exception {
		final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
		final Account accountWithPublicSharedFolder = accountCreatorFactory.get().create();
		accountActionFactory.forAccount(accountWithPublicSharedFolder)
				.grantPublicFolderRight(Mailbox.ID_FOLDER_CALENDAR, "r");
		final String accountWithPublicShareId = accountWithPublicSharedFolder.getId();

		final SoapResponse deleteAccountResponse = this.getSoapClient().newRequest()
				.setCaller(adminAccount).setSoapBody(new DeleteAccountRequest(accountWithPublicShareId))
				.call();
		Assertions.assertEquals(200, deleteAccountResponse.statusCode());

		final SoapResponse getAccountResponse = this.getSoapClient().newRequest()
				.setCaller(adminAccount)
				.setSoapBody(new GetAccountRequest(AccountSelector.fromId(accountWithPublicShareId)))
				.call();

		Assertions.assertTrue(getAccountResponse.body()
				.contains(AccountServiceException.NO_SUCH_ACCOUNT));
	}
}
