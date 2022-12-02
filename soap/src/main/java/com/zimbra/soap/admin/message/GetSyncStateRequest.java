// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.SyncAdminConstants;
import com.zimbra.common.soap.SyncConstants;
import com.zimbra.soap.admin.type.FolderId;
import com.zimbra.soap.admin.type.ItemId;
import com.zimbra.soap.sync.type.DeviceId;
import com.zimbra.soap.type.AccountSelector;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get the requested device folder's SyncState
 */
@XmlRootElement(name = SyncAdminConstants.E_GET_SYNC_STATE_REQUEST)
public class GetSyncStateRequest {
    /**
     * @zm-api-field-description Account
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=true)
    private final AccountSelector account;

    /**
     * @zm-api-field-description Device specification
     */
    @XmlElement(name = SyncConstants.E_DEVICE, required = true)
    private DeviceId deviceId;

    /**
     * @zm-api-field-description Folder specification
     */
    @XmlElement(name = SyncConstants.E_FOLDER, required = false)
    private FolderId folderId;

    @XmlElement(name = SyncConstants.E_ITEMS, required = false)
    private ItemId itemId;
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetSyncStateRequest() {
        this(null);
    }

    public GetSyncStateRequest(AccountSelector account) {
        this.account = account;
    }

    public DeviceId getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public AccountSelector getAccount() {
        return this.account;
    }

    public FolderId getFolderId() {
        return this.folderId;
    }

    public void setFolderId(FolderId folderId) {
        this.folderId = folderId;
    }

    public ItemId getItemId() {
        return itemId;
    }

    public void setItemId(ItemId itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("account", this.account).add("device", this.deviceId)
        .add("folder", this.folderId).toString();
    }
}
