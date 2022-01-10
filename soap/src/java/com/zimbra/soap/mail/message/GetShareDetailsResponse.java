// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.ShareDetails;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=OctopusXmlConstants.E_GET_SHARE_DETAILS_RESPONSE)
public class GetShareDetailsResponse {

    /**
     * @zm-api-field-tag share-details
     * @zm-api-field-description Details on the item being shared
     */
    @XmlElement(name=MailConstants.E_ITEM /* item */, required=true)
    private final ShareDetails details;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetShareDetailsResponse() {
        this((String) null);
    }

    public GetShareDetailsResponse(String id) {
        details = new ShareDetails(id);
    }

    public ShareDetails getShareDetails() {
        return details;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("item", details);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
