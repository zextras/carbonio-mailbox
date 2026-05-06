package com.zextras.mailbox.quota;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.util.AccountUtil;

public class QuotaCheckSingleton {

	private static QuotaCheck instance;

	public static class DefaultQuotaCheck implements QuotaCheck {

		@Override
		public void onSendMessage(Account account) throws ServiceException {
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			long acctQuota = AccountUtil.getEffectiveQuota(account);

			if (account.isMailAllowReceiveButNotSendWhenOverQuota()
					&& acctQuota != 0 && mbox.getSize() > acctQuota) {
				throw MailServiceException.QUOTA_EXCEEDED(acctQuota);
			}

			Domain domain = Provisioning.getInstance().getDomain(account);
			if (domain != null
					&& AccountUtil.isOverAggregateQuota(domain)
					&& !AccountUtil.isSendAllowedOverAggregateQuota(domain)) {
				throw MailServiceException.DOMAIN_QUOTA_EXCEEDED(domain.getDomainAggregateQuota());
			}
		}

		@Override
		public void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException {
			long acctQuota = AccountUtil.getEffectiveQuota(acct);
			if (acctQuota != 0 && newTotalMailboxUsage > acctQuota) {
				throw MailServiceException.QUOTA_EXCEEDED(acctQuota);
			}
			Domain domain = Provisioning.getInstance().getDomain(acct);
			if (domain != null
					&& AccountUtil.isOverAggregateQuota(domain)
					&& !AccountUtil.isReceiveAllowedOverAggregateQuota(domain)) {
				throw MailServiceException.DOMAIN_QUOTA_EXCEEDED(domain.getDomainAggregateQuota());
			}
		}

		@Override
		public void onDeleteMessage(Account acct, long size) {
		}
	}

	public synchronized static QuotaCheck getInstance() {
		if (instance == null) {
			instance = new DefaultQuotaCheck();
		}
		return instance;
	}

	public synchronized static void setInstance(QuotaCheck hook) {
		instance = hook;
	}
}
