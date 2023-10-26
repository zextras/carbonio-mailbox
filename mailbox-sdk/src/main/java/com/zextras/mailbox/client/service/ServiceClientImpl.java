package com.zextras.mailbox.client.service;

import com.zextras.mailbox.client.PooledClient;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;
import java.util.List;

public class ServiceClientImpl extends PooledClient<ZcsPortType> implements ServiceClient {

  public ServiceClientImpl(List<ZcsPortType> services) {
    super(services);
  }
}
