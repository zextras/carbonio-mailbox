package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.ModifyCalendarGroupRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.zimbra.cs.mailbox.Mailbox.getCalendarGroupById;
import static com.zimbra.cs.mailbox.Mailbox.tryRenameCalendarGroup;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.decodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.encodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupXMLHelper.addCalendarIdsToElement;
import static com.zimbra.cs.service.mail.CalendarGroupXMLHelper.createGroupElement;

public class ModifyCalendarGroup extends MailDocumentHandler {

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

    if (shouldModifyListCalendar(req, group)) {
      validateCalendarIds(octxt, mbox, req.getCalendarIds().stream().map(Integer::parseInt).toList());
      mbox.setCustomData(octxt, group.getId(), MailItem.Type.FOLDER, encodeCalendarIds(new HashSet<>(req.getCalendarIds())));
    }

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
    var groupElement = createGroupElement(response, group);
    // TODO: this decode is performed twice, try to do once
    addCalendarIdsToElement(groupElement, decodeCalendarIds(group));
    return response;
  }

  private void validateCalendarIds(OperationContext octxt, Mailbox mbox, List<Integer> calendarIds) throws ServiceException {
    for (int id : calendarIds) {
      if (mbox.getFolderById(octxt, id).getDefaultView() != MailItem.Type.APPOINTMENT) {
        throw ServiceException.FAILURE("Item with ID " + id + " is NOT a calendar");
      }
    }
  }
}
