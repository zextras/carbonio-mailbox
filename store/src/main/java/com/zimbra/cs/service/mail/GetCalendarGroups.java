package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.soap.ZimbraSoapContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GetCalendarGroups extends MailDocumentHandler {

  // TODO - double: use UUID or a fixed string like "all-calendars-id"?
  private static final String ALL_CALENDARS_GROUP_ID = "a970bb9528c94c40bd51bfede60fcb31";
  private static final String ALL_CALENDARS_GROUP_NAME = "All calendars";

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

    final var calendars = mbox.getCalendarFolders(octxt, SortBy.NAME_ASC);
    final var groups = mbox.getCalendarGroups(octxt, SortBy.NAME_ASC);

    return buildResponse(zsc, calendars, groups);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, List<Folder> calendars, List<Folder> groups) throws ServiceException {
    final var response = zsc.createElement(MailConstants.GET_CALENDAR_GROUPS_RESPONSE);

    addAllCalendarsGroup(calendars, response);
    addStoredGroups(groups, response);

    return response;
  }

  private static void addStoredGroups(List<Folder> groups, Element response) throws ServiceException {
    for (final var group : groups) {
      final var groupElement = createGroupElement(group, response);
      for (final var calendarId : decodeCustomMetadata(group)) {
        final var calendarIdElement = groupElement.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME);
        calendarIdElement.setText(calendarId);
      }
    }
  }

  private static void addAllCalendarsGroup(List<Folder> calendars, Element response) {
    final var allCalendarsGroup = response.addNonUniqueElement(GROUP_ELEMENT_NAME);
    allCalendarsGroup.addAttribute(ID_ELEMENT_NAME, ALL_CALENDARS_GROUP_ID);
    allCalendarsGroup.addAttribute(NAME_ELEMENT_NAME, ALL_CALENDARS_GROUP_NAME);

    for (final var calendarFolder : calendars) {
      final var calendarId = allCalendarsGroup.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME);
      calendarId.setText(calendarFolder.getFolderIdAsString());
    }
  }

  private static Element createGroupElement(Folder group, Element response) {
    final var groupElement = response.addNonUniqueElement(GetCalendarGroups.GROUP_ELEMENT_NAME);
    groupElement.addAttribute(ID_ELEMENT_NAME, String.valueOf(group.getId()));
    groupElement.addAttribute(NAME_ELEMENT_NAME, group.getName());
    return groupElement;
  }

  private static List<String> decodeCustomMetadata(Folder group) throws ServiceException {
    final var encodedList =
            group.getCustomData(CALENDAR_IDS_SECTION_KEY).get(CALENDAR_IDS_METADATA_KEY);
    return !encodedList.isEmpty()
            ? Arrays.stream(encodedList.split(LIST_SEPARATOR)).toList()
            : List.of();
  }
}
