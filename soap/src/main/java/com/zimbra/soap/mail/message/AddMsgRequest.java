// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.AddMsgSpec;
import com.zimbra.soap.type.ZmBoolean;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Add a message
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_ADD_MSG_REQUEST)
public class AddMsgRequest {

    /**
     * @zm-api-field-tag filter-sent
     * @zm-api-field-description If set, then do outgoing message filtering if the msg is being added to the Sent
     * folder and has been flagged as sent. Default is unset.
     */
    @XmlAttribute(name=MailConstants.A_FILTER_SENT /* filterSent */, required=false)
    private ZmBoolean filterSent;

    /**
     * @zm-api-field-description Specification of the message to add
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_MSG /* m */, required=true)
    private final AddMsgSpec msg;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AddMsgRequest() {
        this(null);
    }

    public AddMsgRequest(AddMsgSpec msg) {
        this.msg = msg;
    }

    public void setFilterSent(Boolean filterSent) {
        this.filterSent = ZmBoolean.fromBool(filterSent);
    }
    public Boolean getFilterSent() { return ZmBoolean.toBool(filterSent); }
    public AddMsgSpec getMsg() { return msg; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("filterSent", filterSent)
            .add("msg", msg);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
