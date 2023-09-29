// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.domain.usecase;

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

/** Class to manage {@link com.zimbra.cs.mailbox.ACL.Grant} */
public class GrantsService {

  private final Logger logger = LoggerFactory.getLogger(GrantsService.class);

  private final MailboxManager mailboxManager;
  private final Provisioning provisioning;

  public GrantsService(MailboxManager mailboxManager, Provisioning provisioning) {
    this.mailboxManager = mailboxManager;
    this.provisioning = provisioning;
  }

  /**
   * Removes all Grants targeting the user from LDAP and the user Mailbox
   *
   * @param operationContext {@link OperationContext} for the operation
   * @param targetAccountId account id identifying the target {@link com.zimbra.cs.account.Account}
   * @throws ServiceException
   */
  public void revokeAllGrantsForAccountId(
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
    provisioning.modifyAttrs(
        provisioning.getAccountById(targetAccountId),
        Collections.singletonMap(Provisioning.A_zimbraACE, List.of()));
  }

  public Grants getLDAPGrantsForAccountId(String accountId)
      throws ServiceException {
    return provisioning.getGrants(
        TargetType.account.getCode(), TargetBy.id, accountId, null, null, null, false);
  }

  public List<ACLGrant> getAllFolderGrantsForAccountId(
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
