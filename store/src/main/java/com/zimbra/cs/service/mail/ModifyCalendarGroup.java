package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.ModifyCalendarGroupRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // TODO - double: ServiceException.OPERATION_DENIED | ServiceException.FAILURE | ServiceException.PERM_DENIED
    int id = Integer.parseInt(req.getId());
    var group = getFolderById(mbox, octxt, id)
            .orElseThrow(() -> ServiceException.OPERATION_DENIED("Calendar group with ID " + req.getId() + " does NOT exist"));

    // TODO: I was expecting to work with MailItem.Type.CALENDAR_GROUP. Understeand why it works with MailItem.Type.UNKNOWN
    mbox.setCustomData(octxt, group.getId(), MailItem.Type.UNKNOWN, encodeCustomMetadata(req));
    return buildResponse(zsc, group);
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Folder group)
      throws ServiceException {
    final var response = zsc.createElement(MailConstants.MODIFY_CALENDAR_GROUP_RESPONSE);

    final var groupInfo = response.addUniqueElement(GROUP_ELEMENT_NAME);
    groupInfo.addAttribute(ID_ELEMENT_NAME, group.getId());
    groupInfo.addAttribute(NAME_ELEMENT_NAME, group.getName());

    for (final var calendarId : decodeCustomMetadata(group)) {
      final var calendarIdElement = groupInfo.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME);
      calendarIdElement.setText(calendarId);
    }
    return response;
  }

  private static Optional<Folder> getFolderById(Mailbox mbox, OperationContext octxt, int id) throws ServiceException {
    return mbox.getCalendarGroups(octxt, SortBy.NAME_ASC).stream()
            .filter(group -> group.getId() == id).findFirst();
  }

  private static MailItem.CustomMetadata encodeCustomMetadata(ModifyCalendarGroupRequest req)
      throws ServiceException {
    final var encodedList =
        req.getCalendarIds().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(LIST_SEPARATOR));
    final var metadata = new Metadata().put(CALENDAR_IDS_METADATA_KEY, encodedList).toString();
    return new MailItem.CustomMetadata(CALENDAR_IDS_SECTION_KEY, metadata);
  }

  private static List<String> decodeCustomMetadata(Folder group) throws ServiceException {
    final var encodedList =
        group.getCustomData(CALENDAR_IDS_SECTION_KEY).get(CALENDAR_IDS_METADATA_KEY);
    return Arrays.stream(encodedList.split(LIST_SEPARATOR)).toList();
  }
}
