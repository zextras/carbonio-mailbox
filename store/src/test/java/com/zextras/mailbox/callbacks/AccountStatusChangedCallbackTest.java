// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.callbacks;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.carbonio.message_broker.events.services.mailbox.UserStatusChanged;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.callback.AccountStatus;
import com.zimbra.cs.account.callback.CallbackContext;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AccountStatusChangedCallbackTest {

  AccountStatus accountStatus;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    accountStatus = new AccountStatus();
  }

  /**
   * A lot of calls are mocked since they are external calls to service discover or message broker, this
   * just tests that if calls are successful no other exceptions are thrown.
   */
  @Test
  void shouldNotFailWhenExecutingUserStatusChangedCallback(){
    CallbackContext context = Mockito.mock(CallbackContext.class);
    String attrName = "fake";
    Account entry = Mockito.mock(Account.class);

    ServiceDiscoverHttpClient serviceDiscoverHttpClient = Mockito.mock(ServiceDiscoverHttpClient.class);
    MessageBrokerClient messageBrokerClient = Mockito.mock(MessageBrokerClient.class);

    Mockito.when(context.isDoneAndSetIfNot(AccountStatus.class)).thenReturn(false);
    Mockito.when(context.isCreate()).thenReturn(false);
    Mockito.when(entry.getAccountStatus(any(Provisioning.class))).thenReturn("active");
    Mockito.when(entry.getId()).thenReturn("fake-account-id");

    try(MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
        MockedStatic<ServiceDiscoverHttpClient> mockedServiceDiscoverStatic = Mockito.mockStatic(ServiceDiscoverHttpClient.class);
        MockedStatic<MessageBrokerClient> mockedMessageBrokerClientStatic = Mockito.mockStatic(MessageBrokerClient.class)) {

      mockedFiles.when(() -> Files.readString(any(Path.class))).thenReturn("fake-token");
      mockedServiceDiscoverStatic.when(() -> ServiceDiscoverHttpClient.defaultURL("carbonio-message-broker"))
              .thenReturn(serviceDiscoverHttpClient);
      Mockito.when(serviceDiscoverHttpClient.withToken("fake-token")).thenReturn(serviceDiscoverHttpClient);

      Mockito.when(serviceDiscoverHttpClient.getConfig("default/username")).thenReturn(Try.success("fake-username"));
      Mockito.when(serviceDiscoverHttpClient.getConfig("default/password")).thenReturn(Try.success("fake-password"));

      mockedMessageBrokerClientStatic.when(() -> MessageBrokerClient.fromConfig(
          "127.78.0.7",
          20005,
          "fake-username",
          "fake-password"
      )).thenReturn(messageBrokerClient);

      Mockito.when(messageBrokerClient.withCurrentService(Service.MAILBOX)).thenReturn(messageBrokerClient);
      Mockito.when(messageBrokerClient.publish(any(UserStatusChanged.class))).thenReturn(true);

      accountStatus.postModify(context, attrName, entry);

      assertTrue(true);
    }catch(Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
