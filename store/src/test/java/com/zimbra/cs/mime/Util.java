package com.zimbra.cs.mime;


import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Util {

	public static String generateInvite() {
		return "BEGIN:VCALENDAR\r\n" +
						"METHOD:REQUEST\r\n" +
						"PRODID:-//Example Corp//EN\r\n" +
						"VERSION:2.0\r\n" +
						"BEGIN:VEVENT\r\n" +
						"UID:12345678@example.com\r\n" +
						"DTSTAMP:20251017T120000Z\r\n" +
						"DTSTART:20251018T140000Z\r\n" +
						"DTEND:20251018T150000Z\r\n" +
						"SUMMARY:Team Meeting\r\n" +
						"DESCRIPTION:Discuss project status\r\n" +
						"LOCATION:Conference Room A\r\n" +
						"ORGANIZER:mailto:organizer@example.com\r\n" +
						"ATTENDEE;CN=Attendee;RSVP=TRUE:mailto:attendee@example.com\r\n" +
						"END:VEVENT\r\n" +
						"END:VCALENDAR";
	}
}
