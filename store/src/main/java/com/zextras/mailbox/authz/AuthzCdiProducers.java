/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz;

import com.zextras.mailbox.api.InternalApiInitializationException;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.authz.service.RightsService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@ApplicationScoped
public class AuthzCdiProducers {

  @Produces
  @Singleton
  public RightsService rightsService(AccountService accountService) {
    return new RightsService(
        Provisioning::getInstance,
        AccessManager::getInstance,
        () -> {
          try {
            return RightManager.getInstance();
          } catch (ServiceException e) {
            throw new InternalApiInitializationException(e);
          }
        },
        accountService);
  }
}
