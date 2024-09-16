package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import java.util.Map;

public class GetCalendarGroups extends MailDocumentHandler {
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(getZimbraSoapContext(context));

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    final var response = zsc.createElement(MailConstants.GET_CALENDAR_GROUPS_RESPONSE);

    final var group1 = response.addNonUniqueElement("group");
    // TODO: use real group id
    group1.addAttribute("id", "123");
    group1.addAttribute("name", "All Calendars");

    // TODO: load all calendars and use their ids
    final var calendarId = group1.addNonUniqueElement("calendarId");
    calendarId.setText("555");

    return response;
  }
}
