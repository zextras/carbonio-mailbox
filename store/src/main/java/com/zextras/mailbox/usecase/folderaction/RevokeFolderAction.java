package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.service.MountpointService;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to revoke access to a folder and remove revoked mountpoints.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class RevokeFolderAction {

  private final MailboxManager mailboxManager;
  private final MountpointService mountpointService;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public RevokeFolderAction(
      MailboxManager mailboxManager,
      MountpointService mountpointService,
      ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.mountpointService = mountpointService;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to revoke access from a folder. Revokes access, finds grantee mountpoints,
   * deletes links to revoked folder mountpoints.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param granteeId grantee account zimbraId attribute
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> revokeAndDelete(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final String granteeId) {

    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          userMailbox.revokeAccess(operationContext, itemId.getId(), granteeId);

          mountpointService.deleteMountpoints(accountId, folderId, granteeId);
        });
  }
}
