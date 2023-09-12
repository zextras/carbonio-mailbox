package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.fb.FreeBusyProvider;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to manage FreeBusy support on a folder.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class FreeBusyFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public FreeBusyFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to include free busy integration to calendar folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> includeFreeBusyIntegration(
      OperationContext operationContext, String accountId, String folderId) {
    return excludeFreeBusy(operationContext, accountId, folderId, true);
  }

  /**
   * This method is used to exclude free busy integration to calendar folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> excludeFreeBusyIntegration(
      OperationContext operationContext, String accountId, String folderId) {
    return excludeFreeBusy(operationContext, accountId, folderId, false);
  }

  private Try<Void> excludeFreeBusy(
      OperationContext operationContext, String accountId, String folderId, boolean fb) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

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
