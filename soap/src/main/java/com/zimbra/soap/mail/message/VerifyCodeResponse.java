// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_VERIFY_CODE_RESPONSE)
public class VerifyCodeResponse {

    /**
     * @zm-api-field-tag verification-successful
     * @zm-api-field-description Flags whether verification was successful
     */
    @XmlAttribute(name=MailConstants.A_VERIFICATION_SUCCESS /* success */, required=true)
    private final ZmBoolean success;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private VerifyCodeResponse() {
        this(false);
    }

    public VerifyCodeResponse(boolean success) {
        this.success = ZmBoolean.fromBool(success);
    }

    public boolean getSuccess() { return ZmBoolean.toBool(success); }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("success", success);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
