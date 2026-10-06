/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi;

import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.authz.service.RightsService;
import com.zextras.mailbox.publicapi.service.PublicAccountService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@ApplicationScoped
public class PublicApiCdiProducers {

  @Produces
  @Singleton
  public PublicAccountService publicAccountService(
      AccountService accountService, RightsService rightsService) {
    return new PublicAccountService(accountService, rightsService);
  }
}
