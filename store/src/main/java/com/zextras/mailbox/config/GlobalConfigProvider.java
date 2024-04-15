// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.config;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;

public class GlobalConfigProvider {

  private final Provisioning provisioning;

  public GlobalConfigProvider(Provisioning provisioning) {
    this.provisioning = provisioning;
  }

  public Config get() throws ServiceException {
    return provisioning.getConfig();
  }
}
