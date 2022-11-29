// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.SectionAttr;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Mailbox metadata
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_MAILBOX_METADATA_REQUEST)
public class GetMailboxMetadataRequest {

    /**
     * @zm-api-field-description Metadata section specification
     */
    @XmlElement(name=MailConstants.E_METADATA /* meta */, required=false)
    private SectionAttr metadata;

    public GetMailboxMetadataRequest() {
    }

    public void setMetadata(SectionAttr metadata) { this.metadata = metadata; }
    public SectionAttr getMetadata() { return metadata; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("metadata", metadata);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
