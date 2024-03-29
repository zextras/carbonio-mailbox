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
public class FreeBusyUserSpec {

    /**
     * @zm-api-field-tag calendar-folder-id
     * @zm-api-field-description Calendar folder ID; if omitted, get f/b on all calendar folders
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private Integer folderId;

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description Zimbra ID Either "name" or "id" must be specified
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    /**
     * @zm-api-field-tag email
     * @zm-api-field-description Email address.  Either "name" or "id" must be specified
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=false)
    private String name;

    public FreeBusyUserSpec() {
    }

    public void setFolderId(Integer folderId) { this.folderId = folderId; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public Integer getFolderId() { return folderId; }
    public String getId() { return id; }
    public String getName() { return name; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("folderId", folderId)
            .add("id", id)
            .add("name", name);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
