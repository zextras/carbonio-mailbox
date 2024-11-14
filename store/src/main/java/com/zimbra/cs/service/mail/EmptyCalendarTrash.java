package com.zimbra.cs.service.mail;

import com.zimbra.common.mailbox.FolderConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.FolderActionEmptyOpTypes;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;
import io.vavr.control.Try;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.zimbra.cs.service.mail.CalendarGroupCodec.decodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.encodeCalendarIds;

public class EmptyCalendarTrash extends ItemAction {

    private static final String OP_EMPTY = "empty";

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);

        List<Folder> calendarsInTrash = mbox.getCalendarsInTrash(octxt);

        emptyTrash(mbox, octxt, zsc);

        removeCalendarsFromGroups(mbox, octxt, idsFrom(calendarsInTrash));

        return zsc.createElement(MailConstants.EMPTY_CALENDAR_TRASH_RESPONSE);
    }

    private static List<String> idsFrom(List<Folder> trashItems) {
        return trashItems.stream().map(deletedCalendar -> String.valueOf(deletedCalendar.getId())).toList();
    }

    private void emptyTrash(Mailbox mbox, OperationContext octxt, ZimbraSoapContext zsc) throws ServiceException {
        var trashItemId = new ItemId(String.valueOf(FolderConstants.ID_FOLDER_TRASH), zsc);
        mbox.emptyFolder(octxt, trashItemId.getId(), true, FolderActionEmptyOpTypes.APPOINTMENTS);
        mbox.purgeImapDeleted(octxt);
    }

    private void removeCalendarsFromGroups(Mailbox mbox, OperationContext octxt, List<String> calendarIds) throws ServiceException {
        groupsByDeletedCalendars(mbox.getCalendarGroups(octxt, SortBy.NAME_ASC), calendarIds)
                .forEach((group, calendarList) ->
                        tryUpdateCalendarList(mbox, octxt, group, calendarList)
                        .onFailure(e -> ZimbraLog.mailbox.error("Failed to update group with id: " + group.getId(), e))
                );
    }

    private Try<Void> tryUpdateCalendarList(Mailbox mbox, OperationContext octxt, Folder group, List<String> calendarList) {
        return Try.run(() -> updateCalendarList(mbox, octxt, group, calendarList));
    }

    private void updateCalendarList(Mailbox mbox, OperationContext octxt, Folder group, List<String> calendarList) throws ServiceException {
        mbox.setCustomData(octxt, group.getId(), group.getType(), encodeCalendarIds(new HashSet<>(calendarList)));
    }


    private static Map<Folder, List<String>> groupsByDeletedCalendars(List<Folder> groups, List<String> deletedCalendarIds) throws ServiceException {
        var groupToUpdatedIds = new HashMap<Folder, List<String>>();
        for (Folder group : groups) {
            List<String> calendarList = decodeCalendarIds(group);
            var deleted = new HashSet<>(deletedCalendarIds);
            List<String> updatedList = calendarList.stream()
                    .filter(id -> !deleted.contains(id))
                    .toList();
            if (hasCalendarsBeenRemoved(updatedList, calendarList)) {
                groupToUpdatedIds.put(group, updatedList);
            }
        }
        return groupToUpdatedIds;
    }

    private static boolean hasCalendarsBeenRemoved(List<String> updatedList, List<String> calendarList) {
        return updatedList.size() < calendarList.size();
    }
}
