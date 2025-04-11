// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import com.zimbra.common.localconfig.LC;

import java.io.File;
import java.net.URL;

/**
 * Utility Class for Unit Test with Extension.
 */
public class ExtensionTestUtil {
    private static URL classpath;

    public static void init() throws Exception {
        // TODO: this test is not asserting anything. The classpath below could be anything and tests
        //  will still pass
        classpath = new File("random/extensions").toURI().toURL();
        LC.zimbra_extension_common_directory.setDefault(null);
        LC.zimbra_extension_directory.setDefault(null);
    }

    public static void registerExtension(String extensionClassName) throws Exception {
        ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,extensionClassName));
    }


}
