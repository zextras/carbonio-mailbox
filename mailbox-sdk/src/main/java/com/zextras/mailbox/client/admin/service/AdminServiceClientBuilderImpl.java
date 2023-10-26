package com.zextras.mailbox.client.admin.service;

import com.sun.xml.ws.developer.WSBindingProvider;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminService;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdminServiceClientBuilderImpl implements AdminServiceClient.Builder {
  private final URL wsdl;
  private String server = "http://localhost:7071";
  private int size = 1;

  public AdminServiceClientBuilderImpl(URL wsdl, String server) {
    this.wsdl = wsdl;
    this.server = server;
  }

  @Override
  public AdminServiceClient.Builder withServer(String server) {
    this.server = server;
    return this;
  }

  @Override
  public AdminServiceClient.Builder withPool(int size) {
    this.size = size;
    return this;
  }

  @Override
  public AdminServiceClient build() {
    if (wsdl == null) {
      throw new IllegalArgumentException("WSDL parameter is required");
    }
    if (server == null || server.isEmpty()) {
      throw new IllegalArgumentException("Server parameter is required");
    }
    if (size < 1) {
      throw new IllegalArgumentException("Pool size must be greater than 0");
    }
    final var services = createServices();
    return new AdminServiceClientImpl(services);
  }

  private List<ZcsAdminPortType> createServices() {
    return IntStream.range(0, size)
        .mapToObj(i -> createService(wsdl, server))
        .collect(Collectors.toList());
  }

  static ZcsAdminPortType createService(URL wsdl, String server) {
    final var service = new ZcsAdminService(wsdl).getZcsAdminServicePort();
    ((WSBindingProvider) service).setAddress(server + "/service/admin/soap/");
    return service;
  }
}
