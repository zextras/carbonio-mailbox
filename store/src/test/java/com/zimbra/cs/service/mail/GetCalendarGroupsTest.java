package com.zimbra.cs.service.mail;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateFolderRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsResponse;
import com.zimbra.soap.mail.type.NewFolderSpec;
import java.io.IOException;
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

  private void addCalendarTo(Account account, String name) throws Exception {
    final var folder = new NewFolderSpec(name);
    folder.setParentFolderId("1");
    folder.setDefaultView("appointment");
    final var createFolderRequest = new CreateFolderRequest(folder);
    final var createFolderResponse = getSoapClient().executeSoap(account, createFolderRequest);
    assertEquals(HttpStatus.SC_OK, createFolderResponse.getStatusLine().getStatusCode());
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
