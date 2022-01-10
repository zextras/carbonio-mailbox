// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.mailbox.ItemIdentifier;
import com.zimbra.common.service.ServiceException;
import com.zimbra.soap.mail.type.Folder;

public class ZSharedFolder extends ZFolder {

    private final String targetId;
    /**
     * This represents a folder that has been shared with the owning mailbox.
     * It differs from a mountpoint in that the share has not necessarily been accepted,
     * so this folder is typically only visible via a mechanism similar to IMAP's
     * "/home/<username>/..." namespace mechanism.
     */
    public ZSharedFolder(Folder f, ZFolder parent, String targetId, ZMailbox mailbox) throws ServiceException {
        super(f, parent, mailbox);
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    public ItemIdentifier getTargetItemIdentifier() throws ServiceException {
        return new ItemIdentifier(targetId, null);
    }

    @Override
    public boolean isHidden() {
        if (getParent() == null) {
            return false;
        }
        return super.isHidden();
    }

    @Override public boolean inTrash() {
        return false;
    }

    @Override public String toString() {
        return String.format("[ZSharedFolder %s targeId=%s]", getPath(), targetId);
    }

}
