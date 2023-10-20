// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.account.usecase;

import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for operations on folders. Methods were extracted from {@link
 * com.zimbra.cs.account.ShareInfo}
 */
public class FolderUtil {

  private FolderUtil() {}

  /**
   * Flattens and sorts folder starting from the provided root. For example folders A > B > C, D, E
   * > F, G will become a {@link Set} of A,B,C,D,E,F,G
   *
   * @param root start folder
   * @return {@link Set} of {@link Folder}
   */
  public static Set<Folder> flattenAndSortFolderTree(final FolderNode root) {
    Set<Folder> folders = new HashSet<Folder>();
    flattenAndSortFolderTree(root, folders);
    return folders;
  }

  /**
   * Iterates folders starting from the current node and flattens recursively if node has
   * sub-folders.
   *
   * @param node {@link FolderNode}
   * @param flattened {@link Set} of already flattened {@link Folder} where to add new flattened
   *     values
   */
  private static void flattenAndSortFolderTree(FolderNode node, Set<Folder> flattened) {
    if (node.mFolder != null) {
      flattened.add(node.mFolder);
    }
    for (FolderNode subNode : node.mSubfolders) flattenAndSortFolderTree(subNode, flattened);
  }
}
