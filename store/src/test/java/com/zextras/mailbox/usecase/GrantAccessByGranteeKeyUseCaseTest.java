package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrantAccessByGranteeKeyUseCaseTest {

  private MailboxManager mailboxManager;
  private GrantAccessByGranteeKeyUseCase grantAccessByGranteeKeyUseCase;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    grantAccessByGranteeKeyUseCase =
        new GrantAccessByGranteeKeyUseCase(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterGrantAccess() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        grantAccessByGranteeKeyUseCase.grantAccess(
            operationContext, accountId, folderId, "displayName", "accessKey", "r", 120L);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully granted");
    verify(userMailbox, times(1))
        .grantAccess(
            operationContext,
            itemId.getId(),
            "displayName",
            ACL.GRANTEE_KEY,
            ACL.RIGHT_READ,
            "accessKey",
            120L);
  }
}
