// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.zimbra.soap.type.CalDataSource;

public class MailCalDataSource
extends MailDataSource
implements CalDataSource {

    public MailCalDataSource() {
    }

    public MailCalDataSource(CalDataSource data) {
        super(data);
    }
}
