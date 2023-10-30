// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client.service;

import com.zextras.mailbox.client.Client;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;

public interface ServiceClient extends Client<ZcsPortType> {

  interface Builder {
    Builder withServer(String server);

    Builder withPool(int size);

    ServiceClient build();
  }
}
