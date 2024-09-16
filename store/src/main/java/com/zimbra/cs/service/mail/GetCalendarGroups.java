package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.index.SortBy;
import java.util.Map;

public class GetCalendarGroups extends MailDocumentHandler {

  private static final String ALL_CALENDARS_GROUP_ID = "a970bb9528c94c40bd51bfede60fcb31";
  private static final String ALL_CALENDARS_GROUP_NAME = "All Calendars";

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(getZimbraSoapContext(context));
    final var mbox = getRequestedMailbox(zsc);
    final var octxt = getOperationContext(zsc, context);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    final var calendarFolders = mbox.getCalendarFolders(octxt, SortBy.NAME_ASC);

    final var response = zsc.createElement(MailConstants.GET_CALENDAR_GROUPS_RESPONSE);

    final var group1 = response.addNonUniqueElement("group");
    group1.addAttribute("id", ALL_CALENDARS_GROUP_ID);
    group1.addAttribute("name", ALL_CALENDARS_GROUP_NAME);

    for (final var calendarFolder : calendarFolders) {
      final var calendarId = group1.addNonUniqueElement("calendarId");
      calendarId.setText(calendarFolder.getFolderIdAsString());
    }

    return response;
  }
}
