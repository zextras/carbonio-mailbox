package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.CreateAccount;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.type.AccountSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class DeleteAccountApiTest  extends SoapTestSuite {

	private static CreateAccount createAccount;
	private static AccountAction.Factory accountActionFactory;


	@BeforeAll
	static void beforeAll() throws Exception {
		createAccount = getCreateAccountFactory();
		accountActionFactory = getAccountActionFactory();
	}

	@Test
	void shouldDeleteAccountWithPublicSharedFolder() throws Exception {
		final Account adminAccount = createAccount.asGlobalAdmin().create();
		final Account accountWithPublicSharedFolder = createAccount.create();
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
