// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ContactGroupMemberInterface;
import com.zimbra.soap.base.ContactInterface;

import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_ACCOUNT_CONTACT_GROUP_MEMBER, description="Contact group member")
public class ContactGroupMember
implements ContactGroupMemberInterface {

    /**
     * @zm-api-field-tag contact-group-member-type
     * @zm-api-field-description Contact group member type
     */
    @XmlAttribute(name=MailConstants.A_CONTACT_GROUP_MEMBER_TYPE /* type */, required=true)
    private String type;

    /**
     * @zm-api-field-tag contact-group-member-value
     * @zm-api-field-description Contact group member value
     */
    @XmlAttribute(name=MailConstants.A_CONTACT_GROUP_MEMBER_VALUE /* value */, required=true)
    private String value;

    /**
     * @zm-api-field-tag contact-group-member
     * @zm-api-field-description Contact
     */
    @XmlElement(name=MailConstants.E_CONTACT /* cn */, required=false)
    private ContactInfo contact;

    private ContactGroupMember() {
    }

    private ContactGroupMember(String type, String value, ContactInfo contact) {
        setType(type);
        setValue(value);
        setContact(contact);
    }

    public static ContactGroupMember createForTypeValueAndContact(String type, String value, ContactInfo contact) {
        return new ContactGroupMember(type, value, contact);
    }

    @Override
    public void setType(String type) { this.type = type; }
    @Override
    public void setValue(String value) { this.value = value; }
    public void setContact(ContactInfo contact) { this.contact = contact; }
    @Override
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.TYPE, description="Member type. C|G|I")
    public String getType() { return type; }
    @Override
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.VALUE, description="Member value")
    public String getValue() { return value; }
    @Override
    @GraphQLQuery(name=GqlConstants.CONTACT, description="Contact information for dereferenced member")
    public ContactInfo getContact() { return contact; }

    @Override
    public void setContact(ContactInterface contact) {
        setContact((ContactInfo) contact);
    }

    public static Iterable <ContactGroupMember> fromInterfaces(Iterable <ContactGroupMemberInterface> params) {
        if (params == null)
            return null;
        List <ContactGroupMember> newList = Lists.newArrayList();
        for (ContactGroupMemberInterface param : params) {
            newList.add((ContactGroupMember) param);
        }
        return newList;
    }

    public static List <ContactGroupMemberInterface> toInterfaces(Iterable <ContactGroupMember> params) {
        if (params == null)
            return null;
        List <ContactGroupMemberInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("type", type)
            .add("value", value)
            .add("contact", contact);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
