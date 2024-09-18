package com.zimbra.cs.mailbox;

import java.util.Set;

public record CalendarGroup(String id, String name, Set<Integer> calendarIds) {
}
