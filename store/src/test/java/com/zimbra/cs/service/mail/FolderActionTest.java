package com.zimbra.cs.service.mail;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.mailbox.FolderConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.FolderActionEmptyOpTypes;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.CreateContactResponse;
import com.zimbra.soap.mail.message.GetFolderRequest;
import com.zimbra.soap.mail.message.GetFolderResponse;
import com.zimbra.soap.mail.type.ContactSpec;
import com.zimbra.soap.mail.type.GetFolderSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
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

  private static HashMap<String, Object> defaultAccountSoapContext;

  @BeforeAll
  public static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    defaultDomain = provisioning.createDomain(UUID.randomUUID() + ".com", new HashMap<>());
    defaultAccount = provisioning.createAccount(UUID.randomUUID() + "@" + defaultDomain.getName(), "password",
        new HashMap<>());
    defaultAccountSoapContext = getSoapContextFromAccount(defaultAccount);
  }

  private static HashMap<String, Object> getSoapContextFromAccount(Account account)
      throws ServiceException {
    return new HashMap<>() {
      {
        put(
            SoapEngine.ZIMBRA_CONTEXT,
            new ZimbraSoapContext(
                AuthProvider.getAuthToken(account, false),
                account.getId(),
                SoapProtocol.Soap12,
                SoapProtocol.Soap12));
      }
    };
  }

  @Test
  void emptyFolderActionRequestShouldFailWhenMissesType() {

    Element actionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    actionSelectorElement.addAttribute(MailConstants.A_ID, FolderConstants.ID_FOLDER_TRASH);
    actionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_EMPTY);
    actionSelectorElement.addAttribute(MailConstants.A_RECURSIVE, "true");

    Element folderActionRequestElement = new XMLElement(MailConstants.E_FOLDER_ACTION_REQUEST);
    folderActionRequestElement.addUniqueElement(actionSelectorElement);

    var folderActionHandler = new FolderAction();
    var serviceException = assertThrows(ServiceException.class,
        () -> folderActionHandler.handle(folderActionRequestElement, defaultAccountSoapContext));

    assertEquals(ServiceException.INVALID_REQUEST, serviceException.getCode());
    assertEquals("invalid request: missing required attribute: type", serviceException.getMessage());
  }

  @Test
  void emptyFolderRequestShouldDeleteOnlySpecifiedItemsByType() throws Exception {

    var contactsInAccount = createContactsInAccount(defaultAccount, "heelo@demo.com",
        "heelo2@demo.com", "heelo3@demo.com");
    moveContactsToTrash(defaultAccount, contactsInAccount);

    Element actionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    actionSelectorElement.addAttribute(MailConstants.A_ID, FolderConstants.ID_FOLDER_TRASH);
    actionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_EMPTY);
    actionSelectorElement.addAttribute(MailConstants.A_RECURSIVE, "true");
    actionSelectorElement.addAttribute(MailConstants.A_FOLDER_ACTION_EMPTY_OP_MATCH_TYPE,
        FolderActionEmptyOpTypes.EMAILS.name());

    Element folderActionRequestElement = new XMLElement(MailConstants.E_FOLDER_ACTION_REQUEST);
    folderActionRequestElement.addUniqueElement(actionSelectorElement);

    var folderActionHandler = new FolderAction();
    folderActionHandler.handle(folderActionRequestElement, defaultAccountSoapContext);

    var getFolderResponse = getFolder(defaultAccount,
        String.valueOf(FolderConstants.ID_FOLDER_TRASH));

    if (getFolderResponse.getFolder().getItemCount() == 0) {
      fail("deletes all items in the trash folder right now");
    }
  }


  private void moveContactsToTrash(Account targetAccount, ArrayList<String> contactIds) throws ServiceException {

    for (var contactId : contactIds) {
      Element contactActionSelectorElement = new XMLElement(MailConstants.E_ACTION);
      contactActionSelectorElement.addAttribute(MailConstants.A_ID, contactId);
      contactActionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_MOVE);
      contactActionSelectorElement.addAttribute(MailConstants.A_FOLDER, FolderConstants.ID_FOLDER_TRASH);

      Element contactActionRequestElement = new XMLElement(MailConstants.E_CONTACT_ACTION_REQUEST);
      contactActionRequestElement.addUniqueElement(contactActionSelectorElement);

      var contactActionHandler = new ContactAction();
      contactActionHandler.handle(contactActionRequestElement, getSoapContextFromAccount(targetAccount));
    }

  }

  private ArrayList<String> createContactsInAccount(Account targetAccount, String... emails) throws Exception {
    var contactIds = new ArrayList<String>();
    for (var contactEmail : emails) {
      var createContactRequestElement = JaxbUtil.jaxbToElement(
          new CreateContactRequest(new ContactSpec().addEmail(contactEmail)));

      var createContactHandler = new CreateContact();
      var response = createContactHandler.handle(createContactRequestElement,
          getSoapContextFromAccount(targetAccount));

      CreateContactResponse createContactResponse = JaxbUtil.elementToJaxb(response);
      contactIds.add(Objects.requireNonNull(createContactResponse).getContact().getId());
    }

    return contactIds;
  }


  private GetFolderResponse getFolder(Account targetAccount, String folderId) throws Exception {
    var getFolderSpec = new GetFolderSpec();
    getFolderSpec.setFolderId(folderId);
    var getFolderRequest = new GetFolderRequest();
    getFolderRequest.setFolder(getFolderSpec);

    var getFolderElement = JaxbUtil.jaxbToElement(getFolderRequest);

    var getFolderHandler = new GetFolder();
    var response = getFolderHandler.handle(getFolderElement,
        getSoapContextFromAccount(targetAccount));

    return JaxbUtil.elementToJaxb(response);
  }
}