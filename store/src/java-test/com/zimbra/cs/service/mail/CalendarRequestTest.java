// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3

package com.zimbra.cs.service.mail;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.mime.shim.JavaMailInternetAddress;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxLock;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.CalendarMailSender;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.ZAttendee;
import com.zimbra.cs.service.mail.CalendarRequest.MailSendQueue;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


public class CalendarRequestTest {
    
    private ZimbraSoapContext zsc;
    private OperationContext octxt;
    private Mailbox mbox;
    private CalendarItem calItem;
    private ZAttendee addedAttendee1;
    private ZAttendee addedAttendee2;
    private MailSendQueue sendQueue;
    private Invite invite1;
    private Invite invite2;
    private Account account;
    private javax.mail.internet.MimeMessage mm;
    private javax.mail.internet.MimeMessage mm2;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Field lock = Mailbox.class.getDeclaredField("lock");
        lock.setAccessible(true);
        MailboxLock mboxLock = Mockito.mock(MailboxLock.class);
        mbox = Mockito.mock(Mailbox.class);
        lock.set(mbox, mboxLock);
        octxt = Mockito.mock(OperationContext.class);
        invite1 = Mockito.mock(Invite.class);
        invite2 = Mockito.mock(Invite.class);
        calItem = Mockito.mock(CalendarItem.class);
        sendQueue = new CalendarRequest.MailSendQueue();
    }

    /**
     * Test method for {@link com.zimbra.cs.service.mail.CalendarRequest#notifyCalendarItem(com.zimbra.soap.ZimbraSoapContext, com.zimbra.cs.mailbox.OperationContext, com.zimbra.cs.account.Account, com.zimbra.cs.mailbox.Mailbox, com.zimbra.cs.mailbox.CalendarItem, boolean, java.util.List, boolean, com.zimbra.cs.service.mail.CalendarRequest.MailSendQueue)}.
     * @throws Exception 
     */
    @Test
    public void testNotifyCalendarItem() throws Exception {

        JavaMailInternetAddress emailAddress = new JavaMailInternetAddress("test@zimbra.com", "test", MimeConstants.P_CHARSET_UTF8);
        MockedStatic<AccountUtil> mockAccountUtil = Mockito.mockStatic(AccountUtil.class);
        mockAccountUtil.when(() ->AccountUtil.getFriendlyEmailAddress(account))
                .thenReturn(emailAddress);

        List<Address> addressList = new ArrayList<Address>();
        addressList.add((Address)new InternetAddress("test1@zimbra.com", "Test 1"));
        addressList.add((Address)new InternetAddress("test2@zimbra.com", "Test 2"));
        List<ZAttendee> attendeeList = new ArrayList<ZAttendee>();
        attendeeList.add(addedAttendee1);
        attendeeList.add(addedAttendee2);

        Mockito.doReturn(System.currentTimeMillis()).when(octxt.getTimestamp());

        Mockito.doReturn(1).when(invite1).getMailItemId();
        Mockito.doReturn(2).when(invite2).getMailItemId();
        Mockito.doReturn(attendeeList).when(invite1).getAttendees();
        Mockito.doReturn(attendeeList).when(invite2).getAttendees();
        Mockito.doReturn(true).when(invite2).hasRecurId();

        Mockito.when(calItem.isPublic()).thenReturn(true);
        Mockito.when(calItem.allowPrivateAccess(account, true)).thenReturn(true);
        Mockito.when(calItem.getId()).thenReturn(1);
        Mockito.when(calItem.getInvites()).thenReturn(new Invite[] {invite1, invite2});
        Mockito.when(calItem.getSubpartMessage(1)).thenReturn(mm);
        Mockito.when(calItem.getSubpartMessage(2)).thenReturn(mm2);

        Mockito.when(mbox.getCalendarItemById(octxt, calItem.getId())).thenReturn(calItem);
        MockedStatic<CalendarMailSender> calendarMailSenderMockedStatic = Mockito.mockStatic(CalendarMailSender.class);
        calendarMailSenderMockedStatic.when(() -> CalendarMailSender.toListFromAttendees(attendeeList))
                .thenReturn(addressList);
        MockedStatic<CalendarRequest> calendarRequestMockedStatic = Mockito.mockStatic(CalendarRequest.class);
        calendarRequestMockedStatic.when(() -> CalendarRequest.isOnBehalfOfRequest(Mockito.any(ZimbraSoapContext.class)))
                .thenReturn(false);
        calendarRequestMockedStatic.when(() -> CalendarRequest.getAuthenticatedAccount(Mockito.any(ZimbraSoapContext.class)))
            .thenReturn(account);

        //Mockito.stub(Mockito.method(CalendarRequest.class, "isOnBehalfOfRequest", ZimbraSoapContext.class)).toReturn(false);
        //Mockito.stub(Mockito.method(CalendarRequest.class, "getAuthenticatedAccount", ZimbraSoapContext.class)).toReturn(account);

        CalendarRequest.notifyCalendarItem(zsc, octxt, account, mbox, calItem, true, attendeeList, true, sendQueue);
        assertEquals(1, sendQueue.queue.size());
    }
}
