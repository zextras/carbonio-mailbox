package com.zimbra.cs.service.mail;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.mailbox.FolderConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CalendarGroupInfo;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateCalendarGroupResponse;
import com.zimbra.soap.mail.message.CreateFolderRequest;
import com.zimbra.soap.mail.message.CreateFolderResponse;
import com.zimbra.soap.mail.message.EmptyCalendarTrashRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsResponse;
import com.zimbra.soap.mail.type.NewFolderSpec;
import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmptyCalendarTrashTest extends SoapTestSuite {
    
    private static Provisioning provisioning;

    private Account account;
    private Mailbox mbox;

    @BeforeAll
    static void init() {
        provisioning = Provisioning.getInstance();
        
    }

    @BeforeEach
    void setUp() throws Exception {
        account = createAccount().create();
        mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
    }

    @Test
    void empty_calendar_trash() throws Exception {
        var calendarId = createCalendar("myCalendar");
        moveItemToTrashForAccount(account, calendarId);

        var soapResponse = getSoapClient().executeSoap(account, new EmptyCalendarTrashRequest());

        assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
        assertTrue(mbox.getCalendarsInTrash(null).isEmpty());
    }


    @Test
    void empty_calendar_trash_do_not_erase_other_item_types() throws Exception {
        var calendarId = createCalendar("myCalendar");
        var contactId = createItem(account, "myContact", MailItem.Type.CONTACT);
        moveItemsToTrashForAccount(account, List.of(calendarId, contactId));

        var soapResponse = getSoapClient().executeSoap(account, new EmptyCalendarTrashRequest());

        assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
        assertDoesNotThrow(() -> mbox.getFolderById(null, contactId));
    }

    @Test
    void empty_calendar_trash_update_calendar_groups() throws Exception {
        var calendarIdToDelete = createCalendar("myCalendar");
        var otherCalendarId = createCalendar("otherCalendar");
        createGroup(account, "myGroup", List.of(calendarIdToDelete, otherCalendarId));
        moveItemToTrashForAccount(account, calendarIdToDelete);

        var soapResponse = getSoapClient().executeSoap(account, new EmptyCalendarTrashRequest());

        assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
        var groupInfoAfterEmptyCalendarTrash = getGroupInfoAfterDeletion("myGroup");
        assertEquals(1, groupInfoAfterEmptyCalendarTrash.getCalendarIds().size());
        assertEquals(otherCalendarId, groupInfoAfterEmptyCalendarTrash.getCalendarIds().get(0));
    }

    private String createCalendar(String calendarName) throws Exception {
        return createItem(account, calendarName, MailItem.Type.APPOINTMENT);
    }

    private void moveItemToTrashForAccount(Account targetAccount, String itemId) throws ServiceException {
        moveItemToFolderForAccount(targetAccount, itemId, FolderConstants.ID_FOLDER_TRASH);
    }

    private void moveItemsToTrashForAccount(Account targetAccount, List<String> itemIds) throws ServiceException {
        for (String itemId: itemIds) {
            moveItemToTrashForAccount(targetAccount, itemId);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void moveItemToFolderForAccount(Account targetAccount, String itemId, int targetFolderId)
            throws ServiceException {
        Element itemActionSelectorElement = new Element.XMLElement(MailConstants.E_ACTION);
        itemActionSelectorElement.addAttribute(MailConstants.A_ID, itemId);
        itemActionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_MOVE);
        itemActionSelectorElement.addAttribute(MailConstants.A_FOLDER, targetFolderId);

        Element itemActionRequestElement = new Element.XMLElement(MailConstants.E_ITEM_ACTION_REQUEST);
        itemActionRequestElement.addUniqueElement(itemActionSelectorElement);

        var itemActionHandler = new ItemAction();
        itemActionHandler.handle(itemActionRequestElement, getSoapContextForAccount(targetAccount, false));
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

    private String createItem(Account account, String name, MailItem.Type type) throws Exception {
        final var folder = new NewFolderSpec(name);
        folder.setParentFolderId("1");
        folder.setDefaultView(type.toString());
        final var createFolderRequest = new CreateFolderRequest(folder);
        final var createFolderResponse = getSoapClient().executeSoap(account, createFolderRequest);
        assertEquals(HttpStatus.SC_OK, createFolderResponse.getStatusLine().getStatusCode());
        return parseSoapResponse(createFolderResponse, CreateFolderResponse.class).getFolder().getId();
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