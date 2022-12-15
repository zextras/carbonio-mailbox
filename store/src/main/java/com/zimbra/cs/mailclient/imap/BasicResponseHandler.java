// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import java.util.List;

/**
 * A ResponseHandler implementations which accumulates untagged data results
 * matching the specified command code.
 */
public class BasicResponseHandler implements ResponseHandler {
    private final Atom code;
    private final List results;

    public BasicResponseHandler(Atom code, List results) {
        this.code = code;
        this.results = results;
    }

    public BasicResponseHandler(CAtom code, List results) {
        this(code.atom(), results);
    }

    @SuppressWarnings("unchecked")
    public void handleResponse(ImapResponse res) {
        if (res.getCode().equals(code)) {
            results.add(res.getData());
        }
    }
}
