// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.Objects;

public class MockImapCommand extends ImapCommand {

    private String param1;
    private String param2;
    private int param3;

    public MockImapCommand(String param1, String param2, int param3) {
        super();
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MockImapCommand)) {
            return false;
        }
        MockImapCommand mock = (MockImapCommand) obj;
        if (!Objects.equals(param1, mock.param1)) return false;
        return Objects.equals(param2, mock.param2) && param3 == mock.param3;
    }

}
