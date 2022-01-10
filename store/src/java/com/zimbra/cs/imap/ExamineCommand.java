// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class ExamineCommand extends SelectCommand {

    public ExamineCommand(ImapPath path, byte params, QResyncInfo qri) {
        super(path, params, qri);
    }

}
