// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.Pop3DataSource;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class MailPop3DataSource
extends MailDataSource
implements Pop3DataSource {

    /**
     * @zm-api-field-tag leave-on-server
     * @zm-api-field-description Leave messages on the server
     */
    @XmlAttribute(name=MailConstants.A_DS_LEAVE_ON_SERVER /* leaveOnServer */, required=false)
    private ZmBoolean leaveOnServer;

    public MailPop3DataSource() {
        super();
    }

    public MailPop3DataSource(Pop3DataSource data) {
        super(data);
        setLeaveOnServer(data.isLeaveOnServer());
    }

    @Override
    public void setLeaveOnServer(Boolean leaveOnServer) {
        this.leaveOnServer = ZmBoolean.fromBool(leaveOnServer);
    }
    @Override
    public Boolean isLeaveOnServer() { return ZmBoolean.toBool(leaveOnServer); }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("leaveOnServer", leaveOnServer);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
