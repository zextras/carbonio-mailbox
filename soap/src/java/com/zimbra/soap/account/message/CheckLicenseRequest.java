// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Checks whether this account (auth token account or requested account id) is allowed
 * access to the specified feature.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_CHECK_LICENSE_REQUEST)
public class CheckLicenseRequest {

    /**
     * @zm-api-field-description The licensable feature.  These are the valid values (which are case-insensitive):
     * <ul>
     * <li> <b>MAPI</b> - Zimbra Connector For Outlook
     * <li> <b>MobileSync</b> - ActiveSync
     * <li> <b>iSync</b> - Apple iSync
     * <li> <b>SMIME</b> - Zimbra SMIME
     * <li> <b>BES</b> - Zimbra Connector for BlackBerry Enterprise Server
     * <li> <b>EWS</b> - Zimbra EWS Server
     * <li> <b>TouchClient</b> - Zimbra Touch Client
     * </ul>
     */
    @XmlAttribute(name=AdminConstants.A_FEATURE /* feature */, required=true)
    private String feature;

    public CheckLicenseRequest() {
    }

    public void setFeature(String feature) { this.feature = feature; }
    public String getFeature() { return feature; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("feature", feature);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
