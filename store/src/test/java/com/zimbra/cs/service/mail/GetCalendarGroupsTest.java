package com.zimbra.cs.service.mail;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CalendarGroupInfo;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateFolderRequest;
import com.zimbra.soap.mail.message.CreateFolderResponse;
import com.zimbra.soap.mail.message.GetCalendarGroupsRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsResponse;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.NewFolderSpec;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class GetCalendarGroupsTest extends SoapTestSuite {
  private static final String ALL_CALENDARS_GROUP_NAME = "All calendars";

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
  void justAllCalendarsDefaultGroup() throws Exception {
    final var request = new GetCalendarGroupsRequest();

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    assertEquals(1, response.getGroups().size());
    assertEquals(ALL_CALENDARS_GROUP_NAME, response.getGroups().get(0).getName());
    assertEquals(1, response.getGroups().get(0).getCalendarIds().size());
  }

  @Test
  void allCalendarsGroupWithMoreCalendars() throws Exception {
    addCalendarTo(account, "test-calendar-1");
    addCalendarTo(account, "test-calendar-2");

    final var request = new GetCalendarGroupsRequest();

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    assertEquals(1, response.getGroups().size());
    assertEquals(ALL_CALENDARS_GROUP_NAME, response.getGroups().get(0).getName());
    assertEquals(3, response.getGroups().get(0).getCalendarIds().size());
  }

  @Test
  void allGroupsForAccount() throws Exception {
    var firstCalendar = createCalendar(account, "test-calendar-1");
    var secondCalendar = createCalendar(account, "test-calendar-2");
    var thirdCalendar = createCalendar(account, "test-calendar-3");

    String groupName1 = "Test Group 1";
    createGroupFor(account, groupName1, List.of(firstCalendar.getId(), secondCalendar.getId()));
    String groupName2 = "Test Group 2";
    createGroupFor(account, groupName2, List.of(thirdCalendar.getId()));

    final var request = new GetCalendarGroupsRequest();

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    assertEquals(3, response.getGroups().size());
    assertEquals(ALL_CALENDARS_GROUP_NAME, response.getGroups().get(0).getName());
    List<String> groupNames = response.getGroups().stream().map(CalendarGroupInfo::getName).toList();
    assertTrue(groupNames.containsAll(List.of(groupName1, groupName2)));
  }

  private void addCalendarTo(Account account, String name) throws Exception {
    final var folder = new NewFolderSpec(name);
    folder.setParentFolderId("1");
    folder.setDefaultView("appointment");
    final var createFolderRequest = new CreateFolderRequest(folder);
    final var createFolderResponse = getSoapClient().executeSoap(account, createFolderRequest);
    assertEquals(HttpStatus.SC_OK, createFolderResponse.getStatusLine().getStatusCode());
  }

  private void createGroupFor(Account acc, String groupName, List<String> calendarIds) throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName(groupName);
    request.setCalendarIds(calendarIds);

    var response = getSoapClient().executeSoap(acc, request);
    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
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

  private static GetCalendarGroupsResponse parseSoapResponse(HttpResponse httpResponse)
      throws IOException, ServiceException {
    final var responseBody = EntityUtils.toString(httpResponse.getEntity());
    final var rootElement =
        parseXML(responseBody)
            .getElement("Body")
            .getElement(GetCalendarGroupsResponse.class.getSimpleName());
    return JaxbUtil.elementToJaxb(rootElement, GetCalendarGroupsResponse.class);
  }
}
