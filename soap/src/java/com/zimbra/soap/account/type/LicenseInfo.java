// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.LicenseStatus;

import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_LICENSE_INFO, description="License information")
public class LicenseInfo {

    /**
     * @zm-api-field-description Status
     */
    @XmlAttribute(name=AccountConstants.A_STATUS /* status */, required=true)
    private LicenseStatus status;

    /**
     * @zm-api-field-description License attributes
     */
    @XmlElement(name=AccountConstants.E_ATTR /* attr */, required=false)
    private List<LicenseAttr> attrs;

    public LicenseInfo() {
    }

    public void setStatus(LicenseStatus status) { this.status = status; }
    public void setAttrs(Iterable <LicenseAttr> attrs) {
        if (attrs == null) {
            this.attrs = null;
        } else {
            this.attrs = Lists.newArrayList();
            Iterables.addAll(this.attrs, attrs);
        }
    }

    public void addAttr(LicenseAttr attr) {
        if (this.attrs == null) {
            this.attrs = Lists.newArrayList();
        }
        this.attrs.add(attr);
    }

    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.STATUS, description="Status")
    public LicenseStatus getStatus() { return status; }
    @GraphQLQuery(name=GqlConstants.ATTRS, description="attrs")
    public List<LicenseAttr> getAttrs() {
        return attrs;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("status", status)
            .add("attrs", attrs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
