// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.OctopusXmlConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=OctopusXmlConstants.E_GET_DOCUMENT_SHARE_URL_RESPONSE)
public class GetDocumentShareURLResponse {

    /**
     * @zm-api-field-tag url
     * @zm-api-field-description url
     */
    @XmlValue
    private String url;

    public void setUrl(String url) { this.url = url; }
    public String getUrl() { return url; }
}
