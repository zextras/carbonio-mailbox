package com.zextras.mailbox.quota;

import com.zimbra.cs.account.Account;

public class QuotaHookSingleton {
    private static QuotaHook instance;

    public static class DefaultQuotaHook implements QuotaHook {
        @Override
        public IsOverQuota getQuota(Account acct) {
            return new IsOverQuota(false);
        }

        @Override
        public IsOverQuota addMessage(Account acct, long newTotalMailboxUsage) {
            return new IsOverQuota(false);
        }

        @Override
        public void deleteMessage(Account acct, long size) {
        }
    }

    public synchronized static QuotaHook getInstance() {
        if (instance == null) {
            instance = new DefaultQuotaHook();
        }
        return instance;
    }

    public synchronized static void setInstance(QuotaHook hook) {
        instance = hook;
    }
}
