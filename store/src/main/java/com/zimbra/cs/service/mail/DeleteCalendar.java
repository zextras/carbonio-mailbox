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
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;
import io.vavr.control.Try;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteCalendar extends ItemAction {
//    @Override
//    protected String[] getProxiedIdPath(Element request) {
//        String operation = getXPath(request, OPERATION_PATH);
//        if (operation != null && FOLDER_OPS.contains(operation.toLowerCase()))
//            return TARGET_ITEM_PATH;
//        return super.getProxiedIdPath(request);
//    }

//    private static final String OP_EMPTY = "empty";
//    private static final String OP_REFRESH = "sync";
//    private static final String OP_FREEBUSY = "fb";
//    private static final String OP_CHECK = "check";
//    private static final String OP_UNCHECK = '!' + OP_CHECK;
//    private static final String OP_SET_URL = "url";
//    private static final String OP_IMPORT = "import";
//    private static final String OP_GRANT = "grant";
//    private static final String OP_REVOKE = '!' + OP_GRANT;
//    private static final String OP_REVOKEORPHANGRANTS = "revokeorphangrants";
//    private static final String OP_SYNCON = "syncon";
//    private static final String OP_SYNCOFF = '!' + OP_SYNCON;
//    private static final String OP_RETENTIONPOLICY = "retentionpolicy";
//    private static final String OP_DISABLE_ACTIVESYNC = "disableactivesync";
//    private static final String OP_ENABLE_ACTIVESYNC = '!' + OP_DISABLE_ACTIVESYNC;
//    private static final String OP_WEBOFFLINESYNCDAYS = "webofflinesyncdays";
//
//
//    private static final Set<String> FOLDER_OPS = ImmutableSet.of(
//            OP_EMPTY, OP_REFRESH, OP_SET_URL, OP_IMPORT, OP_FREEBUSY, OP_CHECK, OP_UNCHECK, OP_GRANT,
//            OP_REVOKE, OP_REVOKEORPHANGRANTS, OP_UPDATE, OP_SYNCON, OP_SYNCOFF, OP_RETENTIONPOLICY,
//            OP_DISABLE_ACTIVESYNC, OP_ENABLE_ACTIVESYNC, OP_WEBOFFLINESYNCDAYS
//    );


    private static final String LIST_SEPARATOR = "#";
    private static final String CALENDAR_IDS_SECTION_KEY = "calendarIds";
    private static final String CALENDAR_IDS_METADATA_KEY = "cids";

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
            String constraint = action.getAttribute(MailConstants.A_TARGET_CONSTRAINT, null);
            MailItem.TargetConstraint tcon = MailItem.TargetConstraint.parseConstraint(mbox, constraint);

            ItemActionResult localResults = ItemActionHelper.HARD_DELETE(octxt, mbox, responseProto, local, MailItem.Type.FOLDER, tcon).getResult();

            result.appendSuccessIds(localResults.getSuccessIds());
            result.appendNonExistentIds(localResults);
            updateGroups(result, mbox, octxt);
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

    private void updateGroups(ItemActionResult result, Mailbox mbox, OperationContext octxt) throws ServiceException {
        groupCalendarsByUpdatedIds(mbox.getCalendarGroups(octxt, SortBy.NAME_ASC), result.getSuccessIds())
                .forEach((group, calendarList) -> Try.run(() -> mbox.setCustomData(octxt, group.getId(), group.getType(), encodeCalendarList(calendarList)))
                .onFailure(e -> ZimbraLog.mailbox.error("Failed to update group with id: " + group.getId(), e)));
    }


    private static Map<Folder, List<String>> groupCalendarsByUpdatedIds(List<Folder> groups, List<String> deletedCalendarIds) throws ServiceException {
        var groupToUpdatedIds = new HashMap<Folder, List<String>>();
        for (Folder group : groups) {
            List<String> calendarList = decodeCalendarList(group);
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

    private static List<String> decodeCalendarList(Folder group) throws ServiceException {
        return Arrays.stream(group
                        .getCustomData(CALENDAR_IDS_SECTION_KEY)
                        .get(CALENDAR_IDS_METADATA_KEY)
                        .split(LIST_SEPARATOR))
                .toList();
    }

    private MailItem.CustomMetadata encodeCalendarList(List<String> calendarList) throws ServiceException {
        final var encodedList =
                calendarList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(LIST_SEPARATOR));
        final var metadata = new Metadata().put(CALENDAR_IDS_METADATA_KEY, encodedList).toString();
        return new MailItem.CustomMetadata(CALENDAR_IDS_SECTION_KEY, metadata);
    }
}
