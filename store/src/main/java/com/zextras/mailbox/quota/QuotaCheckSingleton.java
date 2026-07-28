package com.zextras.mailbox.quota;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;

public class QuotaCheckSingleton {

	private static QuotaCheck instance;

	public static class DefaultQuotaCheck implements QuotaCheck {

		@Override
		public void onSendMessage(Account account) throws ServiceException {
			// no quota checks
		}

		@Override
		public void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException {
			// no quota checks
		}

		@Override
		public void onDeleteMessage(Account acct, long size) {
			// no quota checks
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
