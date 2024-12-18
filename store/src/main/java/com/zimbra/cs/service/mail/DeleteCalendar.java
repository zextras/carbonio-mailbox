package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;
import io.vavr.control.Try;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.zimbra.cs.service.mail.CalendarGroupCodec.decodeCalendarIds;
import static com.zimbra.cs.service.mail.CalendarGroupCodec.encodeCalendarIds;

public class DeleteCalendar extends ItemAction {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        Element action = request.getElement(MailConstants.E_ACTION);
        String operation = action.getAttribute(MailConstants.A_OPERATION).toLowerCase();

        Element response = zsc.createElement(MailConstants.DELETE_CALENDAR_RESPONSE);
        Element result = response.addUniqueElement(MailConstants.E_ACTION);

        var successfullyRemovedIds = String.join(",", handleDelete(context, request).getSuccessIds());
        result.addAttribute(MailConstants.A_ID, successfullyRemovedIds);
        result.addAttribute(MailConstants.A_OPERATION, operation);
        return response;
    }

    protected ItemActionResult handleDelete(Map<String, Object> context, Element request) throws ServiceException {
        Element action = request.getElement(MailConstants.E_ACTION);
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        SoapProtocol responseProto = zsc.getResponseProtocol();

        // figure out which items are local and which ones are remote, and proxy accordingly
        List<Integer> local = new ArrayList<>();
        Map<String, StringBuilder> remote = new HashMap<>();
        partitionItems(zsc, action.getAttribute(MailConstants.A_ID), local, remote);
        if (remote.isEmpty() && local.isEmpty()) {
            return ItemActionResult.create(getOperation(request));
        }

        // handle referenced items living on other servers
        DeleteActionResult result = (DeleteActionResult) proxyRemoteItems(action, remote, request, context);

        // handle referenced items living on this server
        if (!local.isEmpty()) {
            // Check that the IDs belong to calendars
            mbox.validateCalendarIds(octxt, mbox, local);
            String constraint = action.getAttribute(MailConstants.A_TARGET_CONSTRAINT, null);
            MailItem.TargetConstraint tcon = MailItem.TargetConstraint.parseConstraint(mbox, constraint);

            ItemActionResult localResults = ItemActionHelper.HARD_DELETE(octxt, mbox, responseProto, local, MailItem.Type.FOLDER, tcon).getResult();

            var deletedCalendarIds = localResults.getSuccessIds();
            removeCalendarsFromGroups(mbox, octxt, deletedCalendarIds);

            result.appendSuccessIds(deletedCalendarIds);
            result.appendNonExistentIds(localResults);
        }

        // check if default calendar is deleted, if yes, reset default calendar id
        int defaultCalId = mbox.getAccount().getPrefDefaultCalendarId();
        if (result.mSuccessIds.contains(Integer.toString(defaultCalId))) {
            ZimbraLog.mailbox.info(
                    "Default calendar deleted, so setting default calendar back to \"Calendar\"");
            mbox.resetDefaultCalendarId();
        }
        return result;
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
