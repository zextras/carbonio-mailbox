package com.zimbra.cs.service;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.shim.JavaMailMimeMessage;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.zmime.ZSharedFileInputStream;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.mail.message.GetMsgRequest;
import com.zimbra.soap.mail.message.GetMsgResponse;
import com.zimbra.soap.mail.type.MsgSpec;
import com.zimbra.soap.mail.type.PartInfo;
import javax.mail.internet.MimeMessage;
import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class GetMsgApiTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static MailboxTestUtil.AccountAction.Factory accountActionFactory;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    final MailboxManager mailboxManager = MailboxManager.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
    accountActionFactory = new MailboxTestUtil.AccountAction.Factory(mailboxManager,
        RightManager.getInstance());
    LC.zimbra_use_owasp_html_sanitizer.setDefault(true);
  }

  @Test
  @Description("Test for CO-1804")
  void should_not_remove_wrapped_inner_content_when_html_has_more_than_256_nested_tags() throws Exception {
    var account = accountCreatorFactory.get().create();

    MimeMessage message =
        new JavaMailMimeMessage(
            JMSession.getSession(),
            new ZSharedFileInputStream("data/TestMailRaw/CO-1804"));

    final Message message1 = accountActionFactory.forAccount(account).saveMsgInInbox(message);

    var msgSpec = new MsgSpec(String.valueOf(message1.getId()));
    msgSpec.setWantHtml(true);
    msgSpec.setNeedCanExpand(true);
    msgSpec.setMaxInlinedLength(250000);
    var getMsgRequest = new GetMsgRequest(msgSpec);
    final SoapResponse getMsgResponse = this.getSoapClient().newRequest()
        .setCaller(account)
        .setSoapBody(getMsgRequest)
        .call();

    final String body = getMsgResponse.body();

    Assertions.assertTrue(body.contains("Hello World"));
  }

  @Test
  @Description("Test for CO-1804")
  void should_remove_wrapped_inner_content_when_there_are_512_nested_tags() throws Exception {
    var account = accountCreatorFactory.get().create();

    MimeMessage message =
        new JavaMailMimeMessage(
            JMSession.getSession(),
            new ZSharedFileInputStream("data/TestMailRaw/CO-1804-512"));

    final Message message1 = accountActionFactory.forAccount(account).saveMsgInInbox(message);

    var msgSpec = new MsgSpec(String.valueOf(message1.getId()));
    msgSpec.setWantHtml(true);
    msgSpec.setNeedCanExpand(true);
    msgSpec.setMaxInlinedLength(250000);
    var getMsgRequest = new GetMsgRequest(msgSpec);
    final SoapResponse getMsgResponse = this.getSoapClient().newRequest()
        .setCaller(account)
        .setSoapBody(getMsgRequest)
        .call();

    final String body = getMsgResponse.body();

    Assertions.assertFalse(body.contains("Hello World"));
  }

  @Test
  @Description("Test for CO-1804")
  void should_do_what() throws Exception {
    var account = accountCreatorFactory.get().create();

    MimeMessage message =
        new JavaMailMimeMessage(
            JMSession.getSession(),
            new ZSharedFileInputStream("data/TestMailRaw/CO-1804-table-styling"));

    final Message message1 = accountActionFactory.forAccount(account).saveMsgInInbox(message);

    var msgSpec = new MsgSpec(String.valueOf(message1.getId()));
    msgSpec.setWantHtml(true);
    msgSpec.setNeedCanExpand(true);
    msgSpec.setMaxInlinedLength(250000);
    var getMsgRequest = new GetMsgRequest(msgSpec);
    final SoapResponse getMsgResponse = this.getSoapClient().newRequest()
        .setCaller(account)
        .setSoapBody(getMsgRequest)
        .call();

    final String body = getMsgResponse.body();

    final GetMsgResponse soapResponse = SoapUtils.getSoapResponse(body,
        MailConstants.E_GET_MSG_RESPONSE,
        GetMsgResponse.class);
    final Object element = soapResponse.getMsg().getContentElems().get(0);
    final PartInfo textHtmlPart = ((PartInfo) element).getMimeParts().get(1);
    System.out.println("Content:");
    System.out.println(textHtmlPart.getContent());
  }

}
