// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_LICENSE_RESPONSE)
@XmlType(propOrder = {})
public class GetLicenseResponse {

    /**
     * @zm-api-field-description Block containing attributes relating to the license
     */
    @XmlElement(name=AdminConstants.E_LICENSE /* license */, required=true)
    private AdminAttrsImpl license;

    /**
     * @zm-api-field-description Block containing attributes relating to activation
     */
    @XmlElement(name=AdminConstants.E_ACTIVATION /* activation */, required=false)
    private AdminAttrsImpl activation;

    /**
     * @zm-api-field-description The info element block contains:
     * <table>
     * <tr> <td> <b>Version</b> </td> <td> ZCS version </td> </tr>
     * <tr> <td> <b>Fingerprint</b> </td> <td> System fingerprint required during activation </td> </tr>
     * <tr> <td> <b>Status</b> </td> <td> License and activation status </td> </tr>
     * <tr> <td> <b>TotalAccounts</b> </td> <td> Current number of accounts </td> </tr>
     * <tr> <td> <b>ArchivingAccounts</b> </td> <td> Current number of archiving accounts</td> </tr>
     * <tr> <td> <b>ServerTime</b> </td> <td> Current server time </td> </tr>
     * </table>
     * <br />
     * The value of <b>TotalAccounts</b> can be -1 which indicates that the account counting is still
     * in progress and the server does not have the count.  The account counting can be initiated by
     * creating an account, use of a Network feature, or by sending a CheckLicense Request.
     */
    @XmlElement(name=AdminConstants.E_INFO /* info */, required=true)
    private AdminAttrsImpl info;

    public GetLicenseResponse() {
    }

    public void setLicense(AdminAttrsImpl license) { this.license = license; }
    public void setActivation(AdminAttrsImpl activation) {
        this.activation = activation;
    }
    public void setInfo(AdminAttrsImpl info) { this.info = info; }

    public AdminAttrsImpl getLicense() { return license; }
    public AdminAttrsImpl getActivation() { return activation; }
    public AdminAttrsImpl getInfo() { return info; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("license", license)
            .add("activation", activation)
            .add("info", info);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
