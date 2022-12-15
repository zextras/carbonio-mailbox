// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
public class MailboxBlobConsistency {

    /**
     * @zm-api-field-tag mailbox-id
     * @zm-api-field-description Mailbox ID
     */
    @XmlAttribute(name=AdminConstants.A_ID /* id */, required=true)
    private final int id;

    /**
     * @zm-api-field-description Information about missing blobs
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_MISSING_BLOBS /* missingBlobs */, required=true)
    @XmlElement(name=AdminConstants.E_ITEM /* item */, required=false)
    private List<MissingBlobInfo> missingBlobs = Lists.newArrayList();

    /**
     * @zm-api-field-description Information about items with incorrect sizes
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_INCORRECT_SIZE /* incorrectSize */, required=true)
    @XmlElement(name=AdminConstants.E_ITEM /* item */, required=false)
    private List<IncorrectBlobSizeInfo> incorrectSizes = Lists.newArrayList();

    /**
     * @zm-api-field-description Information about unexpected blobs
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_UNEXPECTED_BLOBS /* unexpectedBlobs */, required=true)
    @XmlElement(name=AdminConstants.E_BLOB /* blob */, required=false)
    private List<UnexpectedBlobInfo> unexpectedBlobs = Lists.newArrayList();

    /**
     * @zm-api-field-description Information about items with incorrect revisions
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_INCORRECT_REVISION /* incorrectRevision */, required=true)
    @XmlElement(name=AdminConstants.E_ITEM /* item */, required=false)
    private List<IncorrectBlobRevisionInfo> incorrectRevisions = Lists.newArrayList();

    /**
     * @zm-api-field-description Information about used Blobs
      */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_USED_BLOBS /* usedBlobs */, required=true)
    @XmlElement(name=AdminConstants.E_ITEM /* item */, required=false)
    private List<UsedBlobInfo> usedBlobs = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailboxBlobConsistency() {
        this(-1);
    }

    public MailboxBlobConsistency(int id) {
        this.id = id;
    }

    public void setMissingBlobs(Iterable <MissingBlobInfo> missingBlobs) {
        this.missingBlobs.clear();
        if (missingBlobs != null) {
            Iterables.addAll(this.missingBlobs, missingBlobs);
        }
    }

    public void addMissingBlob(MissingBlobInfo missingBlob) {
        this.missingBlobs.add(missingBlob);
    }

    public void setIncorrectSizes(Iterable <IncorrectBlobSizeInfo> incorrectSizes) {
        this.incorrectSizes.clear();
        if (incorrectSizes != null) {
            Iterables.addAll(this.incorrectSizes, incorrectSizes);
        }
    }

    public void addIncorrectSize(IncorrectBlobSizeInfo incorrectSize) {
        this.incorrectSizes.add(incorrectSize);
    }

    public void setUnexpectedBlobs(Iterable <UnexpectedBlobInfo> unexpectedBlobs) {
        this.unexpectedBlobs.clear();
        if (unexpectedBlobs != null) {
            Iterables.addAll(this.unexpectedBlobs, unexpectedBlobs);
        }
    }

    public void addUnexpectedBlob(UnexpectedBlobInfo unexpectedBlob) {
        this.unexpectedBlobs.add(unexpectedBlob);
    }

    public void setIncorrectRevisions(Iterable <IncorrectBlobRevisionInfo> incorrectRevisions) {
        this.incorrectRevisions.clear();
        if (incorrectRevisions != null) {
            Iterables.addAll(this.incorrectRevisions, incorrectRevisions);
        }
    }

    public void addIncorrectRevision(IncorrectBlobRevisionInfo incorrectRevision) {
        this.incorrectRevisions.add(incorrectRevision);
    }

    public void setUsedBlobs(Iterable <UsedBlobInfo> usedBlobs) {
        this.usedBlobs.clear();
        if (usedBlobs != null) {
            Iterables.addAll(this.usedBlobs, usedBlobs);
        }
    }

    public void addUsedBlob(UsedBlobInfo usedBlob) {
        this.usedBlobs.add(usedBlob);
    }

    public int getId() { return id; }
    public List<MissingBlobInfo> getMissingBlobs() {
        return Collections.unmodifiableList(missingBlobs);
    }
    public List<IncorrectBlobSizeInfo> getIncorrectSizes() {
        return Collections.unmodifiableList(incorrectSizes);
    }
    public List<UnexpectedBlobInfo> getUnexpectedBlobs() {
        return Collections.unmodifiableList(unexpectedBlobs);
    }
    public List<IncorrectBlobRevisionInfo> getIncorrectRevisions() {
        return Collections.unmodifiableList(incorrectRevisions);
    }
    public List<UsedBlobInfo> getUsedBlobs() {
        return usedBlobs;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("missingBlobs", missingBlobs)
            .add("incorrectSizes", incorrectSizes)
            .add("unexpectedBlobs", unexpectedBlobs)
            .add("incorrectRevisions", incorrectRevisions)
            .add("usedBlobs", usedBlobs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
