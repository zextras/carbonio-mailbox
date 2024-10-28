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
import com.zimbra.soap.mail.message.ModifyCalendarGroupRequest;

import java.util.*;
import java.util.stream.Collectors;

public class ModifyCalendarGroup extends MailDocumentHandler {

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

    // TODO: set for groups

    ModifyCalendarGroupRequest req = zsc.elementToJaxb(request);

    int id = Integer.parseInt(req.getId());
    var group = getCalendarGroupById(mbox, octxt, id)
            .orElseThrow(() -> ServiceException.FAILURE("Calendar group with ID " + req.getId() + " does NOT exist"));

    if (shouldRenameGroup(group, req))
      tryRenameGroup(mbox, octxt, group, req.getName());

    if (shouldModifyListCalendar(group, req))
      mbox.setCustomData(octxt, group.getId(), MailItem.Type.FOLDER, encodeCustomMetadata(req));

    return buildResponse(zsc, group);
  }

  private static boolean shouldRenameGroup(Folder group, ModifyCalendarGroupRequest req) {
    return req.getName() != null && !req.getName().isBlank() && !req.getName().equals(group.getName());
  }

  private static boolean shouldModifyListCalendar(Folder group, ModifyCalendarGroupRequest req) throws ServiceException {
    return req.getCalendarIds() != null && notEquals(group, req);
  }

  private static boolean notEquals(Folder group, ModifyCalendarGroupRequest req) throws ServiceException {
    Set<String> groupCalendarsIds = new HashSet<>(decodeCustomMetadata(group));
    Set<String> reqCalendarsIds = new HashSet<>(req.getCalendarIds());

    return !reqCalendarsIds.equals(groupCalendarsIds);
  }

  private static void tryRenameGroup(Mailbox mbox, OperationContext octxt, Folder group, String groupName) throws ServiceException {
    if (existsGroupName(mbox, octxt, groupName))
      throw ServiceException.OPERATION_DENIED("Calendar group with name " + groupName + " already exists");
    mbox.renameFolder(octxt, group, groupName);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Folder group)
      throws ServiceException {
    final var response = zsc.createElement(MailConstants.MODIFY_CALENDAR_GROUP_RESPONSE);

    final var groupInfo = response.addUniqueElement(GROUP_ELEMENT_NAME);
    groupInfo.addAttribute(ID_ELEMENT_NAME, String.valueOf(group.getId()));
    groupInfo.addAttribute(NAME_ELEMENT_NAME, group.getName());

    for (final var calendarId : decodeCustomMetadata(group)) {
      final var calendarIdElement = groupInfo.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME);
      calendarIdElement.setText(calendarId);
    }
    return response;
  }

  private static boolean existsGroupName(Mailbox mbox, OperationContext octxt, String groupName) throws ServiceException {
    return mbox.getCalendarGroups(octxt, SortBy.NAME_ASC).stream()
            .map(Folder::getName)
            .toList()
            .contains(groupName);
  }

  private static Optional<Folder> getCalendarGroupById(Mailbox mbox, OperationContext octxt, int id) throws ServiceException {
    return mbox.getCalendarGroups(octxt, SortBy.NAME_ASC).stream()
            .filter(group -> group.getId() == id).findFirst();
  }

  private static MailItem.CustomMetadata encodeCustomMetadata(ModifyCalendarGroupRequest req)
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
