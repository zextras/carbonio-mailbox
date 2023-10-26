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
