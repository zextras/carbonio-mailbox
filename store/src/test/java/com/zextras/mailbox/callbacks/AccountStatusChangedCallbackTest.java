// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.callbacks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.events.services.mailbox.UserStatusChanged;
import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.messagebroker.MessageBrokerFactory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.callback.AccountStatus;
import com.zimbra.cs.account.callback.CallbackContext;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AccountStatusChangedCallbackTest extends MailboxTestSuite {

  /**
   * This just tests that if calls are successful no other exceptions are thrown.
   */
  @Test
  void shouldNotFail_When_ExecutingUserStatusChangedCallback_And_EventIsPublishedCorrectly() {
    CallbackContext context = Mockito.mock(CallbackContext.class);
    String attrName = "fake";
    Account entry = Mockito.mock(Account.class);

    Mockito.when(context.isDoneAndSetIfNot(AccountStatus.class)).thenReturn(false);
    Mockito.when(context.isCreate()).thenReturn(false);
    Mockito.when(entry.getAccountStatus(any(Provisioning.class))).thenReturn("active");
    Mockito.when(entry.getId()).thenReturn("fake-account-id");

    Function<UserStatusChanged, Boolean> mockPublishEvent = Mockito.mock(Function.class);
    final AccountStatus accountStatus = new AccountStatus(mockPublishEvent);
    Mockito.when(mockPublishEvent.apply(any(UserStatusChanged.class))).thenReturn(true);

    assertDoesNotThrow(() -> accountStatus.postModify(context, attrName, entry));
    Mockito.verify(mockPublishEvent).apply(any(UserStatusChanged.class));
  }
}
