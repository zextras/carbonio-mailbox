// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import junit.framework.Assert;

import org.junit.Test;


public class ZTagTest {

    @Test
    public void testColor() throws Exception {
        // 4451821 is equivalent long value for cyan
        ZTag.Color color = ZTag.Color.fromString("4451821");
        Assert.assertEquals(color.name(), "cyan");
        color = ZTag.Color.fromString("blue");
        Assert.assertEquals(color.name(), "blue");
        color = ZTag.Color.fromString("0x5b9bf2");
        Assert.assertEquals(color.name(), "orange");
    }
}
