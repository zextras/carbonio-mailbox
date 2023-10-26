package com.zextras.mailbox.client.service;

import com.sun.xml.ws.developer.WSBindingProvider;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;
import https.www_zextras_com.wsdl.zimbraservice.ZcsService;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServiceClientBuilderImpl implements ServiceClient.Builder {
  private final URL wsdl;
  private String server = "http://localhost:7070";
  private int size = 1;

  public ServiceClientBuilderImpl(URL wsdl, String server) {
    this.wsdl = wsdl;
    this.server = server;
  }

  @Override
  public ServiceClient.Builder withServer(String server) {
    this.server = server;
    return this;
  }

  @Override
  public ServiceClient.Builder withPool(int size) {
    this.size = size;
    return this;
  }

  @Override
  public ServiceClient build() {
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
    return new ServiceClientImpl(services);
  }

  private List<ZcsPortType> createServices() {
    return IntStream.range(0, size)
        .mapToObj(i -> createService(wsdl, server))
        .collect(Collectors.toList());
  }

  static ZcsPortType createService(URL wsdl, String server) {
    final var service = new ZcsService(wsdl).getZcsServicePort();
    ((WSBindingProvider) service).setAddress(server + "/service/soap/");
    return service;
  }
}
