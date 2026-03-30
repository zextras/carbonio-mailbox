package com.zextras.mailbox.quota;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;

public interface QuotaCheck {

    void onSendMessage(Account acct) throws ServiceException;

    void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException;

    void onDeleteMessage(Account acct, long size);
}
