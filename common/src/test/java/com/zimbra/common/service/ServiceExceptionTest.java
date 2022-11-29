// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.service;

import org.junit.Assert;
import org.junit.Test;

import com.zimbra.common.service.ServiceException.Argument;

public class ServiceExceptionTest {

    @Test
    public void testArgumentEquals() {
        Argument arg1a = new Argument("1", "one", Argument.Type.STR);
        Argument arg1b = new Argument("1", "one", Argument.Type.STR);
        Argument arg1c = new Argument("1", "two", Argument.Type.STR);
        Argument arg2 = new Argument("2", "one", Argument.Type.STR);
        
        Assert.assertFalse(arg1a.equals(null));
        Assert.assertTrue(arg1a.equals(arg1b));
        Assert.assertFalse(arg1a.equals(arg1c));
        Assert.assertFalse(arg1a.equals(arg2));
    }
}
