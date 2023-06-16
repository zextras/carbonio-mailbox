// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.common.localconfig.LC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link ExtensionUtil}.
 *
 * @author ysasaki
 */
public class ExtensionUtilTest {
    private static URL classpath;

    @BeforeAll
    public static void init() throws Exception {
        classpath = new File("build/test-classes").toURI().toURL();
        LC.zimbra_extension_common_directory.setDefault(null);
        LC.zimbra_extension_directory.setDefault(null);
    }

 @Test
 void simple() throws Exception {
  ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,
    SimpleExtension.class.getName()));
  ExtensionUtil.initAll();
  SimpleExtension ext =
    (SimpleExtension) ExtensionUtil.getExtension("simple");
  assertNotNull(ext);
  assertTrue(ext.isInitialized());
  assertFalse(ext.isDestroyed());
 }

 @Test
 void resign() throws Exception {
  ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,
    ResignExtension.class.getName()));
  ExtensionUtil.initAll();
  assertNull(ExtensionUtil.getExtension("resign"));
  assertTrue(ResignExtension.isDestroyed());
 }

}
