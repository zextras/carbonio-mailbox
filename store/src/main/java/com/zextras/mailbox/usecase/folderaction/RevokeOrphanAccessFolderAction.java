package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.util.GrantType;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to revoke orphan access from a folder. "Orphan grant" is a grant whose grantee
 * object is deleted/non-existing.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class RevokeOrphanAccessFolderAction {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public RevokeOrphanAccessFolderAction(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to revoke orphan access.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param zimbraId folder zimbraId attribute
   * @param grantType grant type
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> revokeOrphanAccess(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final String zimbraId,
      final String grantType) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));
          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          final SearchDirectoryOptions searchDirectoryOptions = new SearchDirectoryOptions();

          final String query = "(" + ZAttrProvisioning.A_zimbraId + "=" + zimbraId + ")";
          searchDirectoryOptions.setFilterString(ZLdapFilterFactory.FilterId.SEARCH_GRANTEE, query);
          searchDirectoryOptions.setOnMaster(true); // search the grantee on LDAP master
          final List<NamedEntry> entries =
              Provisioning.getInstance().searchDirectory(searchDirectoryOptions);

          if (!entries.isEmpty()) {
            throw ServiceException.INVALID_REQUEST("grantee " + zimbraId + " exists", null);
          }

          final Mailbox.FolderNode rootNode =
              userMailbox.getFolderTree(operationContext, itemId, true);
          GrantType.fromGranteeTypeName(grantType)
              .mapTry(
                  type -> {
                    revokeOrphanGrants(operationContext, userMailbox, rootNode, zimbraId, type);
                    return null;
                  });
        });
  }

  private void revokeOrphanGrants(
      final OperationContext octxt,
      final Mailbox mbox,
      final Mailbox.FolderNode node,
      final String granteeId,
      final GrantType type)
      throws ServiceException {
    if (node.getFolder() != null) {
      final boolean userCanAdministerFolder =
          (mbox.getEffectivePermissions(octxt, node.getFolder().getId(), MailItem.Type.FOLDER)
                  & ACL.RIGHT_ADMIN)
              != 0;
      if (userCanAdministerFolder && node.getFolder().getACL() != null) {
        revokeAccess(octxt, mbox, node, granteeId, type, node.getFolder().getACL());
      }
    }

    for (Mailbox.FolderNode subNode : node.mSubfolders) {
      revokeOrphanGrants(octxt, mbox, subNode, granteeId, type);
    }
  }

  private static void revokeAccess(
      final OperationContext octxt,
      final Mailbox mbox,
      final Mailbox.FolderNode node,
      final String granteeId,
      final GrantType type,
      final ACL acl)
      throws ServiceException {
    for (ACL.Grant grant : acl.getGrants()) {
      if (granteeId.equals(grant.getGranteeId())
          && type.getGranteeNumber() == grant.getGranteeType()) {
        mbox.revokeAccess(octxt, node.getFolder().getId(), granteeId);
        break;
      }
    }
  }
}
