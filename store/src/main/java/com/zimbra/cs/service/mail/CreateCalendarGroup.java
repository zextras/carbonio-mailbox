package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateCalendarGroup extends MailDocumentHandler {

  private static final String LIST_SEPARATOR = "#";
  private static final String CALENDAR_IDS_SECTION_KEY = "calendarIds";
  private static final String CALENDAR_IDS_METADATA_KEY = "cids";

  private static final String GROUP_ELEMENT_NAME = "group";
  private static final String ID_ELEMENT_NAME = "id";
  private static final String NAME_ELEMENT_NAME = "name";
  private static final String CALENDAR_ID_ELEMENT_NAME = "calendarId";

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    final var mbox = getRequestedMailbox(zsc);
    final var octxt = getOperationContext(zsc, context);

    CreateCalendarGroupRequest req = zsc.elementToJaxb(request);

    if (existsGroupName(mbox, octxt, req.getName()))
      throw ServiceException.OPERATION_DENIED("Calendar group with name " + req.getName() + " already exists");

    assertCalendarsExist(mbox, octxt, req.getCalendarIds());

    final var fopt = new Folder.FolderOptions();
    fopt.setDefaultView(MailItem.Type.CALENDAR_GROUP);
    fopt.setCustomMetadata(encodeCustomMetadata(req));

    final var group = mbox.createFolder(octxt, req.getName(), 1, fopt);

    return buildResponse(zsc, group);
  }


  private static Element buildResponse(ZimbraSoapContext zsc, Folder group)
      throws ServiceException {
    final var response = zsc.createElement(MailConstants.CREATE_CALENDAR_GROUP_RESPONSE);

    final var groupInfo = response.addUniqueElement(GROUP_ELEMENT_NAME);
    groupInfo.addAttribute(ID_ELEMENT_NAME, group.getId());
    groupInfo.addAttribute(NAME_ELEMENT_NAME, group.getName());

    for (final var calendarId : decodeCustomMetadata(group)) {
      final var calendarIdElement = groupInfo.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME);
      calendarIdElement.setText(calendarId);
    }
    return response;
  }

  private void assertCalendarsExist(Mailbox mbox, OperationContext octxt, List<String> calendarIds) throws ServiceException {
    var existingCalendarIds = mbox.getCalendarFolders(octxt, SortBy.NAME_ASC).stream().map(Folder::getId).toList();
    for (String calendarId : calendarIds) {
        if (!existingCalendarIds.contains(Integer.parseInt(calendarId))) {
        throw ServiceException.FAILURE("Calendar with ID " + calendarId + " does NOT exist");
      }
    }
  }

  private static boolean existsGroupName(Mailbox mbox, OperationContext octxt, String groupName) throws ServiceException {
    return mbox.getCalendarGroups(octxt, SortBy.NAME_ASC).stream()
            .map(Folder::getName)
            .toList()
            .contains(groupName);
  }

  private static MailItem.CustomMetadata encodeCustomMetadata(CreateCalendarGroupRequest req)
      throws ServiceException {
    final var encodedList =
        req.getCalendarIds().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(LIST_SEPARATOR));
    final var metadata = new Metadata().put(CALENDAR_IDS_METADATA_KEY, encodedList).toString();
    return new MailItem.CustomMetadata(CALENDAR_IDS_SECTION_KEY, metadata);
  }

  private static List<String> decodeCustomMetadata(Folder group) throws ServiceException {
    final var encodedList =
        group.getCustomData(CALENDAR_IDS_SECTION_KEY).get(CALENDAR_IDS_METADATA_KEY);
    return Arrays.stream(encodedList.split(LIST_SEPARATOR)).toList();
  }
}
