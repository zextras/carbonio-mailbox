// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMsgActionInfo {

    /**
     * @zm-api-field-tag successes
     * @zm-api-field-description List of ids that were acted on
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private String successes;

    /**
     * @zm-api-field-tag operation-move|read|empty
     * @zm-api-field-description Operation - <b>move|read|empty</b>
     */
    @XmlAttribute(name=MailConstants.A_OPERATION /* op */, required=true)
    private String operation;

    public VoiceMsgActionInfo() {
    }

    public void setSuccesses(String successes) { this.successes = successes; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getSuccesses() { return successes; }
    public String getOperation() { return operation; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("successes", successes)
            .add("operation", operation);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
