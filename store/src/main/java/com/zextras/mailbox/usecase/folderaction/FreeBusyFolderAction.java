package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.fb.FreeBusyChangeNotifier;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to manage FreeBusy support on a folder.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class FreeBusyFolderAction {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;
  private final FreeBusyChangeNotifier freeBusyChangeNotifier;
  private static final boolean ENABLE_FREEBUSY = true;
  private static final boolean DISABLE_FREEBUSY = false;

  @Inject
  public FreeBusyFolderAction(
      MailboxManager mailboxManager,
      ItemIdFactory itemIdFactory,
      FreeBusyChangeNotifier freeBusyChangeNotifier) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
    this.freeBusyChangeNotifier = freeBusyChangeNotifier;
  }

  /**
   * This method is used to include free busy integration to calendar folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> includeFreeBusyIntegration(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return modifyFreeBusy(operationContext, accountId, folderId, ENABLE_FREEBUSY);
  }

  /**
   * This method is used to exclude free busy integration to calendar folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> excludeFreeBusyIntegration(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return modifyFreeBusy(operationContext, accountId, folderId, DISABLE_FREEBUSY);
  }

  private Try<Void> modifyFreeBusy(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final boolean excludeFreeBusy) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, accountId);

                      userMailbox.alterTag(
                          operationContext,
                          itemId.getId(),
                          MailItem.Type.FOLDER,
                          Flag.FlagInfo.EXCLUDE_FREEBUSY,
                          excludeFreeBusy,
                          null);
                      freeBusyChangeNotifier.mailboxChanged(accountId);
                    }));
  }
}
