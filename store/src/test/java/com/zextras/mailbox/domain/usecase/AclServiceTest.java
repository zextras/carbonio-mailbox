// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.acl.AclService;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.account.accesscontrol.generated.UserRights;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AclServiceTest {
  private Account target;
  private MailboxManager mailboxManager;
  private Provisioning provisioning;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    mailboxManager = MailboxManager.getInstance();
    target = MailboxTestUtil.createRandomAccountForDefaultDomain();
    provisioning = Provisioning.getInstance();
    final Account grantee = MailboxTestUtil.createRandomAccountForDefaultDomain();
    final Mailbox granterMailbox = mailboxManager.getMailboxByAccount(target);
    final Folder inboxId = granterMailbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);

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

  @AfterEach
  void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldRevokeAllMailboxFolderGrants() throws ServiceException {
    final AclService grantsService = new AclService(mailboxManager, provisioning);
    grantsService.revokeAllMailboxGrantsForAccountId(null, target.getId());
    assertEquals(0, grantsService.getMailboxFolderGrantsForAccountId(null, target.getId()).size());
    assertEquals(1, grantsService.getGrantsTargetingAccount(target.getId()).getACEs().size());
  }

  @Test
  void shouldRevokeAllGrantsTargetingAccount() throws ServiceException {
    final AclService grantsService = new AclService(mailboxManager, provisioning);
    grantsService.revokeAllGrantsForAccountId(target.getId());
    assertEquals(1, grantsService.getMailboxFolderGrantsForAccountId(null, target.getId()).size());
    assertEquals(0, grantsService.getGrantsTargetingAccount(target.getId()).getACEs().size());
  }

  @Test
  void shouldReturnAllGrantsOnMailboxFolders() throws ServiceException {
    final AclService grantsService = new AclService(mailboxManager, provisioning);
    assertEquals(1, grantsService.getMailboxFolderGrantsForAccountId(null, target.getId()).size());
  }

  @Test
  void shouldReturnGrantsTargetingAccount() throws ServiceException {
    final AclService grantsService = new AclService(mailboxManager, provisioning);
    assertEquals(1, grantsService.getGrantsTargetingAccount(target.getId()).getACEs().size());
  }
}
