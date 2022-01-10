// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.znative.tests;

import java.io.IOException;

import com.zimbra.znative.IO;

public class LinkCountTest {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: no arguments specified");
            return;
        }
        for (int i = 0; i < args.length; i++) {
            try {
                System.out.println(args[i] + ": " + IO.linkCount(args[i]));
            } catch (IOException ioe) {
                System.out.println(args[i] + ": " + ioe);
            }
        }
    }
}
