// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.admin.type.LimitedQuery;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check Global Addressbook Configuration
 * <br />
 * <pre>
 * &lt;CheckGalConfigRequest>
 *     &lt;a n='zimbraGalMode'>ldap&lt;/a>
 *
 *     &lt;a n='zimbraGalLdapURL'>...&lt;/a>
 *     &lt;a n='zimbraGalLdapSearchBase'>...&lt;/a>
 *     &lt;a n='zimbraGalLdapFilter'>...&lt;/a>
 *     &lt;a n='zimbraGalLdapAuthMech'>...&lt;/a>
 *     &lt;a n='zimbraGalLdapBindDn'>...&lt;/a>*
 *     &lt;a n='zimbraGalLdapBindPassword'>...&lt;/a>*
 *     &lt;a n='zimbraGalLdapKerberos5Principal'>...&lt;/a>*
 *     &lt;a n='zimbraGalLdapKerberos5Keytab'>...&lt;/a>*
 *
 *     &lt;a n='zimbraGalSyncLdapURL'>...&lt;/a>
 *     &lt;a n='zimbraGalSyncLdapSearchBase'>...&lt;/a>
 *     &lt;a n='zimbraGalSyncLdapFilter'>...&lt;/a>
 *     &lt;a n='zimbraGalSyncLdapAuthMech'>...&lt;/a>
 *     &lt;a n='zimbraGalSyncLdapBindDn'>...&lt;/a>*
 *     &lt;a n='zimbraGalSyncLdapBindPassword'>...&lt;/a>*
 *     &lt;a n='zimbraGalSyncLdapKerberos5Principal'>...&lt;/a>*
 *     &lt;a n='zimbraGalSyncLdapKerberos5Keytab'>...&lt;/a>*
 *
 *     &lt;a n='zimbraGalAutoCompleteLdapFilter'>...&lt;/a>
 *
 *     &lt;a n='zimbraGalTokenizeAutoCompleteKey'>...&lt;/a>
 *     &lt;a n='zimbraGalTokenizeSearchKey'>...&lt;/a>
 *
 *     &lt;query limit="...">...&lt;/query>*
 *     &lt;action>{GAL-action}&lt;/action>*
 * &lt;/CheckGalConfigRequest>
 * </pre>
 * Notes:
 * <ul>
 * <li> zimbraGalMode must be set to ldap, even if you eventually want to set it to "both".
 * <li> &lt;action> is optional.  GAL-action can be autocomplete|search|sync.  Default is search.
 * <li> &lt;query> is ignored if &lt;action> is "sync".
 * <li> AuthMech can be none|simple|kerberos5.
 *      <ul>
 *      <li> Default is simple if both BindDn/BindPassword are provided.
 *      <li> Default is none if either BindDn or BindPassword are NOT provided.
 *      </ul>
 * <li> BindDn/BindPassword are required if AuthMech is "simple".
 * <li> Kerberos5Principal/Kerberos5Keytab are required only if AuthMech is "kerberos5".
 * <li> zimbraGalSyncLdapXXX attributes are for GAL sync.  They are ignored if &lt;action> is not sync.
 *      <br />
 *      For GAL sync, if a zimbraGalSyncLdapXXX attribute is not set, server will fallback to the corresponding
 *      zimbraGalLdapXXX attribute.
 * </ul>
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_GAL_CONFIG_REQUEST)
@XmlType(propOrder = {"query", "action"})
public class CheckGalConfigRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-description Query
     */
    @XmlElement(name=AdminConstants.E_QUERY)

    /**
     * @zm-api-field-tag GAL-action
     * @zm-api-field-description GAL action
     */
    private LimitedQuery query;
    @XmlElement(name=AdminConstants.E_ACTION)
    private String action;

    public CheckGalConfigRequest() {
        this((LimitedQuery)null, (String)null);
    }

    public CheckGalConfigRequest(LimitedQuery query, String action) {
        this.query = query;
        this.action = action;
    }

    public void setQuery(LimitedQuery query) { this.query = query; }

    public LimitedQuery getQuery() { return query; }
    public void setAction(String action) { this.action = action; }

    public String getAction() { return action; }
}
