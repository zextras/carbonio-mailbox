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
import com.zimbra.soap.type.SearchHit;

@XmlAccessorType(XmlAccessType.NONE)
public class ConversationHitInfo
extends ConversationSummary
implements SearchHit {

    /**
     * @zm-api-field-tag sort-field
     * @zm-api-field-description Sort field value
     */
    @XmlAttribute(name=MailConstants.A_SORT_FIELD /* sf */, required=false)
    private String sortField;

    /**
     * @zm-api-field-description Hits
     */
    @XmlElement(name=MailConstants.E_MSG /* m */, required=false)
    private final List<ConversationMsgHitInfo> messageHits = Lists.newArrayList();

    public ConversationHitInfo() {
        this((String) null);
    }

    public ConversationHitInfo(String id) {
        super(id);
    }

    @Override
    public void setSortField(String sortField) { this.sortField = sortField; }
    public void setMessageHits(Iterable <ConversationMsgHitInfo> messageHits) {
        this.messageHits.clear();
        if (messageHits != null) {
            Iterables.addAll(this.messageHits,messageHits);
        }
    }

    public ConversationHitInfo addMessageHit(ConversationMsgHitInfo messageHit) {
        this.messageHits.add(messageHit);
        return this;
    }

    @Override
    public String getSortField() { return sortField; }

    public List<ConversationMsgHitInfo> getMessageHits() {
        return Collections.unmodifiableList(messageHits);
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("sortField", sortField)
            .add("messageHits", messageHits);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
