package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.ModifyCalendarGroupRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zimbra.cs.mailbox.Mailbox.getCalendarGroupById;
import static com.zimbra.cs.mailbox.Mailbox.tryRenameCalendarGroup;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.decodeCalendarIds;

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

    ModifyCalendarGroupRequest req = zsc.elementToJaxb(request);

    int id = Integer.parseInt(req.getId());
    var group = getCalendarGroupById(octxt, mbox, id)
            .orElseThrow(() -> ServiceException.FAILURE("Calendar group with ID " + req.getId() + " does NOT exist"));

    if (shouldRenameGroup(req, group))
      tryRenameCalendarGroup(octxt, mbox, group, req.getName());

    if (shouldModifyListCalendar(req, group))
      mbox.setCustomData(octxt, group.getId(), MailItem.Type.FOLDER, encodeCustomMetadata(new HashSet<>(req.getCalendarIds())));

    return buildResponse(zsc, group);
  }

  private static boolean shouldRenameGroup(ModifyCalendarGroupRequest req, Folder group) {
    return req.getName() != null && !req.getName().isBlank() && !req.getName().equals(group.getName());
  }

  private static boolean shouldModifyListCalendar(ModifyCalendarGroupRequest req, Folder group) throws ServiceException {
    return req.getCalendarIds() != null && notEquals(req, group);
  }

  private static boolean notEquals(ModifyCalendarGroupRequest req, Folder group) throws ServiceException {
    Set<String> groupCalendarsIds = new HashSet<>(decodeCalendarIds(group));
    Set<String> reqCalendarsIds = new HashSet<>(req.getCalendarIds());

    return !reqCalendarsIds.equals(groupCalendarsIds);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Folder group)
      throws ServiceException {
    final var response = zsc.createElement(MailConstants.MODIFY_CALENDAR_GROUP_RESPONSE);

    final var groupInfo = response.addUniqueElement(GROUP_ELEMENT_NAME);
    groupInfo.addAttribute(ID_ELEMENT_NAME, String.valueOf(group.getId()));
    groupInfo.addAttribute(NAME_ELEMENT_NAME, group.getName());

    // TODO: this decode is performed twice, try to do once
    for (final var calendarId : decodeCalendarIds(group)) {
      final var calendarIdElement = groupInfo.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME);
      calendarIdElement.setText(calendarId);
    }
    return response;
  }

  private static MailItem.CustomMetadata encodeCustomMetadata(HashSet<String> calendars)
      throws ServiceException {
    final var encodedList =
            calendars.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(LIST_SEPARATOR));
    final var metadata = new Metadata().put(CALENDAR_IDS_METADATA_KEY, encodedList).toString();
    return new MailItem.CustomMetadata(CALENDAR_IDS_SECTION_KEY, metadata);
  }
}
