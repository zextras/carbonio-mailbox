// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;

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
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrantsServiceTest {
  private Account target;
  private MailboxManager mailboxManager;
  private Provisioning provisioning;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    mailboxManager = MailboxManager.getInstance();
    target = MailboxTestUtil.createBasicAccount();
    provisioning = Provisioning.getInstance();
    final Account grantee = MailboxTestUtil.createBasicAccount();
    final Mailbox granterMailbox = mailboxManager.getMailboxByAccount(target);
    final Folder inboxId = granterMailbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);

    // LDAP grant
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
    // grant on mailbox item
    granterMailbox.grantAccess(
        null, inboxId.getId(), grantee.getName(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
  }

  @AfterEach
  void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldRevokeUserGrantsFromGrantedAccounts() throws ServiceException {
    final GrantsService grantsService = new GrantsService(mailboxManager, provisioning);
    grantsService.revokeAllGrantsForAccountId(null, target.getId());
    assertEquals(0, grantsService.getAllFolderGrantsForAccountId(null, target.getId()).size());
    assertEquals(0, grantsService.getLDAPGrantsForAccountId(target.getId()).getACEs().size());
  }

  @Test
  void shouldReturnGrantsForGranteeAndTarget() throws ServiceException {
    final GrantsService grantsService = new GrantsService(mailboxManager, provisioning);
    assertEquals(1, grantsService.getAllFolderGrantsForAccountId(null, target.getId()).size());
    assertEquals(1, grantsService.getLDAPGrantsForAccountId(target.getId()).getACEs().size());
  }
}
