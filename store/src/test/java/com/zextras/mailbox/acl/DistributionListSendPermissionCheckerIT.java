// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.acl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.MailServiceException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistributionListSendPermissionCheckerIT extends MailboxTestSuite {

  private static Provisioning provisioning;
  private DistributionListSendPermissionChecker checker;

  @BeforeAll
  static void setUpClass() {
    provisioning = Provisioning.getInstance();
  }

  @BeforeEach
  void setUp() {
    checker = new DistributionListSendPermissionChecker(provisioning, AccessManager.getInstance());
  }

  @Test
  void shouldAllowSender_whenDistributionListHasNoGrants() throws Exception {
    var sender = createAccount().create();
    var dl = createDistributionList();

    assertTrue(checker.findDeniedDistributionLists(sender.getName(), addresses(dl.getName())).isEmpty());
  }

  @Test
  void shouldAllowSender_whenGrantedSendToDistList() throws Exception {
    var sender = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, sender);

    assertTrue(checker.findDeniedDistributionLists(sender.getName(), addresses(dl.getName())).isEmpty());
  }

  @Test
  void shouldDenySender_whenRightGrantedToSomeoneElse() throws Exception {
    var sender = createAccount().create();
    var someoneElse = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);

    var exception =
        assertThrows(
            MailServiceException.class,
            () -> checker.assertCanSendTo(sender.getName(), addresses(dl.getName())));

    assertSame(MailServiceException.SEND_ABORTED_ADDRESS_FAILURE, exception.getCode());
    assertTrue(exception.getMessage().contains(dl.getName()));
    assertEquals(dl.getName(), exception.getArgumentValue("invalid"));
  }

  @Test
  void shouldIgnoreRecipients_thatAreNotDistributionLists() throws Exception {
    var sender = createAccount().create();
    var recipient = createAccount().create();

    var denied =
        checker.findDeniedDistributionLists(
            sender.getName(), addresses(recipient.getName(), "external@outside.com"));

    assertTrue(denied.isEmpty());
  }

  @Test
  void shouldReportOnlyDeniedLists_whenRecipientsAreMixed() throws Exception {
    var sender = createAccount().create();
    var someoneElse = createAccount().create();
    var recipient = createAccount().create();
    var allowedDl = createDistributionList();
    var deniedDl = createDistributionList();
    grantSendToDistList(deniedDl, someoneElse);

    var denied =
        checker.findDeniedDistributionLists(
            sender.getName(),
            addresses(recipient.getName(), allowedDl.getName(), deniedDl.getName()));

    assertEquals(1, denied.size());
    assertEquals(deniedDl.getName(), ((InternetAddress) denied.get(0)).getAddress());
  }

  @Test
  void shouldAllowSender_whenAddressedByAlias() throws Exception {
    var sender = createAccount().create();
    var alias = UUID.randomUUID() + "@" + DEFAULT_DOMAIN_NAME;
    provisioning.addAlias(sender, alias);
    var dl = createDistributionList();
    grantSendToDistList(dl, sender);

    assertTrue(checker.findDeniedDistributionLists(alias, addresses(dl.getName())).isEmpty());
  }

  private static DistributionList createDistributionList() throws Exception {
    return provisioning.createDistributionList(
        UUID.randomUUID() + "@" + DEFAULT_DOMAIN_NAME, new HashMap<>());
  }

  private static void grantSendToDistList(DistributionList dl, Account grantee) throws Exception {
    ACLUtil.grantRight(
        provisioning,
        dl,
        Set.of(
            new ZimbraACE(
                grantee.getId(),
                GranteeType.GT_USER,
                RightManager.getInstance().getRight(Right.RT_sendToDistList),
                null,
                null)));
  }

  private static Address[] addresses(String... emails) throws Exception {
    var result = new Address[emails.length];
    for (int i = 0; i < emails.length; i++) {
      result[i] = new InternetAddress(emails[i]);
    }
    return result;
  }
}
