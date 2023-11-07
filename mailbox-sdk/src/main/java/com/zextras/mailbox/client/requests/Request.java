// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client.requests;

import zimbra.HeaderContext;

public interface Request<Service, Res> {
  Res call(Service service, HeaderContext soapHeaderContext);
}
