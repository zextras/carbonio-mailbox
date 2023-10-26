package com.zextras.mailbox.client;

import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceClientBuilderImpl;
import com.zextras.mailbox.client.service.ServiceClient;
import com.zextras.mailbox.client.service.ServiceClientBuilderImpl;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class MailboxClient {
  private final URL wsdl;

  public MailboxClient(URL wsdl) {
    this.wsdl = wsdl;
  }

  public ServiceClient.Builder newServiceClientBuilder() {
    return new ServiceClientBuilderImpl(wsdl, serviceServer());
  }

  public ServiceClient newServiceClient() {
    return newServiceClientBuilder().build();
  }

  public AdminServiceClient.Builder newAdminServiceClientBuilder() {
    return new AdminServiceClientBuilderImpl(wsdl, adminServiceServer());
  }

  public AdminServiceClient newAdminServiceClient() {
    return newAdminServiceClientBuilder().build();
  }

  private String serviceServer() {
    return wsdl.getProtocol() + "://" + wsdl.getHost() + ":" + wsdl.getPort();
  }

  private String adminServiceServer() {
    return "https://" + wsdl.getHost() + ":7071";
  }

  public static class Builder {

    private boolean trustAllCertificates;
    private String server = "http://localhost:7070";

    public Builder trustAllCertificates() {
      this.trustAllCertificates = true;
      return this;
    }

    public Builder withServer(String server) {
      this.server = server;
      return this;
    }

    public MailboxClient build()
        throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException {
      if (server == null || server.isEmpty()) {
        throw new IllegalArgumentException("Server parameter is required");
      }

      if (trustAllCertificates) {
        disableSslCertificatesCheck();
      }

      return new MailboxClient(wsdlEndpoint());
    }

    private URL wsdlEndpoint() throws MalformedURLException {
      return new URL(server + "/service/wsdl/ZimbraService.wsdl");
    }

    private static void disableSslCertificatesCheck()
        throws NoSuchAlgorithmException, KeyManagementException {
      // ignore host name verification
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);

      // ignore certificate chain validation
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(
          null,
          new TrustManager[] {new TrustAllX509Certificates()},
          new java.security.SecureRandom());
      SSLContext.setDefault(sslContext);
    }
  }
}
