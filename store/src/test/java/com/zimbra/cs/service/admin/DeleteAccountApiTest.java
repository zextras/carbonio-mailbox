package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapExtension;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.mail.MailServiceWithoutTracking;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.type.AccountSelector;
import org.apache.http.HttpResponse;
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
			.addEngineHandler(AccountService.class.getName())
			.addEngineHandler(MailServiceWithoutTracking.class.getName())
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

		final HttpResponse deleteAccountResponse = getSoapClient().newRequest()
				.setCaller(adminAccount).setSoapBody(new DeleteAccountRequest(accountWithPublicShareId))
				.execute();
		Assertions.assertEquals(200, deleteAccountResponse.getStatusLine().getStatusCode());

		final HttpResponse getAccountResponse = getSoapClient().newRequest()
				.setCaller(adminAccount)
				.setSoapBody(new GetAccountRequest(AccountSelector.fromId(accountWithPublicShareId)))
				.execute();

		Assertions.assertTrue(SoapUtils.getResponse(getAccountResponse)
				.contains(AccountServiceException.NO_SUCH_ACCOUNT));
	}
}
