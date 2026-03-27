package com.zextras.mailbox.quota;

import com.zimbra.cs.account.Account;

public interface QuotaHook {

    boolean isOverQuota(Account acct);
}
