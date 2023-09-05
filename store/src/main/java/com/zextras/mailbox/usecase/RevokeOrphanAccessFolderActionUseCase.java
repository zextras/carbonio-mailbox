package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.ldap.GrantType;
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

public class RevokeOrphanAccessFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public RevokeOrphanAccessFolderActionUseCase(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  public Try<Void> revokeOrphanAccess(
      OperationContext operationContext,
      String accountId,
      String folderId,
      String zimbraId,
      String grantType) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));
          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          SearchDirectoryOptions opts = new SearchDirectoryOptions();

          String query = "(" + ZAttrProvisioning.A_zimbraId + "=" + zimbraId + ")";
          opts.setFilterString(ZLdapFilterFactory.FilterId.SEARCH_GRANTEE, query);
          opts.setOnMaster(true); // search the grantee on LDAP master
          List<NamedEntry> entries = Provisioning.getInstance().searchDirectory(opts);

          if (!entries.isEmpty()) {
            throw ServiceException.INVALID_REQUEST("grantee " + zimbraId + " exists", null);
          }
          Mailbox.FolderNode rootNode = userMailbox.getFolderTree(operationContext, itemId, true);
          GrantType.fromGranteeTypeName(grantType)
              .mapTry(
                  type -> {
                    revokeOrphanGrants(operationContext, userMailbox, rootNode, zimbraId, type);
                    return null;
                  });
        });
  }

  private void revokeOrphanGrants(
      OperationContext octxt,
      Mailbox mbox,
      Mailbox.FolderNode node,
      String granteeId,
      GrantType type)
      throws ServiceException {
    if (node.getFolder() != null) {
      boolean canAdmin =
          (mbox.getEffectivePermissions(octxt, node.getFolder().getId(), MailItem.Type.FOLDER)
                  & ACL.RIGHT_ADMIN)
              != 0;
      if (canAdmin && node.getFolder().getACL() != null) {
        revokeAccess(octxt, mbox, node, granteeId, type, node.getFolder().getACL());
      }
    }

    for (Mailbox.FolderNode subNode : node.mSubfolders) {
      revokeOrphanGrants(octxt, mbox, subNode, granteeId, type);
    }
  }

  private static void revokeAccess(
      OperationContext octxt,
      Mailbox mbox,
      Mailbox.FolderNode node,
      String granteeId,
      GrantType type,
      ACL acl)
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
