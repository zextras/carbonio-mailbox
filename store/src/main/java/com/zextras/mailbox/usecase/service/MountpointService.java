package com.zextras.mailbox.usecase.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Service class to perform operations on {@link Mountpoint}.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class MountpointService {
  private final MailboxManager mailboxManager;

  @Inject
  public MountpointService(MailboxManager mailboxManager) {
    this.mailboxManager = mailboxManager;
  }

  /**
   * This method is used to delete mountpoints by ids.
   *
   * @param ownerId {@link Mailbox} owner of mountpoints
   * @param operationContext {@link OperationContext} of a user who has access to perform delete
   *     operation (the owner of mountpoints or an admin)
   * @param mountpointsIds list of mountpoint ids to delete, all should belong to one owner (grantee
   *     user)
   * @throws ServiceException if unable to delete mountpoints
   */
  public void deleteMountpointsByIds(
      String ownerId, OperationContext operationContext, List<Integer> mountpointsIds)
      throws ServiceException {
    try {
      ItemActionHelper.HARD_DELETE(
          operationContext,
          mailboxManager.getMailboxByAccountId(ownerId),
          SoapProtocol.Soap12,
          mountpointsIds,
          Type.MOUNTPOINT,
          null);
    } catch (Exception e) {
      throw ServiceException.FAILURE("Unable to delete Mountpoints", e.getCause());
    }
  }

  /**
   * This method is used to get mountpoints from a mailbox folder. Search is not recursive, it
   * returns mountpoints at first level order.
   *
   * @param ownerId {@link Mailbox} owner of mailbox hosting the mountpoints
   * @param operationContext {@link OperationContext} of a user who has access to folder tree (the
   *     owner of folder tree or an admin)
   * @param rootFolderId {@link ItemId} the directory where to the search for mountpoints
   * @return list of found {@link Mountpoint}
   * @throws ServiceException if unable to get folder tree
   */
  public List<Mountpoint> getMountpointsByPath(
      String ownerId, OperationContext operationContext, ItemId rootFolderId)
      throws ServiceException {

    FolderNode folderTree =
        mailboxManager
            .getMailboxByAccountId(ownerId)
            .getFolderTree(operationContext, rootFolderId, false);
    List<Mailbox.FolderNode> granteeSubFolders = folderTree.getSubFolders();

    return granteeSubFolders.stream()
        .map(FolderNode::getFolder)
        .filter(Mountpoint.class::isInstance)
        .map(Mountpoint.class::cast)
        .collect(Collectors.toList());
  }
}
