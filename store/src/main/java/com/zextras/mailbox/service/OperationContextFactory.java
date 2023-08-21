// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.OperationContext;

/** Factory class for {@link OperationContext}. */
public class OperationContextFactory {

  public OperationContext getOpContext(AuthToken authToken) throws ServiceException {
    return new OperationContext(authToken);
  }
}
