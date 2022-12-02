// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import java.nio.charset.Charset;

import javax.mail.internet.MimePart;
import javax.mail.internet.SharedInputStream;

public interface ZMimePart extends MimePart {
    void appendHeader(ZInternetHeader header);

    @Override
    String getEncoding();

    void endPart(SharedInputStream sis, long partSize, int lineCount);

    Charset defaultCharset();
}
