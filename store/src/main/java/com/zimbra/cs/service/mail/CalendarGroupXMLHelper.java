package com.zimbra.cs.service.mail;

import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Folder;

import java.util.List;

public class CalendarGroupXMLHelper {
    // TODO - double: use UUID or a fixed string like "all-calendars-id"?
    private static final String ALL_CALENDARS_GROUP_ID = "a970bb9528c94c40bd51bfede60fcb31";
    private static final String ALL_CALENDARS_GROUP_NAME = "All calendars";

    private static final String GROUP_ELEMENT_NAME = "group";
    private static final String ID_ELEMENT_NAME = "id";
    private static final String NAME_ELEMENT_NAME = "name";
    private static final String CALENDAR_ID_ELEMENT_NAME = "calendarId";

    private CalendarGroupXMLHelper() {
    }

    protected static Element createGroupElement(Element response, Folder group) {
        final var groupElement = response.addUniqueElement(GROUP_ELEMENT_NAME);
        groupElement.addAttribute(ID_ELEMENT_NAME, String.valueOf(group.getId()));
        groupElement.addAttribute(NAME_ELEMENT_NAME, group.getName());
        return groupElement;
    }

    protected static Element createAllCalendarElement(Element response) {
        final var allCalendarsGroup = response.addNonUniqueElement(GROUP_ELEMENT_NAME);
        allCalendarsGroup.addAttribute(ID_ELEMENT_NAME, ALL_CALENDARS_GROUP_ID);
        allCalendarsGroup.addAttribute(NAME_ELEMENT_NAME, ALL_CALENDARS_GROUP_NAME);
        return allCalendarsGroup;
    }

    protected static void addCalendarIdsToElement(Element element, List<String> calendarIds) {
        if (calendarIds.isEmpty()) return;

        calendarIds.forEach(calendarId ->
                element.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME)
                        .setText(calendarId));

    }
}
