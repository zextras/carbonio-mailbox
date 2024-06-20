package com.zimbra.cs.service.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.*;
import com.zimbra.soap.mail.type.*;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.*;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

@Tag("api")
public class SaveDraftApiTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static GreenMail greenMailAgent;

  @BeforeAll
  public static void setUp() throws Exception {
    java.security.Security.addProvider(new BouncyCastleProvider());
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);

    greenMailAgent =
        new GreenMail(
            new ServerSetup[]{
                new ServerSetup(
                    SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP),
                new ServerSetup(9000, "127.0.0.1", ServerSetup.PROTOCOL_IMAP)
            });
    greenMailAgent.start();
  }

  @AfterAll
  public static void tearDown() {
    greenMailAgent.stop();
  }


  @Test
  public void sendSignedMessage() throws Exception {
    final String password = "password";
    provisioning.createDomain("demo.zextras.io", new HashMap<>());
    Account sender = accountCreatorFactory.get()
        .withUsername("test.smime")
        .withDomain("demo.zextras.io")
        .withPassword(password).create();

    final MsgWithGroupInfo draftMessage = createDraftMessage(sender);
    final HttpResponse sendMsgResponse = sendMessage(sender, draftMessage.getId());

    final SendMsgResponse responseBody = JaxbUtil.elementToJaxb(
        Element.parseXML(new String(sendMsgResponse.getEntity().getContent().readAllBytes()))
            .getElement("Body").listElements().get(0)
    );

    Assertions.assertEquals(200, sendMsgResponse.getStatusLine().getStatusCode());

    final MimeMessage[] receivedMessages = greenMailAgent.getReceivedMessages();
    Assertions.assertEquals(1, receivedMessages.length);
    final MimeMessage receivedMessage = receivedMessages[0];
    Assertions.assertTrue(receivedMessage.getContentType().contains("multipart/signed"));
    MsgWithGroupInfo rawMessage = getRawMessage(sender, responseBody.getMsg().getId());
    System.out.println(rawMessage.getContent().getValue());
  }

  private MsgWithGroupInfo createDraftMessage(Account testAccount) throws Exception {
    final SaveDraftRequest saveDraftRequest = new SaveDraftRequest();
    final SaveDraftMsg msg = new SaveDraftMsg();
    msg.setSubject("Test SMIME email");
    MimePartInfo partInfo = MimePartInfo.createForContentTypeAndContent(
        "text/plain; charset=UTF-8; format=flowed", "hello there");

    msg.setMimePart(partInfo);
    final EmailAddrInfo rcptAddress = new EmailAddrInfo("external@test123.com");
    rcptAddress.setAddressType("t");
    final EmailAddrInfo sender = new EmailAddrInfo(testAccount.getName());
    sender.setAddressType("f");
    msg.setEmailAddresses(List.of(sender, rcptAddress));
    saveDraftRequest.setMsg(msg);

    final MessageInfo draftMsg = getSoapClient().execute(testAccount, saveDraftRequest,
            SaveDraftResponse.class)
        .getMessage();
    final String draftId = draftMsg.getId();

    return getRawMessage(testAccount, draftId);
  }

  private MsgWithGroupInfo getRawMessage(Account testAccount, String draftId) throws Exception {
    final MsgSpec msgSpec = new MsgSpec(draftId);
    msgSpec.setRaw(true);
    return getSoapClient().execute(testAccount,
            new GetMsgRequest(msgSpec), GetMsgResponse.class)
        .getMsg();
  }

  private MsgWithGroupInfo getMessage(Account testAccount, String draftId) throws Exception {
    final MsgSpec msgSpec = new MsgSpec(draftId);
    msgSpec.setRaw(false);
    return getSoapClient().execute(testAccount,
            new GetMsgRequest(msgSpec), GetMsgResponse.class)
        .getMsg();
  }

  private HttpResponse sendMessage(Account testAccount, String draftId) throws Exception {
    final MsgToSend msgToSend = new MsgToSend();

    msgToSend.setDraftId(draftId);
    msgToSend.setSendFromDraft(true);
    final SendMsgRequest soapBodyPOJO = new SendMsgRequest();
    soapBodyPOJO.setMsg(msgToSend);
    return getSoapClient().executeSoap(testAccount,
        soapBodyPOJO);
  }

}