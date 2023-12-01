package com.zimbra.cs.service.admin;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RenameAccountTest {
  private static Provisioning provisioning;
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @AfterEach
  void tearDown() throws ServiceException {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldRenameAccount() throws ServiceException {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    Account userAccount = accountCreatorFactory.get().create();
  }
}
