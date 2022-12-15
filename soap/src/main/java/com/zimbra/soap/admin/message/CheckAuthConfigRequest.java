// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check Auth Config
 * <br />
 * For example:
 * <br />
 * <pre>
 * &lt;CheckAuthConfigRequest>
 *     &lt;a n='zimbraAuthMech'>ldap&lt&lt;/a>
 *     &lt;a n='zimbraAuthLdapURL'>...&lt;/a>
 *     &lt;a n='zimbraAuthLdapBindDn'>...&lt;/a>
 *     &lt;a n='zimbraAuthLdapSearchFilter'>...&lt;/a>
 *     &lt;a n='zimbraAuthLdapSearchBase'>...&lt;/a>
 *     &lt;a n='zimbraAuthLdapSearchBindDn'>...&lt;/a>
 *     &lt;a n='zimbraAuthLdapSearchBindPassword'>...&lt;/a>
 *     &lt;name>...&lt;/name>
 *     &lt;password>...&lt;/password>
 * &lt;/CheckAuthConfigRequest>
 *
 * &lt;CheckAuthConfigResponse>
 *     &lt;code>...&lt;/code>
 *     &lt;message>...&lt;/message>*
 *     &lt;bindDn>{dn-computed-from-supplied-binddn-and-name}&lt;/bindDn>
 * &lt;/CheckAuthConfigResponse>
 * </pre>
 * notes:
 * <ul>
 * <li> zimbraAuthMech must be set to ldap/ad. There is no reason to check zimbra.
 * <li> zimbraAuthLdapURL must be set
 * <li> either zimbraAuthLdapBindDn or zimbraAuthLdapSearchFilter must be set
 * </ul>
 * The following are optional, and only looked at if zimbraAuthLdapSearchFilter is set:
 * <ul>
 * <li> zimbraAuthLdapSearchBase is optional and defaults to ""
 * <li> zimbraAuthLdapSearchBind{Dn,Password} are both optional
 * </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_AUTH_CONFIG_REQUEST)
public class CheckAuthConfigRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=AdminConstants.E_NAME, required=true)
    private String name;

    /**
     * @zm-api-field-tag password
     * @zm-api-field-description Password
     */
    @XmlAttribute(name=AdminConstants.E_PASSWORD, required=true)
    private String password;

    public CheckAuthConfigRequest() {
        this(null, null);
    }

    public CheckAuthConfigRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public String getPassword() { return password; }
}
