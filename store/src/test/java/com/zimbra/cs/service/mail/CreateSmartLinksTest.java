// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.smartlinks.SmartLink;
import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zextras.mailbox.tracking.Event;
import com.zextras.mailbox.tracking.Tracking;
import com.zextras.mailbox.tracking.TrackingUtil;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateSmartLinksRequest;
import com.zimbra.soap.mail.message.CreateSmartLinksResponse;
import com.zimbra.soap.mail.type.AttachmentToConvert;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class CreateSmartLinksTest {

  private static Account account;

  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    account = AccountCreator.Factory.getDefault().get().create();
  }

  @AfterAll
  static void tearDownUp() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldCallTrackingWithCorrectParams() throws Exception {
    SmartLinksGenerator smartLinksGenerator = Mockito.mock(SmartLinksGenerator.class);
    Tracking tracking = Mockito.mock(Tracking.class);
    final CreateSmartLinks createSmartLinks = new CreateSmartLinks(smartLinksGenerator, tracking);
    final Element request = JaxbUtil.jaxbToElement(
        new CreateSmartLinksRequest(
            List.of(new AttachmentToConvert("1", "2"))
        ));
    final Map<String, Object> requestContext = ServiceTestUtil.getRequestContext(account);

    createSmartLinks.handle(request, requestContext);

    final Event event = new Event(TrackingUtil.anonymize(account.getId()), "Mail",
        "SendEmailWithSmartLink");
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    Mockito.verify(tracking, Mockito.times(1)).sendEvent(eventCaptor.capture());
    final Event receivedEvent = eventCaptor.getValue();
    assertEquals(event.getCategory(), receivedEvent.getCategory());
    assertEquals(event.getAction(), receivedEvent.getAction());
    assertEquals(event.getUserId(), receivedEvent.getUserId());
  }

  @Test
  void shouldNotFailIfTrackingFails() throws Exception {
    SmartLinksGenerator smartLinksGenerator = Mockito.mock(SmartLinksGenerator.class);
    Tracking tracking = Mockito.mock(Tracking.class);
    final CreateSmartLinks createSmartLinks = new CreateSmartLinks(smartLinksGenerator, tracking);
    final Element request = JaxbUtil.jaxbToElement(
        new CreateSmartLinksRequest(
            List.of(new AttachmentToConvert("1", "2"))
        ));
    final Map<String, Object> requestContext = ServiceTestUtil.getRequestContext(account);

    when(tracking.sendEvent(any())).thenReturn(Try.run(() -> {throw new RuntimeException("failed");}));
    when(smartLinksGenerator.smartLinksFrom(any(), any())).thenReturn(
        List.of(new SmartLink("http://publicUrl.com/123")));

    final CreateSmartLinksResponse response = JaxbUtil.elementToJaxb(createSmartLinks.handle(request, requestContext));

    assertEquals(1, response.getSmartLinks().size());
    assertEquals("http://publicUrl.com/123", response.getSmartLinks().get(0).getPublicUrl());
  }

}
