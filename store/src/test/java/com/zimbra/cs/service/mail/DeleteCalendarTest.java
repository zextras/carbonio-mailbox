package com.zimbra.cs.service.mail;

import com.zextras.mailbox.util.CreateAccount;
import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CalendarGroupInfo;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateCalendarGroupResponse;
import com.zimbra.soap.mail.message.CreateFolderRequest;
import com.zimbra.soap.mail.message.CreateFolderResponse;
import com.zimbra.soap.mail.message.DeleteCalendarRequest;
import com.zimbra.soap.mail.message.DeleteCalendarResponse;
import com.zimbra.soap.mail.message.GetCalendarGroupsRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsResponse;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.FolderActionSelector;
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
class DeleteCalendarTest extends SoapTestSuite {

  private static CreateAccount createAccount;
  private static Provisioning provisioning;

  private Account account;
  private Mailbox mbox;

  @BeforeAll
  static void init() {
    provisioning = Provisioning.getInstance();
    createAccount = getCreateAccountFactory();
  }

  @BeforeEach
  void setUp() throws Exception {
    account = createAccount.create();
    mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  }

  @Test
  void deletingNonCalendarFolderRaisesException() throws Exception {
    Folder folder = createFolder(account, "i-am-not-a-calendar");
    String folderId = folder.getId();
    var folderActionSelector = new FolderActionSelector(folderId, FolderAction.OP_HARD_DELETE);
    final var request = new DeleteCalendarRequest(folderActionSelector);

    var soapResponse = getSoapClient().executeSoap(account, request);
    
    assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, soapResponse.getStatusLine().getStatusCode());
    var responseBody = EntityUtils.toString(soapResponse.getEntity());
    assertTrue(responseBody.contains("Item with ID " + folderId + " is NOT a calendar"));
  }

  @Test
  void deleteCalendar() throws Exception {
    var calendarIdToDelete = createCalendar(account, "calendar-to-delete").getId();

    deleteCalendar(account, calendarIdToDelete);

    assertThrows(MailServiceException.NoSuchItemException.class, () -> mbox.getFolderById(null, calendarIdToDelete));
  }

  @Test
  void removeCalendarFromGroup() throws Exception {
    var calendarIdToDelete = createCalendar(account, "calendar-to-delete").getId();
    var aCalendarId = createCalendar(account, "test-calendar-2").getId();
    var anotherCalendarId = createCalendar(account, "test-calendar-3").getId();
    var groupName = "Group Calendar";
    createGroup(account, groupName, List.of(calendarIdToDelete, aCalendarId, anotherCalendarId));

    var response = deleteCalendar(account, calendarIdToDelete);

    assertEquals(calendarIdToDelete, response.getAction().getId());
    var calendarIds = getGroupInfoAfterDeletion(groupName).getCalendarIds();
    assertEquals(2, calendarIds.size());
    assertFalse(calendarIds.contains(calendarIdToDelete));
  }

  private CalendarGroupInfo getGroupInfoAfterDeletion(String groupName) throws Exception {
    return searchGroups(account).stream()
            .filter(g -> g.getName().equals(groupName))
            .findFirst().get();
  }

  private CalendarGroupInfo createGroup(Account acc, String groupName, List<String> calendarIds) throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName(groupName);
    request.setCalendarIds(calendarIds);

    var soapResponse = getSoapClient().executeSoap(acc, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse, CreateCalendarGroupResponse.class);
    return response.getGroup();
  }

  private DeleteCalendarResponse deleteCalendar(Account acc, String calendarId) throws Exception {
    var folderActionSelector = new FolderActionSelector(calendarId, FolderAction.OP_HARD_DELETE);

    final var request = new DeleteCalendarRequest(folderActionSelector);
    var soapResponse = getSoapClient().executeSoap(acc, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(soapResponse, DeleteCalendarResponse.class);
  }

  private Folder createCalendar(Account account, String name) throws Exception {
    return createItem(account, name, MailItem.Type.APPOINTMENT);
  }

  private Folder createFolder(Account account, String name) throws Exception {
    return createItem(account, name, MailItem.Type.FOLDER);
  }

  private Folder createItem(Account account, String name, MailItem.Type type) throws Exception {
    final var folder = new NewFolderSpec(name);
    folder.setParentFolderId("1");
    folder.setDefaultView(type.toString());
    final var createFolderRequest = new CreateFolderRequest(folder);
    final var createFolderResponse = getSoapClient().executeSoap(account, createFolderRequest);
    assertEquals(HttpStatus.SC_OK, createFolderResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(createFolderResponse, CreateFolderResponse.class).getFolder();
  }

  private List<CalendarGroupInfo> searchGroups(Account account) throws Exception {
    final var request = new GetCalendarGroupsRequest();

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(soapResponse, GetCalendarGroupsResponse.class).getGroups();
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
