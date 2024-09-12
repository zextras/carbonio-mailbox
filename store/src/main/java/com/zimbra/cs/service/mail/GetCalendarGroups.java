package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class GetCalendarGroups extends MailDocumentHandler {
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(MailConstants.GET_CALENDAR_GROUPS_RESPONSE);

    Element group1 = response.addNonUniqueElement("group");
    // TODO: use real group id
    group1.addAttribute("id", "123");
    group1.addAttribute("name", "All Calendars");

    // TODO: load all calendars and use their ids
    final var calendarId = group1.addNonUniqueElement("calendarId");
    calendarId.setText("555");

    return response;
  }
}
