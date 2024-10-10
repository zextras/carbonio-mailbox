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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SignatureConstants;
import com.zimbra.soap.account.type.MailSignature;
import com.zimbra.soap.type.SearchHit;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class MessageHitInfo
extends MessageInfo
implements SearchHit {

    /**
     * @zm-api-field-tag sort-field-value
     * @zm-api-field-description Sort field value
     */
    @XmlAttribute(name=MailConstants.A_SORT_FIELD /* sf */, required=false)
    private String sortField;

    /**
     * @zm-api-field-tag message-matched-query
     * @zm-api-field-description If the message matched the specified query string
     */
    @XmlAttribute(name=MailConstants.A_CONTENTMATCHED /* cm */, required=false)
    private ZmBoolean contentMatched;

    @XmlElement(name= SignatureConstants.SIGNATURE /* signature */, required=false)
    private MailSignature signature;

    /**
     * @zm-api-field-description Hit Parts -- indicators that the named parts matched the search string
     */
    @XmlElement(name=MailConstants.E_HIT_MIMEPART /* hp */, required=false)
    private final List<Part> messagePartHits = Lists.newArrayList();

    public MessageHitInfo() {
        this(null);
    }
    public MessageHitInfo(String id) {
        super(id);
    }

    @Override
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    public void setContentMatched(Boolean contentMatched) {
        this.contentMatched = ZmBoolean.fromBool(contentMatched);
    }
    public void setSignature(MailSignature signature) {
        this.signature = signature;
    }

    public MailSignature getSignature() {
        return signature;
    }
    public void setMessagePartHits(Iterable <Part> messagePartHits) {
        this.messagePartHits.clear();
        if (messagePartHits != null) {
            Iterables.addAll(this.messagePartHits,messagePartHits);
        }
    }

    public MessageHitInfo addMessagePartHit(Part messagePartHit) {
        this.messagePartHits.add(messagePartHit);
        return this;
    }

    @Override
    public String getSortField() { return sortField; }
    public Boolean getContentMatched() { return ZmBoolean.toBool(contentMatched); }
    public List<Part> getMessagePartHits() {
        return Collections.unmodifiableList(messagePartHits);
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("sortField", sortField)
            .add("contentMatched", contentMatched)
            .add("messagePartHits", messagePartHits);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
