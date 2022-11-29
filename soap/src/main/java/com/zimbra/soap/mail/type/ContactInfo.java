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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ContactGroupMemberInterface;
import com.zimbra.soap.base.ContactInterface;
import com.zimbra.soap.base.CustomMetadataInterface;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import com.zimbra.soap.mail.message.SearchResponse;
import com.zimbra.soap.type.ContactAttr;
import com.zimbra.soap.type.SearchHit;
import com.zimbra.soap.type.ZmBoolean;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * {@link SearchHit} is used in {@link SearchResponse} as the element type for a List
 */
@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_MAIL_CONTACT_INFO, description="Contact information")
public class ContactInfo
implements ContactInterface, SearchHit {

    private static Splitter COMMA_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    // Added by e.g. GalSearchControl.doLocalGalAccountSearch
    /**
     * @zm-api-field-tag contact-sort-field
     * @zm-api-field-description Sort field value
     */
    @XmlAttribute(name=MailConstants.A_SORT_FIELD /* sf */, required=false)
    private String sortField;

    /**
     * @zm-api-field-tag can-expand
     * @zm-api-field-description Set if the user can (has right to) expand group members.  Returned only if needExp
     * is set in the request and only on group entries (type=group in attrs on a <b>&lt;cn></b>).
     */
    @XmlAttribute(name=AccountConstants.A_EXP /* exp */, required=false)
    private ZmBoolean canExpand;

    // id is the only attribute or element that can be required:
    //    GalSearchResultCallback.handleContact(Contact c) and CreateContact.handle
    //    sometimes just create E_CONTACT elements with only an A_ID attribute
    /**
     * @zm-api-field-tag contact-id
     * @zm-api-field-description Unique contact ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private String id;

    /**
     * @zm-api-field-tag imap-uid
     * @zm-api-field-description IMAP UID
     */
    @XmlAttribute(name=MailConstants.A_IMAP_UID /* i4uid */, required=false)
    private Integer imapUid;

    /**
     * @zm-api-field-tag contact-folder-id
     * @zm-api-field-description Folder ID.  When creating a contact, this is the ID of the folder to create the
     * contact in
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private String folder;

    /**
     * @zm-api-field-tag contact-flags
     * @zm-api-field-description Flags.  {flags} = (f)lagged, has (a)ttachment
     */
    @XmlAttribute(name=MailConstants.A_FLAGS /* f */, required=false)
    private String flags;

    /**
     * @zm-api-field-tag contact-tags
     * @zm-api-field-description Tags - Comma separated list of integers.  DEPRECATED - use "tn" instead
     */
    @Deprecated
    @XmlAttribute(name=MailConstants.A_TAGS /* t */, required=false)
    private String tags;

    /**
     * @zm-api-field-tag contact-tag-names
     * @zm-api-field-description Comma-separated list of tag names
     */
    @XmlAttribute(name=MailConstants.A_TAG_NAMES /* tn */, required=false)
    private String tagNames;

    /**
     * @zm-api-field-tag contact-modified-date-secs
     * @zm-api-field-description Modified date in seconds
     */
    @XmlAttribute(name=MailConstants.A_CHANGE_DATE /* md */, required=false)
    private Long changeDate;

    /**
     * @zm-api-field-tag contact-modified-seq
     * @zm-api-field-description Modified sequence
     */
    @XmlAttribute(name=MailConstants.A_MODIFIED_SEQUENCE /* ms */, required=false)
    private Integer modifiedSequenceId;

    /**
     * @zm-api-field-tag contact-date-millis
     * @zm-api-field-description Date in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_DATE /* d */, required=false)
    private Long date;

    /**
     * @zm-api-field-tag saved-sequence-number
     * @zm-api-field-description Saved sequence number
     */
    @XmlAttribute(name=MailConstants.A_REVISION /* rev */, required=false)
    private Integer revisionId;

    /**
     * @zm-api-field-tag contact-file-as
     * @zm-api-field-description Current "file as" string for display/sorting purposes; <b>cannot</b> be used to
     * <b>set</b> the file-as value
     */
    @XmlAttribute(name=MailConstants.A_FILE_AS_STR /* fileAsStr */, required=false)
    private String fileAs;

    /**
     * @zm-api-field-tag contact-email
     * @zm-api-field-description Contact email address
     */
    @XmlAttribute(name="email", required=false)
    private String email;

    /**
     * @zm-api-field-tag contact-email2
     * @zm-api-field-description Contact email address 2
     */
    @XmlAttribute(name="email2", required=false)
    private String email2;

    /**
     * @zm-api-field-tag contact-email3
     * @zm-api-field-description Contact email address 3
     */
    @XmlAttribute(name="email3", required=false)
    private String email3;

    /**
     * @zm-api-field-tag contact-type
     * @zm-api-field-description Contact type
     */
    @XmlAttribute(name="type", required=false)
    private String type;

    /**
     * @zm-api-field-tag contact-dlist
     * @zm-api-field-description Contact dlist
     */
    @XmlAttribute(name="dlist", required=false)
    private String dlist;

    // See GalSearchResultCallback.handleContact(Contact c)
    /**
     * @zm-api-field-tag contact-gal-entry-ref
     * @zm-api-field-description GAL entry reference
     */
    @XmlAttribute(name=AccountConstants.A_REF /* ref */, required=false)
    private String reference;

    /**
     * @zm-api-field-tag contact-too-many-members
     * @zm-api-field-description If number of members on a GAL group is greater than the specified max,
     * do not return any members for the entry.  Instead, set "tooManyMembers.
     */
    @XmlAttribute(name=MailConstants.A_TOO_MANY_MEMBERS /* tooManyMembers */, required=false)
    private ZmBoolean tooManyMembers;

    /**
     * @zm-api-field-description Custom metadata information
     */
    @XmlElement(name=MailConstants.E_METADATA /* meta */, required=false)
    private List<MailCustomMetadata> metadatas = Lists.newArrayList();

    /**
     * @zm-api-field-description Attributes
     */
    @XmlElement(name=MailConstants.E_A /* a */, required=false)
    private final List<ContactAttr> attrs = Lists.newArrayList();

    /**
     * @zm-api-field-description Contact group members
     */
    @XmlElement(name=MailConstants.E_CONTACT_GROUP_MEMBER /* m */, required=false)
    private final List<ContactGroupMember> contactGroupMembers = Lists.newArrayList();

    /**
     * @zm-api-field-tag comma-sep-contact-group-ids
     * @zm-api-field-description Comma separated list of IDs of contact groups this contact is a member of.
     * <br /> Only provided if requested
     */
    @ZimbraJsonAttribute
    @XmlElement(name=MailConstants.E_CONTACT_MEMBER_OF /* memberOf */, required=false)
    private String memberOf;

    public ContactInfo() {
    }

    public ContactInfo(String id) {
        this.id = id;
    }

    private ContactInfo(String sortField, String id) {
        this.sortField = sortField;
        this.id = id;
    }

    public static ContactInfo createForId(String id) {
        return new ContactInfo(id);
    }

    public static ContactInfo createForSortFieldAndId(String sortField, String id) {
        return new ContactInfo(sortField, id);
    }

    @Override
    public void setSortField(String sortField) { this.sortField = sortField; }
    @Override
    public void setCanExpand(Boolean canExpand) { this.canExpand = ZmBoolean.fromBool(canExpand); }
    @Override
    public void setId(String id) { this.id = id; }
    @Override
    public void setFolder(String folder) { this.folder = folder; }
    @Override
    public void setFlags(String flags) { this.flags = flags; }
    @Override
    public void setTags(String tags) { this.tags = tags; }
    @Override
    public void setTagNames(String tagNames) { this.tagNames = tagNames; }
    @Override
    public void setChangeDate(Long changeDate) { this.changeDate = changeDate; }
    @Override
    public void setModifiedSequenceId(Integer modifiedSequenceId) {
        this.modifiedSequenceId = modifiedSequenceId;
    }
    @Override
    public void setDate(Long date) { this.date = date; }
    @Override
    public void setRevisionId(Integer revisionId) {
        this.revisionId = revisionId;
    }
    @Override
    public void setFileAs(String fileAs) { this.fileAs = fileAs; }
    @Override
    public void setEmail(String email) { this.email = email; }
    @Override
    public void setEmail2(String email2) { this.email2 = email2; }
    @Override
    public void setEmail3(String email3) { this.email3 = email3; }
    @Override
    public void setType(String type) { this.type = type; }
    @Override
    public void setDlist(String dlist) { this.dlist = dlist; }
    @Override
    public void setReference(String reference) { this.reference = reference; }
    @Override
    public void setTooManyMembers(Boolean tooManyMembers) {
        this.tooManyMembers = ZmBoolean.fromBool(tooManyMembers);
    }
    public void setMetadatas(Iterable <MailCustomMetadata> metadatas) {
        this.metadatas.clear();
        if (metadatas != null) {
            Iterables.addAll(this.metadatas,metadatas);
        }
    }

    public void addMetadata(MailCustomMetadata metadata) {
        this.metadatas.add(metadata);
    }

    @Override
    public void setAttrs(Iterable <ContactAttr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs,attrs);
        }
    }

    @Override
    public void addAttr(ContactAttr attr) {
        this.attrs.add(attr);
    }

    public void setContactGroupMembers(Iterable <ContactGroupMember> contactGroupMembers) {
        this.contactGroupMembers.clear();
        if (contactGroupMembers != null) {
            Iterables.addAll(this.contactGroupMembers,contactGroupMembers);
        }
    }

    public void addContactGroupMember(ContactGroupMember contactGroupMember) {
        this.contactGroupMembers.add(contactGroupMember);
    }

    public void setMemberOf(String groups) {
        this.memberOf = groups;
    }

    @GraphQLQuery(name="memberOf", description="Comma separated list of IDs of contact groups this contact is a member of")
    public Collection<String> getMemberOf() {
        return Lists.newArrayList(COMMA_SPLITTER.split(Strings.nullToEmpty(memberOf)));
    }

    @Override
    @GraphQLQuery(name=GqlConstants.SORT_FIELD, description="Sort field value")
    public String getSortField() { return sortField; }
    @Override
    @GraphQLQuery(name=GqlConstants.IS_EXPANDABLE, description="Denotes whether user can expand group members")
    public Boolean getCanExpand() { return ZmBoolean.toBool(canExpand); }
    @Override
    @GraphQLQuery(name=GqlConstants.ID, description="Unique contact ID")
    public String getId() { return id; }
    @Override
    @GraphQLQuery(name=GqlConstants.FOLDER_ID, description="The containing Folder ID")
    public String getFolder() { return folder; }
    @Override
    @GraphQLQuery(name=GqlConstants.FLAGS, description="(f)lagged, has (a)ttachment")
    public String getFlags() { return flags; }
    @Override
    @GraphQLIgnore
    public String getTags() { return tags; }
    @Override
    @GraphQLQuery(name=GqlConstants.TAG_NAMES, description="Comma-separated list of tag names")
    public String getTagNames() { return tagNames; }
    @Override
    @GraphQLQuery(name=GqlConstants.LAST_MODIFIED, description="Modified date in seconds")
    public Long getChangeDate() { return changeDate; }
    @Override
    @GraphQLQuery(name=GqlConstants.MODIFIED_SEQUENCE, description="Modified sequence")
    public Integer getModifiedSequenceId() { return modifiedSequenceId; }
    @Override
    @GraphQLQuery(name=GqlConstants.DATE, description="Date in milliseconds")
    public Long getDate() { return date; }
    @Override
    @GraphQLQuery(name=GqlConstants.REVISION, description="Saved sequence number")
    public Integer getRevisionId() { return revisionId; }
    @Override
    @GraphQLQuery(name=GqlConstants.FILE_AS, description="Current fileAs string for display/sorting purposes")
    public String getFileAs() { return fileAs; }
    @Override
    @GraphQLQuery(name=GqlConstants.EMAIL, description="Contact email address")
    public String getEmail() { return email; }
    @Override
    @GraphQLQuery(name=GqlConstants.EMAIL2, description="Contact email address 2")
    public String getEmail2() { return email2; }
    @Override
    @GraphQLQuery(name=GqlConstants.EMAIL3, description="Contact email address 3")
    public String getEmail3() { return email3; }
    @Override
    @GraphQLQuery(name=GqlConstants.TYPE, description="Contact type")
    public String getType() { return type; }
    @Override
    @GraphQLQuery(name=GqlConstants.DLIST, description="Contact dlist")
    public String getDlist() { return dlist; }
    @Override
    @GraphQLQuery(name=GqlConstants.REFERENCE, description="Global Address List entry reference")
    public String getReference() { return reference; }
    @Override
    @GraphQLQuery(name=GqlConstants.IS_TOO_MANY_MEMBERS, description="Denotes whether the number of entries on a GAL group exceeds the specified max")
    public Boolean getTooManyMembers() { return ZmBoolean.toBool(tooManyMembers); }

    @GraphQLQuery(name=GqlConstants.METADATAS, description="Custom metadata information")
    public List<MailCustomMetadata> getMetadatas() {
        return metadatas;
    }
    @Override
    @GraphQLQuery(name=GqlConstants.ATTRIBUTES, description="Attributes")
    public List<ContactAttr> getAttrs() {
        return attrs;
    }

    @GraphQLQuery(name=GqlConstants.CONTACT_GROUP_MEMBERS, description="Contact group members")
    public List<ContactGroupMember> getContactGroupMembers() {
        return Collections.unmodifiableList(contactGroupMembers);
    }

    // non-JAXB method
    @Override
    public void setMetadataInterfaces(
            Iterable<CustomMetadataInterface> metadatas) {
        this.metadatas = MailCustomMetadata.fromInterfaces(metadatas);
    }

    // non-JAXB method
    @Override
    public void addMetadataInterfaces(CustomMetadataInterface metadata) {
        addMetadata((MailCustomMetadata)metadata);
    }

    // non-JAXB method
    @Override
    @GraphQLIgnore
    public List<CustomMetadataInterface> getMetadataInterfaces() {
        return MailCustomMetadata.toInterfaces(metadatas);
    }

    public static Iterable <ContactInfo> fromInterfaces(Iterable <ContactInterface> params) {
        if (params == null)
            return null;
        final List <ContactInfo> newList = Lists.newArrayList();
        for (final ContactInterface param : params) {
            newList.add((ContactInfo) param);
        }
        return newList;
    }

    public static List <ContactInterface> toInterfaces(Iterable <ContactInfo> params) {
        if (params == null)
            return null;
        final List <ContactInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }

    public void setImapUid(Integer imapUid) { this.imapUid = imapUid; }
    @GraphQLQuery(name=GqlConstants.IMAP_UID, description="Imap UID")
    public Integer getImapUid() { return imapUid; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("sortField", sortField)
            .add("canExpand", canExpand)
            .add("id", id)
            .add("imapUid", imapUid)
            .add("folder", folder)
            .add("flags", flags)
            .add("tags", tags)
            .add("tagNames", tagNames)
            .add("changeDate", changeDate)
            .add("modifiedSequenceId", modifiedSequenceId)
            .add("date", date)
            .add("revisionId", revisionId)
            .add("fileAs", fileAs)
            .add("email", email)
            .add("email2", email2)
            .add("email3", email3)
            .add("type", type)
            .add("dlist", dlist)
            .add("reference", reference)
            .add("metadatas", metadatas)
            .add("attrs", attrs)
            .add("contactGroupMembers", contactGroupMembers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    @Override
    public void setContactGroupMemberInterfaces(Iterable<ContactGroupMemberInterface> contactGroupMembers) {
        this.setContactGroupMembers(ContactGroupMember.fromInterfaces(contactGroupMembers));
    }

    @Override
    public void addContactGroupMember(ContactGroupMemberInterface contactGroupMember) {
        this.addContactGroupMember((ContactGroupMember) contactGroupMember);
    }

    @Override
    @GraphQLIgnore
    public List<ContactGroupMemberInterface> getContactGroupMemberInterfaces() {
        return ContactGroupMember.toInterfaces(contactGroupMembers);
    }
}
