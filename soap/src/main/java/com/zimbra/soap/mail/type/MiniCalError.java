// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class MiniCalError {

    /**
     * @zm-api-field-tag calendar-folder-id
     * @zm-api-field-description ID for calendar folder that couldn't be accessed
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag ServiceException-error-code
     * @zm-api-field-description ServiceException error code - service.PERM_DENIED, mail.NO_SUCH_FOLDER,
     * account.NO_SUCH_ACCOUNT, etc.
     */
    @XmlAttribute(name=MailConstants.A_CAL_CODE /* code */, required=true)
    private final String code;

    /**
     * @zm-api-field-tag error-msg-from-exception
     * @zm-api-field-description Error message from the exception (but no stack trace)
     */
    @XmlValue
    private String errorMessage;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MiniCalError() {
        this(null, null);
    }

    public MiniCalError(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getErrorMessage() { return errorMessage; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("code", code)
            .add("errorMessage", errorMessage);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
