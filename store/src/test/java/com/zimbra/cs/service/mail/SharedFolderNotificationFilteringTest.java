package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.mail.message.CreateMountpointRequest;
import com.zimbra.soap.mail.message.FolderActionRequest;
import com.zimbra.soap.mail.type.ActionGrantSelector;
import com.zimbra.soap.mail.type.FolderActionSelector;
import com.zimbra.soap.mail.type.NewMountpointSpec;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class SharedFolderNotificationFilteringTest extends SoapTestSuite {

  private static MailboxManager mailboxManager;

  private Account ownerAccount;
  private Account granteeAccount;

  private static Folder getFirstCalendar(Account user) throws ServiceException {
    var mailbox = mailboxManager.getMailboxByAccount(user);
    var calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.getFirst();
  }

  @BeforeEach
  void setUp() throws Exception {
    ownerAccount = createAccount().create();
    granteeAccount = createAccount().create();
    mailboxManager = MailboxManager.getInstance();
  }

  /** Tests that name changes on shared calendar folders are filtered out from notifications. */
  @Test
  void nameChangeOnSharedCalendarFolderIsFilteredOut() throws Exception {
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
  void colorChangeOnSharedMailFolderIsFilteredOut() throws Exception {
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
  void nameChangeOnSharedMailFolderIsFilteredOut() throws Exception {
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
  void colorChangeOnSharedContactFolderIsFilteredOut() throws Exception {
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
  void multipleRestrictedChangesOnSharedFolderAreFilteredOut() throws Exception {
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

  /**
   * Tests that the \Checked flag toggle on a shared calendar folder is filtered out (the grantee
   * only sees the mountpoint, which has its own flags).
   */
  @Test
  void flagToggleOnSharedCalendarFolderIsFilteredOut() throws Exception {
    var userACalendar = getFirstCalendar(ownerAccount);
    shareFolder(ownerAccount, granteeAccount, userACalendar.getId());
    createMountpoint(granteeAccount, userACalendar, "test shared calendar", "appointment");

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    var checkAction = new FolderActionSelector(userACalendar.getFolderIdAsString(), "check");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(checkAction));

    final SoapResponse response = checkForNotifications(sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Flag toggles on shared calendar folders should be filtered out from notifications");
  }

  /**
   * Tests that name changes on a sub-folder of a shared folder are still delivered to the grantee.
   *
   * <p>The sub-folder is reached through an inherited grant (it has no grant of its own), which is
   * also what happens when a whole mailbox is shared for delegated access; such changes must not be
   * filtered out.
   */
  @Test
  void nameChangeOnSubfolderOfSharedFolderIsDeliveredToGrantee() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();

    var renameAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "rename");
    renameAction.setName("Renamed SubFolder");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(renameAction));

    SoapResponse response = checkForNotifications(setup.sessionId);

    Assertions.assertTrue(
        response.body().contains("<notify"),
        "Name changes on a sub-folder of a shared folder must still be delivered to the grantee");
  }

  /** Color changes on a sub-folder of a shared folder must still reach the grantee. */
  @Test
  void colorChangeOnSubfolderOfSharedFolderIsDeliveredToGrantee() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();

    var colorAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "color");
    colorAction.setColor((byte) 4);
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(colorAction));

    SoapResponse response = checkForNotifications(setup.sessionId);

    Assertions.assertTrue(
        response.body().contains("<notify"),
        "Color changes on a sub-folder of a shared folder must still be delivered to the grantee");
  }

  /**
   * The \Checked toggle on a delegated folder (no mountpoint of its own) must NOT reach the
   * grantee: both accounts often work the mailbox concurrently and echoing the owner's
   * calendar-visibility toggle stomps on the delegate's UI.
   */
  @Test
  void checkedToggleOnSubfolderOfSharedFolderIsFilteredOut() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();

    var checkAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "check");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(checkAction));

    SoapResponse response = checkForNotifications(setup.sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "\\Checked toggles on delegated folders must be suppressed for the grantee");
  }

  /**
   * When the delegate themselves toggles \Checked on a delegated folder, the change must echo
   * back to their own session — the client UI updates its store off the notification.
   */
  @Test
  void checkedToggleByDelegateOnSubfolderIsDeliveredToDelegate() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();

    var checkAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "check");
    SoapResponse actionResponse =
        getSoapClient()
            .newRequest()
            .setCaller(granteeAccount)
            .setRequestedAccount(ownerAccount)
            .setSessionId(setup.sessionId)
            .setSoapBody(new FolderActionRequest(checkAction))
            .execute();

    // The delegate's UI relies on the notification to update its store. It is piggybacked on
    // the same SOAP response as the action itself, so the change must appear in the action's
    // <notify> block.
    Assertions.assertTrue(
        actionResponse.body().contains("<notify"),
        "Delegate's own \\Checked toggle must echo back to their session "
            + "— the UI relies on the notification to update its store");
  }

  /**
   * Symmetric to {@link #checkedToggleOnSubfolderOfSharedFolderIsFilteredOut} — when the
   * delegate toggles \Checked on the owner's folder, the owner must NOT see the toggle echoed
   * into their own session. The bit is shared mailbox state and concurrent users would
   * otherwise keep stomping on each other's calendar-visibility selection.
   */
  @Test
  void checkedToggleByDelegateIsNotEchoedToOwner() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();
    String ownerSessionId = createSessionForOwner();
    acknowledgeOwnerRefresh(ownerSessionId);

    var checkAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "check");
    getSoapClient()
        .newRequest()
        .setCaller(granteeAccount)
        .setRequestedAccount(ownerAccount)
        .setSessionId(setup.sessionId)
        .setSoapBody(new FolderActionRequest(checkAction))
        .execute();

    SoapResponse ownerResponse = checkForOwnerNotifications(ownerSessionId);

    Assertions.assertFalse(
        ownerResponse.body().contains("<notify"),
        "Delegate's \\Checked toggle on the owner's folder must NOT be echoed back to the owner");
  }

  /**
   * Sanity guard for the symmetric filter: the owner's own \Checked toggle must still reach the
   * owner's session — otherwise the owner's UI would stop updating on local actions.
   */
  @Test
  void checkedToggleByOwnerIsDeliveredToOwner() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();
    String ownerSessionId = createSessionForOwner();
    acknowledgeOwnerRefresh(ownerSessionId);

    var checkAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "check");
    SoapResponse actionResponse =
        getSoapClient()
            .newRequest()
            .setCaller(ownerAccount)
            .setSessionId(ownerSessionId)
            .setSoapBody(new FolderActionRequest(checkAction))
            .execute();

    Assertions.assertTrue(
        actionResponse.body().contains("<notify"),
        "Owner's own \\Checked toggle must echo back to their session");
  }

  /**
   * Flag changes other than \Checked on a delegated folder must still reach the grantee — the
   * suppression is scoped to the calendar-visibility bit.
   */
  @Test
  void nonCheckedFlagChangeOnSubfolderOfSharedFolderIsDeliveredToGrantee() throws Exception {
    SubfolderSetup setup = setupSubfolderUnderSharedFolder();

    var syncAction = new FolderActionSelector(String.valueOf(setup.subFolderId), "syncon");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(syncAction));

    SoapResponse response = checkForNotifications(setup.sessionId);

    Assertions.assertTrue(
        response.body().contains("<notify"),
        "Non-\\Checked flag changes on delegated folders must still be delivered to the grantee");
  }

  /**
   * The grantee mounts shared folders from two different accounts. A rename on the owner's folder
   * must still be filtered for the owner's delegate session, even though the grantee has another
   * mountpoint pointing at an unrelated account: the mountpoint-enumeration step must scope the set
   * of mounted folders to the delegate session's target account.
   */
  @Test
  void mountpointToUnrelatedAccountDoesNotInterfereWithSuppression() throws Exception {
    Account otherAccount = createAccount().create();
    var otherCalendar = getFirstCalendar(otherAccount);
    shareFolder(otherAccount, granteeAccount, otherCalendar.getId());
    createMountpoint(granteeAccount, otherCalendar, "other shared calendar", "appointment");

    var ownerCalendar = getFirstCalendar(ownerAccount);
    shareFolder(ownerAccount, granteeAccount, ownerCalendar.getId());
    createMountpoint(granteeAccount, ownerCalendar, "owner shared calendar", "appointment");

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    var renameAction = new FolderActionSelector(ownerCalendar.getFolderIdAsString(), "rename");
    renameAction.setName("Renamed Owner Calendar");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(renameAction));

    SoapResponse response = checkForNotifications(sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "A mountpoint to another account must not leak into the suppression set for the owner's "
            + "delegate session");
  }

  private record SubfolderSetup(int subFolderId, String sessionId) {}

  private SubfolderSetup setupSubfolderUnderSharedFolder() throws Exception {
    var ownerMailbox = mailboxManager.getMailboxByAccount(ownerAccount);
    var sharedParent =
        ownerMailbox.createFolder(
            null,
            "SharedParent-" + UUID.randomUUID(),
            new Folder.FolderOptions().setDefaultView(Type.MESSAGE));
    var subFolder =
        ownerMailbox.createFolder(
            null,
            "SubFolder-" + UUID.randomUUID(),
            sharedParent.getId(),
            new Folder.FolderOptions().setDefaultView(Type.MESSAGE));

    shareFolder(ownerAccount, granteeAccount, sharedParent.getId());
    createMountpoint(granteeAccount, sharedParent, "Shared Parent", Type.MESSAGE.toString());

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);
    return new SubfolderSetup(subFolder.getId(), sessionId);
  }

  /**
   * Tests that name changes on a calendar folder shared with a distribution list (group) are
   * filtered out from notifications for a member of that group.
   */
  @Test
  void nameChangeOnGroupSharedCalendarFolderIsFilteredOut() throws Exception {
    Group group = createDistributionListWithMember(granteeAccount);

    var userACalendar = getFirstCalendar(ownerAccount);
    shareFolderWithGroup(ownerAccount, group, userACalendar.getId());
    createMountpoint(granteeAccount, userACalendar, "group shared calendar", "appointment");

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner renames the folder
    var renameAction = new FolderActionSelector(userACalendar.getFolderIdAsString(), "rename");
    renameAction.setName("Renamed Group Calendar");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(renameAction));

    SoapResponse response = checkForNotifications(sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Name changes on group-shared calendar folders should be filtered out from notifications");
  }

  /**
   * Tests that color changes on a mail folder shared with a distribution list (group) are filtered
   * out from notifications for a member of that group.
   */
  @Test
  void colorChangeOnGroupSharedMailFolderIsFilteredOut() throws Exception {
    Group group = createDistributionListWithMember(granteeAccount);

    var mailbox = mailboxManager.getMailboxByAccount(ownerAccount);
    var mailFolder =
        mailbox.createFolder(
            null, "GroupSharedMail", new Folder.FolderOptions().setDefaultView(Type.MESSAGE));

    shareFolderWithGroup(ownerAccount, group, mailFolder.getId());
    createMountpoint(granteeAccount, mailFolder, "Group Shared Mail", Type.MESSAGE.toString());

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner changes folder color
    var colorAction = new FolderActionSelector(String.valueOf(mailFolder.getId()), "color");
    colorAction.setColor((byte) 4);
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(colorAction));

    SoapResponse response = checkForNotifications(sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Color changes on group-shared mail folders should be filtered out from notifications");
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

  private String createSessionForOwner() throws Exception {
    Element getFolderReq = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
    final SoapResponse response =
        getSoapClient()
            .newSessionRequest()
            .setCaller(ownerAccount)
            .setSoapBody(getFolderReq)
            .execute();
    String sessionId = SoapUtils.getSessionId(response);
    Assertions.assertNotNull(sessionId, "Owner session ID should be present");
    return sessionId;
  }

  private void acknowledgeOwnerRefresh(String sessionId) throws Exception {
    Element noOpReq = new Element.XMLElement(MailConstants.NO_OP_REQUEST);
    getSoapClient()
        .newSessionRequest(sessionId)
        .setCaller(ownerAccount)
        .setSoapBody(noOpReq)
        .execute();
  }

  private SoapResponse checkForOwnerNotifications(String sessionId) throws Exception {
    Element getFolderReq = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
    return getSoapClient()
        .newSessionRequest(sessionId)
        .setCaller(ownerAccount)
        .setSoapBody(getFolderReq)
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

  /**
   * Tests that name changes on a calendar folder shared with an outer distribution list are
   * filtered out from notifications for a user who is a member of a nested (inner) distribution
   * list.
   *
   * <p>Topology: outerDL → innerDL → granteeAccount
   */
  @Test
  void nameChangeOnCalendarFolderSharedWithNestedDLIsFilteredOut() throws Exception {
    Group outerGroup = createNestedDistributionListWithMember(granteeAccount);

    var userACalendar = getFirstCalendar(ownerAccount);
    shareFolderWithGroup(ownerAccount, outerGroup, userACalendar.getId());
    createMountpoint(granteeAccount, userACalendar, "nested dl shared calendar", "appointment");

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner renames the folder
    var renameAction = new FolderActionSelector(userACalendar.getFolderIdAsString(), "rename");
    renameAction.setName("Renamed Nested DL Calendar");
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(renameAction));

    SoapResponse response = checkForNotifications(sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Name changes on calendar folders shared with an outer DL should be filtered out "
            + "for members of a nested inner DL");
  }

  /**
   * Tests that color changes on a mail folder shared with an outer distribution list are filtered
   * out from notifications for a user who is a member of a nested (inner) distribution list.
   *
   * <p>Topology: outerDL → innerDL → granteeAccount
   */
  @Test
  void colorChangeOnMailFolderSharedWithNestedDLIsFilteredOut() throws Exception {
    Group outerGroup = createNestedDistributionListWithMember(granteeAccount);

    var mailbox = mailboxManager.getMailboxByAccount(ownerAccount);
    var mailFolder =
        mailbox.createFolder(
            null,
            "NestedDLSharedMail",
            new Folder.FolderOptions().setDefaultView(Type.MESSAGE));

    shareFolderWithGroup(ownerAccount, outerGroup, mailFolder.getId());
    createMountpoint(granteeAccount, mailFolder, "Nested DL Shared Mail", Type.MESSAGE.toString());

    String sessionId = createSessionForGrantee();
    acknowledgeRefresh(sessionId);

    // Owner changes folder color
    var colorAction = new FolderActionSelector(String.valueOf(mailFolder.getId()), "color");
    colorAction.setColor((byte) 6);
    getSoapClient().executeSoap(ownerAccount, new FolderActionRequest(colorAction));

    SoapResponse response = checkForNotifications(sessionId);

    Assertions.assertFalse(
        response.body().contains("<notify"),
        "Color changes on mail folders shared with an outer DL should be filtered out "
            + "for members of a nested inner DL");
  }

  /**
   * Creates a static distribution list on the default domain and adds {@code member} to it.
   *
   * @return the created {@link Group}
   */
  private Group createDistributionListWithMember(Account member) throws Exception {
    String listAddress = "dl-" + UUID.randomUUID() + "@" + getDefaultDomainName();
    Group group = getProvisioning().createGroup(listAddress, new HashMap<>(), false);
    getProvisioning().addGroupMembers(group, new String[] {member.getName()});
    return group;
  }

  /**
   * Creates two static distribution lists and returns the outer one.
   *
   * <p>Topology: outerDL → innerDL → {@code member}
   *
   * <p>The inner DL contains {@code member} directly. The outer DL contains the inner DL as a
   * member. The folder is shared with the outer DL, so membership resolution must traverse the
   * nesting to reach {@code member}.
   *
   * @return the outer {@link Group}
   */
  private Group createNestedDistributionListWithMember(Account member) throws Exception {
    String domain = getDefaultDomainName();

    // Inner DL: direct member is the grantee account
    String innerAddress = "dl-inner-" + UUID.randomUUID() + "@" + domain;
    Group innerGroup = getProvisioning().createGroup(innerAddress, new HashMap<>(), false);
    getProvisioning().addGroupMembers(innerGroup, new String[] {member.getName()});

    // Outer DL: contains the inner DL as a member
    String outerAddress = "dl-outer-" + UUID.randomUUID() + "@" + domain;
    Group outerGroup = getProvisioning().createGroup(outerAddress, new HashMap<>(), false);
    getProvisioning().addGroupMembers(outerGroup, new String[] {innerGroup.getName()});

    return outerGroup;
  }

  private void shareFolderWithGroup(Account owner, Group group, int folderId) throws Exception {
    var grantRequest = new FolderActionSelector(String.valueOf(folderId), "grant");
    var grant = new ActionGrantSelector("rwidx", "grp");
    grant.setZimbraId(group.getId());
    grant.setDisplayName(group.getName());
    grant.setPassword("");
    grantRequest.setGrant(grant);
    getSoapClient().executeSoap(owner, new FolderActionRequest(grantRequest));
  }
}
