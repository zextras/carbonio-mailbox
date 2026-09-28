// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.acl;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.Rights;
import com.zimbra.cs.mailbox.MailServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks whether a sender is allowed to email the distribution lists among a set of recipients,
 * applying the same {@code sendToDistList} rule the milter enforces at SMTP time. This lets callers
 * that send mail as a side effect (for example calendar invitations) fail early, before persisting
 * anything, instead of silently losing the message.
 */
public class DistributionListSendPermissionChecker {

  private static final Logger logger =
      LoggerFactory.getLogger(DistributionListSendPermissionChecker.class);
  private static final String DENIED_MESSAGE =
      "Sender is not allowed to email this distribution list: ";

  private final Provisioning provisioning;
  private final AccessManager accessManager;

  public DistributionListSendPermissionChecker(
      Provisioning provisioning, AccessManager accessManager) {
    this.provisioning = provisioning;
    this.accessManager = accessManager;
  }

  /**
   * Throws {@link MailServiceException#SEND_ABORTED_ADDRESS_FAILURE} when the sender is not allowed
   * to email at least one of the distribution lists among the recipients. The denied lists are
   * reported as invalid addresses, the other recipients as valid unsent addresses.
   */
  public void assertCanSendTo(String senderEmail, Address[] recipients) throws ServiceException {
    List<Address> denied = findDeniedDistributionLists(senderEmail, recipients);
    if (denied.isEmpty()) {
      return;
    }
    List<Address> unsent = allExcept(recipients, denied);
    throw MailServiceException.SEND_ABORTED_ADDRESS_FAILURE(
        DENIED_MESSAGE + joinAddresses(denied),
        null,
        denied.toArray(new Address[0]),
        unsent.toArray(new Address[0]));
  }

  /** Returns the recipients that are distribution lists the sender is not allowed to email. */
  public List<Address> findDeniedDistributionLists(String senderEmail, Address[] recipients)
      throws ServiceException {
    if (senderEmail == null || recipients == null) {
      return List.of();
    }
    List<Address> denied = new ArrayList<>();
    for (Address recipient : recipients) {
      if (isDeniedDistributionList(senderEmail, recipient)) {
        denied.add(recipient);
      }
    }
    return denied;
  }

  private boolean isDeniedDistributionList(String senderEmail, Address recipient)
      throws ServiceException {
    String address = emailOf(recipient);
    if (address == null || !provisioning.isDistributionList(address)) {
      return false;
    }
    Group group = provisioning.getGroupBasic(Key.DistributionListBy.name, address);
    if (group == null) {
      logger.debug("{} is listed as a distribution list but no group entry was found", address);
      return false;
    }
    boolean allowed =
        accessManager.canDo(senderEmail, group, Rights.User.R_sendToDistList, false);
    if (!allowed) {
      logger.info("sender {} is not allowed to email distribution list {}", senderEmail, address);
    }
    return !allowed;
  }

  private static String emailOf(Address address) {
    if (address instanceof InternetAddress) {
      return ((InternetAddress) address).getAddress();
    }
    return address == null ? null : address.toString();
  }

  private static List<Address> allExcept(Address[] recipients, List<Address> excluded) {
    List<Address> remaining = new ArrayList<>();
    for (Address recipient : recipients) {
      if (!excluded.contains(recipient)) {
        remaining.add(recipient);
      }
    }
    return remaining;
  }

  private static String joinAddresses(List<Address> addresses) {
    return addresses.stream()
        .map(DistributionListSendPermissionChecker::emailOf)
        .collect(Collectors.joining(", "));
  }
}
