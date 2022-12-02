// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to encode meta-data stored in a hash-table that is bound for a blob_metadata.metadata column
 * (text). The map should consist of keys which are Strings (and don't contain the '=' character), and values which are
 * also Strings (and can contain any value).
 *
 * @since Apr 8, 2004
 * @author schemers
 */
public final class BlobMetaData {

    public static void encodeMetaData(String name, String value, StringBuilder sb) {
        if (value == null) {
            return;
        }
        sb.append(name);
        sb.append('=');
        sb.append(value.length());
        sb.append(':');
        sb.append(value);
        sb.append(';');
    }

    public static void encodeMetaData(String name, long value, StringBuilder sb) {
        String str = Long.toString(value);
        sb.append(name);
        sb.append('=');
        sb.append(str.length());
        sb.append(':');
        sb.append(str);
        sb.append(';');
    }

    public static void encodeMetaData(String name, boolean value, StringBuilder sb) {
        sb.append(name);
        sb.append(value ? "=1:1;" : "=1:0;");
    }

    /**
     * Take meta-data encoded via encodeMetaData and decode it back into a Map.
     *
     * @param metaData the encoded meta-data
     * @return a Map consisting of the name/value pairs.
     * @throws BlobMetaDataEncodingException if unable to decode the meta-data.
     */
    public static Map<Object, Object> decode(String metaData) throws BlobMetaDataEncodingException {
        Map<Object, Object> map = new HashMap<Object, Object>();
        int offset = 0;
        int p = 0;
        int len = metaData.length();
        while (offset < len && (p = metaData.indexOf('=', offset)) != -1) {
            String name = metaData.substring(offset, p);

            p++;
            int i = p;

            while (p < len && Character.isDigit(metaData.charAt(p))) {
                p++;
            }
            if (p >= len || metaData.charAt(p) != ':') {
                throw new BlobMetaDataEncodingException(
                        String.format("error decoding value length for key '%s' at offset %d", name, offset));
            }

            int value_len = Integer.parseInt(metaData.substring(i, p));

            p++;
            i = p;

            if (p + value_len > len) {
                throw new BlobMetaDataEncodingException(
                        String.format("invalid value length %d for key '%s' at offset %d", value_len, name, offset));
            }
            String value = metaData.substring(i, p + value_len);

            p += value_len;

            // TODO: should throw an exception and remove the meta data from the DB
            if (p >= len || metaData.charAt(p) != ';') {
                throw new BlobMetaDataEncodingException(
                        String.format("expecting ';' after value for key '%s' at offset %d", name, offset));
            }
            p++;
            map.put(name, value);
            offset = p;
        }
        return map;
    }

    public static Map<Object, Object> decodeRecursive(String metaData, Integer associatedItemId)
    throws BlobMetaDataEncodingException {
        Map<Object, Object> map = decode(metaData);
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if (value.length() > 5 && value.indexOf('=') != -1) {
                    try {
                        entry.setValue(decodeRecursive(value, associatedItemId));
                    } catch (BlobMetaDataEncodingException e) {
                        if (associatedItemId == null) {
                            ZimbraLog.mailbox.warn("Unable to decode BlobMetaData value [%s] due to exception",
                                    value, e);
                        } else {
                            ZimbraLog.mailbox.warn(
                                    "Unable to decode BlobMetaData value [%s] for item=%s due to exception",
                                    value, associatedItemId, e);

                        }
                    }
                }
            }
        }
        return map;
    }

    public static String getString(Map<?, ?> m, String name) {
        return (String) m.get(name);
    }

    public static String getString(Map<?, ?> m, String name, String defaultValue) {
        String value = (String) m.get(name);
        return value != null ? value : defaultValue;
    }

    public static int getInt(Map<?, ?> m, String name) {
        return Integer.parseInt((String) m.get(name));
    }

    public static int getInt(Map<?, ?> m, String name, int defaultValue) {
        String value = (String) m.get(name);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public static long getLong(Map<?, ?> m, String name) {
        return Long.parseLong((String) m.get(name));
    }

    public static long getLong(Map<?, ?> m, String name, long defaultValue) {
        String value = (String) m.get(name);
        return value != null ? Long.parseLong(value) : defaultValue;
    }

    public static boolean getBoolean(Map<?, ?> m, String name) {
        return "1".equals(m.get(name));
    }

    public static boolean getBoolean(Map<?, ?> m, String name, boolean defaultValue) {
        String value = (String) m.get(name);
        return value != null ? "1".equals(value) : defaultValue;
    }

}
