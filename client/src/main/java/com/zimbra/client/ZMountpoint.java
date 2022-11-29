// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import org.json.JSONException;

import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.client.event.ZModifyFolderEvent;
import com.zimbra.client.event.ZModifyMountpointEvent;
import com.zimbra.common.mailbox.FolderConstants;
import com.zimbra.common.mailbox.ItemIdentifier;
import com.zimbra.common.mailbox.MountpointStore;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.mail.type.Mountpoint;

public class ZMountpoint extends ZFolder implements MountpointStore {

    private String mOwnerId;
    private String mOwnerDisplayName;
    private String mRemoteId;

    public ZMountpoint(Element e, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        super(e, parent, mailbox);
        mOwnerDisplayName = e.getAttribute(MailConstants.A_OWNER_NAME, null);
        mRemoteId = e.getAttribute(MailConstants.A_REMOTE_ID);
        mOwnerId = e.getAttribute(MailConstants.A_ZIMBRA_ID);
    }

    public ZMountpoint(Mountpoint m, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        super(m, parent, mailbox);
        mOwnerDisplayName = m.getOwnerEmail();
        mRemoteId = Integer.toString(m.getRemoteFolderId());
        mOwnerId = m.getOwnerAccountId();
    }

    @Override
    public void modifyNotification(ZModifyEvent e) throws ServiceException {
        if (e instanceof ZModifyMountpointEvent) {
            ZModifyMountpointEvent mpe = (ZModifyMountpointEvent) e;
            if (mpe.getId().equals(getId())) {
                mOwnerDisplayName = mpe.getOwnerDisplayName(mOwnerDisplayName);
                mRemoteId = mpe.getRemoteId(mRemoteId);
                mOwnerId = mpe.getOwnerId(mOwnerId);
                super.modifyNotification(e);
            }
        } else if (e instanceof ZModifyFolderEvent) {
            super.modifyNotification(e);
        }
    }

    @Override
    public String toString() {
        return String.format("[ZMountpoint %s]", getPath());
    }

    @Override
    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject jo = super.toZJSONObject();
        jo.put("ownerId", mOwnerId);
        jo.put("ownerDisplayName", mOwnerDisplayName);
        jo.put("remoteId", mRemoteId);
        return jo;
    }

    /**
     * @return primary email address of the owner of the mounted resource
     */
    public String getOwnerDisplayName() {
        return mOwnerDisplayName;
    }

    /**
     * @return zimbra id of the owner of the mounted resource
     */
    public String getOwnerId() {
        return mOwnerId;
    }

    /**
     * @return remote folder id of the mounted folder
     */
    public String getRemoteId() {
        return mRemoteId;
    }

    /**
     *
     * @return the canonical remote id: {owner-id}:{remote-id}
     */
    public String getCanonicalRemoteId() {
        return mOwnerId+":"+mRemoteId;
    }

    @Override
    public ItemIdentifier getTargetItemIdentifier() {
        try {
            return ItemIdentifier.fromOwnerAndRemoteId(mOwnerId, mRemoteId);
        } catch (ServiceException e) {
            // This should never happen...
            ZimbraLog.mailbox.debug("Unexpected failure to generate Target ItemIdentfier for owner=%s remoteID=%s",
                    mOwnerId, mRemoteId, e);
            return new ItemIdentifier(null, FolderConstants.ID_AUTO_INCREMENT);
        }
    }
}
