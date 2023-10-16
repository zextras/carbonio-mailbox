// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.acl;

import com.zextras.mailbox.account.usecase.FolderUtil;
import com.zimbra.common.mailbox.ACLGrant;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightCommand.Grants;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.type.TargetBy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage grants. It manages Grants applied on a {@link Mailbox}, see: {@link
 * com.zimbra.cs.mailbox.ACL.Grant} and Grants applied on Entities on Ldap, see: {@link
 * com.zimbra.cs.account.accesscontrol.ZimbraACE}
 *
 * @since 23.11.0
 * @author davidefrison
 */
public class AclService {

  private final Logger logger = LoggerFactory.getLogger(AclService.class);

  private final MailboxManager mailboxManager;
  private final Provisioning provisioning;

  public AclService(MailboxManager mailboxManager, Provisioning provisioning) {
    this.mailboxManager = mailboxManager;
    this.provisioning = provisioning;
  }

  /**
   * Removes all Grants targeting an account.
   *
   * @param targetAccountId account id identifying the target {@link com.zimbra.cs.account.Account}
   * @throws ServiceException
   */
  public void revokeAllGrantsForAccountId(final String targetAccountId) throws ServiceException {
    provisioning.modifyAttrs(
        provisioning.getAccountById(targetAccountId),
        Collections.singletonMap(Provisioning.A_zimbraACE, List.of()));
  }

  /**
   * Revoke all Grants on Mailbox Folders owned by the requested account.
   *
   * @param operationContext operation details information
   * @param targetAccountId owner of the mailbox
   * @throws ServiceException
   */
  public void revokeAllMailboxGrantsForAccountId(
      final OperationContext operationContext, final String targetAccountId)
      throws ServiceException {
    final Mailbox targetMailbox = mailboxManager.getMailboxByAccountId(targetAccountId);
    final FolderNode rootFolder = targetMailbox.getFolderTree(operationContext, null, false);
    final Set<Folder> allFolders = FolderUtil.flattenAndSortFolderTree(rootFolder);
    allFolders.forEach(
        folder ->
            folder
                .getACLGrants()
                .forEach(
                    grant -> {
                      try {
                        targetMailbox.revokeAccess(
                            operationContext, folder.getId(), grant.getGranteeId());
                      } catch (ServiceException e) {
                        logger.error(
                            "Cannot revoke grant for target {}: {}",
                            targetAccountId,
                            e.getMessage());
                      }
                    }));
  }

  /**
   * Returns all the grants targeting a user.
   *
   * @param accountId id of the target account
   * @return {@link Grants} targeting the account
   * @throws ServiceException
   */
  public Grants getGrantsTargetingAccount(String accountId) throws ServiceException {
    return provisioning.getGrants(
        TargetType.account.getCode(), TargetBy.id, accountId, null, null, null, false);
  }

  /**
   * Returns all grants on all Mailbox folders owned by an account.
   *
   * @param operationContext identifies the operation
   * @param targetAccountId target account owning the folders
   * @return a list of {@link ACLGrant} for the folder is the mailbox identified by the account id
   * @throws ServiceException
   */
  public List<ACLGrant> getMailboxFolderGrantsForAccountId(
      OperationContext operationContext, final String targetAccountId) throws ServiceException {
    final Mailbox grantorMailbox = mailboxManager.getMailboxByAccountId(targetAccountId);

    final FolderNode rootFolder = grantorMailbox.getFolderTree(operationContext, null, false);
    final Set<Folder> allFolders = FolderUtil.flattenAndSortFolderTree(rootFolder);
    return allFolders.stream()
        .map(Folder::getACLGrants)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }
}
