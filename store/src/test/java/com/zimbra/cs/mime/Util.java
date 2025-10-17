package com.zimbra.cs.mime;

public class Util {

	public static String generateInvite() {
		return """
        BEGIN:VCALENDAR\r
        METHOD:REQUEST\r
        PRODID:-//Example Corp//EN\r
        VERSION:2.0\r
        BEGIN:VEVENT\r
        UID:12345678@example.com\r
        DTSTAMP:20251017T120000Z\r
        DTSTART:20251018T140000Z\r
        DTEND:20251018T150000Z\r
        SUMMARY:Team Meeting\r
        DESCRIPTION:Discuss project status\r
        LOCATION:Conference Room A\r
        ORGANIZER:mailto:organizer@example.com\r
        ATTENDEE;CN=Attendee;RSVP=TRUE:mailto:attendee@example.com\r
        END:VEVENT\r
        END:VCALENDAR""";
	}
}
