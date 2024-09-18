package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.CalendarGroup;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;

import java.util.Map;
import java.util.UUID;

public class CreateCalendarGroup extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(getZimbraSoapContext(context));
    final var mbox = getRequestedMailbox(zsc);
    final var octxt = getOperationContext(zsc, context);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    CreateCalendarGroupRequest req = zsc.elementToJaxb(request);

    // TODO: implement duplicated calendar name check logic

    final var group = mbox.createCalendarGroup(octxt, req.getName(), req.getCalendarIds());

    return buildResponse(zsc, group);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, CalendarGroup group) {
    final var response = zsc.createElement(MailConstants.CREATE_CALENDAR_GROUP_RESPONSE);
    final var groupInfo = response.addUniqueElement("group");
    groupInfo.addAttribute("id", group.id());
    groupInfo.addAttribute("name", group.name());
    for (final var calendarId : group.calendarIds()) {
      final var calendarIdElement = groupInfo.addNonUniqueElement("calendarId");
      calendarIdElement.setText(calendarId.toString());
    }
    return response;
  }

}
