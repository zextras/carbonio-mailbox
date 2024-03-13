package com.zimbra.cs.service.mail;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.entities.NodeId;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;
import java.io.IOException;
import javax.mail.internet.MimeMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

class CreateSmartLinksTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;

  private ClientAndServer filesServer;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }


  @BeforeEach
  void setUp() throws Exception {
    filesServer = startClientAndServer(20002);
  }

  @AfterEach
  public void tearDown() throws IOException {
    filesServer.stop();
  }


  static Message createDraft(Account sender) throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(sender);
    MimeMessage mm = new MimeMessage(JMSession.getSmtpSession(sender));
    mm.setText("foo");
    mm.setFileName("myfile");
    ParsedMessage pm = new ParsedMessage(mm, false);
    return mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT);
  }

  @Test
  void shouldNotFail() throws Exception {
    Account account = accountCreatorFactory.get().create();
    var draft = createDraft(account);

    String partName = draft.getParsedMessage().getMessageParts().get(0).getPartName();
    String xml = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", draft.getId(), partName);

    mockUploadFile("node1");
    final String publicUrl = "http://myServer?file=node1";
    mockCreateLinkFilesResponse(publicUrl);

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xml));

    final String xmlResponse = getResponse(resp);
    System.out.println(xmlResponse);
    assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
    assertFalse(xmlResponse.contains("Fault"));
    String expected = "<CreateSmartLinksResponse xmlns=\"urn:zimbraMail\">" +
        "<smartLinks publicUrl=\"" + publicUrl + "\"/>" +
        "</CreateSmartLinksResponse>";
    assertTrue(xmlResponse.contains(expected));
  }

  private void mockCreateLinkFilesResponse(String publicUrl) {
    filesServer
        .when(request().withPath("/graphql/"))
        .respond(
            org.mockserver.model.HttpResponse.response("{"
                    + "    \"data\": {"
                    + "        \"createLink\": {"
                    + "            \"id\": \"a4bbe479-1f42-49e4-8619-b6a068015dd5\","
                    + "            \"url\": \"" + publicUrl + "\","
                    + "            \"description\": \"ef196081-0330-43e3-93a3-e8241e3c7fb2\","
                    + "            \"expires_at\": null,"
                    + "            \"created_at\": 1709906611074,"
                    + "            \"node\": {"
                    + "                \"id\": \"ef196081-0330-43e3-93a3-e8241e3c7fb2\","
                    + "                \"__typename\": \"File\""
                    + "            },\n"
                    + "            \"__typename\": \"Link\""
                    + "        }"
                    + "    }"
                    + "}")
                .withStatusCode(200));
  }

  private void mockUploadFile(String nodeId) throws Exception {
    final NodeId nodeIdObject = new NodeId();
    nodeIdObject.setNodeId(nodeId);
    filesServer
        .when(request().withMethod("POST").withPath("/upload/"))
        .respond(
            org.mockserver.model.HttpResponse.response(new ObjectMapper().writeValueAsString(nodeIdObject))
                .withStatusCode(200));
  }

}