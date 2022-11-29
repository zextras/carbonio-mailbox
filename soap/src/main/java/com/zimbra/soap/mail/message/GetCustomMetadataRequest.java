// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.SectionAttr;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Custom metadata
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_METADATA_REQUEST)
public class GetCustomMetadataRequest {

    /**
     * @zm-api-field-tag item-id
     * @zm-api-field-description Item ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-description Metadata section selector
     */
    @XmlElement(name=MailConstants.E_METADATA /* meta */, required=false)
    private SectionAttr metadata;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetCustomMetadataRequest() {
        this((String) null);
    }

    public GetCustomMetadataRequest(String id) {
        this.id = id;
    }

    public void setMetadata(SectionAttr metadata) { this.metadata = metadata; }
    public String getId() { return id; }
    public SectionAttr getMetadata() { return metadata; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("metadata", metadata);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
