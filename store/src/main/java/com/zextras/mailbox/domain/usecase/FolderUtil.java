// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.domain.usecase;

import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import java.util.HashSet;
import java.util.Set;

public class FolderUtil {

  public static Set<Folder> flattenAndSortFolderTree(FolderNode root) {
    Set<Folder> folders = new HashSet<Folder>();
    flattenAndSortFolderTree(root, folders);
    return folders;
  }

  private static void flattenAndSortFolderTree(FolderNode node, Set<Folder> flattened) {
    if (node.mFolder != null) flattened.add(node.mFolder);
    for (FolderNode subNode : node.mSubfolders) flattenAndSortFolderTree(subNode, flattened);
  }
}
