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
import com.zimbra.soap.mail.type.IdVersionName;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_SAVE_DOCUMENT_RESPONSE)
public class SaveDocumentResponse {

    /**
     * @zm-api-field-description Details of saved document revision
     */
    @XmlElement(name=MailConstants.E_DOC /* doc */, required=false)
    private IdVersionName doc;

    public SaveDocumentResponse() {
    }

    public void setDoc(IdVersionName doc) { this.doc = doc; }
    public IdVersionName getDoc() { return doc; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("doc", doc);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
