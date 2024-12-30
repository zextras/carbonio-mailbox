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
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.mail.type.MsgSpec;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Message
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_MSG_REQUEST)
public class GetMsgRequest {

    /**
     * @zm-api-field-description Message specification
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_MSG /* m */, required=true)
    private final MsgSpec msg;

    /**
     * @zm-api-field-tag encryptionPassword
     * @zm-api-field-description Secure email password can be used for smime or pgp certificate password.
     */
    @XmlAttribute(name= SmimeConstants.A_ENCRYPTION_PASSWORD /* encryptionPassword */, required=false)
    private String encryptionPassword;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetMsgRequest() {
        this(null);
    }

    public GetMsgRequest(MsgSpec msg) {
        this.msg = msg;
    }

    public MsgSpec getMsg() { return msg; }

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("msg", msg)
            .add(SmimeConstants.A_ENCRYPTION_PASSWORD, encryptionPassword);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
