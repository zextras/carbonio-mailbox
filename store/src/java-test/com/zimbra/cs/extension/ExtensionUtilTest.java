// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.localconfig.LC;

/**
 * Unit test for {@link ExtensionUtil}.
 *
 * @author ysasaki
 */
public class ExtensionUtilTest {
    private static URL classpath;

    @BeforeClass
    public static void init() throws Exception {
        classpath = new File("build/test-classes").toURI().toURL();
        LC.zimbra_extension_common_directory.setDefault(null);
        LC.zimbra_extension_directory.setDefault(null);
    }

    @Test
    public void simple() throws Exception {
        ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,
                SimpleExtension.class.getName()));
        ExtensionUtil.initAll();
        SimpleExtension ext =
            (SimpleExtension) ExtensionUtil.getExtension("simple");
        Assert.assertNotNull(ext);
        Assert.assertTrue(ext.isInitialized());
        Assert.assertFalse(ext.isDestroyed());
    }

    @Test
    public void resign() throws Exception {
        ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,
                ResignExtension.class.getName()));
        ExtensionUtil.initAll();
        Assert.assertNull(ExtensionUtil.getExtension("resign"));
        Assert.assertTrue(ResignExtension.isDestroyed());
    }

}
