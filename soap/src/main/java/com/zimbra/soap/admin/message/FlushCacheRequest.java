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
import com.zimbra.soap.admin.type.CacheSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Flush memory cache for specified LDAP or directory scan type/entries
 * <br />
 * Directory scan caches(source of data is on local disk of the server): <b>skin|locale</b>
 * LDAP caches(source of data is LDAP): <b>account|cos|domain|server|zimlet</b>
 * <br />
 * <br />
 * For LDAP caches, one or more optional <b>&lt;entry></b> can be specified.
 * <br />
 * If <b>&lt;entry></b>(s) are specified, only the specified entries will be flushed.
 * If no <b>&lt;entry></b> is given, all enties of the type will be flushed from cache.
 * <br />
 * type can contain a combination of skin, locale and zimlet. E.g. type='skin,locale,zimlet' or type='zimletskin'
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_FLUSH_CACHE_REQUEST)
@XmlType(propOrder = {})
public class FlushCacheRequest {

    /**
     * @zm-api-field-description Cache
     */
    @XmlElement(name=AdminConstants.E_CACHE, required=false)
    private final CacheSelector cache;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FlushCacheRequest() {
        this(null);
    }

    public FlushCacheRequest(CacheSelector cache) {
        this.cache = cache;
    }

    public CacheSelector getCache() { return cache; }
}
