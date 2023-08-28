// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class DocumentActionSelector
extends ActionSelector {

    // Used for "!grant" operation
    /**
     * @zm-api-field-tag id-of-grant-to-revoke
     * @zm-api-field-description Zimbra ID of the grant to revoke (Used for "!grant" operation)
     */
    @XmlAttribute(name=MailConstants.A_ZIMBRA_ID /* zid */, required=false)
    private String zimbraId;

    /**
     * @zm-api-field-description Used for "grant" operation
     */
    @XmlElement(name=MailConstants.E_GRANT /* grant */, required=false)
    private DocumentActionGrant grant;

    public DocumentActionSelector() {
        super();
    }

    public DocumentActionSelector(String ids, String operation) {
        super(ids, operation);
    }

    public static DocumentActionSelector createForIdsAndOperation(String ids, String operation) {
        return new DocumentActionSelector(ids, operation);
    }

    public void setZimbraId(String zimbraId) { this.zimbraId = zimbraId; }
    public void setGrant(DocumentActionGrant grant) { this.grant = grant; }
    public String getZimbraId() { return zimbraId; }
    public DocumentActionGrant getGrant() { return grant; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("zimbraId", zimbraId)
            .add("grant", grant);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
