// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.IdsAttr;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_APPLY_FILTER_RULES_RESPONSE)
public class ApplyFilterRulesResponse {

    /**
     * @zm-api-field-tag comma-sep-msg-ids
     * @zm-api-field-description Comma-separated list of message IDs that were affected
     */
    @XmlElement(name=MailConstants.E_MSG, required=false)
    private IdsAttr msgIds;

    public ApplyFilterRulesResponse() {
    }

    public void setMsgIds(IdsAttr msgIds) { this.msgIds = msgIds; }
    public IdsAttr getMsgIds() { return msgIds; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("msgIds", msgIds)
            .toString();
    }
}
