package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction.Factory;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.SoapParseException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.CreateSmartLinksRequest;
import com.zimbra.soap.mail.type.AttachmentToConvert;
import com.zimbra.soap.mail.type.ContactSpec;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateSmartLinksTest extends SoapTestSuite {

  private static Factory accountActionFactory;
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountActionFactory = new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }


  //<CreateSmartLinksRequest messageId="3453453-54353" xmlns="urn:zimbraMail"><attachmentToConvert><attachmentToConvert part="part1"/><attachmentToConvert part="part2"/></attachmentToConvert></CreateSmartLinksRequest>
  @Test
  void testEcho() throws Exception {
    Account account = accountCreatorFactory.get().create();
    String xml = "<CreateSmartLinksRequest messageId=\"3453453-54353\" xmlns=\"urn:zimbraMail\"><attachmentToConvert part=\"part1\"/><attachmentToConvert part=\"part2\"/></CreateSmartLinksRequest>";
    Element el = Element.parseXML(xml);
//    CreateSmartLinksRequest req1 =  new CreateSmartLinksRequest("3453453-54353", List.of(
//        new AttachmentToConvert("part1"),
//        new AttachmentToConvert("part2")
//    ));
    HttpResponse resp = getSoapClient().executeSoap(account, el);
    printResponse(resp);
  }



}