package com.zextras.mailbox.usecase;

import com.zimbra.cs.fb.FreeBusyProvider;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

public class FreeBusyFolderActionUseCase {
  private final MailboxManager mailboxManager;

  @Inject
  public FreeBusyFolderActionUseCase(MailboxManager mailboxManager) {
    this.mailboxManager = mailboxManager;
  }

  /**
   * This method is used to include free busy integration to calendar folder.
   *
   * @param accountId the target account which mailbox folder will be emptied
   * @param operationContext an {@link OperationContext}
   * @param itemId an {@link ItemId}
   * @return a {@link Try} object with the status of the operation
   * @author Dima Dymkovets
   * @since 23.10.0
   */
  public Try<Void> includeFreeBusyIntegration(
      String accountId, OperationContext operationContext, ItemId itemId) {
    return excludeFreeBusy(accountId, operationContext, itemId, true);
  }

  /**
   * This method is used to exclude free busy integration to calendar folder.
   *
   * @param accountId the target account which mailbox folder will be emptied
   * @param operationContext an {@link OperationContext}
   * @param itemId an {@link ItemId}
   * @return a {@link Try} object with the status of the operation
   * @author Dima Dymkovets
   * @since 23.10.0
   */
  public Try<Void> excludeFreeBusyIntegration(
      String accountId, OperationContext operationContext, ItemId itemId) {
    return excludeFreeBusy(accountId, operationContext, itemId, false);
  }

  private Try<Void> excludeFreeBusy(
      String accountId, OperationContext operationContext, ItemId itemId, boolean fb) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));
          userMailbox.alterTag(
              operationContext,
              itemId.getId(),
              MailItem.Type.FOLDER,
              Flag.FlagInfo.EXCLUDE_FREEBUSY,
              fb,
              null);
          FreeBusyProvider.mailboxChanged(accountId);
        });
  }
}
