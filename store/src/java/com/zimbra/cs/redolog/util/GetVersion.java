// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.util;

import com.zimbra.cs.redolog.Version;

/**
 * Print the current redolog version to stdout.  Used by upgrade script.
 * @author jhahm
 *
 */
public class GetVersion {
    public static void main(String[] args) {
        System.out.println(Version.latest());
    }
}
