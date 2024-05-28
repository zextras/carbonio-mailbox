package com.zimbra.cs.service.mail;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.mailbox.FolderConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.FolderActionEmptyOpTypes;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.CreateContactResponse;
import com.zimbra.soap.mail.message.CreateFolderResponse;
import com.zimbra.soap.mail.message.FolderActionResponse;
import com.zimbra.soap.mail.message.GetFolderRequest;
import com.zimbra.soap.mail.message.GetFolderResponse;
import com.zimbra.soap.mail.type.ContactSpec;
import com.zimbra.soap.mail.type.GetFolderSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class FolderActionTest extends SoapTestSuite {

  @SuppressWarnings("FieldCanBeLocal")
  private static Provisioning provisioning;
  @SuppressWarnings("FieldCanBeLocal")
  private static Domain defaultDomain;
  private static Account defaultAccount;

  @BeforeAll
  public static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    defaultDomain = provisioning.createDomain(UUID.randomUUID() + ".com", new HashMap<>());
    defaultAccount = provisioning.createAccount(UUID.randomUUID() + "@" + defaultDomain.getName(), "password",
        new HashMap<>());
  }

  @AfterEach
  public void tearDown() throws ServiceException {
    provisioning.deleteAccount(defaultAccount.getId());
    defaultAccount = provisioning.createAccount(UUID.randomUUID() + "@" + defaultDomain.getName(), "password",
        new HashMap<>());
  }

  @Test
  void empty_op_folder_action_request_should_fail_when_wrong_type_attribute_in_action_selector()
      throws ServiceException {
    Element actionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    actionSelectorElement.addAttribute(MailConstants.A_ID, FolderConstants.ID_FOLDER_TRASH);
    actionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_EMPTY);
    actionSelectorElement.addAttribute(MailConstants.A_RECURSIVE, Boolean.valueOf(true).toString());
    actionSelectorElement.addAttribute(MailConstants.A_FOLDER_ACTION_EMPTY_OP_MATCH_TYPE, "wrongType");

    Element folderActionRequestElement = new XMLElement(MailConstants.E_FOLDER_ACTION_REQUEST);
    folderActionRequestElement.addUniqueElement(actionSelectorElement);

    var folderActionHandler = new FolderAction();
    var soapContextForAccount = getSoapContextForAccount(defaultAccount, false);

    var serviceException = assertThrows(ServiceException.class,
        () -> folderActionHandler.handle(folderActionRequestElement, soapContextForAccount));
    assertEquals(
        String.format("invalid request: Invalid 'type' parameter. Supported parameters are: %s",
            FolderActionEmptyOpTypes.valueToString()),
        serviceException.getMessage());
  }

  @Test
  void empty_op_folder_action_request_should_not_fail_when_missed_type_attribute_in_action_selector()
      throws Exception {
    var contactsInAccount = createContactsForAccount(defaultAccount, "heelo@demo.com",
        "heelo2@demo.com", "heelo3@demo.com");
    moveItemsToFolderForAccount(defaultAccount, contactsInAccount, FolderConstants.ID_FOLDER_TRASH);

    var appointmentId = createAppointmentForAccount(defaultAccount);
    moveItemToTrashForAccount(defaultAccount, appointmentId);

    var getFolderResponseBeforeEmptyingTrash = getFolderForAccount(defaultAccount,
        String.valueOf(FolderConstants.ID_FOLDER_TRASH));

    assertEquals(4, getFolderResponseBeforeEmptyingTrash.getFolder().getItemCount(),
        "there should be items in the trash");

    folderActionEmptyTrashForAccount(defaultAccount, true, null);

    var getFolderResponseAfterEmptyingTrash = getFolderForAccount(defaultAccount,
        String.valueOf(FolderConstants.ID_FOLDER_TRASH));

    assertEquals(0, getFolderResponseAfterEmptyingTrash.getFolder().getItemCount(),
        "there should be no items left in the trash after emptying the trash");
  }

  @Test
  void empty_op_folder_action_request_should_only_delete_items_specified_by_type_attribute_in_action_selector()
      throws Exception {
    // create and move contacts to trash folder
    var contactsInAccount = createContactsForAccount(defaultAccount, "heelo@demo.com",
        "heelo2@demo.com", "heelo3@demo.com");
    moveItemsToFolderForAccount(defaultAccount, contactsInAccount, FolderConstants.ID_FOLDER_TRASH);

    // create folder under Contacts, populate it with contacts and move the folder to trash folder
    var folderUnderContacts = createFolderForAccount(defaultAccount, FolderConstants.ID_FOLDER_CONTACTS, Type.CONTACT,
        "myContacts");
    var contactsInAccount2 = createContactsForAccount(defaultAccount, "heelo1@demo.com",
        "heelo21@demo.com", "heelo31@demo.com");
    moveItemsToFolderForAccount(defaultAccount, contactsInAccount2, folderUnderContacts);
    moveItemToTrashForAccount(defaultAccount, String.valueOf(folderUnderContacts));
    assertDoesNotThrow(() -> getFolderForAccount(defaultAccount, String.valueOf(folderUnderContacts)));

    // create an appointment and move it to the trash folder
    var appointmentId = createAppointmentForAccount(defaultAccount);
    moveItemToTrashForAccount(defaultAccount, appointmentId);

    // execute empty trash operation for contacts
    folderActionEmptyTrashForAccount(defaultAccount, true, FolderActionEmptyOpTypes.CONTACTS);

    var mailItemNotFoundServiceException = assertThrows(ServiceException.class,
        () -> getFolderForAccount(defaultAccount, String.valueOf(folderUnderContacts)),
        "there should be no folder with contact view");

    assertEquals(String.format("no such folder id: %s", folderUnderContacts),
        mailItemNotFoundServiceException.getMessage());

    var getFolderResponse = getFolderForAccount(defaultAccount,
        String.valueOf(FolderConstants.ID_FOLDER_TRASH));

    assertEquals(1, getFolderResponse.getFolder().getItemCount(),
        "there should be only one appointment left after emptying the trash for contacts");
  }

  @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
  private FolderActionResponse folderActionEmptyTrashForAccount(Account targetAccount, boolean recursively,
      @Nullable FolderActionEmptyOpTypes itemsType) throws ServiceException {
    Element actionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    actionSelectorElement.addAttribute(MailConstants.A_ID, FolderConstants.ID_FOLDER_TRASH);
    actionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_EMPTY);
    actionSelectorElement.addAttribute(MailConstants.A_RECURSIVE, Boolean.valueOf(recursively).toString());
    actionSelectorElement.addAttribute(MailConstants.A_FOLDER_ACTION_EMPTY_OP_MATCH_TYPE,
        itemsType != null ? itemsType.name() : null);

    Element folderActionRequestElement = new XMLElement(MailConstants.E_FOLDER_ACTION_REQUEST);
    folderActionRequestElement.addUniqueElement(actionSelectorElement);

    var folderActionHandler = new FolderAction();
    var response = folderActionHandler.handle(folderActionRequestElement,
        getSoapContextForAccount(targetAccount, false));

    return JaxbUtil.elementToJaxb(response);
  }

  private void moveItemsToFolderForAccount(Account targetAccount, ArrayList<String> itemIds, int targetFolderId)
      throws ServiceException {
    for (var itemId : itemIds) {
      moveItemToFolderForAccount(targetAccount, itemId, targetFolderId);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private int createFolderForAccount(Account targetAccount, int parentFolderId, MailItem.Type view, String name)
      throws Exception {
    var folderElement = new Element.XMLElement(MailConstants.E_FOLDER);
    folderElement.addAttribute(MailConstants.A_FOLDER, String.valueOf(parentFolderId));
    folderElement.addAttribute(MailConstants.A_DEFAULT_VIEW, view.name());
    folderElement.addAttribute(MailConstants.A_NAME, name);

    var createFolderElement = new Element.XMLElement(MailConstants.E_CREATE_FOLDER_REQUEST);
    createFolderElement.addUniqueElement(folderElement);

    var createFolderHandler = new CreateFolder();
    createFolderHandler.setResponseQName(MailConstants.CREATE_FOLDER_RESPONSE);
    var response = createFolderHandler.handle(createFolderElement,
        getSoapContextForAccount(targetAccount, false));

    CreateFolderResponse createFolderResponse = JaxbUtil.elementToJaxb(response);
    return Integer.parseInt(Objects.requireNonNull(createFolderResponse).getFolder().getId());
  }

  private ArrayList<String> createContactsForAccount(Account targetAccount, String... emails) throws Exception {
    var contactIds = new ArrayList<String>();
    for (var contactEmail : emails) {
      var createContactRequestElement = JaxbUtil.jaxbToElement(
          new CreateContactRequest(new ContactSpec().addEmail(contactEmail)));

      var createContactHandler = new CreateContact();
      var response = createContactHandler.handle(createContactRequestElement,
          getSoapContextForAccount(targetAccount, false));

      CreateContactResponse createContactResponse = JaxbUtil.elementToJaxb(response);
      contactIds.add(Objects.requireNonNull(createContactResponse).getContact().getId());
    }

    return contactIds;
  }

  private String createAppointmentForAccount(Account targetAccount) throws Exception {
    var createAppointmentElement =
        Element.parseJSON(
            new String(
                Objects.requireNonNull(FolderActionTest.class.getResourceAsStream("SampleAppointmentRequest.json"))
                    .readAllBytes()),
            MailConstants.CREATE_APPOINTMENT_REQUEST,
            JSONElement.mFactory);
    var createAppointmentHandler = new CreateAppointment();
    createAppointmentHandler.setResponseQName(MailConstants.CREATE_APPOINTMENT_RESPONSE);
    var response = createAppointmentHandler.handle(createAppointmentElement,
        getSoapContextForAccount(targetAccount, false));

    CreateAppointmentResponse createCalendarItemResponse = JaxbUtil.elementToJaxb(response);
    return Objects.requireNonNull(createCalendarItemResponse).getCalItemId();
  }

  private void moveItemToTrashForAccount(Account targetAccount, String itemId) throws ServiceException {
    moveItemToFolderForAccount(targetAccount, itemId, FolderConstants.ID_FOLDER_TRASH);
  }

  @SuppressWarnings("SameParameterValue")
  private void moveItemToFolderForAccount(Account targetAccount, String itemId, int targetFolderId)
      throws ServiceException {
    Element itemActionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    itemActionSelectorElement.addAttribute(MailConstants.A_ID, itemId);
    itemActionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_MOVE);
    itemActionSelectorElement.addAttribute(MailConstants.A_FOLDER, targetFolderId);

    Element itemActionRequestElement = new XMLElement(MailConstants.E_ITEM_ACTION_REQUEST);
    itemActionRequestElement.addUniqueElement(itemActionSelectorElement);

    var itemActionHandler = new ItemAction();
    itemActionHandler.handle(itemActionRequestElement, getSoapContextForAccount(targetAccount, false));
  }

  private GetFolderResponse getFolderForAccount(Account targetAccount, String folderId) throws Exception {
    var getFolderSpec = new GetFolderSpec();
    getFolderSpec.setFolderId(folderId);
    var getFolderRequest = new GetFolderRequest();
    getFolderRequest.setFolder(getFolderSpec);

    var getFolderElement = JaxbUtil.jaxbToElement(getFolderRequest);

    var getFolderHandler = new GetFolder();
    var response = getFolderHandler.handle(getFolderElement,
        getSoapContextForAccount(targetAccount, false));

    return JaxbUtil.elementToJaxb(response);
  }
}