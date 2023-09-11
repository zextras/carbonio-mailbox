package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

public class UpdateFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public UpdateFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  public Try<Void> update(
      OperationContext operationContext,
      String accountId,
      String folderId,
      String newName,
      String flags,
      byte color,
      String view,
      ACL acl) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          final ItemId folderItemId =
              itemIdFactory.create(
                  folderId == null ? "-1" : folderId,
                  operationContext.getmRequestedAccountId() != null
                      ? operationContext.getmRequestedAccountId()
                      : operationContext.getmAuthTokenAccountId());

          if (!folderItemId.belongsTo(userMailbox)) {
            throw ServiceException.INVALID_REQUEST("cannot move folder between mailboxes", null);
          } else if (folderId != null && folderItemId.getId() <= 0) {
            throw MailServiceException.NO_SUCH_FOLDER(folderItemId.getId());
          }

          // TODO:
          //          Element eAcl = action.getOptionalElement(MailConstants.E_ACL);
          //          ACL acl = null;
          //          if (eAcl != null) {
          //            acl =
          //                aclUtil.parseACL(
          //                    eAcl,
          //                    view == null
          //                        ? mbox.getFolderById(octxt, iid.getId()).getDefaultView()
          //                        : MailItem.Type.of(view),
          //                    mbox.getAccount());
          //          }

          if (color >= 0) {
            userMailbox.setColor(operationContext, itemId.getId(), MailItem.Type.FOLDER, color);
          }
          if (acl != null) {
            userMailbox.setPermissions(operationContext, itemId.getId(), acl);
          }
          if (flags != null) {
            userMailbox.setTags(
                operationContext,
                itemId.getId(),
                MailItem.Type.FOLDER,
                Flag.toBitmask(flags),
                null,
                null);
          }
          if (view != null) {
            userMailbox.setFolderDefaultView(
                operationContext, itemId.getId(), MailItem.Type.of(view));
          }
          if (newName != null) {
            userMailbox.rename(
                operationContext,
                itemId.getId(),
                MailItem.Type.FOLDER,
                newName,
                folderItemId.getId());
          } else if (folderItemId.getId() > 0) {
            userMailbox.move(
                operationContext, itemId.getId(), MailItem.Type.FOLDER, folderItemId.getId(), null);
          }
        });
  }
}
