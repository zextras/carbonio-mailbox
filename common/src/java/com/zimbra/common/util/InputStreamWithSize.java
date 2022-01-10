// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.InputStream;

public class InputStreamWithSize {
    public InputStream stream;
    public Long size;

    public InputStreamWithSize(InputStream is, Long length) {
        this.stream = is;
        this.size = length;
    }
}
