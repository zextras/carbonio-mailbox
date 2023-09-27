package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zextras.mailbox.usecase.service.MountpointService;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Use case class to revoke access from a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class RevokeFolderAction {

  private final MailboxManager mailboxManager;
  private final Provisioning provisioning;
  private final MountpointService mountpointService;
  private final ItemIdFactory itemIdFactory;
  private final OperationContextFactory operationContextFactory;

  @Inject
  public RevokeFolderAction(
      MailboxManager mailboxManager,
      Provisioning provisioning,
      MountpointService mountpointService,
      ItemIdFactory itemIdFactory,
      OperationContextFactory operationContextFactory) {
    this.mailboxManager = mailboxManager;
    this.provisioning = provisioning;
    this.mountpointService = mountpointService;
    this.itemIdFactory = itemIdFactory;
    this.operationContextFactory = operationContextFactory;
  }

  /**
   * This method is used to revoke access from a folder. Revokes access, finds grantee mountpoints,
   * deletes links to revoked folder mountpoints. Delete and revoke starts evaluating mountpoints
   * ALWAYS from {@link Mailbox#ID_FOLDER_USER_ROOT} because when sharing a folder the {@link
   * com.zimbra.cs.service.mail.GetFolder} creates shares using the same root.
   *
   * @param operationContext an {@link OperationContext}
   * @param requestedAccountId the requested account where to perform the operation
   * @param folderId the id of the folder (can be id or accountId:id)
   * @param granteeId grantee account zimbraId attribute
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> revokeAndDelete(
      final OperationContext operationContext,
      final String requestedAccountId,
      final String folderId,
      final String granteeId) {

    return mailboxManager
        .tryGetMailboxByAccountId(requestedAccountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, requestedAccountId);
                      final Account granteeAccount = provisioning.getAccountById(granteeId);
                      final OperationContext granteeOpContext =
                          operationContextFactory.create(granteeAccount);
                      final List<Integer> mountPointIds =
                          mountpointService
                              .getMountpointsByPath(
                                  granteeId,
                                  granteeOpContext,
                                  itemIdFactory.create(
                                      String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeId))
                              .stream()
                              .filter(
                                  mpt ->
                                      mpt.getOwnerId().equals(requestedAccountId)
                                          && Objects.equals(mpt.getRemoteId(), itemId.getId()))
                              .map(MailItem::getId)
                              .collect(Collectors.toList());
                      // TODO: the delete action should be performed by the original user who made
                      // the request
                      //  for security reasons.
                      // TODO: revoke probably belongs to another service if we have a
                      // mountpointservice (ACL
                      // service?)
                      userMailbox.revokeAccess(operationContext, itemId.getId(), granteeId);
                      mountpointService.deleteMountpointsByIds(
                          granteeId, granteeOpContext, mountPointIds);
                    }));
  }
}
