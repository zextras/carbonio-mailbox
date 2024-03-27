package com.zimbra.cs.service.mail;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.files.entities.NodeId;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailMessageBuilder;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

class CreateSmartLinksTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;
  private final int attachmentPartIndex = 2;

  private ClientAndServer filesServer;
  private Message draftWithAttachment;
  private Account account;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }


  @BeforeEach
  void setUp() throws Exception {
    filesServer = startClientAndServer(20002);
    account = accountCreatorFactory.get().create();
    draftWithAttachment = createDraftWithAttachment();
  }

  @AfterEach
  public void tearDown() throws IOException {
    filesServer.stop();
  }

  @Test
  void shouldGenerateSmartLinkFromAttachment() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String attachmentPartName = draftWithAttachment.getParsedMessage().getMessageParts().get(attachmentPartIndex).getPartName();
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", draftWithAttachment.getId(), attachmentPartName);

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
    assertFalse(xmlResponse.contains("Fault"));
    String expected = "<CreateSmartLinksResponse xmlns=\"urn:zimbraMail\">" +
        "<smartLinks publicUrl=\"" + publicUrl + "\"/>" +
        "</CreateSmartLinksResponse>";
    assertTrue(xmlResponse.contains(expected));
  }

  @Test
  void shouldReturnServiceNotFoundIfPartNameIsNotValid() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", draftWithAttachment.getId(), "invalid-part-name");

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
    assertTrue(xmlResponse.contains("Fault"));
    System.out.println(xmlResponse);
    assertTrue(xmlResponse.contains("<Code>service.NOT_FOUND</Code>"));
  }

  @Test
  void shouldReturnBotFoundIfPartNameIsMissing() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", draftWithAttachment.getId(), "42");

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
    assertTrue(xmlResponse.contains("Fault"));
    assertTrue(xmlResponse.contains("<Code>service.NOT_FOUND</Code>"));
  }

  @Test
  void shouldReturnInvalidRequestIfAttachmentsAreMissing() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String xmlRequest = "<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\"></CreateSmartLinksRequest>";

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
    assertTrue(xmlResponse.contains("Fault"));
    
    assertTrue(xmlResponse.contains("<Code>service.INVALID_REQUEST</Code>"));
  }

  @Test
  void shouldReturnInvalidRequestIfDraftIdIsNotValid() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String attachmentPartName = draftWithAttachment.getParsedMessage().getMessageParts().get(attachmentPartIndex).getPartName();
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", "invalid-draft-id", attachmentPartName);

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
    assertTrue(xmlResponse.contains("Fault"));
    assertTrue(xmlResponse.contains("<Code>service.PARSE_ERROR</Code>"));
  }

  @Test
  void shouldReturnNotFoundIfDraftIdIsMissing() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String attachmentPartName = draftWithAttachment.getParsedMessage().getMessageParts().get(attachmentPartIndex).getPartName();
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", "42", attachmentPartName);

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
    assertTrue(xmlResponse.contains("Fault"));
    assertTrue(xmlResponse.contains("<Code>service.NOT_FOUND</Code>"));
  }

  @Test
  void shouldFailWhenFilesUploadFails() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockFailingAttachmentUploadOnFilesResponse("node1");
    mockCreateLinkOnFilesResponse(publicUrl);
    String attachmentPartName = draftWithAttachment.getParsedMessage().getMessageParts().get(attachmentPartIndex).getPartName();
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", draftWithAttachment.getId(), attachmentPartName);

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertTrue(xmlResponse.contains("Fault"));
    assertTrue(xmlResponse.contains("<Code>service.FAILURE</Code>"));
    assertTrue(xmlResponse.contains("Files upload failed"));
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
  }

  @Test
  void shouldFailWhenFilesCreateLinkFails() throws Exception {
    final String publicUrl = "http://myServer?file=node1";
    mockAttachmentUploadOnFilesResponse("node1");
    mockFailingCreateLinkOnFilesResponse(publicUrl);
    String attachmentPartName = draftWithAttachment.getParsedMessage().getMessageParts().get(attachmentPartIndex).getPartName();
    String xmlRequest = String.format("<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\">"
        + "<attachments draftId=\"%s\" partName=\"%s\"/>"
        + "</CreateSmartLinksRequest>", draftWithAttachment.getId(), attachmentPartName);

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xmlRequest));

    final String xmlResponse = getResponse(resp);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.getStatusLine().getStatusCode());
    assertTrue(xmlResponse.contains("Fault"));
    assertTrue(xmlResponse.contains("<Code>service.FAILURE</Code>"));
    assertTrue(xmlResponse.contains("Files CreateLink failed"));
  }

  private void mockCreateLinkOnFilesResponse(String publicUrl) {
    filesServer
        .when(request().withPath("/graphql/"))
        .respond(
            response("{"
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

  private void mockAttachmentUploadOnFilesResponse(String nodeId) throws Exception {
    final NodeId nodeIdObject = new NodeId();
    nodeIdObject.setNodeId(nodeId);
    filesServer
        .when(request().withMethod("POST").withPath("/upload/"))
        .respond(
            response(new ObjectMapper().writeValueAsString(nodeIdObject))
                .withStatusCode(200));
  }

  private void mockFailingCreateLinkOnFilesResponse(String publicUrl) {
    filesServer
        .when(request().withPath("/graphql/"))
        .respond(response().withStatusCode(500));
  }

  private void mockFailingAttachmentUploadOnFilesResponse(String nodeId) throws Exception {
    final NodeId nodeIdObject = new NodeId();
    nodeIdObject.setNodeId(nodeId);
    filesServer
        .when(request().withMethod("POST").withPath("/upload/"))
        .respond(response().withStatusCode(500));
  }

  private Message createDraftWithAttachment() throws Exception {

    final ParsedMessage message = new MailMessageBuilder()
        .from(account.getName())
        .body("Hello!")
        .addAttachmentFromResources("/test-save-to-files.txt")
        .build();
    return AccountAction.Factory.getDefault().forAccount(account).saveDraft(message);
  }

}