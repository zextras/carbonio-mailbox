// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.CustomMetadataInterface;
import com.zimbra.soap.base.MessageCommonInterface;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"metadatas"})
public class MessageCommon
implements MessageCommonInterface {

    /**
     * @zm-api-field-tag msg-size
     * @zm-api-field-description Size in bytes
     */
    @XmlAttribute(name=MailConstants.A_SIZE /* s */, required=false)
    private Long size;

    /**
     * @zm-api-field-tag msg-date
     * @zm-api-field-description Date Seconds since the epoch, from the date header in the message
     */
    @XmlAttribute(name=MailConstants.A_DATE /* d */, required=false)
    private Long date;

    /**
     * @zm-api-field-tag folder-id
     * @zm-api-field-description Folder ID
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private String folder;

    /**
     * @zm-api-field-tag conversation-id
     * @zm-api-field-description Converstation ID. only present if <b>&lt;m></b> is not enclosed within a
     * <b>&lt;c></b> element
     */
    @XmlAttribute(name=MailConstants.A_CONV_ID /* cid */, required=false)
    private String conversationId;

    /**
     * @zm-api-field-tag msg-flags
     * @zm-api-field-description Flags.  (u)nread, (f)lagged, has (a)ttachment, (r)eplied, (s)ent by me,
     * for(w)arded, calendar in(v)ite, (d)raft, IMAP-\Deleted (x), (n)otification sent, urgent (!),
     * low-priority (?), priority (+)
     */
    @XmlAttribute(name=MailConstants.A_FLAGS /* f */, required=false)
    private String flags;

    /**
     * @zm-api-field-tag msg-tags
     * @zm-api-field-description Tags - Comma separated list of integers.  DEPRECATED - use "tn" instead
     */
    @Deprecated
    @XmlAttribute(name=MailConstants.A_TAGS /* t */, required=false)
    private String tags;

    /**
     * @zm-api-field-tag msg-tag-names
     * @zm-api-field-description Comma separated list of tag names
     */
    @XmlAttribute(name=MailConstants.A_TAG_NAMES /* tn */, required=false)
    private String tagNames;

    /**
     * @zm-api-field-tag revision
     * @zm-api-field-description Revision
     */
    @XmlAttribute(name=MailConstants.A_REVISION /* rev */, required=false)
    private Integer revision;

    /**
     * @zm-api-field-tag msg-date-metadata-changed
     * @zm-api-field-description Date metadata changed
     */
    @XmlAttribute(name=MailConstants.A_CHANGE_DATE /* md */, required=false)
    private Long changeDate;

    /**
     * @zm-api-field-tag change-sequence
     * @zm-api-field-description Change sequence
     */
    @XmlAttribute(name=MailConstants.A_MODIFIED_SEQUENCE /* ms */, required=false)
    private Integer modifiedSequence;

    /**
     * @zm-api-field-description Custom metadata information
     */
    @XmlElement(name=MailConstants.E_METADATA /* meta */, required=false)
    private final List<MailCustomMetadata> metadatas = Lists.newArrayList();

    public MessageCommon() {
    }

    @Override
    public void setSize(Long size) { this.size = size; }
    @Override
    public void setDate(Long date) { this.date = date; }
    @Override
    public void setFolder(String folder) { this.folder = folder; }
    @Override
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    @Override
    public void setFlags(String flags) { this.flags = flags; }
    @Override
    public void setTags(String tags) { this.tags = tags; }
    @Override
    public void setTagNames(String tagNames) { this.tagNames = tagNames; }
    @Override
    public void setRevision(Integer revision) { this.revision = revision; }
    @Override
    public void setChangeDate(Long changeDate) { this.changeDate = changeDate; }
    @Override
    public void setModifiedSequence(Integer modifiedSequence) {
        this.modifiedSequence = modifiedSequence;
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
    public Long getSize() { return size; }
    @Override
    public Long getDate() { return date; }
    @Override
    public String getFolder() { return folder; }
    @Override
    public String getConversationId() { return conversationId; }
    @Override
    public String getFlags() { return flags; }
    @Override
    public String getTags() { return tags; }
    @Override
    public String getTagNames() { return tagNames; }
    @Override
    public Integer getRevision() { return revision; }
    @Override
    public Long getChangeDate() { return changeDate; }
    @Override
    public Integer getModifiedSequence() { return modifiedSequence; }

    public List<MailCustomMetadata> getMetadatas() {
        return Collections.unmodifiableList(metadatas);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("size", size)
            .add("date", date)
            .add("folder", folder)
            .add("conversationId", conversationId)
            .add("flags", flags)
            .add("tags", tags)
            .add("tagNames", tagNames)
            .add("revision", revision)
            .add("changeDate", changeDate)
            .add("modifiedSequence", modifiedSequence)
            .add("metadatas", metadatas);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    @Override
    public void setMetadataInterfaces(
            Iterable<CustomMetadataInterface> metadatas) {
        for (final CustomMetadataInterface meta : metadatas) {
            addMetadata((MailCustomMetadata)meta);
        }
    }

    @Override
    public void addMetadataInterfaces(CustomMetadataInterface metadata) {
        addMetadata((MailCustomMetadata)metadata);
    }

    @Override
    public List<CustomMetadataInterface> getMetadataInterfaces() {
        final List<CustomMetadataInterface> metas = Lists.newArrayList();
        metas.addAll(metadatas);
        return Collections.unmodifiableList(metas);
    }
}
