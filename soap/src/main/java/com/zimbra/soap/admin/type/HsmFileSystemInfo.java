// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.HsmConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class HsmFileSystemInfo {

    /**
     * @zm-api-field-tag size
     * @zm-api-field-description Size
     */
    @XmlAttribute(name=HsmConstants.A_SIZE /* size */, required=true)
    private final String size;

    /**
     * @zm-api-field-tag filesystem
     * @zm-api-field-description Filesystem
     */
    @XmlValue
    private String fileSystem;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private HsmFileSystemInfo() {
        this(null);
    }

    public HsmFileSystemInfo(String size) {
        this.size = size;
    }

    public void setFileSystem(String fileSystem) { this.fileSystem = fileSystem; }
    public String getSize() { return size; }
    public String getFileSystem() { return fileSystem; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("size", size)
            .add("fileSystem", fileSystem);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
