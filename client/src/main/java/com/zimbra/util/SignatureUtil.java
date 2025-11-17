/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.util;

import com.google.common.collect.ImmutableBiMap;
import com.zimbra.common.account.ZAttrProvisioning;

public class SignatureUtil {

    public static final ImmutableBiMap<String, String> ATTR_TYPE_MAP = ImmutableBiMap.of(
        ZAttrProvisioning.A_zimbraPrefMailSignature, "text/plain",
        ZAttrProvisioning.A_zimbraPrefMailSignatureHTML, "text/html");
        
    public static String mimeTypeToAttrName(String mimeType) {
        return ATTR_TYPE_MAP.inverse().get(mimeType);
    }

    public static String attrNameToMimeType(String attrName) {
        return ATTR_TYPE_MAP.get(attrName);
    }
}
