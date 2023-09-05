package com.zextras.mailbox.usecase.factory;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.OperationContext;

public interface OperationContextFactory {

  OperationContext create(Account account);
}
