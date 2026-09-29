// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.PortUtil;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.CreateAppointmentRequest;
import com.zimbra.soap.mail.message.ForwardAppointmentRequest;
import com.zimbra.soap.mail.message.ModifyAppointmentRequest;
import com.zimbra.soap.mail.type.CalOrganizer;
import com.zimbra.soap.mail.type.CalendarAttendee;
import com.zimbra.soap.mail.type.DtTimeInfo;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.InvitationInfo;
import com.zimbra.soap.mail.type.Msg;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class CreateAppointmentDistributionListTest extends SoapTestSuite {

  private static final String DENIED_MESSAGE = "Sender is not allowed to email this distribution list";

  private static MailboxManager mailboxManager;
  private static Provisioning provisioning;
  private static GreenMail greenMail;

  @BeforeAll
  static void setUpClass() throws Exception {
    final int smtpPort = PortUtil.findFreePort();
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(smtpPort, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    provisioning = Provisioning.getInstance();
    provisioning.getLocalServer().setSmtpPort(smtpPort);
    mailboxManager = MailboxManager.getInstance();
  }

  @AfterAll
  static void tearDownClass() {
    greenMail.stop();
  }

  @BeforeEach
  void setUp() {
    greenMail.reset();
  }

  @Test
  void createAppointment_shouldFail_whenOrganizerNotAllowedToSendToDistributionList()
      throws Exception {
    var organizer = createAccount().create();
    var someoneElse = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);

    var response = createAppointmentInviting(organizer, dl.getName());

    assertSendAbortedFault(response, dl);
    assertTrue(appointmentsOf(organizer).isEmpty());
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  @Test
  void createAppointment_shouldSucceed_whenOrganizerGrantedSendToDistList() throws Exception {
    var organizer = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, organizer);

    var response = createAppointmentInviting(organizer, dl.getName());

    assertEquals(200, response.statusCode(), response.body());
    assertEquals(1, appointmentsOf(organizer).size());
    assertEquals(1, greenMail.getReceivedMessages().length);
  }

  @Test
  void createAppointment_shouldSucceed_whenDistributionListHasNoGrants() throws Exception {
    var organizer = createAccount().create();
    var dl = createDistributionList();

    var response = createAppointmentInviting(organizer, dl.getName());

    assertEquals(200, response.statusCode(), response.body());
    assertEquals(1, appointmentsOf(organizer).size());
    assertEquals(1, greenMail.getReceivedMessages().length);
  }

  @Test
  void modifyAppointment_shouldFail_whenDistributionListRestrictedAfterCreation()
      throws Exception {
    var organizer = createAccount().create();
    var someoneElse = createAccount().create();
    var dl = createDistributionList();
    var created = createAppointment(organizer, inviteMessage(organizer, dl.getName()));
    greenMail.reset();
    grantSendToDistList(dl, someoneElse);

    var response = modifyAppointmentInviting(organizer, created.getCalInvId(), dl.getName());

    assertSendAbortedFault(response, dl);
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  @Test
  void forwardAppointment_shouldFail_whenSenderNotAllowedToSendToDistributionList()
      throws Exception {
    var organizer = createAccount().create();
    var attendee = createAccount().create();
    var someoneElse = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);
    var created = createAppointment(organizer, inviteMessage(organizer, attendee.getName()));
    greenMail.reset();

    var forwardRequest = new ForwardAppointmentRequest();
    forwardRequest.setId(created.getCalItemId());
    var msg = new Msg();
    msg.setEmailAddresses(List.of(new EmailAddrInfo(dl.getName(), "t")));
    forwardRequest.setMsg(msg);
    var response = getSoapClient().executeSoap(organizer, forwardRequest);

    assertSendAbortedFault(response, dl);
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  private static void assertSendAbortedFault(SoapResponse response, DistributionList dl) {
    assertNotEquals(200, response.statusCode(), response.body());
    assertTrue(response.body().contains(MailServiceException.SEND_ABORTED_ADDRESS_FAILURE), response.body());
    assertTrue(response.body().contains(DENIED_MESSAGE), response.body());
    assertTrue(response.body().contains(dl.getName()), response.body());
  }

  private SoapResponse createAppointmentInviting(Account organizer, String attendeeAddress)
      throws Exception {
    var request = new CreateAppointmentRequest();
    request.setMsg(inviteMessage(organizer, attendeeAddress));
    return getSoapClient().executeSoap(organizer, request);
  }

  private SoapResponse modifyAppointmentInviting(
      Account organizer, String appointmentId, String attendeeAddress) throws Exception {
    var request = new ModifyAppointmentRequest();
    request.setId(appointmentId);
    request.setMsg(inviteMessage(organizer, attendeeAddress));
    return getSoapClient().executeSoap(organizer, request);
  }

  private static Msg inviteMessage(Account organizer, String attendeeAddress) {
    var msg = new Msg();
    msg.setFolderId(String.valueOf(Mailbox.ID_FOLDER_CALENDAR));
    msg.setSubject("Team meeting");

    var invitation = new InvitationInfo();
    invitation.setName("Team meeting");
    var calOrganizer = new CalOrganizer();
    calOrganizer.setAddress(organizer.getName());
    invitation.setOrganizer(calOrganizer);
    var calendarAttendee = new CalendarAttendee();
    calendarAttendee.setAddress(attendeeAddress);
    calendarAttendee.setDisplayName(attendeeAddress);
    calendarAttendee.setRsvp(true);
    calendarAttendee.setRole("REQ");
    invitation.addAttendee(calendarAttendee);
    invitation.setDtStart(new DtTimeInfo(nextWeek()));

    msg.addEmailAddress(new EmailAddrInfo(attendeeAddress, "t"));
    msg.addEmailAddress(new EmailAddrInfo(organizer.getName(), "f"));
    msg.setInvite(invitation);
    return msg;
  }

  private static String nextWeek() {
    return LocalDateTime.now().plusDays(7L).format(DateTimeFormatter.ofPattern("yMMdd"));
  }

  private static List<MailItem> appointmentsOf(Account account) throws Exception {
    return mailboxManager.getMailboxByAccount(account).getItemList(null, MailItem.Type.APPOINTMENT);
  }

  private static DistributionList createDistributionList() throws Exception {
    return provisioning.createDistributionList(
        UUID.randomUUID() + "@" + getDefaultDomainName(), new HashMap<>());
  }

  private static void grantSendToDistList(DistributionList dl, Account grantee) throws Exception {
    ACLUtil.grantRight(
        provisioning,
        dl,
        Set.of(
            new ZimbraACE(
                grantee.getId(),
                GranteeType.GT_USER,
                RightManager.getInstance().getRight(Right.RT_sendToDistList),
                null,
                null)));
  }
}
