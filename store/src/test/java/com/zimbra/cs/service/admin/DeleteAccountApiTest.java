package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapExtension;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
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
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("api")
class DeleteAccountApiTest extends SoapTestSuite {

	@RegisterExtension
	static SoapExtension soapExtension = new SoapExtension.Builder()
			.addEngineHandler(NoFilesInstalledAdminService.class.getName())
			.create();

	private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
	private static MailboxTestUtil.AccountAction.Factory accountActionFactory;



	@BeforeAll
	static void beforeAll() throws Exception {
		Provisioning provisioning = Provisioning.getInstance();
		final MailboxManager mailboxManager = MailboxManager.getInstance();
		accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
		accountActionFactory = new MailboxTestUtil.AccountAction.Factory(mailboxManager,
				RightManager.getInstance());
	}

	@Test
	void shouldDeleteAccountWithPublicSharedFolder() throws Exception {
		final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
		final Account accountWithPublicSharedFolder = accountCreatorFactory.get().create();
		accountActionFactory.forAccount(accountWithPublicSharedFolder)
				.grantPublicFolderRight(Mailbox.ID_FOLDER_CALENDAR, "r");
		final String accountWithPublicShareId = accountWithPublicSharedFolder.getId();

		final SoapResponse deleteAccountResponse = getSoapClient().newRequest()
				.setCaller(adminAccount).setSoapBody(new DeleteAccountRequest(accountWithPublicShareId))
				.call();
		Assertions.assertEquals(200, deleteAccountResponse.statusCode());

		final SoapResponse getAccountResponse = getSoapClient().newRequest()
				.setCaller(adminAccount)
				.setSoapBody(new GetAccountRequest(AccountSelector.fromId(accountWithPublicShareId)))
				.call();

		Assertions.assertTrue(getAccountResponse.body()
				.contains(AccountServiceException.NO_SUCH_ACCOUNT));
	}
}
