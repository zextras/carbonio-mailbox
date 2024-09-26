package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class GetCalendarGroups extends MailDocumentHandler {

  // TODO - double: use UUID or a fixed string like "all-calendars-id"?
  private static final String ALL_CALENDARS_GROUP_ID = "a970bb9528c94c40bd51bfede60fcb31";
  private static final String ALL_CALENDARS_GROUP_NAME = "All calendars";

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    final var mbox = getRequestedMailbox(zsc);
    final var octxt = getOperationContext(zsc, context);

    final var calendars = mbox.getCalendarFolders(octxt, SortBy.NAME_ASC);
    // TODO - double: load all calendar groups from datastore

    return buildResponse(zsc, calendars);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, List<Folder> calendars) {
    final var response = zsc.createElement(MailConstants.GET_CALENDAR_GROUPS_RESPONSE);

    addAllCalendarsGroup(calendars, response);
    addFakeCalendarGroup(calendars, response);
    // TODO - double: add other groups loaded from datastore
    return response;
  }

  private static void addAllCalendarsGroup(List<Folder> calendars, Element response) {
    final var allCalendarsGroup = response.addNonUniqueElement("group");
    allCalendarsGroup.addAttribute("id", ALL_CALENDARS_GROUP_ID);
    allCalendarsGroup.addAttribute("name", ALL_CALENDARS_GROUP_NAME);

    for (final var calendarFolder : calendars) {
      final var calendarId = allCalendarsGroup.addNonUniqueElement("calendarId");
      calendarId.setText(calendarFolder.getFolderIdAsString());
    }
  }

  private static void addFakeCalendarGroup(List<Folder> calendars, Element response) {
    final var allCalendarsGroup = response.addNonUniqueElement("group");
    allCalendarsGroup.addAttribute("id", "aec527e27fd543ee88a1d72ebf38d63f");
    allCalendarsGroup.addAttribute("name", "Fake");

    final var calendarFolder = calendars.get(0);
    final var calendarId = allCalendarsGroup.addNonUniqueElement("calendarId");
    calendarId.setText(calendarFolder.getFolderIdAsString());
  }
}
