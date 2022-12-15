// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.zimbra.common.util.EmailUtil;
import com.zimbra.cs.mime.Mime;

/**
 * @author bburtin
 */
public class TestEmailUtil extends TestCase
{
    public void testSplit() {
        assertNull(EmailUtil.getLocalPartAndDomain("foo"));
        assertNull(EmailUtil.getLocalPartAndDomain("foo@"));
        assertNull(EmailUtil.getLocalPartAndDomain("@foo"));
        
        String[] parts = EmailUtil.getLocalPartAndDomain("jspiccoli@example.zimbra.com");
        assertNotNull(parts);
        assertEquals("jspiccoli", parts[0]);
        assertEquals("example.zimbra.com", parts[1]);
    }
    
    /**
     * Tests {@link EmailUtil#isRfc822Message}.
     */
    public void testRfc822()
    throws Exception {
        assertTrue(isRfc822Message("Content-Type: text/plain"));
        assertFalse(isRfc822Message("Content-Type text/plain"));
        assertFalse(isRfc822Message("Content-Type\r\n  :text/plain"));
        
        // Test a line longer than 998 characters.
        StringBuilder buf = new StringBuilder();
        for (int i = 1; i <= 998; i++) {
            buf.append("X");
        }
        buf.append(": Y");
        assertFalse(isRfc822Message(buf.toString()));
    }
    
    /**
     * Confirms that address parsing doesn't blow up when the
     * header value is malformed (bug 32271). 
     */
    public void testParseAddressHeader()
    throws Exception {
        Mime.parseAddressHeader("(Test) <djoe@zimbra.com>,djoe@zimbra.com (Test)");
    }
    
    private boolean isRfc822Message(String content)
    throws IOException {
        return EmailUtil.isRfc822Message(new ByteArrayInputStream(content.getBytes()));
    }
    
    public static void main(String[] args)
    throws Exception {
        TestUtil.runTest(TestEmailUtil.class);
    }
}
