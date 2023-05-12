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
import com.zimbra.soap.base.SpecifyContact;

@XmlAccessorType(XmlAccessType.NONE)
public class ModifyContactSpec implements SpecifyContact<ModifyContactAttr, ModifyContactGroupMember> {

    // Used when modifying a contact
    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID - specified when modifying a contact
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private Integer id;


    /**
     * @zm-api-field-tag tag-names
     * @zm-api-field-description Comma-separated list of tag names
     */
    @XmlAttribute(name=MailConstants.A_TAG_NAMES /* tn */, required=false)
    private String tagNames;

    /**
     * @zm-api-field-description Contact attributes.  Cannot specify <b>&lt;vcard></b> as well as these
     */
    @XmlElement(name=MailConstants.E_ATTRIBUTE /* a */, required=false)
    private final List<ModifyContactAttr> attrs = Lists.newArrayList();

    /**
     * @zm-api-field-description Contact group members.  Valid only if the contact being created is a contact group
     * (has attribute type="group")
     */
    @XmlElement(name=MailConstants.E_CONTACT_GROUP_MEMBER /* m */, required=false)
    private final List<ModifyContactGroupMember> contactGroupMembers = Lists.newArrayList();

    public ModifyContactSpec() {
    }

    public static ModifyContactSpec createForId(Integer id) {
        final ModifyContactSpec spec = new ModifyContactSpec();
        spec.setId(id);
        return spec;
    }

    @Override
    public void setId( Integer id) { this.id = id; }
    @Override
    public void setTagNames(String tagNames) { this.tagNames = tagNames; }
    @Override
    public void setAttrs(Iterable <ModifyContactAttr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs, attrs);
        }
    }

    @Override
    public void addAttr(ModifyContactAttr attr) {
        this.attrs.add(attr);
    }

    @Override
    public void setContactGroupMembers(Iterable <ModifyContactGroupMember> contactGroupMembers) {
        this.contactGroupMembers.clear();
        if (contactGroupMembers != null) {
            Iterables.addAll(this.contactGroupMembers, contactGroupMembers);
        }
    }

    @Override
    public void addContactGroupMember(ModifyContactGroupMember contactGroupMember) {
        this.contactGroupMembers.add(contactGroupMember);
    }

    @Override
    public Integer getId() { return id; }
    @Override
    public String getTagNames() { return tagNames; }
    @Override
    public List<ModifyContactAttr> getAttrs() {
        return attrs;
    }
    @Override
    public List<ModifyContactGroupMember> getContactGroupMembers() {
        return contactGroupMembers;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("tagNames", tagNames)
            .add("attrs", attrs)
            .add("contactGroupMembers", contactGroupMembers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    @Override
    public ModifyContactAttr addAttrWithName(String name) {
        final ModifyContactAttr mca = new ModifyContactAttr(name);
        addAttr(mca);
        return mca;
    }

    @Override
    public ModifyContactAttr addAttrWithNameAndValue(String name, String value) {
        final ModifyContactAttr mca = ModifyContactAttr.fromNameAndValue(name, value);
        addAttr(mca);
        return mca;
    }

    @Override
    public ModifyContactGroupMember addContactGroupMemberWithTypeAndValue(String type, String value) {
        final ModifyContactGroupMember mcgm = ModifyContactGroupMember.createForTypeAndValue(type, value);
        addContactGroupMember(mcgm);
        return mcgm;
    }
}
