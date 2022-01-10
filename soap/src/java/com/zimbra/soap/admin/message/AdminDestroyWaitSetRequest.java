// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Use this to close out the waitset.  Note that the server will automatically time out
 * a wait set if there is no reference to it for (default of) 20 minutes.
 * <p>
 * WaitSet: scalable mechanism for listening for changes to one or more accounts
 * </p>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_ADMIN_DESTROY_WAIT_SET_REQUEST)
public class AdminDestroyWaitSetRequest {

    /**
     * @zm-api-field-tag waitset-id
     * @zm-api-field-description Waitset ID
     */
    @XmlAttribute(name=MailConstants.A_WAITSET_ID /* waitSet */, required=true)
    private final String waitSetId;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AdminDestroyWaitSetRequest() {
        this((String) null);
    }

    public AdminDestroyWaitSetRequest(String waitSetId) {
        this.waitSetId = waitSetId;
    }

    public String getWaitSetId() { return waitSetId; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("waitSetId", waitSetId);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
