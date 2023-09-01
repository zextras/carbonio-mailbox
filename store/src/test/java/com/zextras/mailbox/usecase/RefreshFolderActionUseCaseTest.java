package com.zextras.mailbox.usecase;

import static org.mockito.Mockito.mock;

import com.zimbra.cs.mailbox.MailboxManager;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class RefreshFolderActionUseCaseTest {

  private MailboxManager mailboxManager;
  private EmptyFolderActionUseCase emptyFolderActionUseCase;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    emptyFolderActionUseCase = new EmptyFolderActionUseCase(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterRefreshFolder() throws Exception {}
}
