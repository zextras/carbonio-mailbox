package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.mail.message.CreateMountpointRequest;
import com.zimbra.soap.mail.message.FolderActionRequest;
import com.zimbra.soap.mail.type.ActionGrantSelector;
import com.zimbra.soap.mail.type.FolderActionSelector;
import com.zimbra.soap.mail.type.NewMountpointSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class SharedFolderNotificationFilteringTest extends SoapTestSuite {

  private static MailboxManager mailboxManager;

  private Account ownerAccount;
  private Account granteeAccount;

  private static Folder getFirstCalendar(Account user) throws ServiceException {
    var mailbox = mailboxManager.getMailboxByAccount(user);
    var calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.get(0);
  }

  @BeforeEach
  void setUp() throws Exception {
    ownerAccount = createAccount().create();
    granteeAccount = createAccount().create();
    mailboxManager = MailboxManager.getInstance();
  }

  /** Tests that name changes on shared calendar folders are filtered out from notifications. */
  @Test
  public void nameChangeOnSharedCalendarFolderIsFilteredOut() throws Exception {
    var userACalendar = getFirstCalendar(ownerAccount);
    shareFolder(ownerAccount, granteeAccount, userACalendar.getId());
    createMountpoint(granteeAccount, userACalendar, "test shared calendar", "appointment");

    // Create session
    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner renames the folder
    final FolderActionSelector renameAction =
        new FolderActionSelector(userACalendar.getFolderIdAsString(), "rename");
    renameAction.setName("Renamed Calendar");
    var renameRequest = new FolderActionRequest(renameAction);
    getSoapClient().executeSoap(ownerAccount, renameRequest);

    // Grantee checks for notifications
    final SoapResponse response = checkForNotifications(sessionId);

    // Verify that name changes are filtered out
    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Name changes on shared calendar folders should be filtered out from notifications");
  }

  /** Tests that color changes on shared mail folders are filtered out from notifications. */
  @Test
  public void colorChangeOnSharedMailFolderIsFilteredOut() throws Exception {
    // Create a mail folder
    var mailbox = mailboxManager.getMailboxByAccount(ownerAccount);
    var mailFolder =
        mailbox.createFolder(
            null, "SharedMail", new Folder.FolderOptions().setDefaultView(Type.MESSAGE));

    shareFolder(ownerAccount, granteeAccount, mailFolder.getId());
    createMountpoint(granteeAccount, mailFolder, "Shared Mail", Type.MESSAGE.toString());

    // Create session
    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner changes folder color
    final FolderActionSelector colorAction =
        new FolderActionSelector(String.valueOf(mailFolder.getId()), "color");
    colorAction.setColor((byte) 3);
    var colorRequest = new FolderActionRequest(colorAction);
    getSoapClient().executeSoap(ownerAccount, colorRequest);

    // Grantee checks for notifications
    final SoapResponse response = checkForNotifications(sessionId);

    // Verify that color changes are filtered out
    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Color changes on shared mail folders should be filtered out from notifications");
  }

  /** Tests that name changes on shared mail folders are filtered out from notifications. */
  @Test
  public void nameChangeOnSharedMailFolderIsFilteredOut() throws Exception {
    // Create a mail folder
    var mailbox = mailboxManager.getMailboxByAccount(ownerAccount);
    var mailFolder =
        mailbox.createFolder(
            null, "SharedMail", new Folder.FolderOptions().setDefaultView(Type.MESSAGE));

    shareFolder(ownerAccount, granteeAccount, mailFolder.getId());
    createMountpoint(granteeAccount, mailFolder, "Shared Mail", Type.MESSAGE.toString());

    // Create session
    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner renames the folder
    final FolderActionSelector renameAction =
        new FolderActionSelector(String.valueOf(mailFolder.getId()), "rename");
    renameAction.setName("Renamed Mail Folder");
    var renameRequest = new FolderActionRequest(renameAction);
    getSoapClient().executeSoap(ownerAccount, renameRequest);

    // Grantee checks for notifications
    final SoapResponse response = checkForNotifications(sessionId);

    // Verify that name changes are filtered out
    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Name changes on shared mail folders should be filtered out from notifications");
  }

  /** Tests that color changes on shared contact folders are filtered out from notifications. */
  @Test
  public void colorChangeOnSharedContactFolderIsFilteredOut() throws Exception {
    // Create a contact folder
    var mailbox = mailboxManager.getMailboxByAccount(ownerAccount);
    var contactFolder =
        mailbox.createFolder(
            null, "SharedContacts", new Folder.FolderOptions().setDefaultView(Type.CONTACT));

    // Share the contact folder
    shareFolder(ownerAccount, granteeAccount, contactFolder.getId());
    createMountpoint(granteeAccount, contactFolder, "Shared Contacts", Type.CONTACT.toString());

    // Create session
    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner changes folder color
    final FolderActionSelector colorAction =
        new FolderActionSelector(String.valueOf(contactFolder.getId()), "color");
    colorAction.setColor((byte) 7);
    var colorRequest = new FolderActionRequest(colorAction);
    getSoapClient().executeSoap(ownerAccount, colorRequest);

    // Grantee checks for notifications
    final SoapResponse response = checkForNotifications(sessionId);

    // Verify that color changes are filtered out
    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Color changes on shared contact folders should be filtered out from notifications");
  }

  /** Tests that multiple changes (color, name) on a shared folder are all filtered out. */
  @Test
  public void multipleRestrictedChangesOnSharedFolderAreFilteredOut() throws Exception {
    var userACalendar = getFirstCalendar(ownerAccount);
    shareFolder(ownerAccount, granteeAccount, userACalendar.getId());
    createMountpoint(
        granteeAccount, userACalendar, "test shared calendar", Type.APPOINTMENT.toString());

    // Create session
    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner makes multiple changes: color and name
    // Change 1: Color
    var colorAction = new FolderActionSelector(userACalendar.getFolderIdAsString(), "color");
    colorAction.setColor((byte) 5);
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(colorAction));

    // Change 2: Name
    var nameAction = new FolderActionSelector(userACalendar.getFolderIdAsString(), "rename");
    nameAction.setName("Updated Calendar");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(nameAction));

    // Grantee checks for notifications
    final SoapResponse response = checkForNotifications(sessionId);

    // Verify that all restricted changes are filtered out
    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Multiple restricted changes (color, name) on shared folders should be filtered out");
  }

  // Helper methods

  private String createSessionForGrantee() throws Exception {
    Element getFolderReq = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
    final SoapResponse response =
        getSoapClient()
            .newSessionRequest()
            .setCaller(granteeAccount)
            .setSoapBody(getFolderReq)
            .execute();
    String sessionId = SoapUtils.getSessionId(response);
    Assertions.assertNotNull(sessionId, "Session ID should be present");
    return sessionId;
  }

  private void acknowledgeRefresh(String sessionId) throws Exception {
    Element noOpReq = new Element.XMLElement(MailConstants.NO_OP_REQUEST);
    getSoapClient()
        .newSessionRequest(sessionId)
        .setCaller(granteeAccount)
        .setSoapBody(noOpReq)
        .execute();
  }

  private SoapResponse checkForNotifications(String sessionId) throws Exception {
    Element getFolderReq = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
    return getSoapClient()
        .newSessionRequest(sessionId)
        .setCaller(granteeAccount)
        .setSoapBody(getFolderReq)
        .execute();
  }

  private void shareFolder(Account owner, Account grantee, int folderId) throws Exception {
    var grantRequest = new FolderActionSelector(String.valueOf(folderId), "grant");
    var grant = new ActionGrantSelector("rwidx", "usr");
    grant.setZimbraId(grantee.getId());
    grant.setDisplayName(grantee.getName());
    grant.setPassword("");
    grantRequest.setGrant(grant);
    getSoapClient().executeSoap(owner, new FolderActionRequest(grantRequest));
  }

  private void createMountpoint(
      Account onAccount, Folder sharedFolder, String mountpointName, String view) throws Exception {
    var newMountpointSpec = new NewMountpointSpec(mountpointName);
    newMountpointSpec.setDefaultView(view);
    newMountpointSpec.setRemoteId(sharedFolder.getId());
    newMountpointSpec.setOwnerId(sharedFolder.getAccountId());
    newMountpointSpec.setFolderId("1");
    getSoapClient().executeSoap(onAccount, new CreateMountpointRequest(newMountpointSpec));
  }
}
