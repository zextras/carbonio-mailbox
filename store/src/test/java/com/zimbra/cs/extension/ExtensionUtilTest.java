// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.clamscanner.ClamScannerExtension;
import com.zimbra.cs.nginx.NginxLookupExtension;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ExtensionUtil}.
 *
 * @author ysasaki
 */
public class ExtensionUtilTest extends MailboxTestSuite {

	private static URL classpath;

	@BeforeAll
	public static void init() throws Exception {
		classpath = new File("random/test-classes").toURI().toURL();
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
		Assertions.assertNotNull(ext);
		Assertions.assertTrue(ext.isInitialized());
		Assertions.assertFalse(ext.isDestroyed());
	}

	@Test
	void resign() throws Exception {
		ExtensionUtil.addClassLoader(new ZimbraExtensionClassLoader(classpath,
				ResignExtension.class.getName()));
		ExtensionUtil.initAll();
		Assertions.assertNull(ExtensionUtil.getExtension("resign"));
		Assertions.assertTrue(ResignExtension.isDestroyed());
	}

	@Test
	void initAll_shouldInitNginxLookupExtension() {
		ExtensionUtil.initAll();
		Assertions.assertNotNull(ExtensionUtil.getExtension(NginxLookupExtension.NAME));
	}

	@Test
	void initAll_shouldInitClamScannerExtension() {
		ExtensionUtil.initAll();
		Assertions.assertNotNull(ExtensionUtil.getExtension(ClamScannerExtension.NAME));
	}

}
