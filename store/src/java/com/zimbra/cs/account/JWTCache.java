// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zimbra.common.localconfig.LC;

public class JWTCache {

    private static final Cache <String, JWTInfo> JWT_CACHE = CacheBuilder.newBuilder()
                                                                           .maximumSize(LC.zimbra_authtoken_cache_size.intValue())
                                                                           .build();

    public static void put(String jti, JWTInfo jwtInfo) {
            JWT_CACHE.put(jti, jwtInfo);
    }

    public static JWTInfo get(String jti) {
            return JWT_CACHE.getIfPresent(jti);
    }

    public static void remove(String jti) {
           JWT_CACHE.invalidate(jti);
    }
}