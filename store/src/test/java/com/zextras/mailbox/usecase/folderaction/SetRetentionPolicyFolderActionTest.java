package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetRetentionPolicyFolderActionTest {
  private MailboxManager mailboxManager;
  private SetRetentionPolicyFolderAction setRetentionPolicyFolderAction;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    setRetentionPolicyFolderAction =
        new SetRetentionPolicyFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldSuccessUpdateRetentionPolicy() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    List<Policy> keepPolicies = List.of(Policy.newUserPolicy("10m"));
    List<Policy> purgePolicies = List.of(Policy.newUserPolicy("1H"));
    RetentionPolicy retentionPolicy = new RetentionPolicy(keepPolicies, purgePolicies);

    final Try<Void> operationResult =
        setRetentionPolicyFolderAction.setRetentionPolicy(
            operationContext, accountId, folderId, retentionPolicy);

    verify(userMailbox, times(1))
        .setRetentionPolicy(
            operationContext, itemId.getId(), MailItem.Type.FOLDER, retentionPolicy);
    assertTrue(operationResult.isSuccess());
  }
}
