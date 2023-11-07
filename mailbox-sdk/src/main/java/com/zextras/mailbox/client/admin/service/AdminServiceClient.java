// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client.admin.service;

import com.zextras.mailbox.client.Client;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;

public interface AdminServiceClient extends Client<ZcsAdminPortType> {

  interface Builder {
    AdminServiceClient.Builder withServer(String server);

    AdminServiceClient.Builder withPool(int size);

    AdminServiceClient build();
  }
}
