// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class CacheEntrySelector {

    public static enum CacheEntryBy {

        // case must match protocol
        id, name;

        public static CacheEntryBy fromString(String s) throws ServiceException {
            try {
                return CacheEntryBy.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST("unknown key: "+s, e);
            }
        }

        public com.zimbra.common.account.Key.CacheEntryBy toKeyCacheEntryBy()
        throws ServiceException {
            return com.zimbra.common.account.Key.CacheEntryBy.fromString(this.name());
        }
    }

    /**
     * @zm-api-field-tag cache-entry-key
     * @zm-api-field-description The key used to identify the cache entry. Meaning determined by <b>{cache-entry-by}</b>
     */
    @XmlValue private final String key;

    /**
     * @zm-api-field-tag cache-entry-by
     * @zm-api-field-description Select the meaning of <b>{cache-entry-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY) private final CacheEntryBy cacheEntryBy;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CacheEntrySelector() {
        this(null, null);
    }

    public CacheEntrySelector(CacheEntryBy by, String key) {
        this.cacheEntryBy = by;
        this.key = key;
    }

    public String getKey() { return key; }

    public CacheEntryBy getBy() { return cacheEntryBy; }
}
