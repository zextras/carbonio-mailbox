package com.zextras.mailbox.usecase.factory;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.OperationContext;

/**
 * Factory class to create an {@link OperationContext}.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public interface OperationContextFactory {

  /**
   * Creates {@link OperationContext} by user account.
   *
   * @param account user account
   * @return {@link OperationContext}
   */
  OperationContext create(Account account);
}
