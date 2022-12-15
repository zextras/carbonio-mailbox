// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_ATTACHMENT_SPECIFICATIONS, description="Attachment specifications")
public abstract class AttachSpec {

    /**
     * @zm-api-field-tag optional
     * @zm-api-field-description Optional
     */
    @XmlAttribute(name=MailConstants.A_OPTIONAL /* optional */, required=false)
    private ZmBoolean optional;

    public AttachSpec() {
    }

    public void setOptional(Boolean optional) { this.optional = ZmBoolean.fromBool(optional); }
    @GraphQLQuery(name=GqlConstants.OPTIONAL, description="Optional")
    public Boolean getOptional() { return ZmBoolean.toBool(optional); }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("optional", optional);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
