// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ListDocumentRevisionsSpec {

    /**
     * @zm-api-field-tag item-id
     * @zm-api-field-description Item ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag version
     * @zm-api-field-description Version
     */
    @XmlAttribute(name=MailConstants.A_VERSION /* ver */, required=false)
    private Integer version;

    /**
     * @zm-api-field-tag num-revisions
     * @zm-api-field-description Maximum number of revisions to return starting from <b>{version}</b>
     */
    @XmlAttribute(name=MailConstants.A_COUNT /* count */, required=false)
    private Integer count;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ListDocumentRevisionsSpec() {
        this((String) null);
    }

    public ListDocumentRevisionsSpec(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) { this.version = version; }
    public void setCount(Integer count) { this.count = count; }
    public String getId() { return id; }
    public Integer getVersion() { return version; }
    public Integer getCount() { return count; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("version", version)
            .add("count", count);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
