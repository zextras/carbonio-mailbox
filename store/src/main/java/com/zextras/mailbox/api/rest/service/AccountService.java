package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;

import java.util.function.Supplier;

public class AccountService {

  private final Supplier<Provisioning> provisioningSupplier;

  public AccountService(Supplier<Provisioning> provisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
  }

  public Try<Account> getAccount(String accountId) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      Account accountById = provisioning.getAccountById(accountId);
      if (accountById == null) {
        throw ServiceException.NOT_FOUND("No such account with ID: " + accountId);
      }
      return accountById;
    });
  }
}
