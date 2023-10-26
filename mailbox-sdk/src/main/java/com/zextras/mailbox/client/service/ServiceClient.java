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
