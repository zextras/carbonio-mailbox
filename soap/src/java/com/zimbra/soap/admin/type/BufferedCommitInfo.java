// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class BufferedCommitInfo {

    /**
     * @zm-api-field-tag account-id
     * @zm-api-field-description Account ID
     */
    @XmlAttribute(name=AdminConstants.A_AID /* aid */, required=true)
    private final String aid;

    /**
     * @zm-api-field-tag commit-id
     * @zm-api-field-description Commit ID
     */
    @XmlAttribute(name=AdminConstants.A_CID /* cid */, required=true)
    private final String cid;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BufferedCommitInfo() {
        this((String) null, (String) null);
    }

    public BufferedCommitInfo(String aid, String cid) {
        this.aid = aid;
        this.cid = cid;
    }

    public String getAid() { return aid; }
    public String getCid() { return cid; }
}
