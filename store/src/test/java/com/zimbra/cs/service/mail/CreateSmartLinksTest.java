// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zextras.mailbox.smartlinks.SmartLinksGenerator;
import com.zextras.mailbox.tracking.Event;
import com.zextras.mailbox.tracking.Tracking;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateSmartLinksRequest;
import com.zimbra.soap.mail.type.AttachmentToConvert;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CreateSmartLinksTest {
  private static Account account;
  private Tracking tracking;
  private SmartLinksGenerator smartLinksGenerator;
  private Provisioning provisioning;

  @BeforeAll
  static void setupAll() throws Exception {
    MailboxTestUtil.setUp();
    account = AccountCreator.Factory.getDefault().get().create();
  }

  @BeforeEach
  void setup() {
    tracking = Mockito.mock(Tracking.class);
    smartLinksGenerator = Mockito.mock(SmartLinksGenerator.class);
    provisioning = Provisioning.getInstance();
  }

  @AfterAll
  static void tearDownAll() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldCallTrackingWithCorrectParams() throws Exception {
    final CreateSmartLinks createSmartLinks = new CreateSmartLinks(provisioning, smartLinksGenerator, tracking);
    final Element request = JaxbUtil.jaxbToElement(
        new CreateSmartLinksRequest(
            List.of(new AttachmentToConvert("1", "2"))
        ));
    final Map<String, Object> requestContext = ServiceTestUtil.getRequestContext(account);

    createSmartLinks.handle(request, requestContext);

    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    verify(tracking, times(1)).sendEventIgnoringFailure(eventCaptor.capture());
    final Event receivedEvent = eventCaptor.getValue();
    assertEquals("Mail", receivedEvent.getCategory());
    assertEquals("SendEmailWithSmartLink", receivedEvent.getAction());
    assertEquals(account.getId(), receivedEvent.getUserId());
  }

  @Test
  void shouldNotCallTrackingIfCarbonioSendAnalyticsFalse() throws Exception {
    final CreateSmartLinks createSmartLinks = new CreateSmartLinks(provisioning, smartLinksGenerator, tracking);
    Provisioning.getInstance().getConfig().setCarbonioSendAnalytics(false);
    final Element request = JaxbUtil.jaxbToElement(
        new CreateSmartLinksRequest(
            List.of(new AttachmentToConvert("1", "2"))
        ));
    final Map<String, Object> requestContext = ServiceTestUtil.getRequestContext(account);

    createSmartLinks.handle(request, requestContext);

    verify(tracking, times(0)).sendEventIgnoringFailure(any());
  }

  @Test
  void shouldCallTrackingIfCarbonioSendAnalyticsNotSetOnLDAP() throws Exception {
    final CreateSmartLinks createSmartLinks = new CreateSmartLinks(provisioning, smartLinksGenerator, tracking);
    Provisioning.getInstance().getConfig().unsetCarbonioSendAnalytics();
    final Element request = JaxbUtil.jaxbToElement(
        new CreateSmartLinksRequest(
            List.of(new AttachmentToConvert("1", "2"))
        ));
    final Map<String, Object> requestContext = ServiceTestUtil.getRequestContext(account);

    createSmartLinks.handle(request, requestContext);

    verify(tracking, times(1)).sendEventIgnoringFailure(any());
  }
}
