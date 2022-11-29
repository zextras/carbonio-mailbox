// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.IOException;

/**
 * IMAP4 ID extension (RFC 2971) information.
 */
public final class IDInfo {
    //ID fields
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String OS = "os";
    public static final String OS_VERSION = "os-version";
    public static final String VENDOR = "vendor";
    public static final String SUPPORT_URL = "support-url";
    public static final String ADDRESS = "address";
    public static final String DATE = "date";
    public static final String COMMAND = "command";
    public static final String ARGUMENTS = "arguments";
    public static final String ENVIRONMENT = "environment";
    public static final String X_ORIGINATING_IP = "X-ORIGINATING-IP";
    public static final String X_VIA = "X-VIA";

    //value constant(s)
    public static String DATASOURCE_IMAP_CLIENT_NAME = "ZimbraImapDataSource";

    private final Map<String, String> fields = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    public static IDInfo read(ImapInputStream is) throws IOException {
        is.skipSpaces();
        IDInfo info = new IDInfo();
        if (!is.match('(')) {
            is.skipNil();
            return info;
        }
        while (!is.match(')')) {
            String name = is.readString();
            is.skipChar(' ');
            String value = is.readNString();
            info.fields.put(name, value);
            is.skipSpaces();
        }
        return info;
    }

    public String get(String key) {
        return fields.get(key);
    }

    public String put(String key, String value) {
        return fields.put(key, value);
    }

    public List<ImapData> toRequestParam() {
        List<ImapData> data = new ArrayList<ImapData>(fields.size());
        for (Map.Entry<String, String> e : fields.entrySet()) {
            data.add(ImapData.asString(e.getKey()));
            data.add(ImapData.asNString(e.getValue()));
        }
        return data;
    }
}
