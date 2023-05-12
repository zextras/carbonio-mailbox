// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ContactGroupMemberInterface;
import com.zimbra.soap.base.ContactInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class ContactGroupMember
implements ContactGroupMemberInterface {

    /**
     * @zm-api-field-tag member-type
     * @zm-api-field-description Member type
     * <table>
     * <tr> <td> <b>C</b> </td> <td> reference to another contact </td> </tr>
     * <tr> <td> <b>G</b> </td> <td> reference to a GAL entry </td> </tr>
     * <tr> <td> <b>I</b> </td>
     *      <td> inlined member (member name and email address is embeded in the contact group)</td> </tr>
     * </table>
     */
    @XmlAttribute(name=MailConstants.A_CONTACT_GROUP_MEMBER_TYPE /* type */, required=true)
    private String type;

    /**
     * @zm-api-field-tag member-value
     * @zm-api-field-description Member value
     * <table>
     * <tr> <td> <b>type="C"</b> </td>
     *      <td> Item ID of another contact.  If the referenced contact is in a shared folder, the item ID must be
     *           qualified by zimbraId of the owner.  e.g. {zimbraId}:{itemId} </td> </tr>
     * <tr> <td> <b>type="G"</b> </td> <td> GAL entry reference (returned in SearchGalResponse) </td> </tr>
     * <tr> <td> <b>type="I"</b> </td>
     *      <td> name and email address in the form of: <b>"{name}" &lt;{email}></b> </td> </tr>
     * </table>
     */
    @XmlAttribute(name=MailConstants.A_CONTACT_GROUP_MEMBER_VALUE /* value */, required=true)
    private String value;

    /**
     * @zm-api-field-description Contact information for dereferenced member.
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
    public String getType() { return type; }
    @Override
    public String getValue() { return value; }
    @Override
    public ContactInfo getContact() { return contact; }

    @Override
    public void setContact(ContactInterface contact) {
        setContact((ContactInfo) contact);
    }

    public static Iterable <ContactGroupMember> fromInterfaces(Iterable <ContactGroupMemberInterface> params) {
        if (params == null)
            return null;
        final List <ContactGroupMember> newList = Lists.newArrayList();
        for (final ContactGroupMemberInterface param : params) {
            newList.add((ContactGroupMember) param);
        }
        return newList;
    }

    public static List <ContactGroupMemberInterface> toInterfaces(Iterable <ContactGroupMember> params) {
        if (params == null)
            return null;
        final List <ContactGroupMemberInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("type", type)
            .add("value", value)
            .add("contact", contact);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
