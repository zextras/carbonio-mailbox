package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zextras.mailbox.usecase.service.MountpointService;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to revoke access to a folder and remove revoked mountpoints.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class RevokeFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final MountpointService mountpointService;
  private final ItemIdFactory itemIdFactory;
  private final OperationContextFactory operationContextFactory;

  @Inject
  public RevokeFolderActionUseCase(
      MailboxManager mailboxManager,
      MountpointService mountpointService,
      ItemIdFactory itemIdFactory,
      OperationContextFactory operationContextFactory) {
    this.mailboxManager = mailboxManager;
    this.mountpointService = mountpointService;
    this.itemIdFactory = itemIdFactory;
    this.operationContextFactory = operationContextFactory;
  }

  /**
   * This method is used to revoke access from a folder. Revokes access, finds grantee mountpoints,
   * deletes linked to revoked folder mountpoints.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param granteeId grantee account zimbraId attribute
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> revoke(
      OperationContext operationContext, String accountId, String folderId, String granteeId) {

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

          Account granteeAccount = Provisioning.getInstance().getAccountById(granteeId);
          OperationContext granteeContext = operationContextFactory.create(granteeAccount);
          Mailbox granteeMailbox =
              MailboxManager.getInstance().getMailboxByAccountId(granteeId, false);
          ItemId granteeRootFolderId =
              itemIdFactory.create(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeId);

          List<Mountpoint> granteeMountpoints =
              mountpointService.getMountpointsByPath(
                  granteeMailbox, granteeContext, granteeRootFolderId);

          List<Integer> brokenMountsIds =
              mountpointService.filterMountpointsByOwnerIdAndRemoteFolderId(
                  granteeMountpoints, accountId, folderId);

          mountpointService.deleteMountpoints(granteeMailbox, granteeContext, brokenMountsIds);
        });
  }
}
