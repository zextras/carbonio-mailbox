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

import com.zimbra.common.soap.HsmConstants;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-response-description The aborted attribute is set to true (1) if an HSM session was running at the time the
 * request was made, false (0) otherwise.<p>Note: If the abort request is sent after all the blobs for the last mailbox
 * have been moved, but before its database table has been updated, &lt;AbortHsmRequest> will return true even though
 * the process was not really aborted.  This state is very unlikely.  The official aborted state can be verified
 * with &lt;GetHsmStatusRequest>.</p>
 * @zm-api-command-network-edition true
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=HsmConstants.E_ABORT_HSM_RESPONSE)
public class AbortHsmResponse {

    /**
     * @zm-api-field-description Set to true (1) if an HSM session was running at the time the request was made.
     */
    @XmlAttribute(name=HsmConstants.A_ABORTED /* aborted */, required=true)
    private final ZmBoolean aborted;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AbortHsmResponse() {
        this(false);
    }

    public AbortHsmResponse(boolean aborted) {
        this.aborted = ZmBoolean.fromBool(aborted);
    }

    public boolean getAborted() { return ZmBoolean.toBool(aborted); }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("aborted", aborted);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
