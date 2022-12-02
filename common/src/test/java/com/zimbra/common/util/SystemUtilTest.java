// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import org.junit.Assert;
import org.junit.Test;

public class SystemUtilTest {

    @Test
    public void coalesce() {
        Assert.assertEquals(1, (int) SystemUtil.coalesce(null, 1));
        Assert.assertEquals(null, SystemUtil.coalesce(null, null, null, null));
        Assert.assertEquals(2, (int) SystemUtil.coalesce(2, 3));
        Assert.assertEquals("good", SystemUtil.coalesce("good", "bad", "ugly"));
    }
}
