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
