package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import java.util.stream.Collectors;

public class MountpointManager {
  public void deleteMountpoints(
      Mailbox mailbox, OperationContext operationContext, List<Integer> mountpointsIds)
      throws ServiceException {
    try {
      ItemActionHelper.HARD_DELETE(
          operationContext, mailbox, SoapProtocol.Soap12, mountpointsIds, Type.MOUNTPOINT, null);
    } catch (Exception e) {
      throw ServiceException.FAILURE("Unable to delete Mountpoints", e.getCause());
    }
  }

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
