// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import junit.framework.TestCase;

import com.zimbra.common.mime.MimeDetect;

public final class TestMimeDetect extends TestCase {

    public void testXLSMFileName() {
        assertEquals("application/vnd.ms-excel.sheet.macroEnabled.12", MimeDetect.getMimeDetect().detect("1.xlsm"));
    }

    public void testDOCMextension() {
        assertEquals("application/vnd.ms-word.document.macroEnabled.12", MimeDetect.getMimeDetect().detect("1.DOCM"));
    }

    public void testPPTMextension() {
        assertEquals("application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                MimeDetect.getMimeDetect().detect("1.PPtM"));
    }

    public static void main(String[] args)
    throws Exception {
        TestUtil.cliSetup();
        TestUtil.runTest(TestMimeDetect.class);
    }
}
