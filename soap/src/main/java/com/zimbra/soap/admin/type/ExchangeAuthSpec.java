// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ExchangeAuthSpec {

    @XmlEnum
    public enum AuthScheme {
        // case must match protocol
        basic, form;

        public static AuthScheme fromString(String s)
        throws ServiceException {
            try {
                return AuthScheme.valueOf(s);
            } catch (IllegalArgumentException e) {
               throw ServiceException.INVALID_REQUEST("unknown AuthScheme: " + s + ", valid values: " +
                       Arrays.asList(AuthScheme.values()), null);
            }
        }
    }

    /**
     * @zm-api-field-tag url-to-exchange-server
     * @zm-api-field-description URL to Exchange server
     */
    @XmlAttribute(name=AdminConstants.A_URL /* url */, required=true)
    private final String url;

    /**
     * @zm-api-field-tag exchange-user
     * @zm-api-field-description Exchange user
     */
    @XmlAttribute(name=AdminConstants.A_USER /* user */, required=true)
    private final String authUserName;

    /**
     * @zm-api-field-tag exchange-password
     * @zm-api-field-description Exchange password
     */
    @XmlAttribute(name=AdminConstants.A_PASS /* pass */, required=true)
    private final String authPassword;

    /**
     * @zm-api-field-tag auth-scheme
     * @zm-api-field-description Auth scheme
     */
    @XmlAttribute(name=AdminConstants.A_SCHEME /* scheme */, required=true)
    private final AuthScheme scheme;

    /**
     * @zm-api-field-tag auth-type
     * @zm-api-field-description Auth type
     */
    @XmlAttribute(name=AdminConstants.A_TYPE /* type */, required=false)
    private final String type;
    
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ExchangeAuthSpec() {
        this((String) null, (String) null, (String) null, (AuthScheme) null, null);
    }

    public ExchangeAuthSpec(String url, String authUserName,
                    String authPassword, AuthScheme scheme, String type) {
        this.url = url;
        this.authUserName = authUserName;
        this.authPassword = authPassword;
        this.scheme = scheme;
        this.type = type;
    }

    public String getUrl() { return url; }
    public String getType() { return type; }
    public String getAuthUserName() { return authUserName; }
    public String getAuthPassword() { return authPassword; }
    public AuthScheme getScheme() { return scheme; }
}
