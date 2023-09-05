package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.MountpointManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

public class RevokeFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final MountpointManager mountpointManager;
  private final ItemIdFactory itemIdFactory;
  private final OperationContextFactory operationContextFactory;

  @Inject
  public RevokeFolderActionUseCase(
      MailboxManager mailboxManager,
      MountpointManager mountpointManager,
      ItemIdFactory itemIdFactory,
      OperationContextFactory operationContextFactory) {
    this.mailboxManager = mailboxManager;
    this.mountpointManager = mountpointManager;
    this.itemIdFactory = itemIdFactory;
    this.operationContextFactory = operationContextFactory;
  }

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
              mountpointManager.getMountpointsByPath(
                  granteeMailbox, granteeContext, granteeRootFolderId);

          List<Integer> brokenMountsIds =
              mountpointManager.filterMountpointsByOwnerIdAndRemoteFolderId(
                  granteeMountpoints, accountId, folderId);

          mountpointManager.deleteMountpoints(granteeMailbox, granteeContext, brokenMountsIds);
        });
  }
}
