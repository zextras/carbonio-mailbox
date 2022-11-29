// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.File;
import java.io.FilenameFilter;

public class RegexFilenameFilter implements FilenameFilter {

    protected String regex;
    
    public RegexFilenameFilter(String regex) {
        super();
        this.regex = regex;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.matches(regex);
    }
}