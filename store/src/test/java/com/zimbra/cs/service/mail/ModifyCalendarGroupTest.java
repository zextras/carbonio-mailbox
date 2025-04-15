package com.zimbra.cs.service.mail;

import com.zextras.mailbox.util.AccountCreator;
import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateCalendarGroupResponse;
import com.zimbra.soap.mail.message.CreateFolderRequest;
import com.zimbra.soap.mail.message.CreateFolderResponse;
import com.zimbra.soap.mail.message.ModifyCalendarGroupRequest;
import com.zimbra.soap.mail.message.ModifyCalendarGroupResponse;
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
class ModifyCalendarGroupTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;

	private Account account;

  @BeforeAll
  static void init() {
		Provisioning provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning, soapExtension.getDefaultDomain());
  }

  @BeforeEach
  void setUp() throws Exception {
    account = accountCreatorFactory.get().create();
  }

  @Test
  void addOneCalendar() throws Exception {
    var firstCalendar = createCalendar(account, "Test Calendar 1");
    var secondCalendar = createCalendar(account, "Test Calendar 2");
    var res = addGroupFor(account, "Group Calendar", List.of(firstCalendar.getId(), secondCalendar.getId()));
    // Request
    var request = new ModifyCalendarGroupRequest();
    var id = res.getGroup().getId();
    request.setId(id);
    var addedCalendar = createCalendar(account, "Added Calendar");
    var modifiedCalendarList = List.of(firstCalendar.getId(), secondCalendar.getId(), addedCalendar.getId());
    request.setCalendarIds(modifiedCalendarList);

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertEquals(modifiedCalendarList, group.getCalendarIds());
  }

  @Test
  void modifyListCalendar() throws Exception {
    var firstCalendar = createCalendar(account, "Test Calendar 1");
    var secondCalendar = createCalendar(account, "Test Calendar 2");
    var res = addGroupFor(account, "Group Calendar", List.of(firstCalendar.getId(), secondCalendar.getId()));
    // Request
    var request = new ModifyCalendarGroupRequest();
    var id = res.getGroup().getId();
    request.setId(id);
    var otherCalendar = createCalendar(account, "Other Calendar");
    var modifiedCalendarList = List.of(firstCalendar.getId(), otherCalendar.getId());
    request.setCalendarIds(modifiedCalendarList);

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertEquals(modifiedCalendarList, group.getCalendarIds());
  }

  @Test
  void renameGroup() throws Exception {
    var calendarIds = List.of(
            createCalendar(account, "Test Calendar 1").getId(),
            createCalendar(account, "Test Calendar 2").getId()
    );
    var res = addGroupFor(account, "Group Calendar", calendarIds);
    // Request
    var request = new ModifyCalendarGroupRequest();
    var id = res.getGroup().getId();
    request.setId(id);
    String groupNameModified = "Modified - Group Calendar";
    request.setName(groupNameModified);
    request.setCalendarIds(calendarIds);

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertEquals(groupNameModified, group.getName());
  }
  @Test
  void emptyCalendarIds() throws Exception {
    var calendarIds = List.of(
            createCalendar(account, "Test Calendar 1").getId(),
            createCalendar(account, "Test Calendar 2").getId()
    );
    var groupName = "Group Calendar";
    var res = addGroupFor(account, groupName, calendarIds);
    // Request
    var request = new ModifyCalendarGroupRequest();
    var id = res.getGroup().getId();
    request.setId(id);
    request.setName(groupName);
    request.setCalendarIds(null);

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertNull(group.getCalendarIds());
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

  private CreateCalendarGroupResponse addGroupFor(Account acc, String groupName, List<String> calendarIds) throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName(groupName);
    request.setCalendarIds(calendarIds);

    var soapResponse = getSoapClient().executeSoap(acc, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(soapResponse, CreateCalendarGroupResponse.class);
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
}
