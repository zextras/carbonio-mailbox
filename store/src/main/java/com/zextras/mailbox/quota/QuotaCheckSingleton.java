package com.zextras.mailbox.quota;

import com.zimbra.common.localconfig.LC;
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

	/**
	 * Returns the {@link ZimbraMailQuotaCheck} when {@link LC#bypass_carbonio_storages} is true,
	 * otherwise the registered hook (or the no-op default). The flag is read on every call so that a
	 * local config reload takes effect without a restart.
	 */
	public synchronized static QuotaCheck getInstance() {
		if (LC.bypass_carbonio_storages.booleanValue()) {
			return ZimbraMailQuotaCheck.INSTANCE;
		}
		if (instance == null) {
			instance = new DefaultQuotaCheck();
		}
		return instance;
	}

	public synchronized static void setInstance(QuotaCheck hook) {
		instance = hook;
	}
}
