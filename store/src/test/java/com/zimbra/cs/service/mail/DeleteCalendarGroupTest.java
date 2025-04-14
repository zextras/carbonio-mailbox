package com.zimbra.cs.service.mail;

import static com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
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
import com.zimbra.soap.mail.message.DeleteCalendarGroupRequest;
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
import org.junit.jupiter.api.Test;

class DeleteCalendarGroupTest extends SoapTestSuite {
    private static AccountCreator.Factory accountCreatorFactory;
    private static Provisioning provisioning;

    private Account account;

    @BeforeAll
    static void init() {
        provisioning = Provisioning.getInstance();
        accountCreatorFactory = new AccountCreator.Factory(provisioning, soapExtension.getDefaultDomain());
    }

    @BeforeEach
    void setUp() throws Exception {
        account = accountCreatorFactory.get().create();
    }

    @Test
    void deleteGroup() throws Exception {
        var groupToDelete = addGroupTo(account, "Group1", List.of(
                createCalendar(account, "My Calendar").getId(),
                createCalendar(account, "Fake Calendar").getId())
        );
        var group2 = addGroupTo(account, "Group2", List.of(createCalendar(account, "Other Calendar").getId()));
        String groupIdToDelete = groupToDelete.getGroup().getId();
        String id2 = group2.getGroup().getId();

        final var request = new DeleteCalendarGroupRequest();
        request.setId(groupIdToDelete);

        final var soapResponse = getSoapClient().executeSoap(account, request);

        assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());

        var groups = searchGroups(account).getGroups();
        assertEquals(groupsCountPlusAllCalendar(1), groups.size());
        assertTrue(groups.stream().noneMatch(remainingGroup -> remainingGroup.getId().equals(groupIdToDelete)));
        assertTrue(groups.stream().anyMatch(remainingGroup -> remainingGroup.getId().equals(id2)));
    }

    @Test
    void noIdToDelete() throws Exception {
        final var request = new DeleteCalendarGroupRequest();
        request.setId("thisiddoesnotexist");

        final var soapResponse = getSoapClient().executeSoap(account, request);

        assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, soapResponse.getStatusLine().getStatusCode());
    }

    private CreateCalendarGroupResponse addGroupTo(Account account, String name, List<String> ids) throws Exception {
        final var request = new CreateCalendarGroupRequest();
        request.setName(name);
        request.setCalendarIds(ids);

        final var soapResponse = getSoapClient().executeSoap(account, request);

        assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
        return parseSoapResponse(soapResponse, CreateCalendarGroupResponse.class);
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

    private GetCalendarGroupsResponse searchGroups(Account account) throws Exception {
        final var request = new GetCalendarGroupsRequest();

        final var soapResponse = getSoapClient().executeSoap(account, request);

        assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
        return parseSoapResponse(soapResponse, GetCalendarGroupsResponse.class);
    }

    private int groupsCountPlusAllCalendar(int calendarCount) {
        return calendarCount + 1;
    }

}