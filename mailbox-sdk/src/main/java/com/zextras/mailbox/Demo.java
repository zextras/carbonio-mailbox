package com.zextras.mailbox;

import com.zextras.mailbox.client.MailboxClient;
import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceRequests;
import com.zextras.mailbox.client.service.ServiceClient;
import com.zextras.mailbox.client.service.ServiceRequests;

public class Demo {
  public static void main(String[] args) throws Exception {
    String xml =
        "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<S:Envelope xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\">\n"
            + "    <S:Header>\n"
            + "        <ns5:context xmlns=\"urn:zimbraAdminExt\" xmlns:ns4=\"urn:zimbraAccount\" xmlns:ns5=\"urn:zimbra\">\n"
            + "            <ns5:authToken>dummy-token</ns5:authToken>\n"
            + "            <ns5:authTokenControl voidOnExpired=\"true\"/>\n"
            + "        </ns5:context>\n"
            + "        <context xmlns=\"urn:zimbra\">\n"
            + "            <authTokenControl voidOnExpired=\"true\"/>\n"
            + "        </context>\n"
            + "    </S:Header>\n"
            + "    <S:Body>\n"
            + "        <ns4:GetAccountInfoRequest xmlns=\"urn:zimbraAdminExt\" xmlns:ns4=\"urn:zimbraAccount\" xmlns:ns5=\"urn:zimbra\">\n"
            + "            <ns4:account by=\"name\">foo@test.domain.io</ns4:account>\n"
            + "        </ns4:GetAccountInfoRequest>\n"
            + "    </S:Body>\n"
            + "</S:Envelope>";

    // Rimuovi gli spazi e i caratteri di ritorno a capo tra le tag XML
    String flattenedXml = xml
        .replaceAll("\n( *)<", "<")
        ;

    System.out.print(flattenedXml);

//    final var token = "...";
//    final var tokenAdmin = "...";
//    final var id = "cc475da7-122f-4147-b447-98ba10bced23";
//    final var email = "foo@demo.zextras.io";
//    final var domain = "demo.zextras.io";
//
//    // ---
//    MailboxClient client =
//        new MailboxClient.Builder()
//            .withServer("http://localhost:8080")
//            .trustAllCertificates()
//            .build();
//
//    ServiceClient serviceClient = client.newServiceClientBuilder().withPool(5).build();
//
//    AdminServiceClient adminClient = client.newAdminServiceClient().withPool(5).build();
//
//    // ---
//    final var info = serviceClient.send(ServiceRequests.Info.allSections().withAuthToken(token));
//
//    final var info2 =
//        serviceClient.send(ServiceRequests.Info.sections("children", "attrs", "prefs").withAuthToken(token));
//
//    final var info3 = serviceClient.send(ServiceRequests.Info.sections("children").withAuthToken(token));
//
//    final var accountInfo = serviceClient.send(ServiceRequests.AccountInfo.byId(id).withAuthToken(token));
//
//    final var accountInfo2 =
//        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(token));
//
//    final var domainInfo =
//        adminClient.send(AdminServiceRequests.DomainInfo.byName(domain).withAuthToken(tokenAdmin));
//
//    final var domainInfo2 =
//        adminClient.send(AdminServiceRequests.Domain.byName(domain).withAuthToken(tokenAdmin));
//
//    final var domainInfo3 =
//        adminClient.send(AdminServiceRequests.AccountInfo.byEmail(email).withAuthToken(tokenAdmin));
//
//    // ---
//    System.out.println(info);
//    System.out.println(info2);
//    System.out.println(info3);
//    System.out.println(accountInfo);
//    System.out.println(accountInfo2);
//    System.out.println(domainInfo);
//    System.out.println(domainInfo2);
//    System.out.println(domainInfo3);
  }
}
