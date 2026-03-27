package com.zextras.mailbox.quota;

import com.zimbra.cs.account.Account;

public class QuotaHookSingleton {
    private static QuotaHook instance;

    public static class DefaultQuotaHook implements QuotaHook {
        @Override
        public boolean isOverQuota(Account acct) {
            return false;
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
