// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@JsonPropertyOrder({ "ownerId", "ownerEmail", "ownerName", "folderId", "folderUuid", "folderPath", "view", "rights",
    "granteeType", "granteeId", "granteeName", "granteeDisplayName", "mid" })
public class ShareInfo {

    /**
     * @zm-api-field-tag share-owner-id
     * @zm-api-field-description Owner ID
     */
    @XmlAttribute(name=AccountConstants.A_OWNER_ID /* ownerId */, required=true)
    private String ownerId;

    /**
     * @zm-api-field-tag share-owner-email
     * @zm-api-field-description Owner email
     */
    @XmlAttribute(name=AccountConstants.A_OWNER_EMAIL /* ownerEmail */, required=true)
    private String ownerEmail;

    /**
     * @zm-api-field-tag share-owner-display-name
     * @zm-api-field-description Owner display name
     */
    @XmlAttribute(name=AccountConstants.A_OWNER_DISPLAY_NAME /* ownerName */, required=true)
    private String ownerDisplayName;

    /**
     * @zm-api-field-tag share-folder-id
     * @zm-api-field-description Folder ID
     */
    @XmlAttribute(name=AccountConstants.A_FOLDER_ID /* folderId */, required=true)
    private int folderId;

    /**
     * @zm-api-field-tag share-folder-uuid
     * @zm-api-field-description Folder UUID
     */
    @XmlAttribute(name=AccountConstants.A_FOLDER_UUID /* folderUuid */, required=true)
    private String folderUuid;

    /**
     * @zm-api-field-tag share-fully-qualified-path
     * @zm-api-field-description Fully qualified path
     */
    @XmlAttribute(name=AccountConstants.A_FOLDER_PATH /* folderPath */, required=true)
    private String folderPath;

    /**
     * @zm-api-field-tag share-default-view
     * @zm-api-field-description Default type
     */
    @XmlAttribute(name=MailConstants.A_DEFAULT_VIEW /* view */, required=true)
    private String defaultView;

    /**
     * @zm-api-field-tag share-rights
     * @zm-api-field-description Rights
     */
    @XmlAttribute(name=AccountConstants.A_RIGHTS /* rights */, required=true)
    private String rights;

    /**
     * @zm-api-field-tag grantee-type
     * @zm-api-field-description Grantee type
     */
    @XmlAttribute(name=AccountConstants.A_GRANTEE_TYPE /* granteeType */, required=true)
    private String granteeType;

    /**
     * @zm-api-field-tag grantee-id
     * @zm-api-field-description Grantee ID
     */
    @XmlAttribute(name=AccountConstants.A_GRANTEE_ID /* granteeId */, required=true)
    private String granteeId;

    /**
     * @zm-api-field-tag grantee-name
     * @zm-api-field-description Grantee name
     */
    @XmlAttribute(name=AccountConstants.A_GRANTEE_NAME /* granteeName */, required=true)
    private String granteeName;

    /**
     * @zm-api-field-tag grantee-display-name
     * @zm-api-field-description Grantee display name
     */
    @XmlAttribute(name=AccountConstants.A_GRANTEE_DISPLAY_NAME /* granteeDisplayName */, required=true)
    private String granteeDisplayName;

    /**
     * @zm-api-field-tag mountpoint-id
     * @zm-api-field-description Returned if the share is already mounted.  Contains the folder id of the mountpoint
     * in the local mailbox.
     */
    @XmlAttribute(name=AccountConstants.A_MOUNTPOINT_ID /* mid */, required=false)
    private String mountpointId;

    public ShareInfo() {
    }

    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerId() { return ownerId; }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerEmail() { return ownerEmail; }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public String getOwnerDisplayName() { return ownerDisplayName; }

    public void setFolderId(int folderId) { this.folderId = folderId; }

    public int getFolderId() { return folderId; }

    public void setFolderUuid(String folderUuid) { this.folderUuid = folderUuid; }

    public String getFolderUuid() { return folderUuid; }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() { return folderPath; }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }

    public String getDefaultView() { return defaultView; }

    public void setRights(String rights) { this.rights = rights; }

    public String getRights() { return rights; }

    public void setGranteeType(String granteeType) {
        this.granteeType = granteeType;
    }

    public String getGranteeType() { return granteeType; }

    public void setGranteeId(String granteeId) {
        this.granteeId = granteeId;
    }

    public String getGranteeId() { return granteeId; }

    public void setGranteeName(String granteeName) {
        this.granteeName = granteeName;
    }

    public String getGranteeName() { return granteeName; }

    public void setGranteeDisplayName(String granteeDisplayName) {
        this.granteeDisplayName = granteeDisplayName;
    }

    public String getGranteeDisplayName() { return granteeDisplayName; }

    public void setMountpointId(String mountpointId) {
        this.mountpointId = mountpointId;
    }

    public String getMountpointId() { return mountpointId; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ownerId", ownerId)
            .add("ownerEmail", ownerEmail)
            .add("ownerDisplayName", ownerDisplayName)
            .add("folderId", folderId)
            .add("folderUuid", folderUuid)
            .add("folderPath", folderPath)
            .add("defaultView", defaultView)
            .add("rights", rights)
            .add("granteeType", granteeType)
            .add("granteeId", granteeId)
            .add("granteeName", granteeName)
            .add("granteeDisplayName", granteeDisplayName)
            .add("mountpointId", mountpointId)
            .toString();
    }
}
