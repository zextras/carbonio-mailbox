// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class DirPathInfo {

    /**
     * @zm-api-field-tag path
     * @zm-api-field-description Path
     */
    @XmlAttribute(name=AdminConstants.A_PATH /* path */, required=true)
    private final String path;
    /**
     * @zm-api-field-tag path-exists
     * @zm-api-field-description Flag whether exists
     */
    @XmlAttribute(name=AdminConstants.A_EXISTS /* exists */, required=true)
    private final ZmBoolean exists;
    /**
     * @zm-api-field-tag path-is-directory
     * @zm-api-field-description Flag whether is directory
     */
    @XmlAttribute(name=AdminConstants.A_IS_DIRECTORY /* isDirectory */, required=true)
    private final ZmBoolean directory;
    /**
     * @zm-api-field-tag path-is-readable
     * @zm-api-field-description
     */
    @XmlAttribute(name=AdminConstants.A_READABLE /* readable */, required=true)
    private final ZmBoolean readable;
    /**
     * @zm-api-field-tag path-is-writable
     * @zm-api-field-description
     */
    @XmlAttribute(name=AdminConstants.A_WRITABLE /* writeable */, required=true)
    private final ZmBoolean writable;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DirPathInfo() {
        this((String)null, false, false, false, false);
    }

    public DirPathInfo(String path, boolean exists, boolean directory,
            boolean readable, boolean writable) {
        this.path = path;
        this.exists = ZmBoolean.fromBool(exists);
        this.directory = ZmBoolean.fromBool(directory);
        this.readable = ZmBoolean.fromBool(readable);
        this.writable = ZmBoolean.fromBool(writable);
    }

    public String getPath() { return path; }
    public boolean isExists() { return ZmBoolean.toBool(exists); }
    public boolean isDirectory() { return ZmBoolean.toBool(directory); }
    public boolean isReadable() { return ZmBoolean.toBool(readable); }
    public boolean isWritable() { return ZmBoolean.toBool(writable); }
}
