package com.zextras.mailbox.client.admin.service;

import com.sun.xml.ws.developer.WSBindingProvider;
import com.zextras.mailbox.client.PooledClient;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminService;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdminServiceClient extends PooledClient<ZcsAdminPortType> {
  public AdminServiceClient(List<ZcsAdminPortType> services) {
    super(services);
  }

  public static ZcsAdminPortType from(URL wsdl, String server) {
    final var service = new ZcsAdminService(wsdl).getZcsAdminServicePort();
    ((WSBindingProvider) service).setAddress(server + "/service/admin/soap/");
    return service;
  }

  public static class Builder {
    private final URL wsdl;
    private String server = "http://localhost:7071";
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

    public AdminServiceClient build() {
      final var services = createServices();
      return new AdminServiceClient(services);
    }

    private List<ZcsAdminPortType> createServices() {
      return IntStream.range(0, size)
          .mapToObj(i -> AdminServiceClient.from(wsdl, server))
          .collect(Collectors.toList());
    }
  }
}
