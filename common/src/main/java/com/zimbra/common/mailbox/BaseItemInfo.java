// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import com.zimbra.common.service.ServiceException;

public interface BaseItemInfo {
    MailItemType getMailItemType();
    /** @return item's ID.  IDs are unique within a Mailbox and are assigned in increasing
     * (though not necessarily gap-free) order. */
    int getIdInMailbox() throws ServiceException;
    /**
     * @return the UID the item is referenced by in the IMAP server.  Returns <tt>0</tt> for items that require
     * renumbering because of moves.
     * The "IMAP UID" will be the same as the item ID unless the item has been moved after the mailbox owner's first
     * IMAP session. */
    int getImapUid();
    /** Returns the "external" flag bitmask, which includes {@link Flag#BITMASK_UNREAD} when the item is unread. */
    int getFlagBitmask();
    String[] getTags();
    /** String representation of the item's folder ID */
    int getFolderIdInMailbox() throws ServiceException;
    /** ID of the account containing this item */
    String getAccountId() throws ServiceException;
}
