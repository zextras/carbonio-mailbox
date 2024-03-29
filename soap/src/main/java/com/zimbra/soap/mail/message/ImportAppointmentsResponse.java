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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_IMPORT_APPOINTMENTS_RESPONSE)
public class ImportAppointmentsResponse {

    /**
     * @zm-api-field-tag list-of-created-ids
     * @zm-api-field-description List of created IDs
     */
    @XmlAttribute(name=MailConstants.A_IDS /* ids */, required=true)
    private final String ids;

    /**
     * @zm-api-field-tag num-imported
     * @zm-api-field-description Number of imported appointments
     */
    @XmlAttribute(name=MailConstants.A_NUM /* n */, required=true)
    private final int num;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ImportAppointmentsResponse() {
        this(null, -1);
    }

    public ImportAppointmentsResponse(String ids, int num) {
        this.ids = ids;
        this.num = num;
    }

    public String getIds() { return ids; }
    public int getNum() { return num; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("ids", ids)
            .add("num", num);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
