package com.zextras.mailbox.usecase.service;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
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
  private final ItemIdFactory itemIdFactory;
  private final OperationContextFactory operationContextFactory;
  private final Provisioning provisioning;

  @Inject
  public MountpointService(
      ItemIdFactory itemIdFactory,
      OperationContextFactory operationContextFactory,
      Provisioning provisioning) {
    this.itemIdFactory = itemIdFactory;
    this.operationContextFactory = operationContextFactory;
    this.provisioning = provisioning;
  }

  /**
   * @param accountId
   * @param folderId
   * @param granteeId
   * @return
   */
  public Try<Void> deleteMountpoints(
      final String accountId, final String folderId, final String granteeId) {
    return Try.run(
        () -> {
          final Account granteeAccount = provisioning.getAccountById(granteeId);
          final OperationContext granteeContext = operationContextFactory.create(granteeAccount);
          final Mailbox granteeMailbox =
              MailboxManager.getInstance().getMailboxByAccountId(granteeId, false);
          final ItemId granteeRootFolderId =
              itemIdFactory.create(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeId);

          final List<Mountpoint> granteeMountpoints =
              getMountpointsByPath(granteeMailbox, granteeContext, granteeRootFolderId);

          final List<Integer> brokenMountsIds =
              filterMountpointsByOwnerIdAndRemoteFolderId(granteeMountpoints, accountId, folderId);

          deleteMountpointsByIds(granteeMailbox, granteeContext, brokenMountsIds);
        });
  }

  /**
   * This method is used to delete mountpoints by ids.
   *
   * @param mailbox {@link Mailbox} of the grantee user (the owner of the mountpoints)
   * @param operationContext {@link OperationContext} of a user who has access to perform delete
   *     operation (the owner of mountpoints or an admin)
   * @param mountpointsIds list of mountpoint ids to delete, all should belong to one owner (grantee
   *     user)
   * @throws ServiceException if unable to delete mountpoints
   */
  public void deleteMountpointsByIds(
      Mailbox mailbox, OperationContext operationContext, List<Integer> mountpointsIds)
      throws ServiceException {
    try {
      ItemActionHelper.HARD_DELETE(
          operationContext, mailbox, SoapProtocol.Soap12, mountpointsIds, Type.MOUNTPOINT, null);
    } catch (Exception e) {
      throw ServiceException.FAILURE("Unable to delete Mountpoints", e.getCause());
    }
  }

  /**
   * This method is used to get mountpoints for a grantee user.
   *
   * @param mailbox {@link Mailbox} of the grantee user (the owner of the mountpoints)
   * @param operationContext {@link OperationContext} of a user who has access to folder tree (the
   *     owner of folder tree or an admin)
   * @param rootFolderId {@link ItemId} of the root folder consists of {@link
   *     Mailbox.ID_FOLDER_USER_ROOT} and grantee id
   * @return list of found {@link Mountpoint}
   * @throws ServiceException if unable to get folder tree
   */
  public List<Mountpoint> getMountpointsByPath(
      Mailbox mailbox, OperationContext operationContext, ItemId rootFolderId)
      throws ServiceException {

    FolderNode folderTree = mailbox.getFolderTree(operationContext, rootFolderId, false);
    List<Mailbox.FolderNode> granteeSubFolders = folderTree.getSubFolders();

    return granteeSubFolders.stream()
        .map(FolderNode::getFolder)
        .filter(Mountpoint.class::isInstance)
        .map(Mountpoint.class::cast)
        .collect(Collectors.toList());
  }

  /**
   * Filters mountpoints by grantor id and his folder id.
   *
   * @param mountpoints to filter
   * @param ownerId grantor id
   * @param remoteFolderId folder id which belongs to a grantor
   * @return list of mountpoint ids that pass the filter
   */
  public List<Integer> filterMountpointsByOwnerIdAndRemoteFolderId(
      List<Mountpoint> mountpoints, String ownerId, String remoteFolderId) {
    return mountpoints.stream()
        .filter(
            mpt ->
                mpt.getOwnerId().equals(ownerId)
                    && String.valueOf(mpt.getRemoteId()).equals(remoteFolderId))
        .map(MailItem::getId)
        .collect(Collectors.toList());
  }
}
