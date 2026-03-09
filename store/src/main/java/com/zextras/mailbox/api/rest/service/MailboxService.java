package com.zextras.mailbox.api.rest.service;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;

import java.util.function.Supplier;

public class MailboxService {

	private final Supplier<MailboxManager> mailboxManagerSupplier;
	private final Supplier<SoapProvisioning> soapProvisioningSupplier;
	private final Supplier<Provisioning> provisioningSupplier;
	private final AccountService accountService;

	public MailboxService(Supplier<MailboxManager> mailboxManagerSupplier,
                          Supplier<SoapProvisioning> soapProvisioningSupplier,
                          Supplier<Provisioning> provisioningSupplier, AccountService accountService) {
		this.mailboxManagerSupplier = mailboxManagerSupplier;
		this.soapProvisioningSupplier = soapProvisioningSupplier;
		this.provisioningSupplier = provisioningSupplier;
      this.accountService = accountService;
    }

	public Try<Long> getMailUsage(String accountId) {
		return accountService.getAccount(accountId)
						.flatMap(this::getMailUsage);
	}


	public Try<Long> getMailUsage(Account account) {
		return Try.of(() -> {
			final Provisioning provisioning = provisioningSupplier.get();
			if (provisioning.onLocalServer(account)) {
				final Mailbox mbox = mailboxManagerSupplier.get().getMailboxByAccount(account);
				return mbox.getSize();
			} else {
				final SoapProvisioning.MailboxInfo info = soapProvisioningSupplier.get().getMailbox(account);
				return info.getUsed();
			}
		});
	}
}
