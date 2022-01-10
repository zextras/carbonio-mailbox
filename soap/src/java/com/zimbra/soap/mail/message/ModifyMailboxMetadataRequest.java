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
import com.zimbra.soap.mail.type.MailCustomMetadata;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify Mailbox Metadata
 * <ul>
 * <li> Modify request must contain one or more key/value pairs
 * <li> Existing keys' values will be replaced by new values
 * <li> Empty or null value will remove a key
 * <li> New keys can be added
 * </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_MODIFY_MAILBOX_METADATA_REQUEST)
public class ModifyMailboxMetadataRequest {

    /**
     * @zm-api-field-description Metadata changes
     */
    @XmlElement(name=MailConstants.E_METADATA /* meta */, required=false)
    private MailCustomMetadata metadata;

    public ModifyMailboxMetadataRequest() {
    }

    public void setMetadata(MailCustomMetadata metadata) { this.metadata = metadata; }
    public MailCustomMetadata getMetadata() { return metadata; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("metadata", metadata);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
