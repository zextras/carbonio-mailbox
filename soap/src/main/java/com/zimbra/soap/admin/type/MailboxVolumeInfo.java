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

@XmlAccessorType(XmlAccessType.NONE)
public class MailboxVolumeInfo {

    /**
     * @zm-api-field-tag volume-id
     * @zm-api-field-description Volume ID
     */
    @XmlAttribute(name=AdminConstants.A_ID /* id */, required=true)
    private short id;

    /**
     * @zm-api-field-tag volume-type
     * @zm-api-field-description Volume type
     */
    @XmlAttribute(name=AdminConstants.A_VOLUME_TYPE /* type */, required=true)
    private short volumeType;

    /**
     * @zm-api-field-tag volume-rootpath
     * @zm-api-field-description Root of the mailbox data
     */
    @XmlAttribute(name=AdminConstants.A_VOLUME_ROOTPATH /* rootpath */, required=true)
    private String volumeRootpath;

    private MailboxVolumeInfo() {
    }

    private MailboxVolumeInfo(short id, short volumeType, String volumeRootpath) {
        setId(id);
        setVolumeType(volumeType);
        setVolumeRootpath(volumeRootpath);
    }

    public static MailboxVolumeInfo createForIdTypeAndRootpath(short id, short volumeType, String volumeRootpath) {
        return new MailboxVolumeInfo(id, volumeType, volumeRootpath);
    }

    public void setId(short id) { this.id = id; }
    public void setVolumeType(short volumeType) { this.volumeType = volumeType; }
    public void setVolumeRootpath(String volumeRootpath) { this.volumeRootpath = volumeRootpath; }
    public short getId() { return id; }
    public short getVolumeType() { return volumeType; }
    public String getVolumeRootpath() { return volumeRootpath; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("volumeType", volumeType)
            .add("volumeRootpath", volumeRootpath);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
