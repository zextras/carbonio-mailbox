// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.acl.AclService;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator.Factory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.account.accesscontrol.generated.UserRights;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AclServiceTest extends MailboxTestSuite {
  private static MailboxManager mailboxManager;
  private static Provisioning provisioning;
  private static Factory accountCreatorFactory;

  @BeforeAll
  static void setUp() throws Exception {
    mailboxManager = MailboxManager.getInstance();
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new Factory(provisioning);
  }

  private static void setRights(Account target) throws ServiceException {
    final var grantee = accountCreatorFactory.get().create();
    final var granterMailbox = mailboxManager.getMailboxByAccount(target);
    final var inboxId = granterMailbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);

    // User grant
    ACLUtil.grantRight(
        Provisioning.getInstance(),
        target,
        Set.of(
            new ZimbraACE(
                grantee.getId(),
                GranteeType.GT_USER,
                UserRights.R_sendAs,
                RightModifier.RM_CAN_DELEGATE,
                "")));
    // Grant on mailbox item (folder)
    granterMailbox.grantAccess(
        null, inboxId.getId(), grantee.getName(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
  }

  @Test
  void shouldRevokeAllMailboxFolderGrants() throws ServiceException {
    var target = accountCreatorFactory.get().create();
    setRights(target);

    final var grantsService = new AclService(mailboxManager, provisioning);
    grantsService.revokeAllMailboxGrantsForAccountId(null, target.getId());
    assertEquals(0, grantsService.getMailboxFolderGrantsForAccountId(null, target.getId()).size());
    assertEquals(1, grantsService.getGrantsTargetingAccount(target.getId()).getACEs().size());
  }

  @Test
  void shouldRevokeAllGrantsTargetingAccount() throws ServiceException {
    var target = accountCreatorFactory.get().create();
    setRights(target);
    final var grantsService = new AclService(mailboxManager, provisioning);
    grantsService.revokeAllGrantsForAccountId(target.getId());
    assertEquals(1, grantsService.getMailboxFolderGrantsForAccountId(null, target.getId()).size());
    assertEquals(0, grantsService.getGrantsTargetingAccount(target.getId()).getACEs().size());
  }

  @Test
  void shouldReturnAllGrantsOnMailboxFolders() throws ServiceException {
    var target = accountCreatorFactory.get().create();
    setRights(target);
    final var grantsService = new AclService(mailboxManager, provisioning);
    assertEquals(1, grantsService.getMailboxFolderGrantsForAccountId(null, target.getId()).size());
  }

  @Test
  void shouldReturnGrantsTargetingAccount() throws ServiceException {
    var target = accountCreatorFactory.get().create();
    setRights(target);
    final var grantsService = new AclService(mailboxManager, provisioning);
    assertEquals(1, grantsService.getGrantsTargetingAccount(target.getId()).getACEs().size());
  }
}
