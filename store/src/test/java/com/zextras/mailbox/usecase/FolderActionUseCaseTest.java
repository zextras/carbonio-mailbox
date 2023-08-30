// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FolderActionUseCaseTest {
  private MailboxManager mailboxManager;
  private FolderActionUseCase folderActionUseCase;

  @BeforeEach
  void setUp() throws ServiceException {
    mailboxManager = mock(MailboxManager.class);

    folderActionUseCase = new FolderActionUseCase(mailboxManager);
  }

  @Test
  void shouldBeSuccessAfterEmptyFolder() {
    final String accountId = "account123";
    final String folderId = "folderName";

    final Try<Void> operationResult = folderActionUseCase.empty(accountId, folderId);

    assertTrue(operationResult.isSuccess(), "Folder should be successfully emptied");
  }

  @Test
  void shouldReturnFailureIfTheMailboxDoesntExist() {
    final String accountId = "nonExistingAccount";
    final String folderId = "folderName";

    final Try<Void> operationResult = folderActionUseCase.empty(accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because account id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(IllegalArgumentException.class, gotError);
    assertEquals("unable to locate the mailbox for the given accountId", gotError.getMessage());
  }

  @Test
  void shouldReturnFailureIfTheFolderIdDoesntExist() throws Exception {
    final String accountId = "accountId123";
    final String folderId = "nonExistingFolderName";

    final Mailbox userMailbox = mock(Mailbox.class);

    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);

    final Try<Void> operationResult = folderActionUseCase.empty(accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because folder id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(IllegalArgumentException.class, gotError);
    assertEquals("unable to locate the folder inside the user mailbox", gotError.getMessage());
  }

  @Test
  void shouldFolderBeEmpty() {
    // TODO actually check that the folder is being emptied
  }
}
