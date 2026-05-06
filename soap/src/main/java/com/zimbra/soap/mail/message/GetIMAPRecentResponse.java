// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Return the count of recent items in the folder
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_IMAP_RECENT_RESPONSE)
public class GetIMAPRecentResponse {

    /**
     * @zm-api-field-tag num-recent
     * @zm-api-field-description Number of recent items
     */
    @XmlAttribute(name=MailConstants.A_NUM /* n */, required=true)
    private final int num;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetIMAPRecentResponse() {
        this(0);
    }

    public GetIMAPRecentResponse(int num) {
        this.num = num;
    }

    public int getNum() { return num; }

}
