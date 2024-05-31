package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.mail.message.GetMsgRequest;
import com.zimbra.soap.mail.message.GetMsgResponse;
import com.zimbra.soap.mail.message.SaveDraftRequest;
import com.zimbra.soap.mail.message.SaveDraftResponse;
import com.zimbra.soap.mail.type.MessageInfo;
import com.zimbra.soap.mail.type.MsgSpec;
import com.zimbra.soap.mail.type.MsgWithGroupInfo;
import com.zimbra.soap.mail.type.SaveDraftMsg;
import com.zimbra.soap.type.AccountSelector;
import com.zimbra.soap.type.UrlAndValue;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class SaveDraftApiTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  @BeforeAll
  public static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
  }


  @Test
  public void shouldSaveDraft() throws Exception {
    final String password = "password";
    Account testAccount = accountCreatorFactory.get().withPassword(password).create();

    final SaveDraftRequest saveDraftRequest = new SaveDraftRequest();
    final SaveDraftMsg msg = new SaveDraftMsg();
    msg.setSubject("Test SMIME email");
    msg.setFragment("Hello there");
    saveDraftRequest.setMsg(msg);

    final MessageInfo draftMsg = getSoapClient().execute(testAccount, saveDraftRequest, SaveDraftResponse.class)
        .getMessage();
    final String draftId = draftMsg.getId();


    final MsgSpec msgSpec = new MsgSpec(draftId);
    msgSpec.setRaw(true);
    final MsgWithGroupInfo gotMsg = getSoapClient().execute(testAccount,
            new GetMsgRequest(msgSpec), GetMsgResponse.class)
        .getMsg();
    final UrlAndValue content = gotMsg.getContent();
    final String emlMessage = content.getValue();
  }
}
