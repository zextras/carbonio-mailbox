package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class DeleteAccountApiTest extends SoapTestSuite {

    private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
    private static MailboxTestUtil.AccountAction.Factory accountActionFactory;

    @BeforeAll
    static void beforeAll() throws Exception {
        Provisioning provisioning = Provisioning.getInstance();
        final MailboxManager mailboxManager = MailboxManager.getInstance();
        accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
        accountActionFactory =  new MailboxTestUtil.AccountAction.Factory(mailboxManager, RightManager.getInstance());
    }

    @Test
    void shouldDeleteAccountWithPublicSharedFolder () throws Exception {
       final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
       final Account accountWithPublicSharedFolder = accountCreatorFactory.get().create();
       accountActionFactory.forAccount(accountWithPublicSharedFolder).grantPublicFolderRight(Mailbox.ID_FOLDER_CALENDAR, "r");

        final HttpResponse response = getSoapClient().newRequest()
                .setCaller(adminAccount).setSoapBody(new DeleteAccountRequest(accountWithPublicSharedFolder.getId())).execute();

        final String pippo = SoapUtils.getResponse(response);
        System.out.println(pippo);

        Assertions.assertEquals(200, response.getStatusLine().getStatusCode());

    }
}
