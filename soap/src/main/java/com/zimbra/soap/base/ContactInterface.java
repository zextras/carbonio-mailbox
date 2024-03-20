// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.ContactAttr;

/**
 * 
 * See {@link com.zimbra.cs.service.mail.ToXML} encodeContact, encodeGalContact
 * Note that encodeContactAttachment forces KeyValuePairs to be represented by list of ContactAttr
 */
public interface ContactInterface {
    void setId(String id);
    void setSortField(String sortField);
    void setCanExpand(Boolean canExpand);
    void setFolder(String folder);
    void setFlags(String flags);
    @Deprecated
    void setTags(String tags);
    void setTagNames(String tagNames);
    void setChangeDate(Long changeDate);
    void setModifiedSequenceId(Integer modifiedSequenceId);
    void setDate(Long date);
    void setRevisionId(Integer revisionId);
    void setFileAs(String fileAs);
    void setEmail(String email);
    void setEmail2(String email2);
    void setEmail3(String email3);
    void setType(String type);
    void setDlist(String dlist);
    void setReference(String reference);
    void setTooManyMembers(Boolean tooManyMembers);
    void setMetadataInterfaces(Iterable<CustomMetadataInterface> metadatas);
    void addMetadataInterfaces(CustomMetadataInterface metadata);
    // ContactAttr extends KeyValuePair.
    // com.zimbra.cs.service.mail.ToXML.encodeContactAttachment decorates KeyValuePairs with additional attributes
    void setAttrs(Iterable<ContactAttr> attrs);
    void addAttr(ContactAttr attr);
    void setContactGroupMemberInterfaces(Iterable<ContactGroupMemberInterface> contactGroupMembers);
    void addContactGroupMember(ContactGroupMemberInterface contactGroupMember);


    String getId();
    String getSortField();
    Boolean getCanExpand();
    String getFolder();
    String getFlags();
    @Deprecated
    String getTags();
    String getTagNames();
    Long getChangeDate();
    Integer getModifiedSequenceId();
    Long getDate();
    Integer getRevisionId();
    String getFileAs();
    String getEmail();
    String getEmail2();
    String getEmail3();
    String getType();
    String getDlist();
    String getReference();
    Boolean getTooManyMembers();
    List<CustomMetadataInterface> getMetadataInterfaces();
    List<ContactAttr> getAttrs();
    List<ContactGroupMemberInterface> getContactGroupMemberInterfaces();
}
