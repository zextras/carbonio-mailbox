// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class LmtpAddressTest {

    /**
     * Tests fix for bug 22712.
     */
    @Test
    public void parseQuotedLocalPart() {
        String recipient = "<\"test.\"@domain.com>";
        Assert.assertEquals("test.@domain.com", new LmtpAddress(recipient, null, null).getEmailAddress());
        recipient = "<\"\\\"test.\\\"\"@domain.com>";
        Assert.assertEquals("\"test.\"@domain.com", new LmtpAddress(recipient, null, null).getEmailAddress());
    }
}
