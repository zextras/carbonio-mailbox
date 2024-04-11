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
public class CheckDirSelector {

    /**
     * @zm-api-field-tag full-path
     * @zm-api-field-description Full path to the directory
     */
    @XmlAttribute(name=AdminConstants.A_PATH, required=true)
    private final String path;

    /**
     * @zm-api-field-tag create-if-nec-flag
     * @zm-api-field-description Whether to create the directory or not if it doesn't exist
     */
    @XmlAttribute(name=AdminConstants.A_CREATE, required=false)
    private final ZmBoolean create;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CheckDirSelector() {
        this(null, null);
    }

    public CheckDirSelector(String path) {
        this(path, null);
    }

    public CheckDirSelector(String path, Boolean create) {
        this.path = path;
        this.create = ZmBoolean.fromBool(create);
    }

    public String getPath() { return path; }
    public Boolean isCreate() { return ZmBoolean.toBool(create); }
}
