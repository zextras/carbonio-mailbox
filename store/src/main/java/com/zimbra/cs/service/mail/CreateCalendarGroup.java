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
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.zimbra.cs.mailbox.Mailbox.existsCalendarGroupByName;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.decodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.encodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupXMLHelper.addCalendarIdsToGroupElement;
import static com.zimbra.cs.service.mail.CalendarGroupXMLHelper.createGroupElement;

public class CreateCalendarGroup extends MailDocumentHandler {

  private static final String CALENDAR_IDS_SECTION_KEY = "calendarIds";
  private static final String CALENDAR_IDS_METADATA_KEY = "cids";

  public static final List<String> EMPTY_CALENDAR_LIST = List.of();

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    final var mbox = getRequestedMailbox(zsc);
    final var octxt = getOperationContext(zsc, context);

    CreateCalendarGroupRequest req = zsc.elementToJaxb(request);

    if (existsCalendarGroupByName(octxt, mbox, req.getName()))
      throw ServiceException.OPERATION_DENIED("Calendar group with name " + req.getName() + " already exists");

    var calendarIds = shouldAddCalendars(req)
            ? getValidatedUniqueCalendarIds(req, mbox, octxt)
            : EMPTY_CALENDAR_LIST;

    final var fopt = new Folder.FolderOptions();
    fopt.setDefaultView(MailItem.Type.CALENDAR_GROUP);
    setCustomMetadata(calendarIds, fopt);

    final var group = mbox.createFolder(octxt, req.getName(), 1, fopt);

    return buildResponse(zsc, group);
  }


  private static Element buildResponse(ZimbraSoapContext zsc, Folder group)
      throws ServiceException {
    final var response = zsc.createElement(MailConstants.CREATE_CALENDAR_GROUP_RESPONSE);
    final var groupElement = createGroupElement(response, group);

    addCalendarIdsToGroupElement(groupElement, decodeCalendarIds(group));

    return response;
  }

  private void assertCalendarsExist(Mailbox mbox, OperationContext octxt, List<String> calendarIds) throws ServiceException {
    var existingCalendarIds = mbox.getCalendarFolders(octxt, SortBy.NAME_ASC).stream().map(Folder::getId).toList();
    for (String calendarId : calendarIds) {
        if (!existingCalendarIds.contains(Integer.parseInt(calendarId))) {
        throw ServiceException.FAILURE("Calendar with ID " + calendarId + " does NOT exist");
      }
    }
  }

  private static void setCustomMetadata(List<String> calendarIds, Folder.FolderOptions fopt) throws ServiceException {
    if (!calendarIds.isEmpty()) {
      fopt.setCustomMetadata(encodeCalendarIds(calendarIds));
    } else {
      setEmptyCustomMetadata(fopt);
    }
  }

  private static void setEmptyCustomMetadata(Folder.FolderOptions fopt) throws ServiceException {
    final var metadata = new Metadata().put(CALENDAR_IDS_METADATA_KEY, "").toString();
    fopt.setCustomMetadata(new MailItem.CustomMetadata(CALENDAR_IDS_SECTION_KEY, metadata));
  }

  private static boolean shouldAddCalendars(CreateCalendarGroupRequest req) {
    return req.getCalendarIds() != null && !req.getCalendarIds().isEmpty();
  }

  private List<String> getValidatedUniqueCalendarIds(CreateCalendarGroupRequest req, Mailbox mbox, OperationContext octxt) throws ServiceException {
    // avoiding duplicates from request
    var calendarIds = new HashSet<>(req.getCalendarIds()).stream().toList();
    assertCalendarsExist(mbox, octxt, calendarIds);
    return calendarIds;
  }
}
