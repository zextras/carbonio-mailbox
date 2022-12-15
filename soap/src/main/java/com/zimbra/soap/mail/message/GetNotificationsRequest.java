// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get notifications
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=OctopusXmlConstants.E_GET_NOTIFICATIONS_REQUEST)
public class GetNotificationsRequest {


    /**
     * @zm-api-field-tag mark-seen
     * @zm-api-field-description If set then all the notifications will be marked as seen.
     * Default: <b>unset</b>
     */
    @XmlAttribute(name=OctopusXmlConstants.A_MARKSEEN /* markSeen */, required=false)
    private ZmBoolean markSeen;

    private GetNotificationsRequest() {
    }

    public Boolean isMarkSeen() {
        return ZmBoolean.toBool(markSeen, false);
    }

    public void setMarkSeen(boolean markSeen) {
        this.markSeen = ZmBoolean.fromBool(markSeen);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper;
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
