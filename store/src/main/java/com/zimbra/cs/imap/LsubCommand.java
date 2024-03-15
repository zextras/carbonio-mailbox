// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.Arrays;
import java.util.HashSet;

public class LsubCommand extends AbstractListCommand {
    public LsubCommand(String referenceName, String mailboxName) {
        super(referenceName, new HashSet<String>(Arrays.asList(mailboxName)));
    }
}
