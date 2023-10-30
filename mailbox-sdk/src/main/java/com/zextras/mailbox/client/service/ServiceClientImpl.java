// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client.service;

import com.zextras.mailbox.client.PooledClient;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;
import java.util.List;

public class ServiceClientImpl extends PooledClient<ZcsPortType> implements ServiceClient {

  public ServiceClientImpl(List<ZcsPortType> services) {
    super(services);
  }
}
