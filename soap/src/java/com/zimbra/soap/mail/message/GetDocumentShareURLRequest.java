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
import com.zimbra.soap.mail.type.ItemSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get the download URL of shared document
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=OctopusXmlConstants.E_GET_DOCUMENT_SHARE_URL_REQUEST)
public class GetDocumentShareURLRequest {

    /**
     * @zm-api-field-description Item specification
     */
    @XmlElement(name=MailConstants.E_ITEM /* item */, required=false)
    private ItemSpec item;

    public void setItem(ItemSpec item) { this.item = item; }
    public ItemSpec getItem() { return item; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("item", item);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
