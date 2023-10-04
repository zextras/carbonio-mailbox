// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.account.usecase;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.zextras.mailbox.acl.AclService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import javax.inject.Named;

public class UseCaseDI extends AbstractModule {

  @Provides
  @Named("defaultProvisioning")
  Provisioning getProvisioning() {
    return Provisioning.getInstance();
  }

  @Provides
  @Named("defaultMailboxManager")
  MailboxManager getMailboxManager() throws ServiceException {
    return MailboxManager.getInstance();
  }

  @Provides
  @Named("zimbraLogSecurity")
  Log getZimbraLogSecurity() {
    return ZimbraLog.security;
  }

  @Provides
  AclService getGrantsService(MailboxManager mailboxManager, Provisioning provisioning) {
    return new AclService(mailboxManager, provisioning);
  }
}
