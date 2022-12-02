// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.DataSource;

@XmlAccessorType(XmlAccessType.NONE)
public class MailCaldavDataSource extends MailDataSource implements DataSource {

    public MailCaldavDataSource() {
        super();
    }
    public MailCaldavDataSource(MailCaldavDataSource data) {
        super(data);
    }
}
