package com.zimbra.cs.service.admin;

import static com.zextras.mailbox.util.MailboxTestUtil.SERVER_NAME;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.admin.message.RenameAccountRequest;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RenameAccountTest {
  private static final int PORT = 7070;
  private static Provisioning provisioning;
  private static AccountCreator.Factory accountCreatorFactory;
  private static SoapClient soapClient;
  private static Server server;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
    soapClient = new SoapClient();
    provisioning
        .getServerByName(SERVER_NAME)
        .modify(
            new HashMap<>(
                Map.of(
                    Provisioning.A_zimbraMailPort, String.valueOf(PORT),
                    ZAttrProvisioning.A_zimbraMailMode, "http",
                    ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
                    ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE")));
    final ServletHolder firstServlet = new ServletHolder(FirstServlet.class);
    firstServlet.setInitOrder(1);
    final ServletHolder soapServlet = new ServletHolder(SoapServlet.class);
    soapServlet.setInitParameter("engine.handler.0", "com.zimbra.cs.service.admin.AdminService");
    soapServlet.setInitOrder(2);
    server =
        new JettyServerFactory()
            .withPort(PORT)
            .addServlet("/firstServlet", firstServlet)
            .addServlet(AdminConstants.ADMIN_SERVICE_URI + "*", soapServlet)
            .create();
    server.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.stop();
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldRenameAccount() throws Exception {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    Account userAccount = accountCreatorFactory.get().create();

    RenameAccountRequest request =
        new RenameAccountRequest(userAccount.getId(), "newName@" + userAccount.getDomainName());

    final HttpResponse response =
        soapClient.newRequest().setCaller(adminAccount).setSoapBody(request).execute();
    System.out.println(new String(response.getEntity().getContent().readAllBytes()));
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }
}
