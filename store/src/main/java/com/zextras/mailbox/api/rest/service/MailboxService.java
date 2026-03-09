package com.zextras.mailbox.api.rest.service;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;

import java.util.function.Supplier;

public class MailboxService {

  private final Supplier<MailboxManager> mailboxManagerSupplier;
  private final Supplier<SoapProvisioning> soapProvisioningSupplier;

  public MailboxService(Supplier<MailboxManager> mailboxManagerSupplier,
                        Supplier<SoapProvisioning> soapProvisioningSupplier) {
    this.mailboxManagerSupplier = mailboxManagerSupplier;
    this.soapProvisioningSupplier = soapProvisioningSupplier;
  }


  public Try<Long> getMailUsage(Account account) {
    return Try.of(() -> {
      if (account.isOnLocalServer()) {
        final Mailbox mbox = mailboxManagerSupplier.get().getMailboxByAccount(account);
        return mbox.getSize();
      } else {
        final SoapProvisioning.MailboxInfo info = soapProvisioningSupplier.get().getMailbox(account);
        return info.getUsed();
      }
    });
  }
}
