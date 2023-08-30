// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.usecase;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;

public class FolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemProvider itemProvider;

  public FolderActionUseCase(MailboxManager mailboxManager) {
    this.mailboxManager = mailboxManager;
  }

  /**
   * This method doesn't recursively remove the folders, it just empties the first one. Have a look
   * at {@link #emptyRecursively} for recursively emptying a folder.
   *
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> empty(OperationContext operationContext, String accountId, String folderId) {
    return Try.of(
        () -> {
          // There is the accountId of who is making the request
          // There is the requested mailbox.
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          // FIXME we need to move the new here
          final ItemId itemId = new ItemId(folderId, accountId);

          userMailbox.emptyFolder(operationContext, folderId, false);

          return null;
        });
  }
}
