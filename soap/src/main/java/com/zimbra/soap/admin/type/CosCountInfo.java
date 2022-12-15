// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AdminConstants;

/**
 * Used by {@CountAccountResponse}
 */
@XmlAccessorType(XmlAccessType.NONE)
public class CosCountInfo {

    /**
     * @zm-api-field-tag cos-name
     * @zm-api-field-description Class Of Service (COS) name
     */
    @XmlAttribute(name=AdminConstants.A_NAME, required=true)
    private final String name;

    /**
     * @zm-api-field-tag cos-id
     * @zm-api-field-description Class Of Service (COS) ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-tag account-count
     * @zm-api-field-description Account count.  Note, it doesn't include any account with
     * <b>zimbraIsSystemResource=TRUE</b>, nor does it include any calendar resources.
     */
    @XmlValue
    private final long value;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CosCountInfo() {
        this(null, null, 0);
    }

    public CosCountInfo(String id, String name, long value) {
        this.name = name;
        this.id = id;
        this.value = value;
    }

    public String getName() { return name; }
    public String getId() { return id; }
    public long getValue() { return value; }
}
