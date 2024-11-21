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

import static com.zimbra.cs.mailbox.Mailbox.getCalendarGroupById;
import static com.zimbra.cs.mailbox.Mailbox.tryRenameCalendarGroup;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.decodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.encodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupXMLHelper.addCalendarIdsToElement;
import static com.zimbra.cs.service.mail.CalendarGroupXMLHelper.createUniqueGroupElement;

public class ModifyCalendarGroup extends MailDocumentHandler {

  public static final List<String> EMPTY_LIST = List.of();

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

    String nameFromRequest = req.getName();
    if (shouldRename(nameFromRequest, group.getName()))
      tryRenameCalendarGroup(octxt, mbox, group, nameFromRequest);

    modifyCalendarIds(mbox, octxt, group, req.getCalendarIds());

    return buildResponse(zsc, group);
  }

  private void modifyCalendarIds(Mailbox mbox, OperationContext octxt, Folder group, List<String> idsFromRequest) throws ServiceException {
    int groupId = group.getId();
    if (haveNoIds(idsFromRequest)) {
      modifyListCalendar(octxt, mbox, groupId, EMPTY_LIST);
      return;
    }
    if (haveDifferentIds(decodeCalendarIds(group), idsFromRequest)) {
      mbox.validateCalendarIds(octxt, mbox, idsFromRequest.stream().map(Integer::parseInt).toList());
      modifyListCalendar(octxt, mbox, groupId, idsFromRequest);
    }
  }

  private static void modifyListCalendar(OperationContext octxt, Mailbox mbox, int groupId, List<String> calendarIds) throws ServiceException {
    mbox.setCustomData(octxt, groupId, MailItem.Type.FOLDER, encodeCalendarIds(new HashSet<>(calendarIds)));
  }

  private boolean haveNoIds(List<String> calendarIds) {
    return calendarIds == null || calendarIds.isEmpty();
  }

  private static boolean shouldRename(String nameFromRequest, String nameFromGroup) {
    return nameFromRequest != null && !nameFromRequest.isBlank() && !nameFromRequest.equals(nameFromGroup);
  }

  private static boolean haveDifferentIds(List<String> idsFromGroup, List<String> idsFromRequest) {
    return !new HashSet<>(idsFromRequest).equals(new HashSet<>(idsFromGroup));
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Folder group)
      throws ServiceException {
    final var response = zsc.createElement(MailConstants.MODIFY_CALENDAR_GROUP_RESPONSE);
    var groupElement = createUniqueGroupElement(response, group);
    // TODO: this decode is performed twice, try to do once
    addCalendarIdsToElement(groupElement, decodeCalendarIds(group));
    return response;
  }
}
