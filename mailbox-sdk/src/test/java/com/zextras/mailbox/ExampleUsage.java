// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.client.MailboxClient;
import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceRequests;
import com.zextras.mailbox.client.service.InfoRequests;
import com.zextras.mailbox.client.service.ServiceClient;
import com.zextras.mailbox.client.service.ServiceRequests;

public class ExampleUsage {
  public static void main(String[] args) throws Exception {
    final var token = "...";
    final var tokenAdmin = "...";
    final var id = "cc475da7-122f-4147-b447-98ba10bced23";
    final var email = "foo@demo.zextras.io";
    final var domain = "demo.zextras.io";

    // create the main mailbox client
    MailboxClient client =
        new MailboxClient.Builder()
            .withServer("http://localhost:8080")
            .trustAllCertificates()
            .build();

    // create the service client from the main one (share the same WSDL)
    ServiceClient serviceClient = client.newServiceClientBuilder().withPool(5).build();

    // create the admin service client from the main one (share the same WSDL)
    AdminServiceClient adminClient = client.newAdminServiceClientBuilder().withPool(5).build();

    // send demo requests
    final var info = serviceClient.send(ServiceRequests.Info.allSections().withAuthToken(token));

    final var info2 =
        serviceClient.send(
            ServiceRequests.Info.sections(
                    InfoRequests.Sections.children,
                    InfoRequests.Sections.attrs,
                    InfoRequests.Sections.prefs)
                .withAuthToken(token));

    final var info3 =
        serviceClient.send(
            ServiceRequests.Info.sections(InfoRequests.Sections.children).withAuthToken(token));

    final var accountInfo =
        serviceClient.send(ServiceRequests.AccountInfo.byId(id).withAuthToken(token));

    final var accountInfo2 =
        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(token));

    final var domainInfo =
        adminClient.send(AdminServiceRequests.DomainInfo.byName(domain).withAuthToken(tokenAdmin));

    final var domainInfo2 =
        adminClient.send(AdminServiceRequests.Domain.byName(domain).withAuthToken(tokenAdmin));

    final var domainInfo3 =
        adminClient.send(AdminServiceRequests.AccountInfo.byEmail(email).withAuthToken(tokenAdmin));

    // prints all responses
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
