package com.zextras.mailbox.quota;

import com.zimbra.cs.account.Account;

public interface QuotaHook {

    IsOverQuota getQuota(Account acct);

    IsOverQuota addMessage(Account acct, long newTotalMailboxUsage);

    void deleteMessage(Account acct, long size);
}
