// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AccountConstants;

@XmlType(propOrder = {})
public class SignatureContent {

    /**
     * @zm-api-field-tag signature-content-type
     * @zm-api-field-description Content Type - <b>"text/plain"</b> or <b>"text/html"</b>
     */
    @XmlAttribute(name=AccountConstants.A_TYPE)
    private String contentType;

    /**
     * @zm-api-field-tag signature-value
     * @zm-api-field-description Signature value
     */
    @XmlValue private String content;

    public SignatureContent() {
    }

    public SignatureContent(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
