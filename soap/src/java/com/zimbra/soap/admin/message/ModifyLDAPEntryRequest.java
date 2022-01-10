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

import com.zimbra.common.soap.LDAPUtilsConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify an LDAP Entry
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=LDAPUtilsConstants.E_MODIFY_LDAP_ENTRIY_REQUEST)
public class ModifyLDAPEntryRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag ldap-DN-string
     * @zm-api-field-description a valid LDAP DN String (RFC 2253) that identifies the LDAP object
     */
    @XmlAttribute(name=LDAPUtilsConstants.E_DN /* dn */, required=true)
    private final String dn;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ModifyLDAPEntryRequest() {
        this((String) null);
    }

    public ModifyLDAPEntryRequest(String dn) {
        this.dn = dn;
    }

    public String getDn() { return dn; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("dn", dn);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
