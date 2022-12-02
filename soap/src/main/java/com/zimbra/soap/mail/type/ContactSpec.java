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
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.SpecifyContact;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_CONTACT_SPEC, description="Input for creating a new contact")
public class ContactSpec implements SpecifyContact<NewContactAttr,NewContactGroupMember> {

    // Used when modifying a contact
    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID - specified when modifying a contact
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private Integer id;

    /**
     * @zm-api-field-tag folder-id
     * @zm-api-field-description ID of folder to create contact in. Un-specified means use the default Contacts folder.
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private String folder;

    /**
     * @zm-api-field-tag tags
     * @zm-api-field-description Tags - Comma separated list of integers.  DEPRECATED - use "tn" instead
     */
    @Deprecated
    @XmlAttribute(name=MailConstants.A_TAGS /* t */, required=false)
    @GraphQLIgnore
    private String tags;

    /**
     * @zm-api-field-tag tag-names
     * @zm-api-field-description Comma-separated list of tag names
     */
    @XmlAttribute(name=MailConstants.A_TAG_NAMES /* tn */, required=false)
    private String tagNames;

    /**
     * @zm-api-field-description Either a vcard or attributes can be specified but not both.
     */
    @XmlElement(name=MailConstants.E_VCARD /* vcard */, required=false)
    private VCardInfo vcard;

    /**
     * @zm-api-field-description Contact attributes.  Cannot specify <b>&lt;vcard></b> as well as these
     */
    @XmlElement(name=MailConstants.E_ATTRIBUTE /* a */, required=false)
    @GraphQLInputField(name=GqlConstants.ATTRIBUTES, description="Contact attributes.")
    private final List<NewContactAttr> attrs = Lists.newArrayList();

    /**
     * @zm-api-field-description Contact group members.  Valid only if the contact being created is a contact group
     * (has attribute type="group")
     */
    @XmlElement(name=MailConstants.E_CONTACT_GROUP_MEMBER /* m */, required=false)
    @GraphQLInputField(name=GqlConstants.CONTACT_GROUP_MEMBERS, description="Valid only if the contact being created is a contact group")
    private final List<NewContactGroupMember> contactGroupMembers = Lists.newArrayList();

    public ContactSpec() {
    }

    @Override
    @GraphQLInputField(name=GqlConstants.ID, description="ID - specified when modifying a contact")
    public void setId(Integer id) { this.id = id; }
    @GraphQLInputField(name=GqlConstants.FOLDER_ID, description="ID of folder to create contact in. Un-specified means use the default Contacts folder.")
    public void setFolder(String folder) { this.folder = folder; }
    @Deprecated
    @GraphQLIgnore
    public void setTags(String tags) { this.tags = tags; }
    @Override
    @GraphQLInputField(name=GqlConstants.TAG_NAMES, description="Comma-separated list of tag names")
    public void setTagNames(String tagNames) { this.tagNames = tagNames; }
    @GraphQLInputField(name=GqlConstants.VCARD, description="Either a vcard or attributes can be specified but not both")
    public void setVcard(VCardInfo vcard) { this.vcard = vcard; }
    @Override
    @GraphQLInputField(name=GqlConstants.ATTRIBUTES, description="Contact attributes")
    public void setAttrs(Iterable <NewContactAttr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs, attrs);
        }
    }

    @Override
    @GraphQLIgnore
    public void addAttr(NewContactAttr attr) {
        this.attrs.add(attr);
    }

    @Override
    @GraphQLIgnore
    public NewContactAttr addAttrWithName(String name) {
        final NewContactAttr nca = new NewContactAttr(name);
        addAttr(nca);
        return nca;
    }

    @Override
    @GraphQLIgnore
    public NewContactAttr addAttrWithNameAndValue(String name, String value) {
        final NewContactAttr nca = NewContactAttr.fromNameAndValue(name, value);
        addAttr(nca);
        return nca;
    }

    @Override
    @GraphQLInputField(name=GqlConstants.CONTACT_GROUP_MEMBERS, description="Valid only if the contact being created is a contact group")
    public void setContactGroupMembers(Iterable <NewContactGroupMember> contactGroupMembers) {
        this.contactGroupMembers.clear();
        if (contactGroupMembers != null) {
            Iterables.addAll(this.contactGroupMembers, contactGroupMembers);
        }
    }

    @Override
    @GraphQLIgnore
    public void addContactGroupMember(NewContactGroupMember contactGroupMember) {
        this.contactGroupMembers.add(contactGroupMember);
    }

    @Override
    @GraphQLIgnore
    public NewContactGroupMember addContactGroupMemberWithTypeAndValue(String type, String value) {
        final NewContactGroupMember ncgm = NewContactGroupMember.createForTypeAndValue(type, value);
        addContactGroupMember(ncgm);
        return ncgm;
    }

    @Override
    public Integer getId() { return id; }
    public String getFolder() { return folder; }
    @Deprecated
    public String getTags() { return tags; }
    @Override
    public String getTagNames() { return tagNames; }
    public VCardInfo getVcard() { return vcard; }
    @Override
    public List<NewContactAttr> getAttrs() {
        return attrs;
    }
    @Override
    public List<NewContactGroupMember> getContactGroupMembers() {
        return contactGroupMembers;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("folder", folder)
            .add("tags", tags)
            .add("tagNames", tagNames)
            .add("vcard", vcard)
            .add("attrs", attrs)
            .add("contactGroupMembers", contactGroupMembers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
