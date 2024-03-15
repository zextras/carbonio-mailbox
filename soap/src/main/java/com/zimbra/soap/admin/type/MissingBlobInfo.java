// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class MissingBlobInfo {

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AdminConstants.A_ID /* id */, required=true)
    private final int id;

    /**
     * @zm-api-field-tag revision
     * @zm-api-field-description revision
     */
    @XmlAttribute(name=AdminConstants.A_REVISION /* rev */, required=true)
    private final int revision;

    /**
     * @zm-api-field-tag data-size
     * @zm-api-field-description Data size
     */
    @XmlAttribute(name=AdminConstants.A_SIZE /* s */, required=true)
    private final long size;

    /**
     * @zm-api-field-tag volume-id
     * @zm-api-field-description Volume ID
     */
    @XmlAttribute(name=AdminConstants.A_VOLUME_ID /* volumeId */, required=true)
    private final short volumeId;

    /**
     * @zm-api-field-tag blob-path
     * @zm-api-field-description Blob path
     */
    @XmlAttribute(name=AdminConstants.A_BLOB_PATH /* blobPath */, required=true)
    private final String blobPath;

    /**
     * @zm-api-field-tag external-flag
     * @zm-api-field-description Set if the blob is stored in an ExternalStoreManager rather than locally in FileBlobStore
     */
    @XmlAttribute(name=AdminConstants.A_EXTERNAL /* external */, required=true)
    private final ZmBoolean external;

    /**
     * @zm-api-field-tag version
     * @zm-api-field-description Version
     */
    @XmlAttribute(name=AdminConstants.A_VERSION_INFO_VERSION /* version */, required=true)
    private final int version;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MissingBlobInfo() {
        this(-1, -1, -1L, (short)-1, null, false, -1);
    }

    public MissingBlobInfo(int id, int revision, long size, short volumeId, String blobPath, boolean external, int version) {
        this.id = id;
        this.revision = revision;
        this.size = size;
        this.volumeId = volumeId;
        this.blobPath = blobPath;
        this.external = ZmBoolean.fromBool(external);
        this.version = version;
    }

    public int getId() { return id; }
    public int getRevision() { return revision; }
    public long getSize() { return size; }
    public short getVolumeId() { return volumeId; }
    public String getBlobPath() { return blobPath; }
    public boolean getExternal() { return ZmBoolean.toBool(external); }
    public int getVersion() { return version; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("revision", revision)
            .add("size", size)
            .add("volumeId", volumeId)
            .add("blobPath", blobPath)
            .add("external", external)
            .add("version", version);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
