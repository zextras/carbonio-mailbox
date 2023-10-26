package com.zextras.mailbox.client;

import com.sun.xml.ws.developer.WSBindingProvider;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;
import https.www_zextras_com.wsdl.zimbraservice.ZcsService;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServiceClient extends PooledClient<ZcsPortType> {

  public ServiceClient(List<ZcsPortType> services) {
    super(services);
  }

  public static ZcsPortType from(URL wsdl, String server) {
    final var service = new ZcsService(wsdl).getZcsServicePort();
    ((WSBindingProvider) service).setAddress(server + "/service/soap/");
    return service;
  }

  public static class Builder {
    private final URL wsdl;
    private String server = "http://localhost:7070";
    private int size = 1;

    public Builder(URL wsdl, String server) {
      this.wsdl = wsdl;
      this.server = server;
    }

    public Builder withServer(String server) {
      this.server = server;
      return this;
    }

    public Builder withPool(int size) {
      this.size = size;
      return this;
    }

    public ServiceClient build() {
      final var services = createServices();
      return new ServiceClient(services);
    }

    private List<ZcsPortType> createServices() {
      return IntStream.range(0, size)
          .mapToObj(i -> ServiceClient.from(wsdl, server))
          .collect(Collectors.toList());
    }
  }
}
