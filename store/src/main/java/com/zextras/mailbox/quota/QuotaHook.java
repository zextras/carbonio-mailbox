package com.zextras.mailbox.quota;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;

public interface QuotaHook {

    void checkQuota(Account acct) throws ServiceException;

    void addMessage(Account acct, long newTotalMailboxUsage) throws ServiceException;

    void deleteMessage(Account acct, long size);
}
