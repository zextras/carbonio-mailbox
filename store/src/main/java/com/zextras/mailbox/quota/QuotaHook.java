package com.zextras.mailbox.quota;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;

public interface QuotaHook {

    IsOverQuota getQuota(Account acct);

    void addMessage(Account acct, long newTotalMailboxUsage) throws ServiceException;

    void deleteMessage(Account acct, long size);
}
