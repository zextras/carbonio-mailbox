package com.zextras.mailbox.quota;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.util.AccountUtil;

/**
 * Enforces the quota configured in LDAP (zimbraMailQuota, capped by zimbraMailDomainQuota) inside
 * the mailbox. Used instead of the externally registered {@link QuotaCheck} when the {@code
 * bypass_carbonio_storages} local config key is true.
 */
public final class ZimbraMailQuotaCheck implements QuotaCheck {

  static final ZimbraMailQuotaCheck INSTANCE = new ZimbraMailQuotaCheck();

  private ZimbraMailQuotaCheck() {}

  @Override
  public void onSendMessage(Account acct) throws ServiceException {
    if (!acct.isMailAllowReceiveButNotSendWhenOverQuota()) {
      return;
    }
    final long limit = AccountUtil.getMailQuotaLimit(acct);
    if (limit != 0 && MailboxManager.getInstance().getMailboxByAccount(acct).getSize() > limit) {
      throw MailServiceException.QUOTA_EXCEEDED(limit);
    }
  }

  @Override
  public void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException {
    final long limit = AccountUtil.getMailQuotaLimit(acct);
    if (limit != 0 && newTotalMailboxUsage > limit) {
      throw MailServiceException.QUOTA_EXCEEDED(limit);
    }
  }

  @Override
  public void onDeleteMessage(Account acct, long size) {
    // nothing to enforce when freeing space
  }
}
