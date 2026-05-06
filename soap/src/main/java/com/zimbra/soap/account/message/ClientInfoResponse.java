// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.Collection;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.Attr;

/**
 * @zm-api-response-description Provides a limited amount of information the client may require about the requested hostname.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_CLIENT_INFO_RESPONSE)
public class ClientInfoResponse {

    /**
     * @zm-api-field-description Attributes
     */
    @XmlElement(name=AdminConstants.E_A /* a */, required=false)
    private Collection<Attr> attrList;

    public ClientInfoResponse () {
        this(null);
    }
    public ClientInfoResponse(Collection<Attr> attrList) {
        this.attrList = attrList;
    }

    /**
     * @return the attrList
     */
    public Collection<Attr> getAttrList() {
        return attrList;
    }

    /**
     * @param attrList the attrList to set
     */
    public void setAttrList(Collection<Attr> attrList) {
        this.attrList = attrList;
    }
}
