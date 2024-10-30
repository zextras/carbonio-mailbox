package com.zimbra.cs.service.mail;

import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Folder;

import java.util.List;

public class CalendarGroupXMLHelper {
    private static final String GROUP_ELEMENT_NAME = "group";
    private static final String ID_ELEMENT_NAME = "id";
    private static final String NAME_ELEMENT_NAME = "name";
    private static final String CALENDAR_ID_ELEMENT_NAME = "calendarId";

    protected static Element createGroupElement(Element response, Folder group) {
        final var groupElement = response.addUniqueElement(GROUP_ELEMENT_NAME);
        groupElement.addAttribute(ID_ELEMENT_NAME, String.valueOf(group.getId()));
        groupElement.addAttribute(NAME_ELEMENT_NAME, group.getName());
        return groupElement;
    }

    protected static void addCalendarIdsToResponse(Element groupElement, List<String> calendarIds) {
        if (calendarIds.isEmpty()) return;

        calendarIds.forEach(calendarId ->
                groupElement.addNonUniqueElement(CALENDAR_ID_ELEMENT_NAME)
                        .setText(calendarId));

    }
}
