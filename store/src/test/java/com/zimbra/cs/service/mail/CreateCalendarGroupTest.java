package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateCalendarGroupResponse;
import com.zimbra.soap.mail.message.CreateFolderRequest;
import com.zimbra.soap.mail.message.CreateFolderResponse;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.NewFolderSpec;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("api")
class CreateCalendarGroupTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  private Account account;

  @BeforeAll
  static void init() {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
  }

  @BeforeEach
  void setUp() throws Exception {
    account = accountCreatorFactory.get().create();
  }

  @Test
  void createGroup() throws Exception {
    var firstId = createCalendar(account, "Test Calendar 1").getId();
    var secondId = createCalendar(account, "Test Calendar 2").getId();
    var thirdId = createCalendar(account, "Test Calendar 3").getId();

    final var request = new CreateCalendarGroupRequest();
    request.setName("Test Group");
    request.setCalendarIds(List.of(firstId, secondId, thirdId));

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    var group = response.getGroup();
    assertFalse(StringUtil.isNullOrEmpty(group.getId()));
    assertEquals("Test Group", group.getName());
    assertEquals(List.of(firstId, secondId, thirdId), group.getCalendarIds());
  }


  @Test
  void noDuplicateCalendarInGroup() throws Exception {
    var firstId = createCalendar(account, "Test Calendar 1").getId();

    final var request = new CreateCalendarGroupRequest();
    request.setName("Test Group");
    request.setCalendarIds(List.of(firstId, firstId));

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    var group = response.getGroup();
    assertFalse(StringUtil.isNullOrEmpty(group.getId()));
    assertEquals("Test Group", group.getName());
    assertEquals(List.of(firstId), group.getCalendarIds());
  }


  @Test
  void idDoesNotExists() throws Exception {
    createCalendar(account, "Test Calendar 1").getId();
    var lastCreated = createCalendar(account, "Test Calendar 2").getId();
    var notExistingId = lastCreated + 1;

    final var request = new CreateCalendarGroupRequest();
    request.setName("Test Group");
    request.setCalendarIds(List.of(notExistingId));

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, soapResponse.getStatusLine().getStatusCode());
  }

  @Test
  void shouldNotCreateGroupWhenGroupNameAlreadyExists() throws Exception {
    var sameGroupName = "Test Group";
    createGroupFor(account, sameGroupName, List.of("10", "420", "421"));

    final var request = new CreateCalendarGroupRequest();
    request.setName(sameGroupName);
    request.setCalendarIds(List.of("10", "420", "421"));

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, soapResponse.getStatusLine().getStatusCode());
  }

  private void createGroupFor(Account acc, String groupName, List<String> calendarIds) throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName(groupName);
    request.setCalendarIds(calendarIds);

    getSoapClient().executeSoap(acc, request);
  }

  private Folder createCalendar(Account account, String name) throws Exception {
    final var folder = new NewFolderSpec(name);
    folder.setParentFolderId("1");
    folder.setDefaultView("appointment");
    final var createFolderRequest = new CreateFolderRequest(folder);
    final var createFolderResponse = getSoapClient().executeSoap(account, createFolderRequest);
    assertEquals(HttpStatus.SC_OK, createFolderResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(createFolderResponse, CreateFolderResponse.class).getFolder();
  }

  private static <T> T parseSoapResponse(HttpResponse httpResponse, Class<T> clazz)
          throws IOException, ServiceException {
    final var responseBody = EntityUtils.toString(httpResponse.getEntity());
    final var rootElement =
            parseXML(responseBody)
                    .getElement("Body")
                    .getElement(clazz.getSimpleName());
    return JaxbUtil.elementToJaxb(rootElement, clazz);
  }

  private static CreateCalendarGroupResponse parseSoapResponse(HttpResponse httpResponse)
      throws IOException, ServiceException {
    final var responseBody = EntityUtils.toString(httpResponse.getEntity());
    final var rootElement =
        parseXML(responseBody)
            .getElement("Body")
            .getElement(CreateCalendarGroupResponse.class.getSimpleName());
    return JaxbUtil.elementToJaxb(rootElement, CreateCalendarGroupResponse.class);
  }
}
