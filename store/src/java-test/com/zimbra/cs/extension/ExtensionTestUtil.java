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
 * When unit test is executed, custom classes in java-test/com/zimbra/extensions is compiled
 * and copied to build/test/extensions/com/zimbra/extensions
 */
public class ExtensionTestUtil {
    private static URL classpath;

    public static void init() throws Exception {
        classpath = new File("store/build/test/extensions").toURI().toURL();
        LC.zimbra_extension_common_directory.setDefault(null);
        LC.zimbra_extension_directory.setDefault(null);
    }

    public static void registerExtension(String extensionClassName) throws Exception {
        ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,extensionClassName));
    }


}
