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
import com.zimbra.soap.mail.type.ListDocumentRevisionsSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Returns <b>{num}</b> number of revisions starting from <b>{version}</b> of the
 * requested document.  <b>{num}</b> defaults to 1.  <b>{version}</b> defaults to the current version.
 * <br />
 * Documents that have multiple revisions have the flag "/", which indicates that the document is versioned.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_LIST_DOCUMENT_REVISIONS_REQUEST)
public class ListDocumentRevisionsRequest {

    /**
     * @zm-api-field-description Specification for the list of document revisions
     */
    @XmlElement(name=MailConstants.E_DOC /* doc */, required=true)
    private final ListDocumentRevisionsSpec doc;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ListDocumentRevisionsRequest() {
        this((ListDocumentRevisionsSpec) null);
    }

    public ListDocumentRevisionsRequest(ListDocumentRevisionsSpec doc) {
        this.doc = doc;
    }

    public ListDocumentRevisionsSpec getDoc() { return doc; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("doc", doc);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
