package com.zimbra.cs.service.mail;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.CreateContactResponse;
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

  @Test
  void empty_op_folder_action_request_should_fail_when_misses_type_attribute_in_action_selector() {
    var serviceException = assertThrows(ServiceException.class,
        () -> folderActionEmptyTrashForAccount(defaultAccount, true, null));

    assertEquals(ServiceException.INVALID_REQUEST, serviceException.getCode());
    assertEquals("invalid request: missing required attribute: type", serviceException.getMessage());
  }

  @Test
  void empty_op_folder_action_request_should_only_delete_items_specified_by_type_attribute_in_action_selector()
      throws Exception {

    //create contacts and move to trash
    var contactsInAccount = createContactsForAccount(defaultAccount, "heelo@demo.com",
        "heelo2@demo.com", "heelo3@demo.com");
    moveContactsToTrashForAccount(defaultAccount, contactsInAccount);

    //create appointments and move them to trash
    var appointmentId = createAppointmentForAccount(defaultAccount);
    moveAppointmentToTrashForAccount(defaultAccount, appointmentId);

    // empty trash (only items specified by type)
    folderActionEmptyTrashForAccount(defaultAccount, true, FolderActionEmptyOpTypes.CONTACTS);

    // verify
    var getFolderResponse = getFolderForAccount(defaultAccount,
        String.valueOf(FolderConstants.ID_FOLDER_TRASH));
    // will fail right now
    if (getFolderResponse.getFolder().getItemCount() != 0) {
      fail("deletes all items in the trash folder right now");
    }
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

  private void moveContactsToTrashForAccount(Account targetAccount, ArrayList<String> contactIds)
      throws ServiceException {
    for (var contactId : contactIds) {
      Element contactActionSelectorElement = new XMLElement(MailConstants.E_ACTION);
      contactActionSelectorElement.addAttribute(MailConstants.A_ID, contactId);
      contactActionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_MOVE);
      contactActionSelectorElement.addAttribute(MailConstants.A_FOLDER, FolderConstants.ID_FOLDER_TRASH);

      Element contactActionRequestElement = new XMLElement(MailConstants.E_CONTACT_ACTION_REQUEST);
      contactActionRequestElement.addUniqueElement(contactActionSelectorElement);

      var contactActionHandler = new ContactAction();
      contactActionHandler.handle(contactActionRequestElement, getSoapContextForAccount(targetAccount, false));
    }
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

  private void moveAppointmentToTrashForAccount(Account targetAccount, String appointmentId) throws ServiceException {

    Element itemActionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    itemActionSelectorElement.addAttribute(MailConstants.A_ID, appointmentId);
    itemActionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_MOVE);
    itemActionSelectorElement.addAttribute(MailConstants.A_FOLDER, FolderConstants.ID_FOLDER_TRASH);

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