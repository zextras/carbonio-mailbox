package com.zextras.mailbox;

import com.zextras.mailbox.client.MailboxClient;
import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceRequests;
import com.zextras.mailbox.client.service.ServiceClient;
import com.zextras.mailbox.client.service.ServiceRequests;

public class Demo {
  public static void main(String[] args) throws Exception {
    final var token = "...";
    final var tokenAdmin = "...";
    final var id = "cc475da7-122f-4147-b447-98ba10bced23";
    final var email = "foo@demo.zextras.io";
    final var domain = "demo.zextras.io";

    // ---
    MailboxClient client =
        new MailboxClient.Builder()
            .withServer("http://localhost:8080")
            .trustAllCertificates()
            .build();

    ServiceClient accountClient = client.newServiceClient().withPool(5).build();

    AdminServiceClient adminClient = client.newAdminServiceClient().withPool(5).build();

    // ---
    final var info = accountClient.send(ServiceRequests.Info.allSections().withAuthToken(token));

    final var info2 =
        accountClient.send(ServiceRequests.Info.sections("children", "attrs", "prefs").withAuthToken(token));

    final var info3 = accountClient.send(ServiceRequests.Info.sections("children").withAuthToken(token));

    final var accountInfo = accountClient.send(ServiceRequests.AccountInfo.byId(id).withAuthToken(token));

    final var accountInfo2 =
        accountClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(token));

    final var domainInfo =
        adminClient.send(AdminServiceRequests.DomainInfo.byName(domain).withAuthToken(tokenAdmin));

    final var domainInfo2 =
        adminClient.send(AdminServiceRequests.Domain.byName(domain).withAuthToken(tokenAdmin));

    final var domainInfo3 =
        adminClient.send(AdminServiceRequests.AccountInfo.byEmail(email).withAuthToken(tokenAdmin));

    // ---
    System.out.println(info);
    System.out.println(info2);
    System.out.println(info3);
    System.out.println(accountInfo);
    System.out.println(accountInfo2);
    System.out.println(domainInfo);
    System.out.println(domainInfo2);
    System.out.println(domainInfo3);
  }
}
