package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarGroupCodec {
    private static final String LIST_SEPARATOR = "#";
    private static final String CALENDAR_IDS_SECTION_KEY = "calendarIds";
    private static final String CALENDAR_IDS_METADATA_KEY = "cids";

    private CalendarGroupCodec() {
    }

    protected static List<String> decodeCalendarIds(Folder group) throws ServiceException {
        final var encodedList =
                group.getCustomData(CALENDAR_IDS_SECTION_KEY).get(CALENDAR_IDS_METADATA_KEY);
        return !encodedList.isEmpty()
                ? Arrays.stream(encodedList.split(LIST_SEPARATOR)).toList()
                : List.of();
    }

    protected static MailItem.CustomMetadata encodeCalendarIds(HashSet<String> calendars)
            throws ServiceException {
        final var encodedList =
                calendars.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(LIST_SEPARATOR));
        final var metadata = new Metadata().put(CALENDAR_IDS_METADATA_KEY, encodedList).toString();
        return new MailItem.CustomMetadata(CALENDAR_IDS_SECTION_KEY, metadata);
    }
}
