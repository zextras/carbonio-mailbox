// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.util.List;

public interface FolderStore extends BaseFolderInfo {
    /** Returns the folder's name.  Note that this is the folder's name (e.g. <code>"foo"</code>),
     * not its absolute pathname (e.g. <code>"/baz/bar/foo"</code>) for which, @see #getPath()
     */
    MailboxStore getMailboxStore();
    String getName();
    ItemIdentifier getFolderItemIdentifier();
    String getFolderIdAsString();
    boolean isHidden();
    boolean isDeletable();
    boolean hasSubfolders();
    boolean isInboxFolder();
    boolean isSearchFolder();
    boolean isContactsFolder();
    boolean isChatsFolder();
    boolean isSyncFolder();
    boolean isIMAPSubscribed();
    boolean inTrash();
    boolean isVisibleInImap(boolean displayMailFoldersOnly);
    List<ACLGrant> getACLGrants();
    int getUIDValidity();
    /** @return number of items in folder, including IMAP \Deleted item */
    int getImapMessageCount();
    /** @return number of unread items in folder, including IMAP \Deleted items */
    int getImapUnreadCount();
    /** Returns a counter that increments each time an item is added to the folder. */
    int getImapUIDNEXT();
    /** Returns the change number of the last time
     *  (a) an item was inserted into the folder or
     *  (b) an item in the folder had its flags or tags changed.
     *  This data is used to enable IMAP client synchronization via the CONDSTORE extension. */
    int getImapMODSEQ();
}
