// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.account.type.Prop;

/**
 *
 * @author jylee
 *
 */
public final class ZimletUserProperties {
    private static final Cache<String, ZimletUserProperties> CACHE =
        CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(30, TimeUnit.MINUTES).build();

    private final Set<Prop> properties = new HashSet<Prop>(); // guarded by ZimletUserProperties.this

    public static ZimletUserProperties getProperties(Account account) {
        String key = account.getId();
        ZimletUserProperties prop = CACHE.getIfPresent(key);
        if (prop == null) {
            prop = new ZimletUserProperties(account);
            CACHE.put(key, prop);
        }
        return prop;
    }

    private ZimletUserProperties(Account account) {
        String[] props = account.getMultiAttr(Provisioning.A_zimbraZimletUserProperties);
        for (String prop : props) {
            try {
                properties.add(new Prop(prop));
            } catch (IllegalArgumentException e) {
                ZimbraLog.zimlet.warn("invalid property: %s", e.getMessage());
            }
        }
    }

    public synchronized void setProperty(String zimlet, String key, String value) {
        Prop newProp = new Prop(zimlet, key, value);
        for (Prop prop : properties) {
            if (prop.matches(newProp)) {
                prop.replace(newProp);
                return;
            }
        }
        properties.add(newProp);
    }

    public synchronized Set<Prop> getAllProperties() {
        return ImmutableSet.copyOf(properties);
    }

    public synchronized void saveProperties(Account account) throws ServiceException {
        String[] props = new String[properties.size()];
        int index = 0;
        for (Prop prop: properties) {
            props[index++] = prop.getSerialization();
        }
        try {
            Provisioning.getInstance().modifyAttrs(account,
                Collections.singletonMap(Provisioning.A_zimbraZimletUserProperties, props));
        } catch (ServiceException se) {
            CACHE.invalidate(account.getId());
            throw se;
        }
    }
}
