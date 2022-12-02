// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name="FolderActionResult", description="Folder action response")
public class FolderActionResult extends ActionResult {

    /**
     * @zm-api-field-tag grantee-id
     * @zm-api-field-description Grantee Zimbra ID
     */
    @XmlAttribute(name=MailConstants.A_ZIMBRA_ID /* zid */, required=false)
    @GraphQLQuery(name="zimbraId", description="Grantee Zimbra Id")
    private String zimbraId;

    /**
     * @zm-api-field-tag display-name
     * @zm-api-field-description Display name
     */
    @XmlAttribute(name=MailConstants.A_DISPLAY /* d */, required=false)
    @GraphQLQuery(name="displayName", description="Display name")
    private String displayName;

    /**
     * @zm-api-field-tag access-key
     * @zm-api-field-description Access key (Password)
     */
    @XmlAttribute(name=MailConstants.A_ACCESSKEY /* key */, required=false)
    @GraphQLQuery(name="accessKey", description="Access key (password)")
    private String accessKey;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FolderActionResult() {
        this((String) null, (String) null);
    }

    public FolderActionResult(String id, String operation) {
        super(id, operation);
    }


    public void setZimbraId(String zimbraId) { this.zimbraId = zimbraId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    @GraphQLQuery(name="zimbraId", description="Grantee Zimbra Id")
    public String getZimbraId() { return zimbraId; }
    @GraphQLQuery(name="displayName", description="Display name")
    public String getDisplayName() { return displayName; }
    @GraphQLQuery(name="accessKey", description="Access key (password)")
    public String getAccessKey() { return accessKey; }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("zimbraId", zimbraId)
            .add("displayName", displayName)
            .add("accessKey", accessKey);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
