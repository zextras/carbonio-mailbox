// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.util.Collection;
import java.util.List;

import com.zimbra.common.service.ServiceException;

public interface MailboxStore {
    String getAccountId() throws ServiceException;
    long getSize() throws ServiceException;
    FolderStore getFolderByPath(OpContext octxt, String path) throws ServiceException;
    FolderStore getFolderById(OpContext octxt, String id) throws ServiceException;
    ExistingParentFolderStoreAndUnmatchedPart getParentFolderStoreAndUnmatchedPart(OpContext octxt, String path)
    throws ServiceException;
    /**
     * Copies the items identified in {@link idlist} to folder {@link targetFolder}
     * @param idlist - list of item ids for items to copy
     * @param targetFolder - Destination folder
     * @return Item IDs of successfully copied items
     */
    List<String> copyItemAction(OpContext ctxt, ItemIdentifier targetFolder, List<ItemIdentifier> idlist)
            throws ServiceException;
    void createFolderForMsgs(OpContext octxt, String path) throws ServiceException;
    void renameFolder(OpContext octxt, FolderStore folder, String path) throws ServiceException;
    void deleteFolder(OpContext octxt, String itemId) throws ServiceException;
    void emptyFolder(OpContext octxt, String folderId, boolean removeSubfolders) throws ServiceException;
    void flagFolderAsSubscribed(OpContext ctxt, FolderStore folder) throws ServiceException;
    void flagFolderAsUnsubscribed(OpContext ctxt, FolderStore folder) throws ServiceException;
    List<FolderStore> getUserRootSubfolderHierarchy(OpContext ctxt) throws ServiceException;
    void modifyFolderGrant(OpContext ctxt, FolderStore folder, GrantGranteeType granteeType, String granteeId,
        String perms, String args) throws ServiceException;
    void modifyFolderRevokeGrant(OpContext ctxt, String folderId, String granteeId) throws ServiceException;
    /**
     * Delete <tt>MailItem</tt>s with given ids.  If there is no <tt>MailItem</tt> for a given id, that id is ignored.
     *
     * @param octxt operation context or {@code null}
     * @param itemIds item ids
     * @param nonExistingItems If not null, This gets populated with the item IDs of nonExisting items
     */
    void delete(OpContext octxt, List<Integer> itemIds, List<Integer> nonExistingItems) throws ServiceException;
    /** Resets the mailbox's "recent message count" to 0.  A message is considered "recent" if:
     *     (a) it's not a draft or a sent message, and
     *     (b) it was added since the last write operation associated with any SOAP session. */
    void resetRecentMessageCount(OpContext octxt) throws ServiceException;
    /** Acquire an in process lock relevant for this type of MailboxStore */
    void lock(boolean write);
    /** Release an in process lock relevant for this type of MailboxStore */
    void unlock();
    /** Returns the IDs of all items modified since a given change number.
     *  Will not return modified folders or tags; for these you need to call
     * @return a List of IDs of all caller-visible MailItems of the given type modified since the checkpoint
     */
    List<Integer> getIdsOfModifiedItemsInFolder(OpContext octxt, int lastSync, int folderId)
            throws ServiceException;
    /**
     * @return the item with the specified ID.
     */
    ZimbraMailItem getItemById(OpContext octxt, ItemIdentifier id, MailItemType type) throws ServiceException;
    void flagItemAsRead(OpContext octxt, ItemIdentifier itemId, MailItemType type) throws ServiceException;
    List<ZimbraMailItem> getItemsById(OpContext octxt, Collection<ItemIdentifier> ids) throws ServiceException;
    void alterTag(OpContext octxt, Collection<ItemIdentifier> ids, String tagName, boolean addTag)
            throws ServiceException;
    void setTags(OpContext octxt, Collection<ItemIdentifier> itemIds, int flags, Collection<String> tags)
            throws ServiceException;
    ZimbraSearchParams createSearchParams(String queryString);
    ZimbraQueryHitResults searchImap(OpContext octx, ZimbraSearchParams params) throws ServiceException;
    /**
     * Returns the change sequence number for the most recent transaction.  This will be either the change number
     * for the current transaction or, if no database changes have yet been made in this transaction, the sequence
     * number for the last committed change.
     */
    int getLastChangeID();
    List<Integer> resetImapUid(OpContext octxt, List<Integer> itemIds) throws ServiceException;
    void noOp() throws ServiceException;
}
