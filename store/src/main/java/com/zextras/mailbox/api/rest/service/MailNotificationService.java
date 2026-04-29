/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zextras.mailbox.api.rest.resource.dto.SendNotificationRequest;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;

import java.util.List;
import java.util.function.Supplier;

public class MailNotificationService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<MailboxManager> mailboxManagerSupplier;
  private final Supplier<SoapProvisioning> soapProvisioningSupplier;

  public MailNotificationService(Supplier<Provisioning> provisioningSupplier,
                                 Supplier<MailboxManager> mailboxManagerSupplier,
                                 Supplier<SoapProvisioning> soapProvisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
    this.mailboxManagerSupplier = mailboxManagerSupplier;
    this.soapProvisioningSupplier = soapProvisioningSupplier;
  }

  public Try<Long> send(SendNotificationRequest request) {
    return Try.of(() -> (long) recipientsOf(request).size());
  }

  private static List<String> recipientsOf(SendNotificationRequest request) {
    final List<String> recipients = request.recipients();
    return recipients == null ? List.of() : recipients;
  }
}
