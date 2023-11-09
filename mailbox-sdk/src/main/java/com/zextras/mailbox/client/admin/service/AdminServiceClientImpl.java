// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client.admin.service;

import com.zextras.mailbox.client.PooledClient;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;
import java.util.List;

public class AdminServiceClientImpl extends PooledClient<ZcsAdminPortType>
    implements AdminServiceClient {
  public AdminServiceClientImpl(List<ZcsAdminPortType> services) {
    super(services);
  }
}
