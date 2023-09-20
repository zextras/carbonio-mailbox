package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.midlewarepojo.GrantInput;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.accesscontrol.ACLHelper;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to update a folder.
 *
 * @author Yuliya Aheeva, Davide Polonio
 * @since 23.10.0
 */
public class UpdateFolderAction {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;
  private final ACLHelper aclHelper;

  @Inject
  public UpdateFolderAction(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory, ACLHelper aclHelper) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
    this.aclHelper = aclHelper;
  }

  /**
   * This method is used to update (set permissions, set color, set tags, set folder view, rename,
   * move) a folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param internalGrantExpiryString internal grant expiry to get ACL
   * @param guestGrantExpiryString guest grant expiry to get ACL
   * @param grantInputList list of {@link GrantInput}
   * @param newName new name to rename a folder
   * @param flags flags to tag a folder
   * @param color new color to set
   * @param view view to set
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> update(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final String internalGrantExpiryString,
      final String guestGrantExpiryString,
      final List<GrantInput> grantInputList,
      final String newName,
      final String flags,
      final byte color,
      final String view) {
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

          if (internalGrantExpiryString != null && guestGrantExpiryString != null) {
            final ACL acl =
                aclHelper.parseACL(
                    internalGrantExpiryString,
                    guestGrantExpiryString,
                    grantInputList,
                    view == null
                        ? userMailbox
                            .getFolderById(operationContext, itemId.getId())
                            .getDefaultView()
                        : MailItem.Type.of(view),
                    userMailbox.getAccount());

            userMailbox.setPermissions(operationContext, itemId.getId(), acl);
          }

          if (color >= 0) {
            userMailbox.setColor(operationContext, itemId.getId(), MailItem.Type.FOLDER, color);
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
          }

          if (folderItemId.getId() > 0) {
            userMailbox.move(
                operationContext, itemId.getId(), MailItem.Type.FOLDER, folderItemId.getId(), null);
          }
        });
  }
}
