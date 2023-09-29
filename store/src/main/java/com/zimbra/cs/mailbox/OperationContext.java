// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.mailbox.OpContext;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.redolog.op.RedoableOp;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.session.Session;
import java.util.HashMap;
import java.util.Map;

public class OperationContext implements OpContext {
  public static final boolean CHECK_CREATED = false, CHECK_MODIFIED = true;

  private Account authuser;
  private boolean isAdmin;
  private Session session;
  private RedoableOp player;
  private String requestIP;
  private String userAgent;
  private AuthToken authToken;
  private SoapProtocol mResponseProtocol;
  private String mRequestedAccountId;
  private String mAuthTokenAccountId;
  private Map<String, OperationContextData> contextData;

  boolean changetype = CHECK_CREATED;
  int change = -1;

  public OperationContext(RedoableOp redoPlayer) {
    player = redoPlayer;
  }

  public OperationContext(Account acct) {
    this(acct, false);
  }

  public OperationContext(Mailbox mbox) throws ServiceException {
    this(mbox.getAccount());
  }

  public OperationContext(Account acct, boolean admin) {
    authuser = acct;
    isAdmin = admin;
  }

  public OperationContext(String accountId) throws ServiceException {
    authuser = Provisioning.getInstance().get(AccountBy.id, accountId);
    if (authuser == null) throw AccountServiceException.NO_SUCH_ACCOUNT(accountId);
  }

  public OperationContext(AuthToken auth) throws ServiceException {
    authToken = auth;
    String accountId = auth.getAccountId();
    isAdmin = AuthToken.isAnyAdmin(auth);
    authuser = Provisioning.getInstance().get(AccountBy.id, accountId);
    if (authuser == null || !auth.isZimbraUser()) {
      if (auth.getDigest() != null || auth.getAccessKey() != null) {
        authuser = new GuestAccount(auth);
      } else {
        authuser = GuestAccount.ANONYMOUS_ACCT;
      }
    }
    if (authuser == null) {
      throw AccountServiceException.NO_SUCH_ACCOUNT(accountId);
    }
  }

  public OperationContext(OperationContext octxt) {
    player = octxt.player;
    session = octxt.session;
    authuser = octxt.authuser;
    isAdmin = octxt.isAdmin;
    changetype = octxt.changetype;
    change = octxt.change;
    authToken = octxt.authToken;
  }

  public OperationContext setChangeConstraint(boolean checkModified, int changeId) {
    changetype = checkModified;
    change = changeId;
    return this;
  }

  public OperationContext unsetChangeConstraint() {
    changetype = CHECK_CREATED;
    change = -1;
    return this;
  }

  public OperationContext setSession(Session s) {
    session = s;
    return this;
  }

  Session getSession() {
    return session;
  }

  public RedoableOp getPlayer() {
    return player;
  }

  public long getTimestamp() {
    return (player == null ? System.currentTimeMillis() : player.getTimestamp());
  }

  int getChangeId() {
    return (player == null ? -1 : player.getChangeId());
  }

  public boolean isRedo() {
    return player != null;
  }

  public boolean needRedo() {
    return player == null || !player.getUnloggedReplay();
  }

  public Account getAuthenticatedUser() {
    return authuser;
  }

  public AuthToken getAuthToken() throws ServiceException {
    return getAuthToken(true);
  }

  public AuthToken getAuthToken(boolean constructIfNotPresent) throws ServiceException {
    if (authToken != null) {
      return authToken;
    } else if (constructIfNotPresent && getAuthenticatedUser() != null) {
      return AuthProvider.getAuthToken(getAuthenticatedUser(), isUsingAdminPrivileges());
    }
    return null;
  }

  public boolean isUsingAdminPrivileges() {
    return isAdmin;
  }

  public boolean isDelegatedRequest(Mailbox mbox) {
    return authuser != null && !authuser.getId().equalsIgnoreCase(mbox.getAccountId());
  }

  /**
   * @see
   *     com.zimbra.cs.service.mail.CalendarRequest#isOnBehalfOfRequest(com.zimbra.soap.ZimbraSoapContext)
   */
  public boolean isOnBehalfOfRequest(Mailbox mbox) {
    if (!isDelegatedRequest(mbox)) {
      return false;
    }
    return authuser != null;
  }

  public OperationContext setRequestIP(String addr) {
    requestIP = addr;
    return this;
  }

  public String getRequestIP() {
    return requestIP;
  }

  public OperationContext setUserAgent(String ua) {
    userAgent = ua;
    return this;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setCtxtData(String key, OperationContextData data) {
    if (contextData == null) {
      contextData = new HashMap<String, OperationContextData>();
    }
    contextData.put(key, data);
  }

  public OperationContextData getCtxtData(String key) {
    if (contextData == null) {
      return null;
    } else {
      return contextData.get(key);
    }
  }

  /**
   * @param op
   * @return op if it is an OperationContext, otherwise null
   */
  public static OperationContext asOperationContext(OpContext op) {
    if (op instanceof OperationContext) {
      return (OperationContext) op;
    }
    return null;
  }

  public SoapProtocol getmResponseProtocol() {
    return mResponseProtocol;
  }

  public void setmResponseProtocol(SoapProtocol mResponseProtocol) {
    this.mResponseProtocol = mResponseProtocol;
  }

  public String getmRequestedAccountId() {
    return mRequestedAccountId;
  }

  public void setmRequestedAccountId(String mRequestedAccountId) {
    this.mRequestedAccountId = mRequestedAccountId;
  }

  public String getmAuthTokenAccountId() {
    return mAuthTokenAccountId;
  }

  public void setmAuthTokenAccountId(String mAuthTokenAccountId) {
    this.mAuthTokenAccountId = mAuthTokenAccountId;
  }
}
