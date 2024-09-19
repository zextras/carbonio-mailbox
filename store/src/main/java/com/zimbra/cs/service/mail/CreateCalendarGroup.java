package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import java.util.List;
import java.util.Map;

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

    // TODO - double: implement duplicated calendar name check logic

    final var fopt = new Folder.FolderOptions();
    fopt.setDefaultView(MailItem.Type.CALENDAR_GROUP);
    final var group = mbox.createFolder(octxt, req.getName(), 1, fopt);

    return buildResponse(zsc, group);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Folder group) {
    final var response = zsc.createElement(MailConstants.CREATE_CALENDAR_GROUP_RESPONSE);
    final var groupInfo = response.addUniqueElement("group");
    groupInfo.addAttribute("id", group.getId());
    groupInfo.addAttribute("name", group.getName());
    // TODO - double: read from metadata?
    //    final var calendarIds = group.calendarIds();
    final var calendarIds = List.of("10", "420", "421");
    for (final var calendarId : calendarIds) {
      final var calendarIdElement = groupInfo.addNonUniqueElement("calendarId");
      calendarIdElement.setText(calendarId.toString());
    }
    return response;
  }
}
