// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

/*
<acl [internalGrantExpiry="{millis-since-epoch}"] [guestGrantExpiry="{millis-since-epoch}"]>
  <grant .. />*
</acl>
 */
@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name="Acl", description="Access control level")
public class Acl {

    /**
     * @zm-api-field-tag millis-since-epoch
     * @zm-api-field-description Time when grants to internal grantees expire.
     *   If not specified in the request, defaults to the maximum allowed expiry for internal grants.
     *   If not specified in the response, defaults to 0.
     *   Value of 0 indicates that these grants never expire.
     */
    @XmlAttribute(name=MailConstants.A_INTERNAL_GRANT_EXPIRY /* internalGrantExpiry */, required=false)
    @GraphQLQuery(name="internalGrantExpiry", description="Time when grants to internal grantees expire.")
    private Long internalGrantExpiry;

    /**
     * @zm-api-field-tag millis-since-epoch
     * @zm-api-field-description Time when grants to guest grantees expire.
     *   If not specified in the request, defaults to the maximum allowed expiry for guest/external
     *   user grants.  If not specified in the response, defaults to 0.
     *   Value of 0 indicates that these grants never expire.
     */
    @XmlAttribute(name=MailConstants.A_GUEST_GRANT_EXPIRY /* guestGrantExpiry */, required=false)
    @GraphQLQuery(name="guestGrantExpiry", description="Time when grants to guest grantees expire.")
    private Long guestGrantExpiry;

    /**
     * @zm-api-field-description Grants
     */
    @XmlElement(name=MailConstants.E_GRANT /* grant */, required=false)
    @GraphQLQuery(name="grants", description="Grants")
    private List<Grant> grants = Lists.newArrayList();

    public Acl() {
    }

    @GraphQLQuery(name="internalGrantExpiry", description="Time when grants to internal grantees expire.")
    public Long getInternalGrantExpiry() {
        return internalGrantExpiry;
    }

    public void setInternalGrantExpiry(Long internalGrantExpiry) {
        this.internalGrantExpiry = internalGrantExpiry;
    }

    @GraphQLQuery(name="guestGrantExpiry", description="Time when grants to guest grantees expire.")
    public Long getGuestGrantExpiry() {
        return guestGrantExpiry;
    }

    public void setGuestGrantExpiry(Long guestGrantExpiry) {
        this.guestGrantExpiry = guestGrantExpiry;
    }

    @GraphQLQuery(name="grants", description="Grants")
    public List<Grant> getGrants() {
        return Collections.unmodifiableList(grants);
    }

    public void setGrants(Collection<Grant> grants) {
        this.grants.clear();
        if (grants != null) {
            this.grants.addAll(grants);
        }
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
                .add("internalGrantExpiry", internalGrantExpiry)
                .add("guestGrantExpiry", guestGrantExpiry)
                .add("grants", grants);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
