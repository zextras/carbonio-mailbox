package com.zextras.mailbox;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;

public class AuthenticationInfo {
  private final Account authenticatedAccount;
  private final Account requestedAccount;
  private final AuthToken authToken;

  public AuthenticationInfo(Account authenticatedAccount, Account requestedAccount, AuthToken authToken) {
    this.authenticatedAccount = authenticatedAccount;
    this.requestedAccount = requestedAccount;
    this.authToken = authToken;
  }

  public Account getAuthenticatedAccount() {
    return authenticatedAccount;
  }

  public Account getRequestedAccount() {
    return requestedAccount;
  }

  public AuthToken getAuthToken() {
    return authToken;
  }
}
